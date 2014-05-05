package qcri.dafna.dataModel.dataSet.synthetic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.UniformRandomGenerator;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataSet.ClaimWriter;

public abstract class AbstractSyntheticDataSetCreator {

	List<String> sources;
	List<Double> sourcesErrorFactor;
	List<Double> srcIgnoranceFactor;
	List<String> properties;
	List<String> objects;
	List<Double> objectDifficultyFactor;
	HashMap<String, String> groundTruth;

	String dataSetFileName;
	String truthFileName;
	BufferedWriter datasetWriter;
	BufferedWriter truthWriter;
	int writtenFileNumber = 1;
	int claimId;
	int numberOfClaims;

	public void generateSyntheticDataSet(int numOfSources, int numOfObjects, int numOfPoperties, boolean randomFactors, 
			double srcErrorFactor, double objectDifficulty, double sourceIgnoranceFactor, String filesName, String truthFilesName,
			boolean appendToFile) {
		generateSources(numOfSources, randomFactors, srcErrorFactor, sourceIgnoranceFactor);
		generateObjects(numOfObjects, randomFactors, objectDifficulty);
		generateProperties(numOfPoperties);
		generateGroundTruth();
		

		claimId = Globals.lastClaimID;
		numberOfClaims = 0;
		truthFileName = truthFilesName;
		writeGroundTruthToFile(appendToFile);
		dataSetFileName = filesName;
		
		generateDataSet(appendToFile);
		logNumbers();
	}

	/**
	 * To be override for special cases 
	 */
	void generateGroundTruth() {
		groundTruth = new HashMap<String, String>();
		int i = 0;
		for (String obj : objects) {
			for (String prop : properties) {
				groundTruth.put(obj + ":" + prop, "value" + i);
				i++;
			}
		}
	}

	void writeGroundTruthToFile(boolean append) {
		String [] objectProperty;
		String line;
		try {
			truthWriter = ClaimWriter.openFile(truthFileName, append);
		} catch (IOException e1) {
			System.out.println("Ground Truth is not written");
			e1.printStackTrace();
		}
		for (String op : groundTruth.keySet()) {
			objectProperty = op.split(":");
			line = objectProperty[0] + "\t" + objectProperty[1] + "\t" + groundTruth.get(op) + "\n";
			try {
				truthWriter.write(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			truthWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void generateSources(int n, boolean randomErrorFactor, double errorFactor, double ignorenceFactor) {
		sources = new ArrayList<String>();
		sourcesErrorFactor = new ArrayList<Double>();
		srcIgnoranceFactor = new ArrayList<Double>();

		RandomGenerator rg1 = new JDKRandomGenerator();
		rg1.setSeed(Math.round((Math.random() * 1000000)));
		UniformRandomGenerator randomGenerator1 = new UniformRandomGenerator(rg1);

		RandomGenerator rg2 = new JDKRandomGenerator();
		rg2.setSeed(Math.round((Math.random() * 1000000)));
		UniformRandomGenerator randomGenerator2 = new UniformRandomGenerator(rg2);
		double r;
		for (int i = 0 ; i < n ; i ++) {
			sources.add("Source" + i);
			if (randomErrorFactor) {
				r = normalizeNextRandom(randomGenerator1);
				sourcesErrorFactor.add(r);
				r = normalizeNextRandom(randomGenerator2);
				srcIgnoranceFactor.add(r);
			} else {
				sourcesErrorFactor.add(errorFactor);
				srcIgnoranceFactor.add(ignorenceFactor);
			}
		}
	}

	void generateObjects(int n, boolean randomdifficultyFactor, double difficultyFactor) {
		objects = new ArrayList<String>();
		objectDifficultyFactor = new ArrayList<Double>();
		RandomGenerator rg = new JDKRandomGenerator();
		rg.setSeed(Math.round((Math.random() * 1000000)));
		UniformRandomGenerator randomGenerator = new UniformRandomGenerator(rg);
		for (int i = 0 ; i < n ; i ++) {
			objects.add("object" + i);
			if (randomdifficultyFactor) {
				objectDifficultyFactor.add(normalizeNextRandom(randomGenerator));
			} else {
				objectDifficultyFactor.add(difficultyFactor);
			}
		}
	}

	void generateProperties(int n) {
		properties = new ArrayList<String>();
		for (int i = 0 ; i < n ; i ++) {
			properties.add(Globals.syntheticDataSet_Property + i);
		}
	}

	double normalizeNextRandom(UniformRandomGenerator generator) {
		double random = generator.nextNormalizedDouble() + Math.sqrt(3); /* the nextNormalizedDouble returns a value from -sqrt(3) to +sqrt(3)  */
		random = ((double)random/(2*Math.sqrt(3))); /* now random is from 0 to 1 */
		return random;
	}

	abstract void generateDataSet(boolean append);
	void logNumbers() {
		System.out.println("Number of created claims = " + numberOfClaims);
		System.out.println("Last Claim ID = " + claimId);
	}
}
