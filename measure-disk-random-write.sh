#!/bin/bash
FIOOUTFILE="fiowrite.txt"
if [ ! -e $FIOOUTFILE ] ; then
	touch "fiowrite.txt"
fi

FIORESULT=$(fio --name=randomwrite --ioeng=posixaio --runtime=15 --time_based --readwrite=randwrite --size=500M --iodepth=4 --direct=1 > fiowrite.txt)
IOPLINE=$(grep iops fiowrite.txt)
RESULT=$( echo $IOPLINE | sed -e 's/.*iops=\(.*\)runt.*/\1/' | sed 's/[ \t]*$//' | sed s/,*\r*$//)

rm randomwrite*

echo $RESULT