#!/bin/bash

TASK_NAME="print_parameters"
IN="$TASK_NAME.in"
OUT="$TASK_NAME.out"
LOG="$TASK_NAME.log"

echo "task: $TASK_NAME with $(cat $IN)"
mv $IN $OUT
