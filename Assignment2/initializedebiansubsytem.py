import os
import subprocess

def initializedebiansubsytem(subdirectory ):

	devnull = open(os.devnull, 'w')
	cmd = "sudo debootstrap stable subsystem/ http://deb.debian.org/debian/"
	returned_value = subprocess.call(cmd, shell=True, stdout=devnull)
	print("script done")

testdebootsrap()
