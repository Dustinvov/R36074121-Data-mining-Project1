package org.dustinP1.apriori.loader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class IbmFileLoader extends Loader{

	public IbmFileLoader(String filePath) {
		super(filePath);
	}

	@Override
	protected int[][] load() throws IOException {
		Reader in;
		in = new FileReader(filePath);
		Scanner s = new Scanner(in);
		
		ArrayList<Integer[]> arrayList = new ArrayList<Integer[]>();
		int currentLine = 1;
		ArrayList<Integer> lineList = new ArrayList<Integer>();
		while(s.hasNext()) {
			String sp = s.nextLine();
			//System.out.println(sp);
			Scanner t = new Scanner(sp);
			
			int tempLine = t.nextInt();
			int tempNum = t.nextInt();
			//System.out.println(tempLine + "\t" + tempNum);
			t.close();
			
			if(tempLine != currentLine) {		
				Integer[] line = new Integer[lineList.size()];
				lineList.toArray(line);
				
				arrayList.add(line);
				
				lineList = new ArrayList<Integer>();
				currentLine = tempLine;
			}
			
			lineList.add(tempNum);
			nameHash.put(String.valueOf(tempNum), tempNum);
			
		}
		Integer[] line = new Integer[lineList.size()];
		lineList.toArray(line);
		arrayList.add(line);
		
		s.close();
		
		int[][] testSubject = new int[arrayList.size()][];
		for(int i = 0 ; i < arrayList.size() ; i++) {
			testSubject[i] = Arrays.stream(arrayList.get(i)).mapToInt(Integer::intValue).toArray();
		}
		
		return testSubject;
	}

}
