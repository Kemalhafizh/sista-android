package com.sultanagung1.sista.core.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentOutputFile: File? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var amplitudeJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _recordedFilePath = MutableStateFlow<String?>(null)
    val recordedFilePath: StateFlow<String?> = _recordedFilePath.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun startRecording() {
        if (_isRecording.value) return
        _errorMessage.value = null

        // RECORD_AUDIO is a runtime permission: check it explicitly instead of
        // relying on MediaRecorder throwing, so the student gets a clear reason.
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            _errorMessage.value = "Izin mikrofon belum diberikan. Aktifkan izin Mikrofon untuk Sulaone di Setelan HP, lalu coba lagi."
            return
        }

        val audioDir = File(context.cacheDir, "tahsin_records").apply { mkdirs() }
        val outputFile = File(audioDir, "tahsin_${System.currentTimeMillis()}.m4a")

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        try {
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            // A real failure (no mic permission, mic in use, unsupported
            // encoder on this device) must not be reported as "recording" —
            // the previous fallback silently pretended it worked and later
            // synthesized fake waveform data for a file that was never written.
            try { recorder.release() } catch (_: Exception) {}
            _errorMessage.value = "Gagal mengakses mikrofon: ${e.localizedMessage ?: "perangkat tidak mendukung perekaman audio"}"
            return
        }

        mediaRecorder = recorder
        currentOutputFile = outputFile
        _isRecording.value = true
        _recordingSeconds.value = 0
        _amplitudes.value = emptyList()

        // Start amplitude & timer sampling loop
        amplitudeJob = scope.launch {
            while (_isRecording.value) {
                delay(100)
                _recordingSeconds.value += 1

                // maxAmplitude is the real peak PCM amplitude (0..32767) since the
                // last call — 0f when momentarily silent is a genuine reading, not
                // a failure, so it must render as a genuinely quiet bar, never a
                // randomized one.
                val amp = try {
                    ((mediaRecorder?.maxAmplitude ?: 0).toFloat() / 32767f).coerceIn(0f, 1f)
                } catch (_: Exception) {
                    0f
                }

                val currentList = _amplitudes.value.toMutableList()
                if (currentList.size > 50) currentList.removeAt(0)
                currentList.add(amp)
                _amplitudes.value = currentList
            }
        }
    }

    /** Returns the real recorded file's absolute path, or null if nothing was actually recorded. */
    fun stopRecording(): String? {
        if (!_isRecording.value) return _recordedFilePath.value

        _isRecording.value = false
        amplitudeJob?.cancel()

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            _errorMessage.value = "Rekaman gagal disimpan: ${e.localizedMessage ?: "berkas audio rusak"}"
            mediaRecorder = null
            currentOutputFile = null
            _recordedFilePath.value = null
            return null
        }
        mediaRecorder = null

        val path = currentOutputFile?.takeIf { it.exists() && it.length() > 0 }?.absolutePath
        if (path == null) {
            _errorMessage.value = "Rekaman gagal disimpan: berkas audio tidak ditemukan"
        }
        _recordedFilePath.value = path
        return path
    }

    private val _playbackPositionMs = MutableStateFlow(0)
    val playbackPositionMs: StateFlow<Int> = _playbackPositionMs.asStateFlow()

    private val _playbackDurationMs = MutableStateFlow(0)
    val playbackDurationMs: StateFlow<Int> = _playbackDurationMs.asStateFlow()

    private var positionPollJob: Job? = null

    /**
     * [remoteUrl] plays a previously-submitted recording fetched from the
     * backend (teacher review, or a student re-listening to their own past
     * submission); omitted, it replays the just-recorded local file.
     */
    fun startPlayback(remoteUrl: String? = null, onCompleted: () -> Unit = {}) {
        if (_isPlaying.value) return
        _errorMessage.value = null

        try {
            mediaPlayer?.release()
            val player = MediaPlayer().apply {
                when {
                    remoteUrl != null -> setDataSource(context, android.net.Uri.parse(remoteUrl))
                    currentOutputFile?.exists() == true -> setDataSource(currentOutputFile!!.absolutePath)
                    else -> throw IllegalStateException("Tidak ada berkas audio untuk diputar")
                }
                prepare()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    playbackParams = playbackParams.setSpeed(_playbackSpeed.value)
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    positionPollJob?.cancel()
                    _playbackPositionMs.value = 0
                    onCompleted()
                }
                start()
            }
            mediaPlayer = player
            _playbackDurationMs.value = player.duration
            _isPlaying.value = true

            positionPollJob = scope.launch {
                while (_isPlaying.value) {
                    _playbackPositionMs.value = try { mediaPlayer?.currentPosition ?: 0 } catch (_: Exception) { 0 }
                    delay(100)
                }
            }
        } catch (e: Exception) {
            _isPlaying.value = false
            _errorMessage.value = "Gagal memutar audio: ${e.localizedMessage ?: "berkas tidak dapat dibaca"}"
        }
    }

    /** Seeks to [positionMs] — used when tapping a point on the waveform. */
    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _playbackPositionMs.value = positionMs
        } catch (_: Exception) {}
    }

    fun pausePlayback() {
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
        positionPollJob?.cancel()
        _isPlaying.value = false
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.playbackParams = it.playbackParams.setSpeed(speed)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun release() {
        amplitudeJob?.cancel()
        positionPollJob?.cancel()
        try {
            mediaRecorder?.release()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
        mediaPlayer = null
    }
}
