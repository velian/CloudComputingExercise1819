initialtime= date +%s
sleep 5s
secondtime= date +%s
time= $(($secondtime-$initialtime))
echo $time