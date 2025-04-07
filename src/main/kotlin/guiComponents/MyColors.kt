package guiComponents

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

/**
 * Contains the common colors used within QSRK!
 */
object MyColors {
    val matchSyntax = Color.hsl(hue = 43f, saturation = 1f, lightness = 0.51f) // Yellow
    val clickableSyntax = Color.hsl(hue = 199f, saturation = 0.64f, lightness = 0.73f) // Cyan 199
    val errorSyntax = Color.hsv(hue = 346f, saturation = 0.8f, value = 1f) // Magenta
    val fieldBackground = Color.DarkGray
    val fieldBorder = Color.Transparent
    val windowBackground = Color.hsv(hue = 15f, saturation = 0.75f, value = 1f) // Orange 
    val cursorColor = SolidColor(Color.White)
}