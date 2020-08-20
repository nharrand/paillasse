#!/bin/bash

TASK_NAME="owasp_dependency_check"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"


mvn org.owasp:dependency-check-maven:check > $LOG 2>&1

#TODO parse target/dependency-check-report.html
if [ $? -eq 0 ]; then
	echo "{\"success\":\"true\"}" > $OUT
else
	echo "{\"success\":\"false\"}" > $OUT
fi


