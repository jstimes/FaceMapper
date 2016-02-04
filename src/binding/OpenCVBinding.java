package binding;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import database.Database;

//This class serves as an interface between the GUI and the python backend
// It calls scripts, analyzes results and returns the Result structure defined below
// so the GUI can display the appropriate information
public class OpenCVBinding {
	
	final static String FACE_IMAGES_PATH = "data/faces";
	final static String pythonFilePath = "src/python/";
	final static String trainScript = "train_for_user.py";
	final static String recognizeScript = "identify_users.py";
	final static String noRecognizedFacesMsg = "0 faces found";
	
//	public static void main(String[] args) throws IOException {
//		
//		System.out.println("Training...");
//		HashMap<String, String> map = new HashMap<String,String>();
//		map.put("Paul", "C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\paul");
//		map.put("Jacob", "C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\jacob");
//		map.put("Andrew", "C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\andrew");
//		//FaceMap training = recognize("C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\paul\\paul.1");
//		Result training = recognize("C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\jacob\\jacob.1.jpg", true);
//		//System.out.println(training.label);
//		//Result training = trainDir(map);
//		
//		training.printOutput();
//		training.printWarnings();
//		training.printErrors();
//		for(String entry : Database.retrieveStudentAttendanceRecords("Jacob")){
//			System.out.println(entry);
//		}
//		FaceMap map = Database.retrieveDateAttendanceRecords("math", "01022016");
//		for(FaceMapElement fme : map.elements){
//			System.out.println(fme.name + " " + fme.confidence);
//		}
//		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
//		LinkedList<String> images = new LinkedList<String>();
//		images.add("C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\jacob\\jacob.1.jpg");
//		images.add("C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\jacob\\jacob.2.jpg");
//		images.add("C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\jacob\\jacob.3.jpg");
//		images.add("C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\jacob\\jacob.4.jpg");
//		images.add("C:\\Users\\Jacob\\Desktop\\Spring16\\SE329\\project1\\FaceMapper\\data\\faces\\jacob\\jacob.5.jpg");
//		map.put("Jacob", images);
//		trainFiles(map);
//	}
	
