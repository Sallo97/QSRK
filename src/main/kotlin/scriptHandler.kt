import androidx.compose.runtime.MutableState
import java.io.IOException

/**
 * This file handle the execution of the written script, creating the associated process, parsing the output and checking
 * the process's termination.
 */

/**
 * Describes the type of line, that is if it is an error, exception or a normal line.
 */
enum class LineType { ERROR, EXCEPTION, MISSING;
    companion object {
        private val ERROR_REGEX = Regex("(\\w*/*)*\\w+\\.kts:\\d+:\\d+: error: .*([\\n\\r])*")
        private val EXCEPTION_REGEX = Regex("java\\.lang\\..+: .+([\\n\\r])*")
        private val MISSING_REGEX = Regex("Cannot run program \"\\w+\": error=\\d+, No such file or directory")

        /**
         * Returns the LineT type associated to [line]
         */
        fun parseLine(line : String?) : LineType? {
            return line?.let {
                when {
                    ERROR_REGEX.matches(line) -> ERROR
                    EXCEPTION_REGEX.matches(line) -> EXCEPTION
                    MISSING_REGEX.matches(line) -> MISSING
                    else -> null
                }
            }
        }
    }
}

/**
 * Handles the execution of the Kotlin's script pointed by [source].
 */
fun executeSource(source: String, content: MutableState<StringBuilder>) : Int {
    // Initialising process
    try {
        val script = ProcessBuilder("kotlinc", "-script", source)
            .redirectErrorStream(true)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start()

        // Reading the output stream
        script.inputReader().use { outReader ->
            val line = StringBuilder()
            var nextChar: Char
            while (outReader.read().apply { nextChar = this.toChar() } != -1) {
                line.append(nextChar)
                content.value.append(nextChar)

                // Checking line
                if (nextChar == '\n' || nextChar == '\r') {
                    // Parsing line
//                    LineType.parseLine(line.toString())?.let {
//                        TODO("Implement how to handle the modification of text")
//                    }
                    line.clear()
                }
            }
        }

        // Check existStatus
        return script.waitFor()

    } catch (ioExc: IOException) {
        if (LineType.parseLine(ioExc.message)  == LineType.MISSING)
            println("Your system misses the Kotlin compiler, please be sure that you installed kotlinc")
        else System.err.println("IOException: ${ioExc.message}\n script has been aborted.")
        return -1 //TODO maybe find a more fitting status than -1
    }
}