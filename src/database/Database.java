package database;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import binding.FaceMap;
import binding.FaceMap.FaceMapElement;

public class Database {
	
	final static String faceMapsPath = "src/database/facemaps";
	final static String nameLabelMapPath = "src/database/usernameLabelMapping";
	final static String attendancePath = "src/database/attendance";
	
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
	
	public static String getNextFacemapPath(){
		File imagesDir = new File(faceMapsPath);
		int numFiles = imagesDir.list().length;
		return faceMapsPath + "/facemap" + (numFiles+1) + ".jpg";
	}
	
	//TODO fix date storage
	public static void takeAttendance(FaceMap facemap, String className, Date date){
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
			newRecord.put("date", date.toString());
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
	
	public static ArrayList<String> retrieveAttendanceRecords(String studentName){
		//TODO
		return null;
	}
	
	public static FaceMap retrieveAttendanceRecords(Date date){
		//TODO
		return null;
	}

}
