package guiComponents.fields

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


@Composable
inline fun lineField(textStyle: TextStyle) {
    // LineBox
    BasicTextField(
        value = LineNumbers.text,
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