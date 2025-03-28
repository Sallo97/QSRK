/**
 * This file handle the execution of the written script, creating the associated process, parsing the output and checking
 * the process's termination.
 */

/**
 * Handles the execution of the Kotlin's script pointed by [source].
 */
fun executeScript(source: String) {
    // Initialising process
    val processScript = ProcessBuilder("kotlinc", "-script", source)
        .redirectErrorStream(true)
        .redirectInput(ProcessBuilder.Redirect.INHERIT)
        .start()

    // Reading the output stream
    val outputReader = processScript.inputReader()
    val line = StringBuilder()

    do {
        val nextVal = outputReader.read()
        val nextChar = nextVal.toChar()

        print(nextChar) // TODO line is here only for debug purposes, remember to remove it.
        line.append(nextChar)

        // Checking line
        if (nextChar == '\n' || nextChar == '\r') {
            parseLine(line.toString())
            line.clear()
        }

    } while (nextVal != -1)
}

/**
 * Checks if [line] is an error or an exception.
 */
fun parseLine(line: String) {
    val errorRegex = Regex("(\\w*/*)*\\w+\\.kts:\\d+:\\d+: error: .*([\\n\\r])*")
    val exceptionRegex = Regex("java\\.lang\\..+: .+([\\n\\r])*")

    if (errorRegex.matches(line)) {
        println("FOUND AN ERROR!")
    } else if (exceptionRegex.matches(line)) {
        println("FOUND AN EXCEPTION!")
    }

}

fun main() {
    val scriptPath = "test.kts"
    executeScript(scriptPath)
}