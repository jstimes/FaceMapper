#!/usr/bin/python

# Import the required modules
import cv2, os
import numpy as np
import sys
import identify_users
from PIL import Image

#TODO possibly split this script into two functions,
# one that takes a directory and another just a bunch of possibly random filepaths?
def main():
	try:
		pathToImages = str(sys.argv[1])
		pathToTrainedModel = os.path.join('src', 'python', 'TrainedModel')
		#pathToTrainedModel = "src/python/TrainedModel"
		user = str(sys.argv[2])
	except IndexError:
		print "Incorret syntax. Expecting python train_for_user.py PATHTOIMAGES USER"
		return

	# For face detection we will use the Haar Cascade provided by OpenCV.
	cascadePath = os.path.join('src', 'python', 'haarcascade_frontalface_default.xml')
	faceCascade = cv2.CascadeClassifier(cascadePath)

	# For face recognition we will the the LBPH Face Recognizer 
	recognizer = cv2.createLBPHFaceRecognizer()
	
	# Load a previously created model:
	recognizer.load(pathToTrainedModel)

	def get_images_and_labels(path, userLabel):
		# Append all the absolute image paths in a list image_paths
		image_paths = [os.path.join(path, f) for f in os.listdir(path)]

		# images will contains face images
		images = []

		# labels will contains the label that is assigned to the image
		labels = []
		
		# save last image to test training with
		last = None 

		imageCount = 0
		totalFaces = 0
		for image_path in image_paths:
			imageCount = imageCount + 1
			
			if imageCount == len(image_paths):
				last = image_path
				break
			
			# Read the image and convert to grayscale
			image_pil = Image.open(image_path).convert('L')

			# Convert the image format into numpy array
			image = np.array(image_pil, 'uint8')

			nbr = int(userLabel)

			# Detect the face in the image
			faces = faceCascade.detectMultiScale(image, 1.3, 5)
			if len(faces) == 0:
				print "no faces detected in",image_path

			faceCount = 0
			# If face is detected, append the face to images and the label to labels
			for (x, y, w, h) in faces:
				faceCount = faceCount + 1
				totalFaces = totalFaces + 1
				images.append(image[y: y + h, x: x + w])
				labels.append(nbr)
				cv2.waitKey(1000)

			#print "Found", faceCount, "faces in image", imageCount, "for user: ", nbr

		# return the images list and labels list and last image for testing
		return images, labels, last

	# Path to the dataset
	path = pathToImages

	#Get the labels and images
	images, labels, last = get_images_and_labels(path, user)
	if len(images) < 2:
		print "Error - need at least 2 images"
		return
	cv2.destroyAllWindows()

	# Perform the training and update previous model
	recognizer.update(images, np.array(labels))

	# save model to be used & updated again
	recognizer.save(pathToTrainedModel)
	
	# Test if model had enough data to recognize last picture in directory
	test = identify_users.identify(last,False,"")
	if len(test) > 0 and int(test[0]) == int(user):
		print "success"
	else:
		print "failure"

if __name__ == '__main__':
	main()

