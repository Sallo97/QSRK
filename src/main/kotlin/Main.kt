import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

@Composable
@Preview
fun App() {

    MaterialTheme {
        var script by remember { mutableStateOf(TextFieldValue(text = "Write your script here!")) }
        val output = remember { mutableStateOf("The output of you script will be printed here!") }
        val status = remember { mutableStateOf(ScriptStatus()) }
        val currentProcess: MutableState<Process?> = remember { mutableStateOf(null) }
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        BoxWithConstraints {
//            val windowHeight = maxHeight
//            val windowWidth = maxWidth
            val scope = rememberCoroutineScope()

            Row {
                Spacer(Modifier.width(10.dp))
                Column {
                    Spacer(Modifier.height(10.dp))

                    // ScriptBox
                    BasicTextField(
                        value = script,
                        onValueChange = { script = it },
                        modifier = Modifier
                            .background(color = Color.LightGray)
                            .border(
                                width = 2.dp,
                                brush = SolidColor(Color.DarkGray),
                                shape = RectangleShape
                            )
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight(0.8f)
                    )

                    Spacer(Modifier.height(10.dp))

                    // OutputBox
                    BasicTextField(
                        onTextLayout = {
                            textLayoutResult = it
                        },

                        enabled = false,
                        visualTransformation = ErrorTransformation,
                        readOnly = true,
                        value = output.value,
                        onValueChange = { },
                        modifier = Modifier
                            .background(color = Color.LightGray)
                            .border(
                                width = 2.dp,
                                brush = SolidColor(Color.DarkGray),
                                shape = RectangleShape
                            )
                            .fillMaxWidth(0.85f)
                            .fillMaxHeight(0.8f)
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
                                                val row = listError.first() - 1
                                                val col = if (listError.size == 2) (listError[1] - 1) else 0
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
 * TODO add better description
 */
fun findCursorPosition(row:Int, col:Int = 0, text: String) : Int{
    // Retrieving actual position
    var offsetRow = 0
    val lines = text.lines()
    for (i in 0..<row)
        offsetRow += lines[i].length + 1
    return offsetRow + col

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

