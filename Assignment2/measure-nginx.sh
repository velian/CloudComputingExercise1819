#!/bin/bash

# Execute this on the client
wget $1/output.dat | sed -n "s/^.*in\s*\([0-9]*\).*$/\1/p"

