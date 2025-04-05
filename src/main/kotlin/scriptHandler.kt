import androidx.compose.runtime.MutableState
import java.io.IOException

/**
 * This file handle the execution of the written script, creating the associated process, parsing the output and checking
 * the process's termination.
 */

/**
 * Describes the type of line, that is if it is an error, exception or a normal line.
 */
enum class LineType {
    ERROR, EXCEPTION, MISSING;

    companion object {
        private val ERROR_REGEX = Regex("(\\w*/*)*\\w+\\.kts:(\\d+:\\d+: error: .*([\\n\\r])*)")
        private val EXCEPTION_REGEX = Regex("(\\t+at )(\\w+.<init>\\(\\w+\\d+.kts)([:\\d]+)(\\)\\n*)")
        private val MISSING_REGEX = Regex("Cannot run program \"\\w+\": error=\\d+, No such file or directory")

        /**
         * checks the [line] type and updates the content accordingly
         */
        fun parseLine(line: String?): LineType? =
            line?.let {
                when {
                    ERROR_REGEX.matches(line) -> ERROR
                    EXCEPTION_REGEX.matches(line) -> EXCEPTION
                    MISSING_REGEX.matches(line) -> MISSING
                    else -> null
                }
            }

        /**
         * Checks if the current [line] must be modified, if so updated [content] with the new version.
         */
        fun updateContent(line: String, content: MutableState<String>, startLineIdx: Int) {
            println(line)
            val type = parseLine(line)
            println(type)
            when (type) {
                ERROR -> {
                    val newLine = line.replace(ERROR_REGEX, "script.kts:$2")
                    content.value = content.value.substring(startIndex = 0, endIndex = startLineIdx) + newLine
                }

                EXCEPTION -> {
                    println("SONOQUI")
                    val newLine = line.replace(EXCEPTION_REGEX, "$1script.kts$3$4")
                    content.value = content.value.substring(startIndex = 0, endIndex = startLineIdx) + newLine
                }

                else -> {
                    //TODO
                }
            }
        }
    }
}

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
                    LineType.updateContent(line.toString(), content, startLineIdx)

                    line.clear()
                    startLineIdx = content.value.length
                }
            }
        }

        // Check existStatus
        return script.waitFor()

    } catch (ioExc: IOException) {
        if (LineType.parseLine(ioExc.message) == LineType.MISSING)
            println("Your system misses the Kotlin compiler, please be sure that you installed kotlinc")
        else System.err.println("IOException: ${ioExc.message}\n script has been aborted.")
        return -1 //TODO maybe find a more fitting status than -1
    }
}