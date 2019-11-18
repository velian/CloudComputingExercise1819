cd /home/ec2-user/AmazonBenchmarkingPack/

FILE="cpu.csv"
if [ ! -e $FILE ] ; then
	touch $FILE
	$( echo "time,value" >> $FILE)
fi

RESULT=$(./measure-cpu.sh)
DATE=$(date +%s)
echo $DATE,$RESULT >> $FILE

FILE="mem.csv"
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

FILE="disk-sequential-write.csv"
if [ ! -e $FILE ] ; then
	touch $FILE
	$( echo "time,value" >> $FILE)
fi

RESULT=$(./measure-disk-sequential-write.sh)
DATE=$(date +%s)
echo $DATE,$RESULT >> $FILE

FILE="disk-sequential-read.csv"
if [ ! -e $FILE ] ; then
	touch $FILE
	$( echo "time,value" >> $FILE)
fi

RESULT=$(./measure-disk-sequential-read.sh)
DATE=$(date +%s)
echo $DATE,$RESULT >> $FILE

