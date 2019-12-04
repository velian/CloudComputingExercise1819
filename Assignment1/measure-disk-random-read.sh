#!/bin/bash
FIOOUTFILE="fioread.txt"
if [ ! -e $FIOOUTFILE ] ; then
	touch "fioread.txt"
fi

FIORESULT=$(fio --name=randomread --ioeng=posixaio --runtime=15 --time_based --readwrite=randread --size=500M --iodepth=4 --direct=1 > fioread.txt)
IOPLINE=$(grep iops fioread.txt)
RESULT=$( echo $IOPLINE | sed -e 's/.*iops=\(.*\)runt.*/\1/' | sed 's/[ \t]*$//' | sed s/,*\r*$//)
rm randomread*
echo $RESULT