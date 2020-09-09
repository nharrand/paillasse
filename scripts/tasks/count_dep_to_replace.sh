#!/bin/bash

TASK_NAME="count_dep_to_replace"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"

PARAM=$(cat $IN)

g=$(cat $IN | jq -r .g)
a=$(cat $IN | jq -r .a)

if [[ -f $LOG ]]
then
	rm $LOG
fi

mvn dependency:tree > $LOG 2>&1
if [ $? -ne 0 ]; then
	exit -1
fi

NB_DEP_TO_REPLACED=$(grep "$g:$a" $LOG | wc -l)

echo "{\"nb_dependencies_to_replaced\":\"$NB_DEP_TO_REPLACED\"}" > $OUT
