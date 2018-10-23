package org.dustinP1.apriori.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class FPGrowth extends Method {

	public FPGrowth(double s, double c, boolean d) {
		super(s, c, d);
	}

	@Override
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
		if(debug) {
			System.out.println("support count: " + supportCount);
		}
		
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
		
		//sort the arraylist from c1
		ArrayList<Entry<Integer,Integer>> c1_Order = new ArrayList<Entry<Integer,Integer>>(c1.entrySet());
		Collections.sort(c1_Order, (k,v)->k.getValue().compareTo(v.getValue()));
		//Collections.reverse(c1_Order);
		
		//[preview] dump c1 result after ordering
		if(debug) {
			System.out.println("C1 ordered.");
			System.out.printf("supportCount: %.0f\n",supportCount);
			c1_Order.forEach((k)->{
				System.out.printf("%d\t%d\n",k.getKey(),k.getValue());
			});
			System.out.println("-----------");
		}
		
		//build tree from root
		FPTreeNode root = new FPTreeNode();
		HashMap<Integer,FPTreeNode> treeIndex = new HashMap<Integer,FPTreeNode>();
		c1.forEach((k,v)->{
			treeIndex.put(k, null);
		});
		
		@SuppressWarnings("unchecked")
		ArrayList<Entry<Integer,Integer>>[] subedTestSubject = new ArrayList[testSubject.length];
		
		int tempTestSubjectCount = 0;
		for(int[] row : testSubject) {
			//sort the element in row
			HashMap<Integer,Integer> tempRowHashMap = new HashMap<Integer,Integer>();
			for(int i : row) {
				if(c1.containsKey(i))
					tempRowHashMap.put(i, c1.get(i));
			}
			ArrayList<Entry<Integer,Integer>> tempRowArrayList = new ArrayList<Entry<Integer,Integer>>(tempRowHashMap.entrySet());
			Collections.sort(tempRowArrayList, (k,v)->k.getValue().compareTo(v.getValue()));
			Collections.reverse(tempRowArrayList);
			
			subedTestSubject[tempTestSubjectCount] = tempRowArrayList;
			tempTestSubjectCount++;
		}
		
		
		//build fp-tree
		for(int i = 0; i < subedTestSubject.length ; i++) {
			
			
			ArrayList<Entry<Integer,Integer>> tempRowArrayList = subedTestSubject[i];
			
			//set currentNode at root
			FPTreeNode currentNode = root;
			
			//insert nodes
			for(Entry<Integer,Integer> element : tempRowArrayList) {
				/*int tempIsfound = -1;
				for(int i = 0 ; i < currentNode.nodes.size() ; i++) {
					if(currentNode.nodes.get(i).name == element.getKey()) {
						tempIsfound = i;
						break;
					}
				}*/
				
				int tempIsfound = currentNode.childNode.containsKey(element.getKey()) ? 1 : -1;
				
				FPTreeNode tempNode;
				if(tempIsfound == -1) {
					tempNode = new FPTreeNode();
					tempNode.name = element.getKey();
					tempNode.count = 1;
					tempNode.parent = currentNode;
					//currentNode.nodes.add(tempNode);
					currentNode.childNode.put(tempNode.name, tempNode);
					
					FPTreeNode indexEnd = treeIndex.get(element.getKey());
					if(indexEnd != null) {
						while(indexEnd.nextPartner != null) {
							indexEnd = indexEnd.nextPartner;
						}
						indexEnd.nextPartner = tempNode;
					}else {
						treeIndex.put(element.getKey(), tempNode);
					}
				}else {
					//tempNode = currentNode.nodes.get(tempIsfound);
					tempNode = currentNode.childNode.get(element.getKey());
					tempNode.count++;
				}
				currentNode = tempNode;
			}
			
			if(debug) {
				if(i % 1000 == 0) {
					System.out.println("Tree build proccess: " + i);
					usedTime();
				}
			}
			
		}
		
		System.out.println("Tree builded:");
		usedMemory();
		usedTime();
		System.out.println("");
		
		//[preview] dump c1 result after remove object
		if(debug) {
			System.out.println("Tree builded.");
			FPTreeNode.recureDump(root,0);
			System.out.println("-----------");
		}
		
		//big loop for each item
		for( Entry<Integer,Integer> i : c1_Order) {
			//recurse to make entire set
			HashMap<Set<Integer>,Integer> partSelectSet = treeMining(i.getKey(), i.getValue(), new HashSet<Integer>(), treeIndex, supportCount);
			if(debug) {
				for(Entry<Set<Integer>, Integer> k : partSelectSet.entrySet()) {
					System.out.println(k.getKey() + "\t" + k.getValue());
				}
			}
			
			
			if(debug) {
				System.out.println("For item "+ i.getKey() +", selectSet is generated.");
				usedMemory();
				usedTime();
				System.out.println("");
			}
			selectSet.putAll( partSelectSet );
		}
		
		System.out.println("all select set is generated");
		usedMemory();
		usedTime();
		System.out.println("");
		
		if(debug) {
			System.out.println("all select set is generated");
			selectSet.forEach((k,v)->{
				System.out.println(k.toString()+"\t"+v);
			});
			System.out.println("-----------");
		}
		
		return selectSet;
	}
	
	private HashMap<Set<Integer>,Integer> treeMining(Integer index,Integer count, HashSet<Integer> currentHashSet, HashMap<Integer,FPTreeNode> treeIndex, double supportCount) {
		if(debug) {
			System.out.println("currentHashSet: "+ currentHashSet);
			System.out.println("count: "+ count);
		}

		
		//find the reverse point of each item
		FPTreeNode tempStartNode = treeIndex.get(index);
		ArrayList<FPTreeNode> bortherNodes = new ArrayList<FPTreeNode>();
		do{
			bortherNodes.add(tempStartNode);
			tempStartNode = tempStartNode.nextPartner;
		}while(tempStartNode != null); 
		
		//create conditional pattern base root
		FPTreeNode cpbTreeRoot = new FPTreeNode();
		HashMap<Integer,FPTreeNode> cpbTreeIndex = new HashMap<Integer,FPTreeNode>();
		//reverse each item to get whole patten
		
		for(FPTreeNode j : bortherNodes) {
			int endPointCount = j.count;
			ArrayList<FPTreeNode> tempPatten = new ArrayList<FPTreeNode>();
			FPTreeNode pointer = j;
			do {
				tempPatten.add(pointer);
				pointer = pointer.parent;
			}while(pointer != null);
			
			Collections.reverse(tempPatten);
			
			FPTreeNode cpbTreePointer = cpbTreeRoot;
			for(int k = 1 ; k < tempPatten.size() - 1 ; k++) {
				FPTreeNode pointerNode = tempPatten.get(k);
				
				/*int tempIsfound = -1;
				for(int m = 0 ; m < cpbTreePointer.nodes.size() ; m++) {
					if(cpbTreePointer.nodes.get(m).name == pointerNode.name) {
						tempIsfound = m;
						break;
					}
				}*/
				
				int tempIsfound = cpbTreePointer.childNode.containsKey(pointerNode.name) ? 1 : -1;
				
				FPTreeNode pointerChildren;
				if(tempIsfound == -1) {
					pointerChildren = new FPTreeNode();
					pointerChildren.name = pointerNode.name;
					pointerChildren.count = endPointCount;
					pointerChildren.parent = cpbTreePointer;
					//cpbTreePointer.nodes.add(pointerChildren);
					cpbTreePointer.childNode.put(pointerChildren.name, pointerChildren);
					
					FPTreeNode indexEnd = cpbTreeIndex.get(pointerNode.name);
					if(indexEnd != null) {
						while(indexEnd.nextPartner != null) {
							indexEnd = indexEnd.nextPartner;
						}
						indexEnd.nextPartner = pointerChildren;
					}else {
						cpbTreeIndex.put(pointerNode.name, pointerChildren);
					}
				}else {
					//pointerChildren = cpbTreePointer.nodes.get(tempIsfound);
					pointerChildren = cpbTreePointer.childNode.get(pointerNode.name);
					pointerChildren.count += endPointCount;
				}
				
				cpbTreePointer = pointerChildren;
			}
		}
		if(debug) {
			System.out.println("For item "+ index +", cpbTree is ready.");
			usedMemory();
			usedTime();
			System.out.println("");
		}
		
		
		//cutting the cpbTree under the rate
		if(debug) {
			FPTreeNode.recureDump(cpbTreeRoot,0);
			System.out.println("-----------");
		}
		final HashMap<Integer,Integer> tempCpbTreeCount = new HashMap<Integer,Integer>();
		countTree(cpbTreeRoot,tempCpbTreeCount);
		if(debug) {
			for(Entry<Integer, Integer> k : tempCpbTreeCount.entrySet()) {
				System.out.println(k.getKey() + "\t" + k.getValue());
			}
		}
		for(Entry<Integer, Integer> k : tempCpbTreeCount.entrySet()) {
			if(k.getValue() < supportCount)
				cpbTreeIndex.remove(k.getKey());
		}
		
		cuttingTree(cpbTreeRoot,supportCount,tempCpbTreeCount);
		if(debug) {
			FPTreeNode.recureDump(cpbTreeRoot,0);
			System.out.println("-----------");
		}
		
		//recurse to make entire set
		HashMap<Set<Integer>,Integer> partSelectSet = new HashMap<Set<Integer>,Integer>();
		if(cpbTreeRoot.childNode.size() != 0) {
		//if(cpbTreeRoot.nodes.size() != 0) {
			ArrayList<Integer> cpbTreeIndexKeyList = new ArrayList<Integer>(cpbTreeIndex.keySet());
			Collections.sort(cpbTreeIndexKeyList, (k,v)->tempCpbTreeCount.get(k).compareTo(tempCpbTreeCount.get(v)));
			Collections.reverse(cpbTreeIndexKeyList);
			
			for(int nextIndex : cpbTreeIndexKeyList) {
				HashSet<Integer> nextHashSet = new HashSet<Integer>(currentHashSet);
				nextHashSet.add(index);
				HashMap<Set<Integer>,Integer>tempPartSelectSet = treeMining(nextIndex, tempCpbTreeCount.get(nextIndex), nextHashSet, cpbTreeIndex, supportCount);
				partSelectSet.putAll( tempPartSelectSet );
			}
		}
		currentHashSet.add(index);
		partSelectSet.put(currentHashSet, count);
		
		return partSelectSet;
	}
	
	private void countTree(FPTreeNode node, HashMap<Integer, Integer> tempCpbTreeCount) {
		if(tempCpbTreeCount.containsKey(node.name)) {
			int tempValue = tempCpbTreeCount.get(node.name);
			tempCpbTreeCount.put(node.name, tempValue + node.count);
		}else {
			tempCpbTreeCount.put(node.name, node.count);
		}
		
		
		//for(FPTreeNode i : node.nodes) {
		for(Entry<Integer,FPTreeNode> i : node.childNode.entrySet()) {
			countTree(i.getValue(),tempCpbTreeCount);
		}
	}

	private boolean cuttingTree(FPTreeNode Cnode, double supportCount, HashMap<Integer, Integer> tempCpbTreeCount) {
		//ArrayList<FPTreeNode> tempNodes = new ArrayList<FPTreeNode>();
		HashMap<Integer,FPTreeNode> tempNodes = new HashMap<Integer,FPTreeNode>();
		for(Entry<Integer,FPTreeNode> i : Cnode.childNode.entrySet()) {
		//for(FPTreeNode i : Cnode.nodes) {
			boolean childFlag = cuttingTree(i.getValue(),supportCount,tempCpbTreeCount);
			if(childFlag) {
				//tempNodes.add(i);
				tempNodes.put(i.getKey(), i.getValue());
			}else {
				for( Entry<Integer,FPTreeNode> j : i.getValue().childNode.entrySet()) {
				//for( FPTreeNode j : i.nodes) {
					//tempNodes.add(j);
					tempNodes.put(j.getKey(), j.getValue());
				}
			}
		}
		
		//Cnode.nodes = tempNodes;
		Cnode.childNode = tempNodes;
		
		boolean selfFlag = tempCpbTreeCount.get(Cnode.name) >= supportCount;
		return selfFlag;
	}
	
	private HashMap<Set<Integer>,Integer> recurseMakeSet(FPTreeNode p, HashSet<Integer> s,int topic) {
		if(p.name != 0)
			s.add(p.name);
		
		HashMap<Set<Integer>,Integer> result;
		if(p.nodes.size() == 1) {
			result = recurseMakeSet(p.nodes.get(0),s,topic);
		}else if(p.nodes.size() > 1) {
			result = new HashMap<Set<Integer>,Integer>();
			for(FPTreeNode i : p.nodes) {
				HashMap<Set<Integer>,Integer> reback = recurseMakeSet(i,new HashSet<Integer>(s),topic);
				for( Entry<Set<Integer>,Integer> j : reback.entrySet() ) {
					if(result.get(j.getKey()) != null) {
						result.put( j.getKey(), result.get( j.getKey() ) + j.getValue() );
					}else {
						result.put( j.getKey(), j.getValue() );
					}
				}
			}
		}else {
			result = new HashMap<Set<Integer>,Integer>();
			int coundNum = p.count;
			
			Integer[] tempIntegerArr = new Integer[s.size()];
			s.toArray(tempIntegerArr);
			int[] tempSetArr = Arrays.stream(tempIntegerArr).mapToInt(Integer::intValue).toArray();
			for(int i = 1 ; i <= s.size() ; i++) {
				Set<Integer>[] tempSet = getSuperSet(tempSetArr,i);
				for(Set<Integer> j : tempSet) {
					j.add(topic);
					result.put(j, coundNum);
				}
			}
			
		}
		
		return result;
	}
}

class FPTreeNode{
	ArrayList<FPTreeNode> nodes;
	HashMap<Integer,FPTreeNode> childNode; 
	FPTreeNode parent;
	FPTreeNode nextPartner;
	int count;
	int name;
	public FPTreeNode() {
		nodes = new ArrayList<FPTreeNode>();
		childNode = new HashMap<Integer,FPTreeNode>();
		count = 0;
	}
	
	static void recureDump(FPTreeNode a,int spaceCount) {
		for(int i = 1 ; i <= spaceCount ; i++)
			System.out.print("\t");
		System.out.println("this node:" + a.name +"\t" + a);
		for(int i = 1 ; i <= spaceCount ; i++)
			System.out.print("\t");
		System.out.println("count:" + a.count);
		for(int i = 1 ; i <= spaceCount ; i++)
			System.out.print("\t");
		System.out.println("my parents:" + a.parent);
		for(int i = 1 ; i <= spaceCount ; i++)
			System.out.print("\t");
		System.out.println("my brother:" + a.nextPartner);
		for(Entry<Integer,FPTreeNode> i : a.childNode.entrySet()) {
			recureDump(i.getValue(),spaceCount+2);
		}
	}
}
