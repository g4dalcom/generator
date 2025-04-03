@echo off
jpackage ^
  --name GeneratorApp ^
  --input build/libs ^
  --main-jar generator-0.0.1-SNAPSHOT.jar ^
  --main-class com.gdlatte.generator.FxApp ^
  --type exe ^
  --dest dist ^
  --win-shortcut ^
  --win-dir-chooser ^
  --win-menu ^
  --win-console ^
   --module-path "C:\javafx-sdk-21.0.6\lib" ^
  --java-options "--add-modules=javafx.controls,javafx.fxml"
pause
