package guiComponents.fields

import LineNumbers
import guiComponents.MyColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import parsing.syntaxParsing.SyntaxTransformation

/**
 * Creates the panel for editing the script.
 */
@Composable
inline fun editField(
    script: MutableState<TextFieldValue>,
    textStyle: TextStyle,
    lineNumbers: LineNumbers
) {
    BasicTextField(
        value = script.value,
        onValueChange = { script.value = it },
        visualTransformation = SyntaxTransformation,
        textStyle = textStyle,
        cursorBrush = MyColors.cursorColor, // 👈 White cursor
        modifier = Modifier
            .background(MyColors.fieldBackground)
            .border(
                width = 0.dp,
                brush = SolidColor(MyColors.fieldBorder),
                shape = RectangleShape
            )
            .onKeyEvent { keyEvent ->
                // A user can either create new lines by pressing `\n` or removing some with `DEL`
                if (keyEvent.key == Key.Enter || keyEvent.key == Key.Backspace) {
                    lineNumbers.updateLines(script.value.text)
                    true
                } else {
                    false
                }
            }
    )
}