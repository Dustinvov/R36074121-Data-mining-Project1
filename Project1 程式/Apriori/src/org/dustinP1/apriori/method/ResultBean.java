package org.dustinP1.apriori.method;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class ResultBean{
	public Set<Integer> wholeSet;
	public Set<Integer> inputSet;
	public Set<Integer> outputSet;
	public Double support;
	public Double lift;
	public Double confidence;
	public Integer combinedRow;
	public ResultBean(Set<Integer> a,Set<Integer> b,Set<Integer> c,double s,double l,double con,int comb) {
		wholeSet = a;
		inputSet = b;
		outputSet = c;
		support = s;
		lift = l;
		confidence = con;
		combinedRow = comb;
	}
	
	public static String setToString(Set<Integer> a, HashMap<String,Integer> m) {
		String output = "[";
		for(int i : a) {
			for(Entry<String,Integer> entry: m.entrySet()){
	            if(new Integer(i).equals(entry.getValue())){
	            	output += entry.getKey() + ",";
	                break; //breaking because its one to one map
	            }
	        }
		}
		output += "]";
		return output;
	}
}
