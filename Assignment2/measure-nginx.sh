#!/bin/bash

# Execute this on the client
result_string=$(wget $1/output.dat)
result=$(echo result_string | sed -n "s/^.*in\s*\([0-9]*\).*$/\1/p")

rm output.dat

echo result
