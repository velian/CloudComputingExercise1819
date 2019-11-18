FILE="cpu.csv"
if [ ! -e $FILE ] ; then
	touch $FILE
	$( echo "time,value" >> $FILE)
fi

RESULT=$(./measure-mem.sh)
DATE=$(date +%s)
echo $DATE,$RESULT >> $FILE

FILE="disk-random-read.csv"
if [ ! -e $FILE ] ; then
	touch $FILE
	$( echo "time,value" >> $FILE)
fi

RESULT=$(./measure-disk-random-read.sh)
DATE=$(date +%s)
echo $DATE,$RESULT >> $FILE

FILE="disk-random-write.csv"
if [ ! -e $FILE ] ; then
	touch $FILE
	$( echo "time,value" >> $FILE)
fi

RESULT=$(./measure-disk-random-write.sh)
DATE=$(date +%s)
echo $DATE,$RESULT >> $FILE

