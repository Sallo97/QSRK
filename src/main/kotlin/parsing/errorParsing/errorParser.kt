package parsing.errorParsing

import parsing.Segment
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Returns true if [line] indicated that the user misses the `kotlinc` compiler, false otherwise
 */
fun isMissingCompiler(line: String?): Boolean = LineType.fromLine(line) == LineType.MISSING_COMPILER

/**
 * Handles the highlighting of errors in the printed output of a process.
 */
object ErrorParser {
    /**
     * Given a range obtained within a single line, returns the new range of said line within the [offset]
     * where it is found in content. If it is the last segment of a line it also counts the LF.
     */
    private fun IntRange.rangeInContent(offset: Int, isLastSegment: Boolean = false): IntRange {
        val newLast = if (isLastSegment) last + 1 else last
        return IntRange(first + offset, newLast + offset)
    }

    /**
     * Parses [line], creating a list of segments for stylising said line.
     * The segments will have ranges after [offset]
     */
    fun parseLine(line: String, offset: Int = 0): List<Segment> {

        val newSegments =
            when (LineType.fromLine(line)) {
                LineType.ERROR -> {
                    // An error line is composed of the following components:
                    // - clickableRange = the initial portion in the form "path_to_script:row:col:" (will be parsed as
                    //                      clickable).
                    // - spaceSegment1 = the space between clickableRange and errorRange.
                    // - errorRange = the message "error:" (will be parsed with a red coloring).
                    // - spaceSegment2 = the space between errorRange and remainderRange.
                    // - remainderSegment = the explanation by kotlinc of the error.
                    val matchResult = LineType.ERROR_REGEX.find(line)!!

                    val clickableRange = IntRange(
                        start = matchResult.groups[1]!!.range.first,
                        endInclusive = matchResult.groups[2]!!.range.last
                    ).rangeInContent(offset)

                    val clickableSegment = Segment.createClickableSegment(clickableRange)

                    val spaceSegment1 = Segment(
                        range = IntRange(start = clickableRange.last + 1, endInclusive = clickableRange.last + 1)
                    )

                    val errorRange = matchResult.groups[3]!!.range.rangeInContent(offset)
                    val errorSegment = Segment.createErrorSegment(errorRange)

                    val spaceSegment2 = Segment(
                        range = IntRange(start = errorRange.last + 1, endInclusive = errorRange.last + 1),
                        style = SpanStyle(fontWeight = FontWeight.Bold)
                    )

                    val explanationRange = matchResult.groups[4]!!.range.rangeInContent(offset, true)
                    val explanationSegment = Segment(range = explanationRange)

                    listOf(clickableSegment, spaceSegment1, errorSegment, spaceSegment2, explanationSegment)
                }

                LineType.EXCEPTION -> {
                    // With Java Exception we parse only the line containing the indication of the row where it happens.
                    // - start = the initial portion in the form "\tat".
                    // - clickableRange = "Class.<init>(script.kts:row)" (must be clickable within the GUI)
                    // - remainderRange = the remaining space (if there is any)
                    val matchResult = LineType.EXCEPTION_REGEX.find(line)!!

                    val startRange = matchResult.groups[1]!!.range.rangeInContent(offset)
                    val startSegment = Segment(
                        range = startRange,
                    )

                    val clickableRange = IntRange(
                        start = matchResult.groups[2]!!.range.first,
                        endInclusive = matchResult.groups[6]!!.range.last
                    ).rangeInContent(offset)
                    val clickableSegment = Segment.createClickableSegment(clickableRange)

                    val remainderRange = matchResult.groups[7]!!.range.rangeInContent(offset, true)
                    val remainderSegment = Segment(
                        range = remainderRange,
                    )

                    listOf(startSegment, clickableSegment, remainderSegment)
                }

                else -> {
                    val segmentRange = IntRange(0, line.lastIndex).rangeInContent(offset, true)
                    val segment = Segment(range = segmentRange)
                    listOf(segment)
                }
            }
        return newSegments
    }
}

/**
 * Describes the type of lines parsed for errors:
 * ERROR = it describes a Kotlin error.
 * EXCEPTION = it describes a Java exception.
 * MISSING_COMPILER = it indicates that the user misses the kotlinc compiler.
 */
enum class LineType {
    ERROR, EXCEPTION, MISSING_COMPILER;

    companion object {
        val ERROR_REGEX = Regex("((?:/*\\w*)*.kts)(:\\d*:\\d*:) (error:) ((.*\\s*)*)")
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
                    MISSING_REGEX.matches(line) -> MISSING_COMPILER
                    else -> null
                }
            }
    }
}
