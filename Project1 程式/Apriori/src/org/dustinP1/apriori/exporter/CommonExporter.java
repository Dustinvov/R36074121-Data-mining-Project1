package org.dustinP1.apriori.exporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.dustinP1.apriori.method.ResultBean;

public class CommonExporter extends Exporter {
	
	public CommonExporter(int maxout) {
		super(maxout);
	}
	
	public void export(ArrayList<ResultBean> result, HashMap<String,Integer> nameHash) {
		sortResult(result);
		
		dumpResult(result, nameHash);
	}
}
