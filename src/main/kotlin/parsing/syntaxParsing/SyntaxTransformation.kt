package parsing.syntaxParsing

import parsing.Segment
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Implements Syntax Highlighting in the edit panel.
 */
object SyntaxTransformation : VisualTransformation {
    private val tokens = listOf(
        "as", "break", "class", "else", "false", "for", "fun", "val", "println",
        "if", "interface", "object", "data", "true", "null", "return", "error", "while"
    )

    /**
     * Parse [text] and applies coloring the appropriate constructs of the Kotlin language.
     */
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val wordRegex = "\\S+".toRegex()
        var lastTextIdx = 0

        val result = buildAnnotatedString {
            // If the text is empy, no parsing is required
            if (rawText.isEmpty()) {
                append(rawText)
            } else {
                // For each line it subdivides it into words. Each word is then parsed into a syntaxSegment or a
                // normal segment. Space characters in the beginning, middle and end of lines are also parsed into
                // segments.
                rawText.lines().also {
                    val lastLine = it.lastIndex
                    it.forEachIndexed { index, line ->
                        var lastLineIdx = -1  // Reset for each new line

                        wordRegex.findAll(line).forEach { matchResult ->
                            // Check if there is some space between the current world and the previous one that we need
                            // to append.
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

                            // Append the current word
                            append(
                                AnnotatedString(
                                    text = matchResult.value,
                                    spanStyle = parseWord(matchResult.value, matchResult.range).style
                                )
                            )
                            lastLineIdx = matchResult.range.last
                        }

                        // Appending missing space characters at the end line if they exists
                        if (lastLineIdx < line.length - 1) {
                            append(
                                AnnotatedString(
                                    text = line.substring(lastLineIdx + 1)
                                )
                            )
                        }

                        // Appending missing `\n` if its not the last line
                        // Note that the last line never terminates with a `\n`
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