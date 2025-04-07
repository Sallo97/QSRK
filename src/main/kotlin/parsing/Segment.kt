package parsing

import guiComponents.MyColors
import androidx.compose.ui.text.SpanStyle
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
                color = MyColors.clickableSyntax,
                textDecoration = TextDecoration.Underline
            ),
            clickable = true
        )

        fun createSyntaxSegment(range: IntRange): Segment = Segment(
            range = range,
            style = SpanStyle(
                color = MyColors.matchSyntax
            )
        )

        fun createErrorSegment(range: IntRange): Segment = Segment(
            range = range,
            style = SpanStyle(
                color = MyColors.errorSyntax,
            )
        )
    }
}