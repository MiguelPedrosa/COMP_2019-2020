gradle build && java -jar jasminTest/jasmin.jar ./jFiles/"$@".j -d ./jasminTest/libs && java -cp ./jasminTest/libs "$@" && rm ./jasminTest/libs/"$@".class