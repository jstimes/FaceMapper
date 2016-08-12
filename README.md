# FaceMapper
SE 329 project

The goal was to develop an application to automate attendance taking in classrooms. After being fed training images for the faces of students in a class, it attempts to identify those individuals in class photos and take attendance accordingly.

This project is functional in a Windows environment through Eclipse, with Python 2.7, OpenCV 2.4.10.

The folder 'data/faces' contains training images and 'data/test' contains images used for testing. The constructed facemaps are stored in 'src/database/facemaps'.

The application is run from 'src/gui/Main.java'. First train the recognition model by clicking 'Train' then enter a name and select photos to be used to train the model on that user. The more photos, the better the results. At least 2 are necessary as the last one is used to test if the training was successful or not. 

Once the model is trained on each user desired, click identify to upload and identify users in another individual or group photo. Our model works well with individual photos but struggles with pictures containing several people. We believe this is due to lack of training data and poor qualitiy group photos.  

When identifying a group photo, there is an option to take attendance if being used in a class setting. The application will save attendance info for those recognized in the photo and it can be retrieved later via the remaining UI buttons. 
