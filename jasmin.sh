
gradle build
java -jar jasminTest/jasmin.jar "$@".j -d ./jasminTest/libs
java -cp jasminTest/libs "$@"

rm -r ./jasminTest/libs/$@.class