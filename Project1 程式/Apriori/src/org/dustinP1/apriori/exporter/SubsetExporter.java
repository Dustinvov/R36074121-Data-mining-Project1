package org.dustinP1.apriori.exporter;

import java.util.ArrayList;
import java.util.HashMap;

import org.dustinP1.apriori.method.ResultBean;

public class SubsetExporter extends Exporter {

	public SubsetExporter(int maxout) {
		super(maxout);
	}

	@Override
	public void export(ArrayList<ResultBean> result, HashMap<String, Integer> nameHash) {
		ArrayList<ResultBean> subsetResult = new ArrayList<ResultBean>();
		int resultSize = result.size();
		
		
		System.out.println("This is subset exporter, only the most related result will be display.");
		System.out.println("There are " + resultSize + " orginal results.");
		
		//scan all result to check is it is the most related bean.
		for(int i = 0; i < resultSize ; i++) {
			boolean flag = true;
			
			ResultBean resultA = result.get(i);
			
			for(int j = 0 ; j < resultSize ; j++) {
				if(i == j)
					continue;
				
				ResultBean resultB = result.get(j);
				if( resultA.inputSet.containsAll(resultB.inputSet) && resultA.outputSet.containsAll(resultB.outputSet) ) {
					flag = false;
					break;
				}
			}
			
			if(flag)
				subsetResult.add(resultA);
		}
		
		System.out.println("After subseting, there are " + subsetResult.size() + " results.");
		
		sortResult(subsetResult);
		dumpResult(subsetResult, nameHash);
	}

}
