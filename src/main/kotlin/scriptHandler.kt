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
fun executeScript(source: String) {
    // Initialising process
    try {
        val processScript = ProcessBuilder("kotlinc", "-script", source)
            .redirectErrorStream(true)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start()

        // Reading the output stream
        processScript.inputReader().use {outReader ->
            val line = StringBuilder()
            var nextChar: Char
            while (outReader.read().apply { nextChar = this.toChar() } != -1) {
                print(nextChar) // TODO line is here only for debug purposes, remember to remove it.
                line.append(nextChar)

                // Checking line
                if (nextChar == '\n' || nextChar == '\r') {
                    // Parsing line
                    LineType.parseLine(line.toString())?.let {
                        if (it == LineType.ERROR) {
                            // TODO Update the terminalLabel to handle the case of error
                        } else if (it == LineType.EXCEPTION) {
                            // TODO handle the terminalLabel to handle the case of exception
                        }
                    }
                    line.clear()
                }
            }
        }

        // Check existStatus
        println("\n---EXITING...---")
        println("process ${processScript.pid()} with exit status ${processScript.exitValue()}")
    } catch (ioExc: IOException) {
        if (LineType.parseLine(ioExc.message)  == LineType.MISSING)
            println("Your system misses the Kotlin compiler, please be sure that you installed kotlinc")
        else System.err.println("IOException: ${ioExc.message}\n script has been aborted.")
    }
}


fun main() {
    val scriptPath = "test.kts"
    executeScript(scriptPath)
}