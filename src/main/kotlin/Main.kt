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
import androidx.compose.material.icons.filled.*
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
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText


data class ScriptStatus(val statusType: StatusType = StatusType.WAITING) {
    val icon = toIcon(statusType)

    /**
     * The possible type of status of a script:
     * SUCCESS -> The script has terminated with success (i.e. exitStatus = 0).
     * FAIL -> The script has terminated with failure (i.e. exitStatus < 0)
     * RUNNING -> The script has been started and has yet to finish
     * WAITING -> No script has been started (this status happens only at the start of the program)
     */
    enum class StatusType {
        SUCCESS, FAIL, RUNNING, WAITING, ABORTED;
    }

    companion object {
        /**
         * Given the [exitStatus] of a terminated script, returns the associated StatusT.
         */
        fun fromExitStatus(exitStatus: Int): ScriptStatus {
            val statusType =
                when (exitStatus){
                    0 -> StatusType.SUCCESS
                    137 -> StatusType.ABORTED
                    else -> StatusType.FAIL
                }
            return ScriptStatus(statusType)
        }

        /**
         * Given a [statusType] returns the associated icon (as of now it is just a text)
         */
        private fun toIcon(statusType: StatusType): ImageVector =
            when (statusType) {
                StatusType.SUCCESS -> Icons.Filled.Check
                StatusType.FAIL -> Icons.Filled.Warning
                StatusType.RUNNING -> Icons.Filled.Refresh
                StatusType.WAITING -> Icons.Filled.Star
                StatusType.ABORTED -> Icons.Filled.Info
            }
    }
}


@Composable
@Preview
fun App() {

    MaterialTheme {
        var script by remember { mutableStateOf("Write your script here!") }
        val output = remember { mutableStateOf("The output of you script will be printed here!") }
        val status = remember { mutableStateOf(ScriptStatus()) }
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
                        enabled = status.value.statusType != ScriptStatus.StatusType.RUNNING,
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                scriptExecution(output, script, status, currentProcess)
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
    )
    tempFile.toFile().deleteOnExit()
    tempFile.writeText(text = body)

    status.value = ScriptStatus(ScriptStatus.StatusType.RUNNING)

    // Create a process for said file and prints its execution in the terminal
    val exitStatus = executeSource(source = tempFile.toString(), content = output, currentProcess)

    // General updates after process termination
    output.value += "\nScript terminated with exit status: $exitStatus"
    status.value = ScriptStatus.fromExitStatus(exitStatus)
    currentProcess.value = null
}


fun main() =
    application {
        Window(title = "QSRK!", onCloseRequest = ::exitApplication) {
            App()
        }
    }

