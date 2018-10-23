package org.dustinP1.apriori.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

public class Apriori extends Method{

	public Apriori(double s, double c,boolean d) {
		super(s,c,d);
	}
	
	public HashMap<Set<Integer>,Integer> runMethod() {
		//prepare HashMap for Choose set C1
		HashMap<Integer,Integer> c1 = new HashMap<Integer,Integer>();
		HashMap<Set<Integer>,Integer> trashSet = new HashMap<Set<Integer>,Integer>();
		HashMap<Set<Integer>,Integer> selectSet = new HashMap<Set<Integer>,Integer>();
		//int totalCount = 0;
		int totalRow = 0;
		
		//do first scan, count total nums and build C1
		for(int[] i : testSubject) {
			for(int j : i) {
				if(c1.containsKey(j)) {
					int tempNum = c1.get(j);
					c1.put(j, tempNum + 1);
				}else {
					c1.put(j, 1);
				}
				//totalCount++;
			}
			totalRow++;
		}
		
		//[preview] dump c1 result
		if(debug) {
			System.out.println("C1 scanned.");
			System.out.printf("total: %d\n",totalRow);
			c1.forEach((k,v)->{
				System.out.printf("%d\t%d\n",k,v);
			});
			System.out.println("-----------"); 
		}
		
		//cale the support itemCount
		final double supportCount = support * totalRow;
		
		//remove support rate under setting in C1
		HashMap<Integer,Integer> tempC1 = new HashMap<Integer,Integer>();
		c1.forEach((k,v)->{
			if(v >= supportCount) {
				tempC1.put(k, v);
				HashSet<Integer> tempC1Key = new HashSet<Integer>();
				tempC1Key.add(k);
				selectSet.put(tempC1Key, v);
			}
		});
		c1 = tempC1;
		
		System.out.println("Prepare C1:");
		usedMemory();
		usedTime();
		System.out.println("");
		
		//[preview] dump c1 result after remove object
		if(debug) {
			System.out.println("C1 selected.");
			System.out.printf("supportCount: %.0f\n",supportCount);
			c1.forEach((k,v)->{
				System.out.printf("%d\t%d\n",k,v);
			});
			System.out.println("-----------");
		}
		
		//loop for create select set from 2
		int currentSetNum = 2;
		while(true) {
			//prepare Set HashMap for currentSelectSet
			HashMap<Set<Integer>,Integer> currentSelectSet = new HashMap<Set<Integer>,Integer>();
			
			//generate currentSelectSet clean set by universal C1
			int[] tempC1Arr = new int[c1.keySet().size()];
			ArrayList<Integer> tempC1ArrayList = new ArrayList<Integer>(c1.keySet());
			for(int n = 0; n < tempC1ArrayList.size(); n++)
			{
			    //autoboxing implicitly converts Boolean to boolean
				tempC1Arr[n] = tempC1ArrayList.get(n); 
			}
			Set<Integer>[] tempC2Arr = getSuperSet(tempC1Arr,currentSetNum);
			for(int i = 0 ; i < tempC2Arr.length ; i++) {
				currentSelectSet.put(tempC2Arr[i], 0);
			}
			
			//[preview] dump currentSelectSet result
			if(debug) {
				System.out.println("Current round:"+currentSetNum);
				System.out.println("orignal super set is prepared.");
				System.out.printf("Current set total: %d\n",currentSelectSet.size());
				currentSelectSet.forEach((k,v)->{
					System.out.println(k.toString()+"\t"+v);
				});
				System.out.println("-----------");
			}
			
			//remove patten in trash set
			HashMap<Set<Integer>,Integer> tempCurrentSelectSet = new HashMap<Set<Integer>,Integer>();
			for(Entry<Set<Integer>,Integer> j : currentSelectSet.entrySet()) {
				boolean tempFlag = true;
				for(Entry<Set<Integer>,Integer> k : trashSet.entrySet()) {
					if(j.getKey().containsAll(k.getKey())) {
						tempFlag = false;
						break;
					}
				}
				if(tempFlag)
					tempCurrentSelectSet.put(j.getKey(),j.getValue());
			}
			currentSelectSet = new HashMap<Set<Integer>,Integer>(tempCurrentSelectSet);
			
			//[preview] dump c3 result before counting.
			if(debug) {
				System.out.println("currentSelectSet generated.");
				currentSelectSet.forEach((k,v)->{
					System.out.println(k.toString()+"\t"+v);
				});
				System.out.println("-----------");
			}
			
			//decide should continue or not
			if(currentSelectSet.size() == 0)
				break;
			
			//scan entire data to build currentSelectSet
			for(int[] i : testSubject) {
				for(Entry<Set<Integer>,Integer> j : currentSelectSet.entrySet()) {
					HashSet<Integer> tempSetI = new HashSet<Integer>();
					for(int k : i) {
						tempSetI.add(k);
					}
					if(tempSetI.containsAll(j.getKey())) {
						currentSelectSet.put(j.getKey(), j.getValue() + 1);
					}
				}
			}
			
			//[preview] dump currentSelectSet result
			if(debug) {
				System.out.println("currentSelectSet is calc.");
				System.out.printf("currentSelectSet total: %d\n",currentSelectSet.size());
				currentSelectSet.forEach((k,v)->{
					System.out.println(k.toString()+"\t"+v);
				});
				System.out.println("-----------");
			}
			
			//remove support rate under setting in C1
			tempCurrentSelectSet.clear();
			
			currentSelectSet.forEach((k,v)->{
				if(v >= supportCount) {
					tempCurrentSelectSet.put(k, v);
				}else {
					trashSet.put(k, v);
				}
			});
			currentSelectSet = new HashMap<Set<Integer>,Integer>(tempCurrentSelectSet);
			
			//[preview] dump currentSelectSet result after remove object
			if(debug) {
				System.out.println("currentSelectSet selected.");
				System.out.printf("supportCount: %.0f\n",supportCount);
				currentSelectSet.forEach((k,v)->{
					System.out.println(k.toString()+"\t"+v);
				});
				System.out.println("-----------");
			}
			
			
			//send currentSelectSet into selectSet
			currentSelectSet.forEach((k,v)->{
				selectSet.put(k, v);
			});
			
			//decide should continue or not
			if(currentSelectSet.size() == 0)
				break;
			
			System.out.println("Prepare C"+currentSetNum+":");
			usedMemory();
			usedTime();
			System.out.println("");
			
			//ready to next round
			currentSetNum++;
		}
		
		//[preview] dump SelectSet
		if(debug) {
			System.out.println("all select set is generated");
			selectSet.forEach((k,v)->{
				System.out.println(k.toString()+"\t"+v);
			});
			System.out.println("-----------");
		}
		
		return selectSet;
	}

}
