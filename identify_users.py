#!/usr/bin/python

# Import the required modules
import cv2, os
import sys
import numpy as np
from PIL import Image

def main():
    try:
        pathToTrainedModels = str(sys.argv[1])
        pathToTestImage = str(sys.argv[2])
    except IndexError:
        print "Incorret syntax. Expecting python identify_users.py pathToTrainedModels pathToTestImage"
        return

    # For face detection we will use the Haar Cascade provided by OpenCV.
    cascadePath = "haarcascade_frontalface_default.xml"
    faceCascade = cv2.CascadeClassifier(cascadePath)

    print "Loading model"

    # For face recognition we will the the LBPH Face Recognizer
    recognizer = cv2.createLBPHFaceRecognizer() 
    recognizer.load(pathToTrainedModels)

    print "Opening image for prediction"

    #Open image for prediction
    predict_image_pil = Image.open(pathToTestImage).convert('L')
    predict_image = np.array(predict_image_pil, 'uint8')
    faces = faceCascade.detectMultiScale(predict_image, 1.3, 5)

    print "Making predictions"

    #For each possible face in that image
    for (x, y, w, h) in faces:
        nbr_predicted, conf = recognizer.predict(predict_image[y: y + h, x: x + w])
        #nbr_actual = int(os.path.split(image_path)[1].split(".")[0].replace("subject", "")) #Not testing at this time.
        print "We believe this face is", nbr_predicted, "with", conf, "confidence"

        # if nbr_actual == nbr_predicted:
        #     print "{} is Correctly Recognized with confidence {}".format(nbr_actual, conf)
        # else:
        #     print "{} is Incorrect Recognized as {}".format(nbr_actual, nbr_predicted)
        cv2.imshow("Recognizing Face", predict_image[y: y + h, x: x + w])
        cv2.waitKey(1000)

if __name__ == '__main__':
    main()

