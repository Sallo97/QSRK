import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Handles the Syntax Highlighting
 * TODO
 */
object SyntaxTransformation : VisualTransformation {
    private val tokens = listOf(
        "as", "break", "class", "else", "false", "for", "fun", "val", "println",
        "if", "interface", "object", "data", "true", "null", "return", "error", "while"
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val wordRegex = "\\S+".toRegex()
        var lastTextIdx = 0

        val result = buildAnnotatedString {
            if (rawText.isEmpty()) {
                append(rawText)
            }
            else {
                rawText.lines().also {
                    val lastLine = it.lastIndex
                    it.forEachIndexed { index, line ->
                        var lastLineIdx = -1  // Reset for each new line

                        wordRegex.findAll(line).forEach { matchResult ->
                            // Check if we need to append space
                            if (matchResult.range.first > lastLineIdx + 1) {
                                val spaceSegment = IntRange(
                                    start = lastLineIdx + 1,
                                    endInclusive = matchResult.range.first - 1
                                )
                                append(
                                    AnnotatedString(
                                        text = line.substring(spaceSegment),
                                    )
                                )
                            }

                            // Append word
                            append(
                                AnnotatedString(
                                    text = matchResult.value,
                                    spanStyle = parseWord(matchResult.value, matchResult.range).style
                                )
                            )
                            lastLineIdx = matchResult.range.last
                        }

                        // Appending missing space in the line if it exists
                        if (lastLineIdx < line.length - 1) {
                            append(
                                AnnotatedString(
                                    text = line.substring(lastLineIdx + 1)
                                )
                            )
                        }

                        // Appending missing \n if its not the last line
                        if (index < lastLine) {
                            append(
                                AnnotatedString(
                                    text = "\n"
                                )
                            )
                        }

                        lastTextIdx += line.length + (if (index < lastLine) 1 else 0)
                    }
                }
            }
        }

        return TransformedText(result, OffsetMapping.Identity)
    }

    /**
     * Determines the style of the word
     */
    private fun parseWord(word: String, range: IntRange): Segment =
        if (word in tokens)
            Segment.createSyntaxSegment(range = range)
        else
            Segment(range = range)
}