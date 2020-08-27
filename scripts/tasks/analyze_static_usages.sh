#!/bin/bash

JARS_PATH="/app/lib"

TASK_NAME="analyze_static_usages"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"

PARAM=$(cat $IN)

packages=$(cat $IN | jq -r .packages)


CUR_DIR=$(pwd)
#Static analysis
echo "java -cp $JARS_PATH/depswap-test-harness-0.1-SNAPSHOT-jar-with-dependencies.jar se.kth.assertteam.depanalyzer.Analyzer $CUR_DIR $packages"
STATIC_USAGES=$(java -cp $JARS_PATH/depswap-test-harness-0.1-SNAPSHOT-jar-with-dependencies.jar se.kth.assertteam.depanalyzer.Analyzer $CUR_DIR $packages)
if [ $? -eq 0 ]; then
	echo $STATIC_USAGES > $OUT
else
	exit -1
fi

