import os
import subprocess

def initializeDebianSubsytem(subdirectory):

	devnull = open(os.devnull, 'w')

	cmd = "mkdir " + subdirectory
	returned_value = subprocess.call(cmd, shell=True, stdout=devnull)

	cmd = "sudo debootstrap stable " + subdirectory + "/ http://deb.debian.org/debian/"
	returned_value = subprocess.call(cmd, shell=True, stdout=devnull)
	print("script done")

testdebootsrap()
