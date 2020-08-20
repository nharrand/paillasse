#!/bin/bash

TASK_NAME="clone_repo"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"

PARAM=$(cat $IN)

echo "task: $TASK_NAME with $PARAM"

commit=$(cat $IN | jq -r .commit)

REPO=$(cat $IN | jq -r .repo)
URL=$(cat $IN | jq -r .url)

#Get code
if [[ ! -d $REPO ]]
then
	GIT_TERMINAL_PROMPT=0 git clone $URL
	if [ $? -ne 0 ]; then
		exit -1
	fi
fi


cd $REPO
if [ $? -ne 0 ]; then
	exit -1
fi

if [ "$commit" == "*" ]; then
	commit=$(git log --format="%H" -n 1)
fi

git checkout $commit
if [ $? -ne 0 ]; then
	exit -1
fi


if [[ -f $OUT ]]
then
	rm $OUT
fi

echo "{\"sucess\":true,\"commit\":\"$commit\"}" > ../$OUT

#Write results in $OUT
