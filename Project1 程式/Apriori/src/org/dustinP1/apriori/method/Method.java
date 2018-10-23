package org.dustinP1.apriori.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

abstract public class Method {

	protected int[][] testSubject;
	protected long startTime;
	protected double support;
	protected double confidence;
	protected boolean debug;
	
	public Method(double s, double c,boolean d) {
		support = s;
		confidence = c;
		debug = d;
	}
	
	public ArrayList<ResultBean> startTest(int[][] t) {
		testSubject = t;
		System.out.println("Data is ready, start method.");
		
		startTime = System.currentTimeMillis();
		HashMap<Set<Integer>,Integer> selectSet = runMethod();
		
		System.out.println("Method finished. frequent itemset is generated.");
		System.out.println("We found "+selectSet.size()+" frequent itemsets.");
		System.out.println("Using Time:" + usedMilliSecond() + " ms\n\n");
		
		ArrayList<ResultBean> result = generateResultBean(selectSet, testSubject.length);
		
		System.out.println("ResultBean generated.");
		System.out.println("Using Time:" + usedMilliSecond() + " ms\n\n");
		
		return result;
	}
	
	abstract public HashMap<Set<Integer>,Integer> runMethod();
	
	protected static final long MEGABYTE = 1024L * 1024L;

	protected static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }
	
	protected static void usedMemory() {
		// Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory : " + memory + " bytes.");
        System.out.println("Used memory : "
                + bytesToMegabytes(memory) + " Mb.");
	}
	
	protected long usedMilliSecond() {
        return System.currentTimeMillis() - startTime;
    }
	
	protected void usedTime() {
        System.out.println("Used time : " + usedMilliSecond() + " ms.");
	}
	
	//get a super set from a int array with a specific size
	protected static Set<Integer>[] getSuperSet(int[] arr,int size){

		ArrayList<Set<Integer>> result = new ArrayList<Set<Integer>>();
		
		int[] pointer = new int[size];
		int[] limited = new int[size];
		for(int i = 0 ; i < size ; i++) {
			pointer[i] = i;
			limited[i] = arr.length - size + i;
		}
		
		int flag = size - 1;
		while(flag >= 0) {
			while(pointer[flag] <= limited[flag]) {
				boolean tempInterPoint = false;
				while(flag < size - 1) {
					tempInterPoint = true;
					flag++;
					pointer[flag] = pointer[flag-1] + 1;
				}
				if(tempInterPoint) {
					pointer[flag] = pointer[flag-1] + 1;
				}
				
				Set<Integer> tempRecord = new HashSet<Integer>();
				for(int i : pointer) {
					tempRecord.add(arr[i]);
					//System.out.print(arr[i]+" ");
				}
				result.add(tempRecord);
				//System.out.println();
				pointer[flag]++;
			}
			flag--;
			if(flag >= 0)
				pointer[flag]++;
		}
		int resultSize = result.size();
		@SuppressWarnings("unchecked")
		Set<Integer>[] resultArr = new Set[resultSize];
		
		result.toArray(resultArr);
		
		return resultArr;
	}
	
	protected ArrayList<ResultBean> generateResultBean(HashMap<Set<Integer>,Integer> selectSet, int totalRow) {
		//turn selectSet HashMap into arraylist
		ArrayList<Entry<Set<Integer>,Integer>> selectArrayList = new ArrayList<Entry<Set<Integer>,Integer>>();
		for(Entry<Set<Integer>,Integer> i:selectSet.entrySet()) {
			selectArrayList.add(i);
		}
			
		//two pointer to run all pairs
		ArrayList<ResultBean> result = new ArrayList<ResultBean>();
		
		for(int i = 0 ; i < selectArrayList.size() ; i++) {
			for(int j = i + 1 ; j < selectArrayList.size() ; j++) {
				Set<Integer> setA = selectArrayList.get(i).getKey();
				Set<Integer> setB = selectArrayList.get(j).getKey();
				
				int countA,countB;
				
				//check two set is the subset relation
				if(setA.containsAll(setB)) {
					countA = selectArrayList.get(i).getValue();
					countB = selectArrayList.get(j).getValue();
				}else if(setB.containsAll(setA)) {
					Set<Integer> tempSet = setA;
					setA = setB;
					setB = tempSet;
					countA = selectArrayList.get(j).getValue();
					countB = selectArrayList.get(i).getValue();
				}else {
					continue;
				}
				
				//count Confidence of this pair
				double pair_confidence = (double)countA / (double)countB;
				
				//is the Confidence higher then our target?
				if(pair_confidence >= confidence) {
					if(debug)
						System.out.println(setA + "\t" + setB);
					//find the third set for calc lift
					Set<Integer> setC = new HashSet<Integer>(setA);
					setC.removeAll(setB);
					int countC = selectSet.get(setC);
					
					double support_A = countA / (double)totalRow;
					double support_B = countB / (double)totalRow;
					double support_C = countC / (double)totalRow;
					double pair_lift = support_A / (support_B * support_C);
					ResultBean r = new ResultBean(setA,setB,setC,support_A,pair_lift,pair_confidence,countA);
					result.add(r);
					
				}
			}
		}
		
		System.out.println("Final matching:");
		usedMemory();
		System.out.println("");
		
		return result;
	}

}
