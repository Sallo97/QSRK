# QSRK!
**QSRK!** (**Q**uick **S**cript **R**unner for **K**otlin!) is a simple GUI application that lets users write, run, stop, and read in real-time the output of a Kotlin script.

## Features
- Write Kotlin script in the edit panel supporting basic syntax highlighting for common language constructs.
- Execute the script or forcefully terminate it through the play and stop buttons.
- View the real-time output produced by a running script in the build-in output panel. 
- In case Kotlin's errors or Java exceptions are generated during execution, they are printed by the output panel and highlighed, permitting user to see them. Additionally it's possible to click them to focus the editor cursor on the associated line.

## Supported Operating Systems
At the time of writing the application has only been tested on a MacBook Pro M2 with macOS Sequoia 15.4 (ARM64).

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

## Fun Facts
- I have chosen **QSRK!** as the name of the application because it seemed to me the world an alien would say. For this reason the play and stop buttons are a little weird.
- The play button is actually called **Plaju**, while the stop button is called **Stoompd**.
- Originally I had also designed **Monitrix**, which was the status icon and mascotte of the program. Sadly for time costraints I wasn't able to add it, but you can see the concept I have developed for it in the file `status_icon_concepts.png`.