#!/bin/bash

TASK_NAME="mvn_test_again"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"


mvn test -Dsurefire.skipAfterFailureCount=1 > $LOG 2>&1
if [ $? -eq 0 ]; then
	echo "{\"success\":\"true\"}" > $OUT
else
	echo "{\"success\":\"false\"}" > $OUT
fi
