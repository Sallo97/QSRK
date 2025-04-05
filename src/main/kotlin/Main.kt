import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

/**
 * The possible type of status of a script.
 */
enum class StatusT {
    SUCCESS, FAIL, RUNNING, NOTHING;

    companion object {
        /**
         * Given the [exitStatus] of a script returns the associated StatusT.
         */
        fun fromExitStatus(exitStatus: Int): StatusT = if (exitStatus == 0) SUCCESS else FAIL

        /**
         * Given a [statusT] returns the associated icon (as of now it is just a text)
         */
        fun toIcon(statusT: StatusT): ImageVector =
            when (statusT) {
                SUCCESS -> Icons.Filled.Check
                FAIL -> Icons.Filled.Warning
                RUNNING -> Icons.Filled.Refresh
                NOTHING -> Icons.Filled.Star
            }
    }
}

@Composable
@Preview
fun App() {

    MaterialTheme {
        var script by remember { mutableStateOf("Write your script here!") }
        val output = remember { mutableStateOf("The output of you script will be printed here!") }
        val statusIcon = remember { mutableStateOf(StatusT.toIcon(StatusT.NOTHING)) }
        val currentProcess: MutableState<Process?> = remember { mutableStateOf(null) }

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
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Spacer(Modifier.height(10.dp))

                    // PlayButton
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                executeSource(output, script, statusIcon, currentProcess)
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
                        imageVector = statusIcon.value,
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
 * Aborts the [currentProcess] if it exists.
 */
fun terminateProcess(currentProcess: MutableState<Process?>) {
    currentProcess.value?.let {
        it.destroyForcibly()
        it.waitFor()
    }
}

/**
 * executes the [body] as a Kotlin script, updating the [output] Text Label
 * accordingly.
 */
private fun executeSource(
    output: MutableState<String>,
    body: String, statusIcon: MutableState<ImageVector>,
    currentProcess: MutableState<Process?>
) {

    output.value = ""

    // Save the content of body in the file tempScript.kts
    val permissions = PosixFilePermissions.fromString("rwxrwxrwx")
    val tempFile = createTempFile(
        prefix = "tempScript",
        suffix = ".kts",
        PosixFilePermissions.asFileAttribute(permissions)
    )
    tempFile.toFile().deleteOnExit()
    tempFile.writeText(text = body)

    statusIcon.value = StatusT.toIcon(StatusT.RUNNING)

    // Create a process for said file and prints its execution in the terminal
    val exitStatus = executeSource(source = tempFile.toString(), content = output, currentProcess)

    // TODO update the content to print that the process terminated and its exitStatus
    // Update the icon accordingly to the exit status
    statusIcon.value = StatusT.toIcon(StatusT.fromExitStatus(exitStatus))
    currentProcess.value = null
}


fun main() = runBlocking {
    application {

        Window(title = "QSRK!", onCloseRequest = ::exitApplication) {
            App()
        }
    }
}

