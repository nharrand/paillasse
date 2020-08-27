#!/bin/bash

JARS_PATH="/app/lib"

TASK_NAME="transform_pom"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"

PARAM=$(cat $IN)

g=$(cat $IN | jq -r .g)
a=$(cat $IN | jq -r .a)
v=$(cat $IN | jq -r .v)
rg=$(cat $IN | jq -r .rg)
ra=$(cat $IN | jq -r .ra)
rv=$(cat $IN | jq -r .rv)

java -jar $JARS_PATH/depswap-test-harness-0.1-SNAPSHOT-jar-with-dependencies.jar ./ "$g:$a:$v" "$rg:$ra:$rv" $JARS_PATH
if [ $? -eq 0 ]; then
	echo "{\"sucess\":true}" > $OUT
else
	echo "{\"sucess\":false}" > $OUT
fi
