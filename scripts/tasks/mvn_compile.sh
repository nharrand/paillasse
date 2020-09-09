#!/bin/bash

TASK_NAME="mvn_compile"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"

PARAM=$(cat $IN)

echo "task: $TASK_NAME with $PARAM"

mvn compile > $LOG 2>&1

if [ $? -eq 0 ]; then
	echo "{\"sucess\":true}" > $OUT
else
	echo "{\"sucess\":false}" > $OUT
fi
