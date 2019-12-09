#!/bin/bash

# Execute this on the client
result_string=$(wget $1/output.dat 2>&1)

result=$(echo $result_string | sed -n "s/^.*=\(\S*\)s.*$/\1/p")

rm output.dat

echo $result
