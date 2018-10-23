package org.dustinP1.apriori.loader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVLoader extends Loader {
	
	private boolean skipZero;
	
	public CSVLoader(String filePath) {
		super(filePath);
		this.skipZero = false;
	}
	
	public CSVLoader(String filePath, boolean skipZero) {
		super(filePath);
		this.skipZero = skipZero;
	}
	
	@Override
	protected int[][] load() throws IOException {
		System.out.print("prepare data...");
		Reader in;
		CSVParser csvFileParser;
		String[] colName;
		//String[] colName;
		in = new FileReader(filePath);
		CSVFormat csvFileFormat = CSVFormat.EXCEL.withFirstRecordAsHeader().withNullString(" ");
		csvFileParser = new CSVParser(in, csvFileFormat);
		colName = new String[csvFileParser.getHeaderMap().keySet().size()];
		csvFileParser.getHeaderMap().keySet().toArray(colName);
		
		//prepare observer arraylist and String-int hashmap
		ArrayList<Integer[]> csvArrayList = new ArrayList<Integer[]>();
		int totalRows = 0;
		
		for (CSVRecord record : csvFileParser) {
			int colSize = record.size();
			
			String[] var = new String[colSize];
		    for(int i = 0 ; i < colSize ; i++) {
		    	var[i] = record.get(colName[i]);
		    }
		    Integer[] hashArr;
	    	ArrayList<Integer> hashArrTemp = new ArrayList<Integer>();
	    	
	    	for(int i = 0 ; i < colSize ; i++) {
	    		if(skipZero && var[i] == null)
	    			continue;
	    		
		    	String varAllName = colName[i]+":"+var[i];
		    	if(!nameHash.containsKey(varAllName))
		    		nameHash.put(varAllName, varAllName.hashCode());
		    	hashArrTemp.add( nameHash.get(varAllName) );
		    }
	    	
	    	hashArr = new Integer[hashArrTemp.size()];
	    	hashArrTemp.toArray(hashArr);
		    csvArrayList.add(hashArr);
		    totalRows++;
		}
		
		int[][] testSubject = new int[totalRows][];
		for(int i = 0 ; i < totalRows ; i++) {
			testSubject[i] = Arrays.stream(csvArrayList.get(i)).mapToInt(Integer::intValue).toArray();
		}
		
		csvFileParser.close();
		return testSubject;
	}

}
