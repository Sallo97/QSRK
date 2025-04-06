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
                        if (segment.clickable){
                            val annotationData = rawText.substring(segment.range)
                                .substringAfterLast(".kts:")
                                .substringBeforeLast(":")
                            pushStringAnnotation(tag = "CLICKABLE", annotation = annotationData)
                            withStyle(style = segment.style) {
                                append(text = rawText.substring(segment.range))
                            }
                            pop()
                        } else {
                            append(
                                AnnotatedString(
                                    text = rawText.substring(segment.range),
                                    spanStyle = segment.style
                                )
                            )
                        }

                        lastIdx = segment.range.last + 1
                    }

                // Parse the remaining portion of text
                val remainderText = rawText.substring(lastIdx, rawText.length)
                remainderText.lines().forEach { line ->
                    ErrorParser.parseLine(line = line, startLineIdx = lastIdx).also {
                        it.forEach { newSegment ->
                            if (newSegment.clickable){
                                val annotationData = rawText.substring(newSegment.range)
                                    .substringAfterLast(".kts:")
                                    .substringBeforeLast(":")
                                pushStringAnnotation(tag = "CLICKABLE", annotation = annotationData)
                                withStyle(style = newSegment.style) {
                                    append(text = rawText.substring(newSegment.range))
                                }
                                pop()
                            }
                            else {
                                append(
                                    AnnotatedString(
                                        text = rawText.substring(newSegment.range),
                                        spanStyle = newSegment.style
                                    )
                                )
                            }
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