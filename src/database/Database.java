package database;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import binding.FaceMap;
import binding.FaceMap.FaceMapElement;

//This class serves as a wrapper between the application and
// the database...which is actually a collection of JSON files not a database
public class Database {
	
	final static String faceMapsPath = "src/database/facemaps";
	final static String nameLabelMapPath = "src/database/usernameLabelMapping";
	final static String attendancePath = "src/database/attendance";
	
	///Returns the label for a given name,
	// Each name is actually represented as a number label for 
	// our face training algorithm
	// these are all stored in usernameLabelMapping (a JSON file)
	public static long getLabelForUser(String query) {
		JSONParser parser = new JSONParser();
		JSONArray a;
		try {
			a = (JSONArray) parser.parse(new FileReader(nameLabelMapPath));
			//Used to determine what label to assign a new user
			long lastLabel = 0;
			for (Object o : a){
			    JSONObject user = (JSONObject) o;
	
			    String name = (String) user.get("name");
			    long label = (long) user.get("label");
			    lastLabel = label;
			    if(name.equals(query)){
			    	return label;
			    }
			}
			
			//If couldn't find query in array of names, need to add a new user:
			long newLabel = lastLabel+1;
			JSONObject newUser = new JSONObject();
			newUser.put("name", query);
			newUser.put("label", newLabel);
			a.add(newUser);
			
			FileWriter file = new FileWriter(nameLabelMapPath);
			file.write(a.toJSONString());
			file.close();
			
			return newLabel;
		} 
		catch (IOException | ParseException e) {
			System.out.println(e.getMessage());
			return -1;
		}
	}
	
	//Returns the name for a given label,
	// Each name is actually represented as a number label for 
	// our face training algorithm
	public static String getNameForLabel(long query) {
		JSONParser parser = new JSONParser();
		JSONArray a;
		try {
			a = (JSONArray) parser.parse(new FileReader(nameLabelMapPath));
			for (Object o : a){
			    JSONObject user = (JSONObject) o;
	
			    String name = (String) user.get("name");
			    long label = (long) user.get("label");
			    if(label == query){
			    	return name;
			    }
			}
			return "label not found";
		} 
		catch (IOException | ParseException e) {
			System.out.println(e.getMessage());
			return "json error";
		}
	}
	
	//Our facemap "database" is simply a folder, 'facemaps' that stores
	// images named facemapX.jpg, so this method returns a path with an X that
	// hasn't been used yet
	public static String getNextFacemapPath() {
		File imagesDir = new File(faceMapsPath);
		int numFiles = imagesDir.list().length;
		return faceMapsPath + "/facemap" + (numFiles+1) + ".jpg";
	}
	
	//Calls the other takeAttendance, but with today as the date string
	public static void takeAttendance(FaceMap facemap, String className) {
		int month = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int day = Calendar.getInstance().get(Calendar.MONTH) + 1; //month starts at 0
		int year = Calendar.getInstance().get(Calendar.YEAR);
		String dateStr = "";
		
		if(month < 10){
			dateStr += "0";
		}
		dateStr += month;
		if (day < 10){
			dateStr += "0";
		}
		dateStr += Integer.toString(day) + Integer.toString(year);
		takeAttendance(facemap, className, dateStr);
	}
	
	//Saves a facemap for the specified class for the given date in the attendance DB (JSON file)
	//Useful to have manual date entry for testing
	public static void takeAttendance(FaceMap facemap, String className, String dateStringMMDDYYYY) {
		JSONParser parser = new JSONParser();
		JSONArray a;
		try {
			a = (JSONArray) parser.parse(new FileReader(attendancePath));
			
			JSONObject thisClass = null;
			for(int i=0; i<a.size(); i++){
				JSONObject curClassObj = (JSONObject) a.get(i);
				if(curClassObj.get("class").equals(className)){
					thisClass = curClassObj;
					break;
				}
			}
			//Didn't find class already in DB:
			if(thisClass == null){
				JSONObject newClass = new JSONObject();
				newClass.put("class", className);
				newClass.put("records", new JSONArray());
				thisClass = newClass;
				a.add(newClass);
			}
			
			//Get the records array from the class entry:
			JSONArray records = (JSONArray) thisClass.get("records");
			
			JSONObject newRecord = new JSONObject();
			
			newRecord.put("date", dateStringMMDDYYYY);
			newRecord.put("mapPath", facemap.getMapPath());
			JSONArray students = new JSONArray();
			for(FaceMapElement elem : facemap.getElements()) {
				JSONObject student = new JSONObject();
				student.put("name", elem.name);
				student.put("confidence", elem.confidence);
				students.add(student);
			}
			newRecord.put("students", students);
			records.add(newRecord);
			
			FileWriter file = new FileWriter(attendancePath);
			file.write(a.toJSONString());
			file.close();
		} 
		catch (IOException | ParseException e) {
			e.printStackTrace();
			return;
		}
	}
	
