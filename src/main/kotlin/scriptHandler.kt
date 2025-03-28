/**
 * This file handle the execution of the written script, creating the associated process, parsing the output and checking
 * the process's termination.
 */

import java.io.IOException

/**
 * Handles the execution of the Kotlin's script pointed by [source].
 */
fun executeScript(source: String){
    // Initialising process
    val processScript = ProcessBuilder("kotlinc", "-script", source)
        .redirectErrorStream(true)
        .redirectInput(ProcessBuilder.Redirect.INHERIT)
        .start()

    // Reading the output stream
    val outputReader = processScript.inputReader()

    do {
        val nextVal = outputReader.read()
        print(nextVal)
    }while (nextVal != -1)
}

fun main(){
    val scriptPath = "test.kts"
    executeScript(scriptPath)
}