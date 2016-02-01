# FaceMapper
SE 329 project

***Testing***
Use sample.py with an image to see how/what it is detecting as a face. This is important to use so that you know what is being trained on. Look at tweaking the parameters to "detectMultiScale" if you are having undesirable results (i.e, it's finding more than one face in a training image.)

***Training***
Use train_for_user.py to train, serialize, and save a model for a certain user (look at in-file comments for parameters)

***Testing***
Use identify_users.py to load models for each user in the system and then attempt to identify users in an image