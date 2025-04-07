package guiComponents.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Defines the stop button of the GUI. When pressed if a process in running, it aborts it, otherwise it does nothing.
 */
@Composable
fun stopButton(
    scope: CoroutineScope,
    currentProcess: MutableState<Process?>
) {
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
}

/**
 * Aborts the [currentProcess] if it exists, otherwise does nothing.
 */
private fun terminateProcess(currentProcess: MutableState<Process?>) = currentProcess.value?.destroyForcibly()
