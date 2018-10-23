package org.dustinP1.apriori.exporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.dustinP1.apriori.method.ResultBean;

public abstract class Exporter {
	protected int maxOutput;
	
	public Exporter(int maxout) {
		maxOutput = maxout;
	}
	
	public abstract void export(ArrayList<ResultBean> result, HashMap<String,Integer> nameHash);
	
	protected void sortResult(ArrayList<ResultBean> r) {
		//sort by confidence
		Collections.sort(r, (k,v)->{
			ResultBean kk = (ResultBean)k;
			ResultBean vv = (ResultBean)v;
			int output = kk.confidence.compareTo(vv.confidence);
			if(output == 0)
				output = kk.support.compareTo(vv.support);
			if(output == 0)
				output = kk.combinedRow.compareTo(vv.combinedRow);
			if(output == 0)
				output = kk.lift.compareTo(vv.lift);
			return output;
		});
		Collections.reverse(r);
	}
	
	protected void dumpResult(ArrayList<ResultBean> r,HashMap<String, Integer> nameHash) {
		//dump result
		System.out.println("\n\n--------RESULT--------");
		System.out.printf("set1 => set2\tsupport confidence lift\n");
		int count = 0;
		for(ResultBean i : r) {
			//String whole = ResultBean.setToString(i.wholeSet,nameHash);
			String input = ResultBean.setToString(i.inputSet,nameHash);
			String output = ResultBean.setToString(i.outputSet,nameHash);
			System.out.printf("%s => %s\t%.4f\t%.4f\t%.4f\n",input,output,i.support,i.confidence,i.lift);
			count++;
			if(count > maxOutput) {
				System.out.printf("(%d of record not print due to maximum limit of output...)",r.size()-maxOutput);
				break;
			}
		}
	}
}
