package parsing

import guiComponents.MyColors
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration

/**
 * Represents a [range] of contiguous text coming from a source which needs to be rendered with the specified [style].
 * [clickable] specifies if the portion of text needs also to be rendered clickable.
 */
data class Segment(val range: IntRange, val style: SpanStyle = SpanStyle(), val clickable: Boolean = false) {
    companion object {

        /**
         * Creates a clickable segment with an appropriate styling for a portion of text in [range].
         */
        fun createClickableSegment(range: IntRange): Segment = Segment(
            range = range,
            style = SpanStyle(
                color = MyColors.clickableSyntax,
                textDecoration = TextDecoration.Underline
            ),
            clickable = true
        )

        /**
         * Creates a segment for a portion of text in [range] which is marked by the Syntax Highlighter.
         */
        fun createSyntaxSegment(range: IntRange): Segment = Segment(
            range = range,
            style = SpanStyle(
                color = MyColors.matchSyntax
            )
        )

        /**
         * Creates a segment for a portion of text in [range] which is marked as an error.
         */
        fun createErrorSegment(range: IntRange): Segment = Segment(
            range = range,
            style = SpanStyle(
                color = MyColors.errorSyntax,
            )
        )
    }
}