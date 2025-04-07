package guiComponents.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import parsing.errorParsing.ErrorTransformation

@Composable
fun outputField(
    textLayoutResult: MutableState<TextLayoutResult?>,
    output: MutableState<String>,
    textStyle: TextStyle,
    script: MutableState<TextFieldValue>
){
    BasicTextField(
        onTextLayout = {
            textLayoutResult.value = it
        },
        enabled = false,
        readOnly = true,
        visualTransformation = ErrorTransformation,
        value = output.value,
        textStyle = textStyle,
        onValueChange = { },
        modifier = Modifier
            .background(color = MyColors.fieldBackground)
            .border(
                width = 2.dp,
                brush = SolidColor(MyColors.fieldBorder),
                shape = RectangleShape
            )
            .fillMaxWidth(0.85f)
            .fillMaxHeight(0.8f)
            .verticalScroll(rememberScrollState())  // Enable scrolling
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    textLayoutResult.let { it ->
                        val clickedCharOffset = it.value!!.getOffsetForPosition(tapOffset)
                        ErrorTransformation.currentAnnotatedString?.let { annotatedString ->
                            annotatedString.getStringAnnotations(
                                tag = "CLICKABLE",
                                start = clickedCharOffset,
                                end = clickedCharOffset
                            ).firstOrNull()?.let {
                                // Parsing tag to retrieve row and col
                                val listError = it.item.split(":").map { str ->
                                    str.substringBefore(")").toInt()
                                }
                                // Determine and setting cursor position
                                val row = listError.first()
                                val col = if (listError.size == 2) (listError[1]) else 0
                                val cursorOffset = findCursorPosition(row, col, script.value.text)
                                script.value =
                                    script.value.copy(selection = TextRange(cursorOffset, cursorOffset))
                            }
                        }
                    }
                }
            }
    )
}

/**
 * Given the [row] and [col] of an error description referring to [text], returns the exact cursor position the error
 * refers to
 */
private fun findCursorPosition(row: Int, col: Int = 0, text: String): Int {
    // Retrieving actual position
    val realRow = if (row != 0) row - 1 else 0
    val realCol = if (col != 0) col - 1 else 0
    var offsetRow = 0
    val lines = text.lines()
    for (i in 0..<realRow)
        offsetRow += lines[i].length + 1
    return offsetRow + realCol

}