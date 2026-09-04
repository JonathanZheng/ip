# SevenSix

This project is named SevenSix. Given below are instructions on how to use it.

## Running the JavaFX GUI

Use JDK 25, then run the following command from the project root:

```bash
./gradlew run
```

The GUI accepts the same commands as the console version. Press Enter or click `Send` to submit a
command. Tasks are saved to `data/duke.txt` by default.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/duke/SevenSix.java` file, right-click it, and choose `Run SevenSix.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see the following output:
   ```
   Welcome to SevenSix!
   ```

## Building an executable JAR

Run the following command from the project root:

```bash
./gradlew clean shadowJar
```

This creates the executable `build/libs/duke.jar`. Copy that file into an empty folder and run
the chatbot with:

```bash
java -jar duke.jar
```

The JAR is a generated build artifact and should not be committed to Git.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
