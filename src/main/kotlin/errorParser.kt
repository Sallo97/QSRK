import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Represents a [range] of contiguous text in the output which needs to be rendered with the specified [style]
 * and could be [clickable] or not.
 */
data class Segment(val range: IntRange, val style: SpanStyle, val clickable: Boolean = false) {
    companion object {
        fun createClickableSegment(range: IntRange): Segment = Segment(
            range = range,
            style = SpanStyle(
                color = Color.Cyan,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            ),
            clickable = true
        )
    }
}

/**
 * Handles the highlighting of errors in the printed output of a process.
 */
object ErrorParser {
    private val segments: MutableList<Segment> = mutableListOf()

    /**
     * Returns true if [line] indicated that the user misses the `kotlinc` compiler, false otherwise
     */
    fun isMissingCompiler(line: String?): Boolean = LineType.fromLine(line) == LineType.MISSING

    /**
     * Given a range obtained within a single line, returns the new range of said line within the [offset]
     * where it is found in content.
     */
    private fun IntRange.rangeInContent(offset: Int): IntRange =
        IntRange(first + offset, last + offset)

    /**
     * Parses [line], modifying the content and setting the style accordingly.
     */
    fun parseLine(line: String, content: MutableState<String>, startLineIdx: Int) {

        // Update content by replacing the name to the temp file in the lines with `script.kts`
        val lineType = LineType.fromLine(line)
        val (newLine, newSegments) =
            when (lineType) {
                LineType.ERROR -> {
                    val newLine = LineType.replacePath(lineType, line)!!
                    val matchResult = LineType.ERROR_REGEX.find(newLine)!!

                    val clickableRange = matchResult.groups[2]!!.range.rangeInContent(startLineIdx)
                    val clickableSegment = Segment.createClickableSegment(clickableRange)

                    val errorRange = matchResult.groups[3]!!.range.rangeInContent(startLineIdx)
                    val errorSegment = Segment(
                        range = errorRange,
                        style = SpanStyle(
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    newLine to listOf(clickableSegment, errorSegment)
                }

                LineType.EXCEPTION -> {
                    val newLine = LineType.replacePath(lineType, line)!!
                    val matchResult = LineType.EXCEPTION_REGEX.find(newLine)!!

                    val clickableRange = matchResult.groups[3]!!.range.rangeInContent(startLineIdx)
                    val clickableSegment = Segment.createClickableSegment(clickableRange)

                    newLine to listOf(clickableSegment)
                }

                else -> line to null
            }
        content.value = content.value.substring(startIndex = 0, endIndex = startLineIdx) + newLine
        newSegments?.let { segments.addAll(it) }
    }
}

/**
 * TODO Add better description
 */
private enum class LineType {
    ERROR, EXCEPTION, MISSING;

    companion object {
        val ERROR_REGEX = Regex("((?:/*\\w*)*.kts)(:\\d*:\\d*:) (error:) ((\\w*\\s*)*)")
        val EXCEPTION_REGEX = Regex("(\\s+at )(\\w+)(.<init>)(\\(\\w+.kts)([:\\d]+)(\\))(\\n*)")
        val MISSING_REGEX = Regex("Cannot run program \"\\w+\": error=\\d+, No such file or directory")

        /**
         * checks the [line] type and updates the content accordingly
         */
        fun fromLine(line: String?): LineType? =
            line?.let {
                when {
                    ERROR_REGEX.matches(line) -> ERROR
                    EXCEPTION_REGEX.matches(line) -> EXCEPTION
                    MISSING_REGEX.matches(line) -> MISSING
                    else -> null
                }
            }

        /**
         * Given a [line] of type [lineType] replace the full path to the temp file
         * with `script.kts`
         */
        fun replacePath(lineType: LineType, line: String): String? = when (lineType) {
                ERROR -> line.replace(LineType.ERROR_REGEX, "script.kts$2 $3 $4")
                EXCEPTION -> line.replace(LineType.EXCEPTION_REGEX, "$1Script$3(script.kts$5$6$7")
                else -> null
            }
    }
}
