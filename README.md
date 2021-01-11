Executables are PianoRollGUI.java and CustomFunctions.java. You may run either one.

To ensure compatibility with later versions of JDK/JRE, configure build path by adding the .jar files from the directory "./JavaFX_OpenJFX_11.0.2/lib/" to the build path (e.g. in Eclipse, right-click on project name -> Build Path -> Configure Build Path... -> click on Libraries tab -> click on "Add External JARS..." button -> add the aforementioned .jar files and apply).

In the event you experience the error "Runtime Components are Missing" upon execution, follow the directions at https://edencoding.com/runtime-components-error/ (e.g. in Eclipse, go to Run -> Run Configurations... -> select Java Application > CustomFunctions from the left navbar -> click on Arguments tab -> under VM arguments, enter "--module-path JavaFX_OpenJFX_11.0.2/lib --add-modules javafx.controls,javafx.fxml" (without the quotes). Repeat the same steps, this time selecting Java Application > PianoRollGUI instead.
