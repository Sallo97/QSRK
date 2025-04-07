# QSRK!
**QSRK!** (**Q**uick **S**cript **R**unner for **K**otlin!) is a simple GUI Desktop application developed using Compose Multiplatform that lets users write, run, stop, and read in real-time the output of a Kotlin script.

## Features
- Write Kotlin script in the edit panel supporting basic syntax highlighting for common language constructs.
- Execute the script or forcefully terminate it through the play and stop buttons.
- View the real-time output produced by a running script in the build-in output panel. 
- In case Kotlin's errors or Java exceptions are generated during execution, they are printed by the output panel and highlighed, permitting user to see them. Additionally it's possible to click them to focus the editor cursor on the associated line.

## Supported Operating Systems
At the time of writing the application has been tested on a MacBook Pro M2 with macOS Sequoia 15.4 (ARM64).

## Building and Execution
It's possible to compile and execute the application either within the IDE *IntelliJ IDEA* or manually using `gradlew`.

### Building and execution with IntelliJ IDEA

Clone the following repository and then open it with **IntelliJ IDEA**. To build the project either press `Cmd + F9` on macOS, or click on `Build` -> `Build Project`. After the compilation, **QSRK!** can be executed by selecting `MainKt` in the configuration panel on the top-left of the IDE and then by pressing the play button to its right.

### Building and executing manually with gradlew
It is possible to compile the process manually within a terminal using `gradlew`. Clone the following repository and open the terminal inside the generated folder, then execute:

```bash
./gradlew build // Building QSRK!
./gradlew run   // Running the application
```

## Project Structure
The project is divided in the `Main.kt` file, which handles the initialization of the GUI, and by the following packages:
- **scriptHandler** : `ScriptHandler` and `scriptStatus` implement respectively the logic for starting the script and synchronising the script's status icon. The executing of the script is done by creating an apposite process with Java's `ProcessBuilder` API. The Standard Output and Standard Error have been merged in the same pipe for handling the error parsing. The characters coming from the stream are read greedely one at a time, in order to let the user see its execution with less delay possible. To guarantee that the GUI remains reactive during the process's execution coroutines have been used.
- **parsing** : for handling the parsing of character the class `Segment` has been created. A `Segment` contains data regarding a range of contiguous characters in a text and how they should be stylised during their rendering. 
    - **syntaxParsing** : The Syntax Highlighter has been implemented by `SyntaxTransformation`. Since an user can modify dynamically the edited text, it needs to re-check the whole body each time an interaction occurs. 
    - **errorParsing:** : The parsing of errors and exception has been delegated to `errorParser` and `SyntaxTransformation`.  The first one defines methods to detect errors within lines and construct segments from them, the second handles the rendering in the output panel. Since the printed output of a process only happends text to it, instead of parsing at each update the whole text, a list contains the segments of the already parsed lines in the previous iterations. In this way the parsing speeds up significantly.
- **guiComponents** : determine the classes and functions that are used for constructing the graphical part of the application. `MyColor` is a object used for conveniently stored the colors used throught the GUI portion. Additionally:
    - **buttons :** defines the function for construction the play and stop buttons.
    - **fields :** defines the TextLabels composing the GUI. Notably the edit panel is actually generated as the combination of `editField` and `lineField`. The two generates two separated labels, one for writing the script and one for the numbering of the lines. The two are synchronized in a way such that when the user press either `\n` or `backspace`, `editField` gets triggered and checks if some lines have been removed or added.

## Fun Facts
- I have chosen **QSRK!** as the name of the application because it seemed to me the world an alien would say. For this reason the play and stop buttons are a little weird.
- The play button is actually called **Plaju**, while the stop button is called **Stoompd**.
- Originally I had also designed **Monitrix**, which was the status icon and mascotte of the program. Sadly for time costraints I wasn't able to add it, but you can see the concept I have developed for it in the file `status_icon_concepts.png`.