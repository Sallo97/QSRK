import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import guiComponents.buttons.playButton
import guiComponents.buttons.stopButton
import parsing.errorParsing.ErrorTransformation
import parsing.syntaxParsing.SyntaxTransformation
import scriptHandler.ScriptStatus


data object LineNumbers{
    var text: String = "1"
    var count: Int = 1
}
fun LineNumbers.addLine(){
    count ++
    text += "\n${count}"
}


@Composable
@Preview
fun App() {

    MaterialTheme {
        var script by remember { mutableStateOf(TextFieldValue(text = "Write your script here!")) }
        val output = remember { mutableStateOf("The output of you script will be printed here!") }
        val status = remember { mutableStateOf(ScriptStatus()) }
        val currentProcess: MutableState<Process?> = remember { mutableStateOf(null) }
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        val textStyle = TextStyle(color = Color.LightGray, fontFamily = FontFamily.Monospace)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MyColors.windowBackground)
        ){
            val scope = rememberCoroutineScope()

            Row {
                Spacer(Modifier.width(10.dp))
                Column {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .background(MyColors.fieldBackground, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight(0.8f)
                            .verticalScroll(rememberScrollState())  // Enable scrolling
                    ) {
                        Row {
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

                            // ScriptBox
                            BasicTextField(
                                value = script,
                                onValueChange = { script = it },
                                visualTransformation = SyntaxTransformation,
                                textStyle = textStyle,
                                modifier = Modifier
                                    .background(MyColors.fieldBackground)
                                    .border(
                                        width = 0.dp,
                                        brush = SolidColor(MyColors.fieldBorder),
                                        shape = RectangleShape
                                    )
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.key == Key.Enter) {
                                            LineNumbers.addLine()
                                            true
                                        } else {
                                            false
                                        }
                                    }
                            )
                        }
                    }


                    Spacer(Modifier.height(10.dp))

                    // OutputBox
                    Box(
                        modifier = Modifier
                            .background(MyColors.fieldBackground, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        BasicTextField(
                            onTextLayout = {
                                textLayoutResult = it
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
                                        textLayoutResult?.let { it ->
                                            val clickedCharOffset = it.getOffsetForPosition(tapOffset)
                                            ErrorTransformation.currentAnnotatedString?.let { annotatedString ->
                                                annotatedString.getStringAnnotations(
                                                    tag = "CLICKABLE",
                                                    start = clickedCharOffset,
                                                    end = clickedCharOffset
                                                ).firstOrNull()?.let {
                                                    // Parsing tag to retrieve row and col
                                                    val listError = it.item.split(":").map { str ->
                                                        str.toInt()
                                                    }
                                                    // Determine and setting cursor position
                                                    val row = listError.first()
                                                    val col = if (listError.size == 2) (listError[1]) else 0
                                                    val cursorOffset = findCursorPosition(row, col, script.text)
                                                    script =
                                                        script.copy(selection = TextRange(cursorOffset, cursorOffset))
                                                }
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Spacer(Modifier.height(10.dp))

                    playButton(
                        status,
                        scope,
                        output,
                        currentProcess,
                        script
                    )
                    Spacer(Modifier.height(10.dp))
                    stopButton(scope,currentProcess)
                    Spacer(Modifier.height(10.dp))

                    // StatusIcon
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

/**
 * Given the [row] and [col] of an error description referring to [text], returns the exact cursor position the error
 * refers to
 */
private fun findCursorPosition(row: Int, col: Int = 0, text: String): Int {
    // Retrieving actual position
    val realRow = row - 1
    val realCol = col - 1
    var offsetRow = 0
    val lines = text.lines()
    for (i in 0..<realRow)
        offsetRow += lines[i].length + 1
    return offsetRow + realCol

}

fun main() =
    application {
        Window(title = "QSRK!", onCloseRequest = ::exitApplication) {
            App()
        }
    }

