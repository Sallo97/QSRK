import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Represents a [range] of contiguous text in the output which needs to be rendered with the specified [style]
 * and could be [clickable] or not.
 */
data class Segment(val range: IntRange, val style: SpanStyle = SpanStyle(), val clickable: Boolean = false) {
    companion object {
        fun createClickableSegment(range: IntRange): Segment = Segment(
            range = range,
            style = SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline
            ),
            clickable = true
        )
    }
}


/**
 * Returns true if [line] indicated that the user misses the `kotlinc` compiler, false otherwise
 */
fun isMissingCompiler(line: String?): Boolean = LineType.fromLine(line) == LineType.MISSING

/**
 * Handles the highlighting of errors in the printed output of a process.
 */
class ErrorParser {
    val segments: MutableList<Segment> = mutableListOf()

    /**
     * Given a range obtained within a single line, returns the new range of said line within the [offset]
     * where it is found in content.
     */
    private fun IntRange.rangeInContent(offset: Int): IntRange =
        IntRange(first + offset, last + offset)

    /**
     * Parses [line], modifying the content and setting the style accordingly.
     */
    fun parseLine(line: String, content: MutableState<String>, startLineIdx: Int = 0) {

        // Update content by replacing the name to the temp file in the lines with `script.kts`
        val lineType = LineType.fromLine(line)
        val (newLine, newSegments) =
            when (lineType) {
                LineType.ERROR -> {
                    val newLine = LineType.replacePath(lineType, line)!!
                    val matchResult = LineType.ERROR_REGEX.find(newLine)!!

                    val clickableRange = IntRange (
                        start = matchResult.groups[1]!!.range.first,
                        endInclusive = matchResult.groups[2]!!.range.last
                    ).rangeInContent(startLineIdx)

                    val clickableSegment = Segment.createClickableSegment(clickableRange)

                    // CONSIDER THE SPACE HERE!
                    val spaceSegment1 = Segment(
                        range = IntRange(start = clickableRange.last+1, endInclusive = clickableRange.last+1)
                    )

                    val errorRange = matchResult.groups[3]!!.range.rangeInContent(startLineIdx)
                    val errorSegment = Segment(
                        range = errorRange,
                        style = SpanStyle(
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    val spaceSegment2 = Segment(
                        range = IntRange(start = errorRange.last+1, endInclusive = errorRange.last+1),
                        style = SpanStyle(fontWeight = FontWeight.Bold)
                    )

                    val remainderRange = matchResult.groups[4]!!.range.rangeInContent(startLineIdx)
                    val remainderSegment = Segment (range = remainderRange)

                    newLine to listOf(clickableSegment, spaceSegment1, errorSegment, spaceSegment2, remainderSegment)
                }

                LineType.EXCEPTION -> {
                    val newLine = LineType.replacePath(lineType, line)!!
                    val matchResult = LineType.EXCEPTION_REGEX.find(newLine)!!

                    val startRange = matchResult.groups[1]!!.range.rangeInContent(startLineIdx)
                    val startSegment = Segment (
                        range = startRange,
                        style = SpanStyle(fontWeight = FontWeight.Bold)
                    )

                    val clickableRange = IntRange(
                        start = matchResult.groups[2]!!.range.first,
                        endInclusive = matchResult.groups[6]!!.range.last
                    ).rangeInContent(startLineIdx)
                    val clickableSegment = Segment.createClickableSegment(clickableRange)

                    val remainderRanger = matchResult.groups[7]!!.range.rangeInContent(startLineIdx)
                    val remainderSegment = Segment(
                        range = remainderRanger,
                        style = SpanStyle(fontWeight = FontWeight.Bold)
                    )

                    newLine to listOf(startSegment, clickableSegment, remainderSegment)
                }

                else -> {
                    val segmentRange = IntRange(0, line.lastIndex).rangeInContent(startLineIdx)
                    val segment = Segment(range = segmentRange)
                    line to listOf(segment)
                }
            }
        content.value = content.value.substring(startIndex = 0, endIndex = startLineIdx) + newLine
        segments.addAll(newSegments)
    }
}

/**
 * TODO Add better description
 */
enum class LineType {
    ERROR, EXCEPTION, MISSING;

    companion object {
        val ERROR_REGEX = Regex("((?:/*\\w*)*.kts)(:\\d*:\\d*:) (error:) ((\\w*\\s*)*)")
        val EXCEPTION_REGEX = Regex("(\\s+at )(\\w+)(.<init>)(\\(\\w+.kts)([:\\d]+)(\\))(\\s*)")
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
