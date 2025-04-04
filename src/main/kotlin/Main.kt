import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.*

@Composable
@Preview
fun App() {

    MaterialTheme {
        var script by remember { mutableStateOf("Write your script here!") }
        var output = remember { mutableStateOf("The output of you script will be printed here!") }

        BoxWithConstraints {
            val windowHeight = maxHeight
            val windowWidth = maxWidth
            val scope = rememberCoroutineScope()

            Row {
                Spacer(Modifier.width(10.dp))
                Column {
                    Spacer(Modifier.height(10.dp))

                    val scriptBox = BasicTextField(
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

                    val outputBox = BasicTextField(
                        readOnly = true,
                        value = output.value,
                        onValueChange = {output.value = it},
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

                    val play = Button(
                        onClick = {
                            scope.launch (Dispatchers.IO){
                                executeScript(output, script)
                            }
                        },
                        content = {
                            Text("PLAY SCRIPT")
                        },
                        modifier = Modifier
                            .size(width = 100.dp, height = 100.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    val stop = Button(
                        onClick = { },
                        content = {
                            Text("STOP SCRIPT")
                        },
                        modifier = Modifier
                            .size(width = 100.dp, height = 100.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    val status = Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Status Icon",
                        modifier = Modifier
                            .size(width = 100.dp, height = 100.dp)
                    )
                }
            }
        }

    }
}

fun executeScript(output: MutableState<String>, body: String) {
    output.value = "Starting executing script..."

    // Save the content of body in the file tempScript.kts
    val permissions = PosixFilePermissions.fromString("rwxrwxrwx")
    val tempFile = createTempFile(
        prefix = "tempScript",
        suffix = ".kts",
        PosixFilePermissions.asFileAttribute(permissions)
    )
    tempFile.toFile().deleteOnExit()
    tempFile.writeText(text = body)

    // create a process for said file and prints its execution in the terminal
    executeSource(source = tempFile.toString(), content = output)


}


fun main() = runBlocking {
    application {

        Window(title = "QSRK!", onCloseRequest = ::exitApplication) {
            App()
        }
    }
}

