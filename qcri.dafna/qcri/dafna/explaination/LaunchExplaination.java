package qcri.dafna.explaination;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import qcri.dafna.allegation.Allegate;
import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;
import qcri.dafna.experiment.ExperimentDataSetConstructor;
import qcri.dafna.voter.VoterParameters;
import au.com.bytecode.opencsv.CSVWriter;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.featureselection.ranking.RecursiveFeatureEliminationSVM;
import net.sf.javaml.featureselection.scoring.RELIEF;
import net.sf.javaml.tools.data.FileHandler;

public class LaunchExplaination {
	public static void main(String args[])
	{
		launchAllegator();
	}

	public static void launchAllegator()
	{
		double toleranceFactor = 0.01; // 0.1 max, 0 min
		String dataSetDirectory = Globals.directory_formattedDAFNADataset_Books_Folder + "/claims";
		String groundTruthDir = Globals.directory_formattedDAFNADataset_Books_Folder + "/truth";
		String outputPath = Globals.directory_formattedDAFNADataset_Books_Folder + "/experimentResult";
		String delim = ",";
		DataSet ds = ExperimentDataSetConstructor.readDataSet(dataSetDirectory, toleranceFactor, groundTruthDir, outputPath, delim);
		
		VoterQualityMeasures q = null;
		boolean convergence100 = false;
		boolean profileMemory = false;
		double cosineSimDiff = 0.001;  // 0-1
		double startingTrust = 0.8;  // 0-1
		double startingConf = 1;  // 0-1 
		double startingErrorFactor = 0.4;  // 0-1
		VoterParameters params = new VoterParameters(cosineSimDiff, startingTrust, startingConf, startingErrorFactor);
		String[] algoParams = new String[2];
		algoParams[0] = "0.5";
		algoParams[1] = "0.1";
		
		String confidenceFilePath = "/home/dalia/Desktop/Backups/results/4/Confidences.csv";
		String trustWorthinessFilePath = "/home/dalia/Desktop/Backups/results/4/Trustworthiness.csv";
		//String claimID = "24326";
		//Allegate algo5 = new Allegate(ds, params, "Truth Finder", algoParams, claimID, confidenceFilePath, trustWorthinessFilePath);
		//q = algo5.launchVoter(convergence100, profileMemory);
		
		for(int i = 24432; i<26436; i++){
			ds = ExperimentDataSetConstructor.readDataSet(dataSetDirectory, toleranceFactor, groundTruthDir, outputPath, delim);
			String claimID = String.valueOf(i);
			MetricsGenerator algo5 = new MetricsGenerator(ds, params, claimID, confidenceFilePath, trustWorthinessFilePath);
			q = algo5.launchVoter(convergence100, profileMemory);
		}
		
		try {
			Dataset data = FileHandler.loadDataset(new File("/home/dalia/Desktop/explaination.data"),11, ",");
			RELIEF reliefF = new RELIEF();
			reliefF.build(data);
			for (int i = 0; i < reliefF.noAttributes(); i++)
	            System.out.println(reliefF.score(i));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    			
		//System.out.println(q.getPrecision());
		//System.out.println(q.getRecall());
		//System.out.println(q.getAccuracy());
		//System.out.println(q.getSpecificity());
		
		String confidenceResultFile = outputPath + System.getProperty("file.separator") + "Confidences.csv";
		BufferedWriter confidenceWriter;
		try {
			confidenceWriter = Files.newBufferedWriter(Paths.get(confidenceResultFile), 
					Globals.FILE_ENCODING, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			CSVWriter csvWriter = new CSVWriter(confidenceWriter, ',');

			/*header*/
			writeConfidenceResult(csvWriter, "ClaimID", "Confidence", "IsTrue", "BucketId");
		for (List<ValueBucket> bList : ds.getDataItemsBuckets().values()) {
			for (ValueBucket b : bList) {
				for (SourceClaim claim :  b.getClaims()) {
					writeConfidenceResult(csvWriter, String.valueOf(claim.getId()), 
							String.valueOf(b.getConfidence()), String.valueOf(claim.isTrueClaimByVoter()), String.valueOf(b.getId()));
				}
			}
		}
		confidenceWriter.close();
		} catch (IOException e) {
			System.out.println("Cannot write the confidence results");
			e.printStackTrace();
		}
	}
	
	private static void writeConfidenceResult(CSVWriter writer, String claimId,	String confidence, String trueOrFalse, String bucketValue) {
		String [] lineComponents = new String[]{claimId, confidence, trueOrFalse, bucketValue};
		writer.writeNext(lineComponents);
	}
}
