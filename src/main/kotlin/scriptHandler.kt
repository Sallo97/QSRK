import androidx.compose.runtime.MutableState
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
    currentProcess: MutableState<Process?>
): Int {

    // Initialising process
    try {
        val script = ProcessBuilder("kotlinc", "-script", source)
            .redirectErrorStream(true)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start()

        currentProcess.value = script

        // Reading the output stream
        var startLineIdx = 0
        script.inputReader().use { outReader ->
            val line = StringBuilder()
            var nextChar: Char
            while (outReader.read().apply { nextChar = this.toChar() } != -1) {
                line.append(nextChar)
                content.value += nextChar

                // Checking line
                if (nextChar == '\n' || nextChar == '\r') {
                    // Parsing line
                    ErrorParser.parseLine(line.toString(), content, startLineIdx)

                    line.clear()
                    startLineIdx = content.value.length
                }
            }
        }

        // Check existStatus
        return script.waitFor()

    } catch (ioExc: IOException) {
        if (ErrorParser.isMissingCompiler(ioExc.message))
            content.value += "\nYour system misses the Kotlin compiler, please be sure that you installed kotlinc"
        else
            content.value += "\nThe script was abruptly terminated"
        return 130
    }
}