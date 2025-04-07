package guiComponents.fields

import LineNumbers
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import guiComponents.MyColors

/**
 * Creates the panel that numbers the lines of the current written script.
 */
@Composable
inline fun lineField(
    textStyle: TextStyle,
    lineNumbers: LineNumbers) {
    // LineBox
    BasicTextField(
        value = lineNumbers.lineText.value,
        readOnly = true,
        onValueChange = { },
        textStyle = textStyle,
        modifier = Modifier
            .background(MyColors.fieldBackground)
            .border(
                width = 2.dp,
                brush = SolidColor(MyColors.fieldBorder),
                shape = RectangleShape
            )
            .fillMaxWidth(0.04f)
            .fillMaxHeight()
    )
}