#!/bin/bash

echo "Building project..."
mvn clean package

echo "Creating DEB with JavaFX..."

jpackage \
--name DuplicateFileDetector \
--input target/ \
--main-jar PR_1_FILE_DUPE-1.0-SNAPSHOT.jar \
--main-class com.example.pr_1_file_dupe.HelloApplication \
--type deb \
--icon src/main/resources/com/example/pr_1_file_dupe/img/logo.png \
--dest . \
--module-path /home/tahaur/Downloads/javafx-sdk-26/lib \
--add-modules javafx.controls,javafx.fxml

echo "Done!"
ls *.deb
