package guiComponents.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import scriptHandler.ScriptStatus
import scriptHandler.manageExecution
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.writeText

/**
 * Defines the play button which is used for start executing the written script.
 */
@Composable
fun playButton(
    status: MutableState<ScriptStatus>,
    scope: CoroutineScope,
    output: MutableState<String>,
    currentProcess: MutableState<Process?>,
    script: TextFieldValue) {
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
}

/**
 * Prepare the execution for [body] as a Kotlin script, resetting [output] accordingly, updating [status] and setting
 * the process as the [currentProcess].
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
    val tempFile = kotlin.io.path.createTempFile(
        prefix = "tempScript",
        suffix = ".kts",
        PosixFilePermissions.asFileAttribute(permissions)
    ).also {
        it.toFile().deleteOnExit()
        it.writeText(text = body)
    }

    // Set the status as `RUNNING`
    status.value = ScriptStatus(ScriptStatus.StatusType.RUNNING)

    // Create a process for said file and prints its execution in the terminal
    val exitStatus = manageExecution(
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