package org.dustinP1.apriori.loader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class SpaceSperateLoader extends Loader {

	public SpaceSperateLoader(String filePath) {
		super(filePath);
	}

	@Override
	protected int[][] load() throws IOException {
		Reader in;
		in = new FileReader(filePath);
		Scanner s = new Scanner(in);
		
		ArrayList<Integer[]> arrayList = new ArrayList<Integer[]>();
		
		while(s.hasNext()) {
			String sp = s.nextLine();
			Scanner t = new Scanner(sp);
			
			ArrayList<Integer> lineList = new ArrayList<Integer>();
			while(t.hasNextInt()) {
				int temp = t.nextInt();
				lineList.add(temp);
				nameHash.put(String.valueOf(temp), temp);
			}
			t.close();
			Integer[] line = new Integer[lineList.size()];
			lineList.toArray(line);
			
			arrayList.add(line);
		}
		s.close();
		
		int[][] testSubject = new int[arrayList.size()][];
		for(int i = 0 ; i < arrayList.size() ; i++) {
			testSubject[i] = Arrays.stream(arrayList.get(i)).mapToInt(Integer::intValue).toArray();
		}
		
		return testSubject;
	}

}
