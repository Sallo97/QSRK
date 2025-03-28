import java.io.IOException

/**
 * This file handle the execution of the written script, creating the associated process, parsing the output and checking
 * the process's termination.
 */

/**
 * Describes the type of line, that is if it is an error, exception or a normal line.
 */
enum class LineType { ERROR, EXCEPTION }

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
        processScript.inputReader().use {
            val line = StringBuilder()
            var nextChar: Char
            while (it.read().apply { nextChar = this.toChar() } != -1) {
                print(nextChar) // TODO line is here only for debug purposes, remember to remove it.
                line.append(nextChar)

                // Checking line
                if (nextChar == '\n' || nextChar == '\r') {
                    // Parsing line
                    val lineType = parseLine(line.toString())
                    if (lineType == LineType.ERROR) {
                        // TODO Update the terminalLabel to handle the case of error
                    } else if (lineType == LineType.EXCEPTION) {
                        // TODO handle the terminalLabel to handle the case of exception
                    }
                    line.clear()
                }
            }
        }

        // Check existStatus
        println("\n---EXITING...---")
        println("process ${processScript.pid()} with exit status ${processScript.exitValue()}")
    } catch (ioExc: IOException) {
        val missingCompilerRegex = Regex("Cannot run program \"\\w+\": error=\\d+, No such file or directory")
        if (ioExc.message != null && missingCompilerRegex.matches(ioExc.message!!))
            println("Your system misses the Kotlin compiler, please be sure that you installed kotlinc")
        else throw ioExc
    }
}

/**
 * Checks if [line] is an error or an exception.
 */
fun parseLine(line: String): LineType? {
    val errorRegex = Regex("(\\w*/*)*\\w+\\.kts:\\d+:\\d+: error: .*([\\n\\r])*")
    val exceptionRegex = Regex("java\\.lang\\..+: .+([\\n\\r])*")

    if (errorRegex.matches(line)) {
        return LineType.ERROR
    } else if (exceptionRegex.matches(line)) {
        return LineType.EXCEPTION
    }
    return null
}

fun main() {
    val scriptPath = "test.kts"
    executeScript(scriptPath)
}