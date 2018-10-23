package org.dustinP1.apriori.loader;

import java.io.IOException;
import java.util.HashMap;

abstract public class Loader {
	
	protected String filePath;
	
	protected HashMap<String,Integer> nameHash;
	
	int[][] inputData;
	
	public Loader(String filePath) {
		this.filePath = filePath;
		this.nameHash = new HashMap<String,Integer>();
	}
	
	public HashMap<String,Integer> getNameHash(){
		return nameHash;
	}
	
	public String getFilePath(){
		return filePath;
	}
	
	public int[][] getData() throws IOException{
		if(inputData == null) {
			inputData = load();
		}
		return inputData;
	}
	
	protected abstract int[][] load() throws IOException;
}
