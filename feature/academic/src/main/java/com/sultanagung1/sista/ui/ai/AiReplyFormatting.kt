package com.sultanagung1.sista.ui.ai

/**
 * FASE 76.7: presentation rules for tutor replies, kept free of Compose so they
 * are unit-tested on the JVM.
 */
object AiReplyFormatting {

    /** Roughly 5-7 lines of body text in a 290dp bubble. */
    const val MAX_BUBBLE_CHARS = 420

    /**
     * Splits a long reply into several short bubbles, the 2026 chat pattern for
     * people reading on a phone between other things: one wall of text becomes
     * a few pieces that can be read one at a time.
     *
     * - Paragraph breaks (blank lines) are the preferred cut; short paragraphs
     *   are merged back together up to [maxChars].
     * - A paragraph longer than [maxChars] is cut at sentence ends.
     * - A fenced code block (```) is never cut, even if it is long.
     * - Joining the bubbles with blank lines gives back the original
     *   paragraphs, so nothing is dropped.
     */
    fun splitIntoBubbles(text: String, maxChars: Int = MAX_BUBBLE_CHARS): List<String> {
        val normalized = text.replace("\r\n", "\n").trim()
        if (normalized.isEmpty()) return emptyList()
        if (normalized.length <= maxChars) return listOf(normalized)

        val pieces = mutableListOf<String>()
        for (paragraph in paragraphs(normalized)) {
            if (paragraph.length <= maxChars || paragraph.startsWith("```")) {
                pieces += paragraph
            } else {
                pieces += splitSentences(paragraph, maxChars)
            }
        }

        val bubbles = mutableListOf<String>()
        val current = StringBuilder()
        for (piece in pieces) {
            val isCode = piece.startsWith("```")
            val joinedLength = if (current.isEmpty()) piece.length else current.length + 2 + piece.length
            if (current.isNotEmpty() && (isCode || joinedLength > maxChars)) {
                bubbles += current.toString()
                current.clear()
            }
            if (current.isNotEmpty()) current.append("\n\n")
            current.append(piece)
            if (isCode) {
                bubbles += current.toString()
                current.clear()
            }
        }
        if (current.isNotEmpty()) bubbles += current.toString()
        return bubbles
    }

    /** Paragraphs separated by blank lines; a ``` fence and its body stay one paragraph. */
    private fun paragraphs(text: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inFence = false
        for (line in text.split("\n")) {
            if (line.trimStart().startsWith("```")) inFence = !inFence
            if (!inFence && line.isBlank()) {
                if (current.isNotBlank()) result += current.toString().trim()
                current.clear()
            } else {
                if (current.isNotEmpty()) current.append("\n")
                current.append(line)
            }
        }
        if (current.isNotBlank()) result += current.toString().trim()
        return result
    }

    private val SENTENCE_END = Regex("""(?<=[.!?])\s+""")

    private fun splitSentences(paragraph: String, maxChars: Int): List<String> {
        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        for (sentence in paragraph.split(SENTENCE_END)) {
            if (current.isNotEmpty() && current.length + 1 + sentence.length > maxChars) {
                chunks += current.toString()
                current.clear()
            }
            if (current.isNotEmpty()) current.append(' ')
            current.append(sentence)
        }
        if (current.isNotEmpty()) chunks += current.toString()
        return chunks
    }

    /** Plain text plus the character ranges that were wrapped in **double asterisks**. */
    data class StyledText(val text: String, val boldRanges: List<IntRange>)

    /**
     * Strips `**bold**` markers and reports where the bold runs are. The tutor's
     * own greeting uses this syntax, which the bubble used to print literally
     * with the asterisks showing. An unmatched `**` is left as typed.
     */
    fun parseBold(raw: String): StyledText {
        val out = StringBuilder()
        val ranges = mutableListOf<IntRange>()
        var i = 0
        while (i < raw.length) {
            val open = raw.indexOf("**", i)
            if (open < 0) {
                out.append(raw, i, raw.length)
                break
            }
            val close = raw.indexOf("**", open + 2)
            if (close < 0 || close == open + 2) {
                out.append(raw, i, raw.length)
                break
            }
            out.append(raw, i, open)
            val start = out.length
            out.append(raw, open + 2, close)
            ranges += start until out.length
            i = close + 2
        }
        return StyledText(out.toString(), ranges)
    }
}
