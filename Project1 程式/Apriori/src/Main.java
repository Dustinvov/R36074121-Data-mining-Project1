import java.io.*;
import java.util.ArrayList;
import org.dustinP1.apriori.exporter.CommonExporter;
import org.dustinP1.apriori.exporter.Exporter;
import org.dustinP1.apriori.exporter.SubsetExporter;
import org.dustinP1.apriori.loader.CSVLoader;
import org.dustinP1.apriori.loader.IbmFileLoader;
import org.dustinP1.apriori.loader.Loader;
import org.dustinP1.apriori.loader.SpaceSperateLoader;
import org.dustinP1.apriori.method.Apriori;
import org.dustinP1.apriori.method.FPGrowth;
import org.dustinP1.apriori.method.Method;
import org.dustinP1.apriori.method.ResultBean;

@SuppressWarnings("unused")
public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		//String filePath = "C:\\Users\\Dustin Shin\\Desktop\\資料探勘(資工)\\testing.txt";
		//String filePath = "C:\\Users\\Dustin Shin\\Desktop\\資料探勘(資工)\\Project1\\dataset.txt";
		String filePath = "C:\\Users\\Dustin Shin\\Desktop\\資料探勘(資工)\\Project1\\players_stats.csv";
				
		//define support, confidence
		
		final double support = 0.1;
				
		final double confidence = 0.9;
		final boolean debug = false;
		final int maxOutput = 10000;		
		
		//Set up loader and method
		
		
		//Loader load = new IbmFileLoader(filePath);
		Loader load = new CSVLoader(filePath,true);
		Exporter exporter = new SubsetExporter(maxOutput);
		Method methodApriori = new FPGrowth(support,confidence,debug);
		//Method methodApriori = new Apriori(support,confidence,debug);
		Main main = new Main(load, methodApriori, exporter);
		main.runWholeTest();
		
		
	}
	
	private Loader loader;
	private Exporter exporter;
	private Method method;
	private int[][] inputData;
	
	public Main(Loader fileLoader, Method method, Exporter exporter) {
		this.loader = fileLoader;
		this.method = method;
		this.exporter = exporter;
	}
	
	public void runWholeTest() {
		System.out.println("Wait for connection");
		try {
			Thread.sleep(30*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		if(loadData())
			runTest();
	}
	
	private boolean loadData() {
		try {
			inputData = loader.getData();
			return true;
		} catch (IOException e) {
			System.out.println("Input file is wrong! " + e.getLocalizedMessage());
			return false;
		}
	}
	
	private void runTest() {
		ArrayList<ResultBean> result =  method.startTest(inputData);
		exporter.export(result, loader.getNameHash());
	}
	
	
	
	
}

