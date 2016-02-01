package binding;

import java.util.ArrayList;

//I really have no idea what this should look like
public class FaceMap {
	
	//A file path to the saved image that contains rectangles and names of 
	// individuals recongized in a submitted photo
	String mapImageFilePath;
	
	//Stores all individuals recognized in this map
	ArrayList<FaceMapElement> elements;

	public FaceMap(){
		elements = new ArrayList<FaceMapElement>();
	}
	
	public void setImagePath(String path){
		mapImageFilePath = path;
	}
	
	public void addPersonConfidencePair(String name, double conf){
		elements.add(new FaceMapElement(name, conf));
	}
	
	public String getMapPath(){
		return mapImageFilePath;
	}
	
	public ArrayList<FaceMapElement> getElements(){
		return elements;
	}
	
	public class FaceMapElement {
		public String name;
		public double confidence;
		
		FaceMapElement(String name, double conf){
			this.name = name;
			this.confidence = conf;
		}
	}
}
