package qcri.dafna.dataModel.dataSet.synthetic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.UniformRandomGenerator;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataSet.ClaimWriter;

public class BooleanDataSetCreator extends AbstractSyntheticDataSetCreator {

	//	BufferedWriter datasetWriterTrueAndFalse;
	//	String fileName_trueAndFalseDataSet;
	//	String truthFileName_trueAndFalseDataSet;
	//	HashMap<String, String> groundTruthTrueAndFalse;

	/**
	 * The percentage of elements set to true in the ground truth from within the whole created objects.
	 */
	private double gtTrutFactor;
	/**
	 * The percentage of elements set to true in the ground truth from within the whole created objects.
	 * @param groundTruthTruePercentage
	 */
	public BooleanDataSetCreator(double groundTruthTrueFactor, String fileName_dataSetTrueAndFalse, String truthFileName_dataSetTrueAndFalse) {
		gtTrutFactor = groundTruthTrueFactor;
		//		fileName_trueAndFalseDataSet = fileName_dataSetTrueAndFalse;
		//		truthFileName_trueAndFalseDataSet = truthFileName_dataSetTrueAndFalse;
	}

	@Override
	void generateGroundTruth() {
		groundTruth = new HashMap<String, String>();
		//		groundTruthTrueAndFalse = new HashMap<String, String>();

		RandomGenerator rg = new JDKRandomGenerator();
		rg.setSeed(Math.round((Math.random() * 1000000)));
		UniformRandomGenerator randomGenerator = new UniformRandomGenerator(rg);
		double r;

		for (String obj : objects) {
			for (String prop : properties) {
				r = normalizeNextRandom(randomGenerator);
				if (r < gtTrutFactor) {
					groundTruth.put(obj + ":" + prop, "True");
					//					groundTruthTrueAndFalse.put(obj + ":" + prop, "True");
				} else {
					//					groundTruthTrueAndFalse.put(obj + ":" + prop, "False");
				}
			}
		}
	}

	@Override
	void writeGroundTruthToFile(boolean append) {
		super.writeGroundTruthToFile(append);

		//		String [] objectProperty;
		//		String line;
		//		BufferedWriter writer;
		//		try {
		//			writer = ClaimWriter.openFile(truthFileName_trueAndFalseDataSet, append);
		//
		//			for (String op : groundTruthTrueAndFalse.keySet()) {
		//				objectProperty = op.split(":");
		//				line = objectProperty[0] + "\t" + objectProperty[1] + "\t" + groundTruthTrueAndFalse.get(op) + "\n";
		//				try {
		//					writer.write(line);
		//				} catch (IOException e) {
		//					System.out.println("Ground Truth is not written");
		//					e.printStackTrace();
		//				}
		//			}
		//			writer.close();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
	}

	@Override
	void generateProperties(int n) {
		this.properties = new ArrayList<String>();
		for (int i = 0 ; i < n ; i ++) {
			properties.add(Globals.syntheticDataSet_BooleanProperty + i);
		}
	}

	@Override
	void generateDataSet(boolean append) {
		try {
			datasetWriter = ClaimWriter.openFile(dataSetFileName + writtenFileNumber + ".txt", append);
			//			datasetWriterTrueAndFalse = ClaimWriter.openFile(fileName_trueAndFalseDataSet + writtenFileNumber + ".txt", append);
		} catch (IOException e) {
			e.printStackTrace();
		}
		RandomGenerator rgSrcIngorance;
		RandomGenerator rgSrcError;
		int fileEntriesCount = 0;
		for (int s = 0; s < sources.size(); s ++) {

			rgSrcIngorance = new JDKRandomGenerator();
			rgSrcIngorance.setSeed(Math.round((Math.random() * 1000000)));
			UniformRandomGenerator randomGeneratorSrcIgnorance = new UniformRandomGenerator(rgSrcIngorance);

			rgSrcError = new JDKRandomGenerator();
			rgSrcError.setSeed(Math.round((Math.random() * 1000000)));
			UniformRandomGenerator randomGeneratorSrcError = new UniformRandomGenerator(rgSrcError);

			for (int o = 0 ; o < objects.size(); o ++) {
				for (int p = 0 ; p < properties.size(); p ++) {
					/* decide whether the source skip this property and not to give his opinion */
					if (normalizeNextRandom(randomGeneratorSrcIgnorance) > srcIgnoranceFactor.get(s)) {
						if (normalizeNextRandom(randomGeneratorSrcError) > sourcesErrorFactor.get(s) * objectDifficultyFactor.get(o)) {
							/* no error */
							if (groundTruth.containsKey(objects.get(o) + ":" + properties.get(p))) {
								// write a claim with True
								if (ClaimWriter.writeClaim(datasetWriter, ++claimId, objects.get(o), properties.get(p), "True", "null", 
										sources.get(s), Globals.delimiterText)) {
									//									ClaimWriter.writeClaim(datasetWriterTrueAndFalse, ++claimId, objects.get(o), properties.get(p), "True", "null", 
									//											sources.get(s), Globals.delimiterText);
									numberOfClaims ++;
									fileEntriesCount ++;
								}
							} else {
								// do nothing for the dataset with only true values
								// insert a false claim with the dataset with true and false values
								//								if (ClaimWriter.writeClaim(datasetWriterTrueAndFalse, ++claimId, objects.get(o), properties.get(p), "False", "null", 
								//										sources.get(s), Globals.delimiterText)) {
								//								numberOfClaims ++;
								//								fileEntriesCount ++;
								//							}
							}
						} else {
							/* Make an error */ 
							if (groundTruth.containsKey(objects.get(o) + ":" + properties.get(p))) {
								// skip for the dataset with only true values
								// insert a false claim with the dataset with true and false values
								//								if (ClaimWriter.writeClaim(datasetWriterTrueAndFalse, ++claimId, objects.get(o), properties.get(p), "False", "null", 
								////										sources.get(s), Globals.delimiterText)) {
								//									numberOfClaims ++;
								//									fileEntriesCount ++;
								//								}
							} else {
								// write true claim 
								if (ClaimWriter.writeClaim(datasetWriter, ++claimId, objects.get(o), properties.get(p), "True", "null", 
										sources.get(s), Globals.delimiterText)) {
									//									ClaimWriter.writeClaim(datasetWriterTrueAndFalse, ++claimId, objects.get(o), properties.get(p), "True", "null", 
									////											sources.get(s), Globals.delimiterText);
									numberOfClaims ++;
									fileEntriesCount ++;
								}
							}
						}
					}
				}
			}
			if (fileEntriesCount > Globals.MaxFileEntriesCount) {
				fileEntriesCount = 0;
				try {
					datasetWriter.flush();
					datasetWriter.close();
					//					datasetWriterTrueAndFalse.close();
					writtenFileNumber ++;
					datasetWriter = ClaimWriter.openFile(dataSetFileName + writtenFileNumber + ".txt", append);
					//					datasetWriterTrueAndFalse = ClaimWriter.openFile(fileName_trueAndFalseDataSet + writtenFileNumber + ".txt", append);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			datasetWriter.flush();
			datasetWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

