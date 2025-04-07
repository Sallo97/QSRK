package scriptHandler

import androidx.compose.runtime.MutableState
import parsing.errorParsing.isMissingCompiler
import java.io.IOException

/**
 * This file handle the execution of the written script, creating the associated process, parsing the output and checking
 * the process's termination.
 */


/**
 * Handles the execution of the Kotlin's script pointed by [source].
 */
fun executeScript(
    source: String, content: MutableState<String>,
    currentProcess: MutableState<Process?>,
): Int {

    // Initialising process
    try {
        val script = ProcessBuilder("kotlinc", "-script", source)
            .redirectErrorStream(true)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start()

        currentProcess.value = script

        // Reading the output stream
        script.inputReader().use { outReader ->
            val line = StringBuilder()
            var nextChar: Char
            while (outReader.read().apply { nextChar = this.toChar() } != -1) {
                line.append(nextChar)
                content.value += nextChar

                // Checking line
                if (nextChar == '\n' || nextChar == '\r') {
                    line.clear()
                }
            }
        }

        // Check existStatus
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