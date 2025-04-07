import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

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
        val backgroundColor = Color.DarkGray
        val borderColor = Color.Transparent

        BoxWithConstraints {
//            val windowHeight = maxHeight
//            val windowWidth = maxWidth
            val scope = rememberCoroutineScope()

            Row {
                Spacer(Modifier.width(10.dp))
                Column {
                    Spacer(Modifier.height(10.dp))

                    Row{
                        // ScriptBox
                        BasicTextField(
                            value = LineNumbers.text,
                            readOnly = true,
                            onValueChange = {  },
                            textStyle = textStyle,
                            modifier = Modifier
                                .background(backgroundColor)
                                .border(
                                    width = 2.dp,
                                    brush = SolidColor(borderColor),
                                    shape = RectangleShape
                                )
                                .fillMaxWidth(0.025f)
                                .fillMaxHeight(0.8f)
                        )

                        // ScriptBox
                        BasicTextField(
                            value = script,
                            onValueChange = { script = it },
                            textStyle = textStyle,
                            modifier = Modifier
                                .background(backgroundColor)
                                .border(
                                    width = 0.dp,
                                    brush = SolidColor(borderColor),
                                    shape = RectangleShape
                                )
                                .fillMaxWidth(0.85f)
                                .fillMaxHeight(0.8f)
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.key == Key.Enter){
                                        LineNumbers.addLine()
                                        true
                                    } else {
                                        false
                                    }
                                }
                        )
                    }


                    Spacer(Modifier.height(10.dp))

                    // OutputBox
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
                            .background(color = backgroundColor)
                            .border(
                                width = 2.dp,
                                brush = SolidColor(borderColor),
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
                                                script = script.copy(selection = TextRange(cursorOffset, cursorOffset))
                                            }
                                        }
                                    }
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Spacer(Modifier.height(10.dp))

                    // PlayButton
                    Button(
                        enabled = status.value.statusType != ScriptStatus.StatusType.RUNNING,
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                scriptExecution(output, script.text, status, currentProcess)
                            }
                        },
                        content = {
                            Image(
                                painter = painterResource("drawable/playButton.svg"),
                                contentDescription = "button icon",
                                modifier = Modifier.size(100.dp)
                            )
                        },
                        modifier = Modifier
                            .size(100.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    // StopButton
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.Default) {
                                terminateProcess(currentProcess)

                            }
                        },
                        content = {
                            Image(
                                painter = painterResource("drawable/stopButton.svg"),
                                contentDescription = "button icon",
                                modifier = Modifier.size(100.dp)
                            )
                        },
                        modifier = Modifier
                            .size(100.dp)
                    )

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

/**
 * Aborts the [currentProcess] if it exists.
 */
private fun terminateProcess(currentProcess: MutableState<Process?>) = currentProcess.value?.destroyForcibly()

/**
 * executes the [body] as a Kotlin script, updating the [output] Text Label
 * accordingly.
 */
private fun scriptExecution(
    output: MutableState<String>,
    body: String,
    status: MutableState<ScriptStatus>,
    currentProcess: MutableState<Process?>
) {
    output.value = ""

    // Save the content of body in the file tempScript.kts
    val permissions = PosixFilePermissions.fromString("rwxrwxrwx")
    val tempFile = createTempFile(
        prefix = "tempScript",
        suffix = ".kts",
        PosixFilePermissions.asFileAttribute(permissions)
    ).also {
        it.toFile().deleteOnExit()
        it.writeText(text = body)
    }

    status.value = ScriptStatus(ScriptStatus.StatusType.RUNNING)

    // Create a process for said file and prints its execution in the terminal
    val exitStatus = executeScript(
        source = tempFile.toString(),
        content = output,
        currentProcess
    )

    // General updates after process termination
    val exitMessage = "\nScript terminated with exit status: $exitStatus"
    output.value += exitMessage
    status.value = ScriptStatus.fromExitStatus(exitStatus)
    currentProcess.value = null
}


fun main() =
    application {
        Window(title = "QSRK!", onCloseRequest = ::exitApplication) {
            App()
        }
    }

