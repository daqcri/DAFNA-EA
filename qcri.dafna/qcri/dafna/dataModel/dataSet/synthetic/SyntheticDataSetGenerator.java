package qcri.dafna.dataModel.dataSet.synthetic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import qcri.dafna.dataModel.data.Globals;

public class SyntheticDataSetGenerator {

	public static void main(String[] args) {
		double groundTruthTrueFactor = 0.6;
		List<String> folders = new ArrayList<String>(); 
		List<Integer> sources = new ArrayList<Integer>();
		List<Integer> di = new ArrayList<Integer>();
		String numSrcNumDI = "/1000s100di"; folders.add(numSrcNumDI);sources.add(1000);di.add(100);
		numSrcNumDI = "/1000s1000di";folders.add(numSrcNumDI);sources.add(1000);di.add(1000);
		numSrcNumDI = "/1000s10000di";folders.add(numSrcNumDI);sources.add(1000);di.add(10000);
		numSrcNumDI = "/5000s100di";folders.add(numSrcNumDI);sources.add(5000);di.add(100);
		numSrcNumDI = "/5000s1000di";folders.add(numSrcNumDI);sources.add(5000);di.add(1000);
		numSrcNumDI = "/10000s100di";folders.add(numSrcNumDI);sources.add(10000);di.add(100);
		numSrcNumDI = "/10000s1000di";folders.add(numSrcNumDI);sources.add(10000);di.add(1000);

		
		
		
		for (int i = 0; i < folders.size(); i ++) {
//		for (String s : folders) {
			numSrcNumDI = folders.get(i);
			
			String baseTF = Globals.directory_syntheticDataSet_BooleanTrueAndFalse_base + numSrcNumDI+ "/claims";
			String baseTF_Truth = Globals.directory_syntheticDataSet_BooleanTrueAndFalse_base + numSrcNumDI+ "/truth";

			AbstractSyntheticDataSetCreator booleanDataSet = new BooleanDataSetCreator(groundTruthTrueFactor,

					baseTF + "/booleanSynth", baseTF_Truth + "/truth.txt");
			String fileName = Globals.directory_syntheticDataSet_Boolean_base + numSrcNumDI + "/claims";
			String truthtFileName = Globals.directory_syntheticDataSet_Boolean_base + numSrcNumDI + "/truth";
			
			final File dataFolder = new File(fileName);
			dataFolder.mkdirs();
			fileName = fileName + "/booleanSynth";

			final File truthFolder = new File(truthtFileName);
			truthFolder.mkdirs();
			truthtFileName = truthtFileName + "/truth";
			
			int numOfSources = sources.get(i);//1000
			int numOFObjetcs = di.get(i);//100;
			int numOfProperties = 1;

			boolean randomFactors = true;
			/* when random factors is true the next factors are not used */
			double srcErrorFactor = 0.15;
			double objectDifficulty = 0.05;
			double sourceIgnoranceFactor = 0.2;
			boolean appendToFile = false;

			booleanDataSet.generateSyntheticDataSet(numOfSources, numOFObjetcs, numOfProperties, randomFactors, 
					srcErrorFactor, objectDifficulty, sourceIgnoranceFactor, fileName, truthtFileName, appendToFile);
		}

	}

}
