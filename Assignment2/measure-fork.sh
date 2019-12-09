EXECUTABLE="forktest"
if [ ! -e $EXECUTABLE ] ; then
	cc -O -o forktest fork.c -lm
fi

STARTTIME=$(date +%s%N)
RESULT=$(./forktest 100 1710)
if [ RESULT > 0 ] ; then
	ENDTIME=$(date +%s%N)
	RESULTTIME=$(($ENDTIME - $STARTTIME))
	echo ${RESULTTIME}
fi