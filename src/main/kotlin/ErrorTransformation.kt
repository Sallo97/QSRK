import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

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
    private fun appendSegment(segment:Segment, builder:AnnotatedString.Builder, rawText: String){
        if (segment.clickable){
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
                remainderText.lines().forEach { line ->
                    ErrorParser.parseLine(line = line, startLineIdx = lastIdx).also {
                        it.forEach { newSegment ->
                            appendSegment(newSegment, builder = this, rawText)
                            lastIdx = newSegment.range.last + 1
                        }
                        segments.addAll(it)
                    }
                }
                if(lastIdx < rawText.length) {
                    append(rawText.substring(lastIdx))
                }
            }.also {
                currentAnnotatedString = it
                return TransformedText(it, OffsetMapping.Identity)
            }
        return TransformedText(text, OffsetMapping.Identity)
    }
}