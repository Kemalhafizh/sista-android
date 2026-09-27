package com.sultanagung1.sista.core.audio

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Decodes a real audio file (local path or a plain http/https URL — Android's
 * MediaExtractor streams network sources directly, no manual download needed)
 * into [bucketCount] amplitude buckets, so a previously-recorded submission
 * gets a genuine waveform on review, not a synthesized placeholder shape.
 * Used by TahsinAudioPlayer for the teacher's review screen and the
 * student's own submission history — anywhere a waveform is shown for audio
 * that wasn't just recorded live (which already has real-time amplitude
 * samples from AudioRecorderManager).
 */
object AudioWaveformExtractor {

    suspend fun extract(source: String, bucketCount: Int = 100): Result<List<Float>> = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        var decoder: MediaCodec? = null
        try {
            extractor.setDataSource(source)

            var trackIndex = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                val mime = f.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    trackIndex = i
                    format = f
                    break
                }
            }
            if (trackIndex < 0 || format == null) {
                return@withContext Result.failure(IllegalStateException("Tidak ada trek audio pada berkas ini"))
            }
            extractor.selectTrack(trackIndex)

            val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else 0L
            val mime = format.getString(MediaFormat.KEY_MIME)!!
            decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(format, null, null, 0)
            decoder.start()

            val bucketSums = DoubleArray(bucketCount)
            val bucketCounts = IntArray(bucketCount)
            val bufferInfo = MediaCodec.BufferInfo()
            var sawInputEos = false
            var sawOutputEos = false

            while (!sawOutputEos) {
                if (!sawInputEos) {
                    val inputIndex = decoder.dequeueInputBuffer(10_000)
                    if (inputIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputIndex)
                        val sampleSize = if (inputBuffer != null) extractor.readSampleData(inputBuffer, 0) else -1
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            sawInputEos = true
                        } else {
                            val presentationTimeUs = extractor.sampleTime
                            decoder.queueInputBuffer(inputIndex, 0, sampleSize, presentationTimeUs, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputIndex = decoder.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outputIndex >= 0) {
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        sawOutputEos = true
                    }
                    if (bufferInfo.size > 0) {
                        val outputBuffer = decoder.getOutputBuffer(outputIndex)
                        if (outputBuffer != null) {
                            val bucketIndex = if (durationUs > 0) {
                                ((bufferInfo.presentationTimeUs.toDouble() / durationUs) * (bucketCount - 1))
                                    .toInt().coerceIn(0, bucketCount - 1)
                            } else 0

                            // 16-bit PCM: pair up bytes into signed samples, accumulate RMS per bucket.
                            outputBuffer.rewind()
                            val shortCount = bufferInfo.size / 2
                            var sumSquares = 0.0
                            for (s in 0 until shortCount) {
                                val lo = outputBuffer.get().toInt() and 0xFF
                                val hi = outputBuffer.get().toInt()
                                val sample = (hi shl 8) or lo
                                sumSquares += (sample * sample).toDouble()
                            }
                            if (shortCount > 0) {
                                val rms = sqrt(sumSquares / shortCount)
                                bucketSums[bucketIndex] += rms
                                bucketCounts[bucketIndex] += 1
                            }
                        }
                    }
                    decoder.releaseOutputBuffer(outputIndex, false)
                }
            }

            val rawBuckets = DoubleArray(bucketCount) { i ->
                if (bucketCounts[i] > 0) bucketSums[i] / bucketCounts[i] else 0.0
            }
            // Forward-fill empty buckets (silence-detection artifacts / uneven
            // frame distribution) from the nearest earlier real sample instead
            // of leaving a fake-looking hard zero gap.
            var lastNonZero = 0.0
            for (i in rawBuckets.indices) {
                if (rawBuckets[i] > 0.0) lastNonZero = rawBuckets[i] else rawBuckets[i] = lastNonZero
            }

            val maxVal = rawBuckets.maxOrNull()?.takeIf { it > 0 } ?: 1.0
            val normalized = rawBuckets.map { (abs(it) / maxVal).toFloat().coerceIn(0f, 1f) }

            Result.success(normalized)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try { decoder?.stop() } catch (_: Exception) {}
            try { decoder?.release() } catch (_: Exception) {}
            try { extractor.release() } catch (_: Exception) {}
        }
    }
}
