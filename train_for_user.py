#!/usr/bin/python

# Import the required modules
import cv2, os
import numpy as np
import sys
from PIL import Image

def main():
	try:
		pathToImages = str(sys.argv[1])
		pathToTrainedModel = str(sys.argv[2])
	except IndexError:
		print "Incorret syntax. Expecting python train_for_user.py PATHTOIMAGES PATHTOSAVE"
		return

	# For face detection we will use the Haar Cascade provided by OpenCV.
	cascadePath = "haarcascade_frontalface_default.xml"
	faceCascade = cv2.CascadeClassifier(cascadePath)

	# For face recognition we will the the LBPH Face Recognizer 
	recognizer = cv2.createLBPHFaceRecognizer()

	def get_images_and_labels(path):
		# Append all the absolute image paths in a list image_paths
		image_paths = [os.path.join(path, f) for f in os.listdir(path)]

		# images will contains face images
		images = []

		# labels will contains the label that is assigned to the image
		labels = []

		imageCount = 0
		for image_path in image_paths:
			imageCount = imageCount + 1
			# Read the image and convert to grayscale
			image_pil = Image.open(image_path).convert('L')

			# Convert the image format into numpy array
			image = np.array(image_pil, 'uint8')

			# Get the label of the image
			#nbr = str(os.path.split(image_path)[1].split(".")[0]) #Label is the name of the
			nbr = 1 #TODO: Need actual labels

			# Detect the face in the image
			faces = faceCascade.detectMultiScale(image, 1.3, 5)

			faceCount = 0
			# If face is detected, append the face to images and the label to labels
			for (x, y, w, h) in faces:
				faceCount = faceCount + 1
				images.append(image[y: y + h, x: x + w])
				labels.append(nbr)
				cv2.imshow("Adding faces to traning set...", image[y: y + h, x: x + w])
				cv2.waitKey(1000)

			print "Found", faceCount, "faces in image", imageCount, "for user: ", nbr

		# return the images list and labels list
		return images, labels

	# Path to the Yale Dataset
	path = pathToImages

	#Get the labels and images
	images, labels = get_images_and_labels(path)
	cv2.destroyAllWindows()

	print "Training..."

	# Perform the tranining
	recognizer.train(images, np.array(labels))

	print "Saving..."

	#Save model for user
	recognizer.save(pathToTrainedModel)

	print "Model Saved"

if __name__ == '__main__':
	main()

