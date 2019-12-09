#!/bin/bash
EXECUTABLE="memsweep"
if [ ! -e $EXECUTABLE ] ; then
	cc -O -o memsweep memsweep.c -lm
fi

STARTTIME=$(date +%s)
LASTTIME=$(date +%s)
RESULTARRAY=()
ARRAYSIZE=$((0))

while [ $(($LASTTIME - $STARTTIME)) -lt 10 ] 
do
RESULT=$(./memsweep)
RESULT=${RESULT: -6}
LASTTIME=$(date +%s)
RESULTARRAY+=($RESULT)
((ARRAYSIZE++))
done

SORTEDARRAY=($(for l in ${RESULTARRAY[@]}; do echo $l; done | sort))

MIDPOINT=$(( ( ( ARRAYSIZE + 1 ) / 2 ) -1 ))

echo ${SORTEDARRAY[$MIDPOINT]}