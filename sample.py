#!/usr/bin/python

# Import the required modules
import cv2, os
import numpy as np
import sys
from PIL import Image

def main():
	try:
		pathToImage = str(sys.argv[1])
	except IndexError:
		print "Incorret syntax. Expecting python sample.py PATHTOIMAGE"
		return

	# For face detection we will use the Haar Cascade provided by OpenCV.
	cascadePath = "haarcascade_frontalface_default.xml"
	faceCascade = cv2.CascadeClassifier(cascadePath)

	#Open image for prediction
	predict_image_pil = Image.open(pathToImage).convert('L')
	predict_image = np.array(predict_image_pil, 'uint8')
	faces = faceCascade.detectMultiScale(predict_image, 1.3, 5)

	for (x,y,w,h) in faces:
		cv2.rectangle(predict_image,(x,y),(x+w,y+h),(255,0,0),2)
		roi_color = predict_image[y:y+h, x:x+w]

	cv2.imshow('Sample Image',predict_image)
	cv2.waitKey(0)
	cv2.destroyAllWindows()

if __name__ == '__main__':
	main()