	//Trains recognition algorithm on the (name, face images) pairs supplied
	// Parameter should be Map<Name, List<filepaths to images of Name>>
	// Call this if images for each person are scattered and not in the same directory
	public static Result trainFiles(Map<String, List<String>> mapOfPeopleToFiles){
		Result result = new Result();
		HashMap<String, String> usersToDirectories = new HashMap<>();
		for(String key : mapOfPeopleToFiles.keySet()){
			String nameLower = key.toLowerCase();
			File userDirectory = new File(FACE_IMAGES_PATH + "/" + nameLower);
			
			if(!userDirectory.exists()){
				
				//Directory of face images for this user does not yet exist so make one:
				if(!userDirectory.mkdirs()){
					result.appendError("Internal error - failed to create faces directory for " + key + " with path: " + userDirectory.getPath());
					return result;
				}
			}
			//directory may or may not have existed before, but it must now
			// So now add supplied images to this directory:
			List<String> imagePaths = mapOfPeopleToFiles.get(key);
			for(String imagePath : imagePaths){
				File imageFile = new File(imagePath);
				if(!imageFile.exists()){
					result.appendError("Internal error - couldn't find file: " + imagePath);
					return result;
				}
				String imageName = imageFile.getName();
				File target = new File(userDirectory.getAbsolutePath() + "/" + imageName);
				try {
					//Copies the original file to a new file in this users face-images directory
					// If the file already exists here (based on same names), it will replace the existing one with this
					Files.copy(imageFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} 
				catch (IOException e) {
					result.appendError("Internal error - couldn't copy image file " + imageFile.getAbsolutePath() + " to " + target.getAbsolutePath());
					return result;
				}
			}
			
			//If made it here, added all given images to the users face-images folder, so
			// add that folder to a new map to be passed to the other train function
			usersToDirectories.put(key, userDirectory.getAbsolutePath());
		}
		
		//Now just call the other method that expects organized directories
		return trainDir(usersToDirectories);
	}
	
	//Trains recognition algorithm on the (name, face images) pairs supplied
	// Parameter should be Map<Name, filepath-to-directory-of-faces-of-names>
	//Call this if for each user to be trained on has their own directory of images
	public static Result trainDir(Map<String, String> mapOfPeopleToDirectories) {
		Result result = new Result();
		for(String user : mapOfPeopleToDirectories.keySet()){
			String directory = mapOfPeopleToDirectories.get(user);
			System.out.println(directory);
			long label = Database.getLabelForUser(user);
			
			//train script expects a directory path for training images folder, and user name (label) associated with those photos
			ArrayList<String> args = new ArrayList<String>();
			args.add(directory);

			args.add(Long.toString(label));
			Result pythonResult = executeScript(trainScript, args);
			
			if(pythonResult.success){
				for(String output : pythonResult.outputs){
					if(output.contains("failure")) {
						result.appendError("Failed to train model on " + user);
					}
					else if (output.contains("Error")){
						result.appendError(output + " for " + user);
					}
					else if(output.contains("no faces")){
						//Should let user know one of their images was undetectable by our algorithm
						result.appendWarning(output);
					}
					else {
						result.appendOutput("Successfully trained model on " + user);
					}
				}
			}
			
			else {
				for(String error : pythonResult.errors){
					result.appendError(error);
				}
			}
			
		}
		return result;
	}
	
	//Given a filepath to an image, this method will attempt to recognize all detected faces within
	// and save the data into a facemap
	public static Result recognize(String filepath, boolean attendance, String className){
		Result result = new Result();
		
		//Arguments to this script are filepath to image to be recognized,
		// and filepath for facemapped image to be saved to
		ArrayList<String> args = new ArrayList<String>();
		args.add(filepath);
		
		String facemapPath = Database.getNextFacemapPath();
		args.add(facemapPath);
		
		Result pythonResult = executeScript(recognizeScript, args);
		if(pythonResult.success){
			for(String s : pythonResult.outputs){
				if(s.equals(noRecognizedFacesMsg)) {
					result.appendError(noRecognizedFacesMsg);
					return result;
				}
				else {
					//Results for each face come back as <int> <double>, label confidence
					String[] parts = s.split(" ");
					String name = Database.getNameForLabel(Long.parseLong(parts[0]));
					double confidence = Double.parseDouble(parts[1]);
					result.facemap.addPersonConfidencePair(name, confidence);
				}
			}
			result.facemap.setImagePath(facemapPath);
			if(attendance){
				Database.takeAttendance(result.facemap, className);
			}
		}
		else {
			for(String error : pythonResult.errors){
				result.appendError(error);
				System.out.println(error);
			}
		}
		return result;
	}
	
	//This method runs the specified script with the given arguments, if any
	private static Result executeScript(String script, ArrayList<String> arguments) {
		try {
			String args = "";
			for(String arg : arguments){
				args += " " + arg;
			}
			System.out.println(args);
			Process p = Runtime.getRuntime().exec("python " + pythonFilePath + script + args);
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

	        String line;
	        Result result = new Result();
	        
	        //Get any output:
	        while ((line = stdInput.readLine()) != null) {
	           result.appendOutput(line);
	        }
	        
	        //Get any errors encountered:
	        while ((line = stdError.readLine()) != null) {
	        	result.appendError(line);
	        }
	        
	        return result;
		}
		catch (IOException e){
			Result r = new Result();
			r.appendError("Failed to execute script");
			return r;
		}
	}
	
	//A structure to hold our results from executing python scripts
	/// and to return information about them to the GUI
	public static class Result {
		
		public boolean success;
		public ArrayList<String> outputs;
		public ArrayList<String> warnings;
		public ArrayList<String> errors;
		public FaceMap facemap;
		
		public Result(){
			success = true;
			errors = new ArrayList<String>();
			outputs = new ArrayList<String>();
			warnings = new ArrayList<String>();
			facemap = new FaceMap();
		}
		
		//Success remains true until an error message is added
		public void appendError(String e){
			errors.add(e);
			success = false;
		}
		
		public void appendOutput(String o){
			outputs.add(o);
		}
		
		public void appendWarning(String w){
			warnings.add(w);
		}
		
		public void printOutput(){
			for(String output : outputs){
				System.out.println(output);
			}
		}
		
		public void printWarnings(){
			for(String warning : warnings){
				System.out.println(warning);
			}
		}
		
		public void printErrors(){
			for(String error : errors){
				System.out.println(error);
			}
		}
	}
}
