#!/bin/bash
#cd /home/ec2-user/AmazonBenchmarkingPack/
$LOOP="0"

while [ $LOOP -lt 51 ]
do

echo "Running "
echo $LOOP
free && sync && echo 3 > /proc/sys/vm/drop_caches && free

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


FILE="disk-random-write.csv"
if [ ! -e $FILE ] ; then
	touch $FILE
	$( echo "time,value" >> $FILE)
fi

RESULT=$(./measure-disk-random.sh)
DATE=$(date +%s)
echo $DATE,$RESULT >> $FILE

FILE="fork.csv"
if [ ! -e $FILE ] ; then
	touch $FILE
	$( echo "time,value" >> $FILE)
fi

RESULT=$(./measure-fork.sh)
DATE=$(date +%s)
echo $DATE,$RESULT >> $FILE

$LOOP=$[$LOOP+1]

done