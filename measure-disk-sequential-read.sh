start_time=$(date +%s%N)
dd if=/tmp/test.bin of=/dev/null bs=1G count=1 #&> /dev/null
elapsed_time=$(($(date +%s%N)-$start_time))
bc <<< "scale=3; 1000/($elapsed_time/1000000000)"
