package parsing.errorParsing

import parsing.Segment
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle

object ErrorTransformation : VisualTransformation {
    private val segments: MutableList<Segment> = mutableListOf()
    var currentAnnotatedString: AnnotatedString? = null

    /**
     * Resets the errorTransformation for handling a new process
     */
    private fun reset() {
        segments.clear()
    }

    /**
     * Appends the segments
     */
    private fun appendSegment(segment: Segment, builder: AnnotatedString.Builder, rawText: String) {
        if (segment.clickable) {
            val annotationData = rawText.substring(segment.range)
                .substringAfterLast(".kts:")
                .substringBeforeLast(":")
            builder.pushStringAnnotation(tag = "CLICKABLE", annotation = annotationData)
            builder.withStyle(style = segment.style) {
                append(text = rawText.substring(segment.range))
            }
            builder.pop()
        } else {
            builder.append(
                AnnotatedString(
                    text = rawText.substring(segment.range),
                    spanStyle = segment.style
                )
            )
        }
    }

    /**
     * Handles the colouring of the text.
     */
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        var lastIdx = 0

        if (rawText == "") reset()
        else
            buildAnnotatedString {

                // Appending already parsed segments
                if (segments.isNotEmpty())
                    segments.forEach { segment ->
                        appendSegment(segment, builder = this, rawText)
                        lastIdx = segment.range.last + 1
                    }

                // Parse the remaining portion of text
                val remainderText = rawText.substring(lastIdx, rawText.length)
                remainderText.lines().also { lines ->
                    // Parse all the lines except the last one
                    lines.take(lines.size - 1).forEach { line ->
                        ErrorParser.parseLine(line = line, offset = lastIdx).also { newSegments ->
                            newSegments.forEach { segment ->
                                appendSegment(segment, this, rawText)
                                lastIdx = segment.range.last + 1
                            }
                            segments.addAll(newSegments)
                        }
                    }
                }

                if (lastIdx < rawText.length) {
                    append(rawText.substring(lastIdx))
                }
            }.also {
                currentAnnotatedString = it
                return TransformedText(it, OffsetMapping.Identity)
            }
        return TransformedText(text, OffsetMapping.Identity)
    }
}