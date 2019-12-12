LOOP="0"

while [ $LOOP -lt 51 ]
do

echo "Running "
echo $LOOP
free && sync && echo 3 > /proc/sys/vm/drop_caches && free

FILE="nginx.csv"
if [ ! -e $FILE ] ; then
	touch $FILE
	$( echo "time,value" >> $FILE)
fi

RESULT=$(./measure-nginx.sh & ./measure-nginx.sh)
DATE=$(date +%s)
echo $DATE,$RESULT >> $FILE

done