#!/usr/bin/python

# Import the required modules
import cv2, os
import sys
import json
import numpy as np
from PIL import Image

def identify(pathToTestImage,isMainScript,newImagePath):
    
    #TODO move this try block to the main method part
    try:
        #pathToTrainedModels = str(sys.argv[1])
        pathToTrainedModel = os.path.join('src', 'python', 'TrainedModel')
        #pathToTrainedModel = "/home/tweets/FaceMapper/src/python/TrainedModel"
        #pathToTrainedModel = "src/python/TrainedModel"
        #pathToTestImage = str(sys.argv[1])
    except IndexError:
        print "Incorret syntax. Expecting python identify_users.py pathToModel pathToSaveImage"
        return

    # For face detection we will use the Haar Cascade provided by OpenCV.
    cascadePath = os.path.join('src', 'python', 'haarcascade_frontalface_default.xml')
    #cascadePath = "src/python/haarcascade_frontalface_default.xml"
    faceCascade = cv2.CascadeClassifier(cascadePath)

    # For face recognition we will use the the LBPH Face Recognizer
    recognizer = cv2.face.createLBPHFaceRecognizer()
    print pathToTrainedModel
    recognizer.load(pathToTrainedModel)

    #Open image for prediction
    predict_image_pil = Image.open(pathToTestImage).convert('L')
    predict_image = np.array(predict_image_pil, 'uint8')
    faces = faceCascade.detectMultiScale(predict_image, 1.3, 5)
    if len(faces) == 0:
        print "0 faces found"
        return
        
    results = []
    
    img = cv2.imread(pathToTestImage, cv2.IMREAD_COLOR)
    
    #For each possible face in that image
    for (x, y, w, h) in faces:
        nbr_predicted, conf = recognizer.predict(predict_image[y: y + h, x: x + w])
        #nbr_actual = int(os.path.split(image_path)[1].split(".")[0].replace("subject", "")) #Not testing at this time.
        #print "We believe this face is", nbr_predicted, "with", conf, "confidence"

		# Add label for each face found in image
        label = "Unrecognized"
        filePath = os.path.join('src' 'database' 'userNameLabelMapping')
        with open(filePath) as data_file:    
        	data = json.load(data_file)
        	for i,val in enumerate(data):
        		if data[i]["label"] == nbr_predicted:
        			label = data[i]["name"]
        			break

        if isMainScript:
        	print nbr_predicted, conf
        if nbr_predicted is None: 
         #TODO handle this case..might not even happen
         # because I've never seen it and the model may try to match the image no matter what
         # but i haven't actually found any documentation to confirm this
        	print "null"  
         
        #parameters are image, label-text, origin point for label, font, font-size, RGB text color, thickness
        cv2.putText(img, label, (x,y+h), cv2.FONT_HERSHEY_PLAIN, 3, (243,74,32), 2);
        results.append(nbr_predicted)
        
    if isMainScript:
    	cv2.imwrite(newImagePath, img)
    return results

if __name__ == '__main__':
    identify(str(sys.argv[1]),True,sys.argv[2])

