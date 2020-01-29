import random

with open("spots.csv", "w") as file:

	for x in range (2000):
	
		lat = random.uniform(13.084130, 13.753934)
	
		lon = random.uniform(52.370734, 52.681835)
	
		file.write(str(lat) + "," + str(lon) + "\n")



