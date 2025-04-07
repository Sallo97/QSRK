import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import guiComponents.MyColors
import guiComponents.buttons.playButton
import guiComponents.buttons.stopButton
import guiComponents.fields.editField
import guiComponents.fields.lineField
import guiComponents.fields.outputField
import scriptHandler.ScriptStatus

/**
 * The following class handles the updating of the numbered lines in the output panel.
 */
class LineNumbers(val lineText: MutableState<String>) {
    private var count: Int = 1

    /**
     * Updates the number lines in the output panel in order to match the number of lines in [text].
     */
    fun updateLines(text: String) {
        text.lines().size.apply {
            // Case: we need to add more lines
            if (count < this) {
                for (i in 1..(this - count)) {
                    count++
                    lineText.value += "\n${count}"
                }
            }

            // Case: we need to remove lines
            else {
                for (i in 1..(count - this)) {
                    count--
                    lineText.value = lineText.value.substringBeforeLast("\n")
                }
            }
        }
    }
}



@Composable
@Preview
fun App() {
    /**
     * Constructs the GUI application. QSRK! is mainly composed of a row containing two column:
     * - the first one defines the edit panel (which also considers a line panel) and output panel.
     * - the second one defines the play and stop buttons and the status icon.
     */
    MaterialTheme {
        val script = remember { mutableStateOf(TextFieldValue(text = "Write your script here!")) }
        val output = remember { mutableStateOf("The output of you script will be printed here!") }
        val status = remember { mutableStateOf(ScriptStatus()) }
        val currentProcess: MutableState<Process?> = remember { mutableStateOf(null) }
        val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
        val lineNumbers = LineNumbers(remember { mutableStateOf("1") })
        val textStyle = TextStyle(color = Color.LightGray, fontFamily = FontFamily.Monospace)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MyColors.windowBackground)
        ) {
            val scope = rememberCoroutineScope()

            Row {

                Spacer(Modifier.width(10.dp))

                // Panels construction
                Column {
                    Spacer(Modifier.height(10.dp))

                    // Line Panel + Edit Panel constructions
                    Box(
                        modifier = Modifier
                            .background(MyColors.fieldBackground, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight(0.8f)
                            .verticalScroll(rememberScrollState())  // Enable scrolling
                    ) {
                        Row {
                            lineField(textStyle, lineNumbers)
                            editField(script, textStyle, lineNumbers)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Output Panel construction
                    Box(
                        modifier = Modifier
                            .background(MyColors.fieldBackground, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        outputField(
                            textLayoutResult,
                            output,
                            textStyle,
                            script
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Buttons constructions
                Column {
                    Spacer(Modifier.height(10.dp))
                    playButton(
                        status,
                        scope,
                        output,
                        currentProcess,
                        script.value
                    )
                    Spacer(Modifier.height(10.dp))
                    stopButton(scope, currentProcess)
                    Spacer(Modifier.height(10.dp))
                    Icon(
                        imageVector = status.value.icon,
                        contentDescription = "status Icon",
                        modifier = Modifier
                            .size(100.dp)
                    )
                }
            }
        }

    }
}


fun main() =
    application {
        Window(title = "QSRK!", onCloseRequest = ::exitApplication) {
            App()
        }
    }

