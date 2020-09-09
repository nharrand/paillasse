#!/bin/bash

TASK_NAME="find_pom"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"

PARAM=$(cat $IN)

echo "task: $TASK_NAME with $PARAM"

REPO=$(cat $IN | jq -r .repo)

poms=$(find $REPO -name "pom.xml" | grep .)
if [ $? -ne 0 ]; then
	exit -1
fi
pom=$(echo $poms | head -n 1)
n_repo=$(echo $pom | sed 's/pom.xml//')

echo "{\"sucess\":true,\"repo\":\"$n_repo\",\"o_repo\":\"$REPO\",\"pom\":\"$pom\"}" > $OUT
