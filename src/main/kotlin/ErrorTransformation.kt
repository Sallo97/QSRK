import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation


object ErrorTransformation : VisualTransformation {
    private val segments: MutableList<Segment> = mutableListOf()


    /**
     * Resets the errorTransformation for handling a new process
     */
    private fun reset() {
        segments.clear()
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
                        append(
                            AnnotatedString(
                                text = rawText.substring(segment.range),
                                spanStyle = segment.style
                            )
                        )
                        lastIdx = segment.range.last + 1
                    }

                // Parse the remaining portion of text
                val remainderText = rawText.substring(lastIdx, rawText.length)
                remainderText.lines().forEach { line ->
                    ErrorParser.parseLine(line = line, startLineIdx = lastIdx).also {
                        it.forEach { newSegment ->
                            append(
                                AnnotatedString(
                                    text = rawText.substring(newSegment.range),
                                    spanStyle = newSegment.style
                                )
                            )
                            lastIdx = newSegment.range.last + 1
                        }
                        segments.addAll(it)
                    }
                }
                if(lastIdx < rawText.length) {
                    append(rawText.substring(lastIdx))
                }
            }.also {
                return TransformedText(it, OffsetMapping.Identity)
            }
        return TransformedText(text, OffsetMapping.Identity)
    }
}