package scriptHandler

import androidx.compose.runtime.MutableState
import parsing.errorParsing.isMissingCompiler
import java.io.IOException


/**
 * Sets [currentProcess] as a new process pointed by [source].
 * Manages the whole execution of the process, handling the reading of the characters coming from both the Standard
 * Output and Standard Error.
 * Returns the exitStatus by which the process terminates.
 */
fun manageExecution(
    source: String, content: MutableState<String>,
    currentProcess: MutableState<Process?>,
): Int {

    // Creating the process
    try {
        val script = ProcessBuilder("kotlinc", "-script", source)
            .redirectErrorStream(true)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start()

        currentProcess.value = script

        // Reading the output stream
        script.inputReader().use { outReader ->
            var nextChar: Char
            while (outReader.read().apply { nextChar = this.toChar() } != -1) {
                content.value += nextChar
            }
        }

        // Wait for process completion
        return script.waitFor()

    } catch (ioExc: IOException) {
        val message = if (isMissingCompiler(ioExc.message))
            "Your system misses the Kotlin compiler, please be sure that you installed kotlinc"
        else
            "The script was abruptly terminated"
        content.value += message
        return 130
    }
}