	//This returns all entries for the given student returned as a list of Strings like:
	// <class> <date>
	// grouped by class, but no sorting performed
	public static ArrayList<String> retrieveStudentAttendanceRecords(String studentName){
		JSONParser parser = new JSONParser();
		JSONArray a;
		ArrayList<String> results = new ArrayList<String>();
		try {
			a = (JSONArray) parser.parse(new FileReader(attendancePath));
			
			//Find all records saved on the specified date:
			for(int clas=0; clas<a.size(); clas++){
				JSONObject curClassObj = (JSONObject) a.get(clas);
				
				//Get the records array from the class entry:
				JSONArray records = (JSONArray) curClassObj.get("records");
				
				//Check if there is a record that has the specified student:
				for(int record = 0; record < records.size(); record++){
					JSONObject recordObj = (JSONObject) records.get(record);
						
					JSONArray students = (JSONArray) recordObj.get("students");
					for(int student = 0; student < students.size(); student++){
						JSONObject studentObj = (JSONObject) students.get(student);
						String name = (String) studentObj.get("name");
						if(name.equals(studentName)){
							String date = (String) recordObj.get("date");
							String className = (String) curClassObj.get("class");
							results.add(className + " " + date);
						}
					}
				}
			}
			
			return results;
			
		} 
		catch (IOException | ParseException e) {
			e.printStackTrace();
			return results;
		}
	}
	
	//Retrieves a FaceMap that has been saved in the attendance DB for the given date and class
	// returns null if not found
	public static FaceMap retrieveDateAttendanceRecords(String className, String dateStringMMDDYYYY){
		HashMap<String, FaceMap> classesToFaceMaps = retrieveDateAttendanceRecords(dateStringMMDDYYYY);
		if(classesToFaceMaps.containsKey(className)){
			return classesToFaceMaps.get(className);
		}
		else {
			return null;
		}
	}
	
	//Returns all facemaps stored for the specified date
	// Data is returned as a map with the classname as key, Facemap as value
	public static HashMap<String, FaceMap> retrieveDateAttendanceRecords(String dateStringMMDDYYYY){
		JSONParser parser = new JSONParser();
		JSONArray a;
		HashMap<String, FaceMap> results = new HashMap<String, FaceMap>();
		try {
			a = (JSONArray) parser.parse(new FileReader(attendancePath));
			
			//Find all records saved on the specified date:
			for(int clas=0; clas<a.size(); clas++){
				JSONObject curClassObj = (JSONObject) a.get(clas);
				
				//Get the records array from the class entry:
				JSONArray records = (JSONArray) curClassObj.get("records");
				
				//Check if there is a record for the specified date:
				for(int record = 0; record < records.size(); record++){
					JSONObject recordObj = (JSONObject) records.get(record);
					
					if (recordObj.get("date").equals(dateStringMMDDYYYY)){
						
						//If found, reconstruct facemaps to return in hashmap
						FaceMap map = new FaceMap();
						
						String imgPath = (String) recordObj.get("mapPath");
						map.setImagePath(imgPath);
						
						JSONArray students = (JSONArray) recordObj.get("students");
						for(int student = 0; student < students.size(); student++){
							JSONObject studentObj = (JSONObject) students.get(student);
							double confidence = (double) studentObj.get("confidence");
							String name = (String) studentObj.get("name");
							map.addPersonConfidencePair(name, confidence);
						}
						String className = (String) curClassObj.get("class");
						results.put(className, map);
					}
				}
			}
			
			return results;
			
		} 
		catch (IOException | ParseException e) {
			e.printStackTrace();
			return results;
		}
	}

}
