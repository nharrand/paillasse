#!/bin/bash

TASK_NAME="mvn_test"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"


mvn test -Dsurefire.skipAfterFailureCount=1 > $LOG 2>&1
if [ $? -eq 0 ]; then
	NB_TESTS=$(grep "Tests run: " $LOG | cut -d ',' -f1 | cut -d ' ' -f3 | paste -sd+ | bc)
	NB_TESTS=$((NB_TESTS / 2))
	echo "NB_TESTS: $NB_TESTS"
	echo "{\"nb_test\":\"$NB_TESTS\"}" > $OUT
else
	exit -1
fi

