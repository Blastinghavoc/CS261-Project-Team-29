#!/bin/sh

javac -cp "./src/datagathering/jsoup.jar"  -d ./classes ./src/*.java ./src/nlp/*.java ./src/database/*.java ./src/datagathering/*.java ./src/gui/*.java ./src/ai/*.java
java -cp "./classes":"./src/database/sqlite-jdbc-3.21.0.jar":"./src/datagathering/jsoup.jar" footsiebot.Core tradinghour
