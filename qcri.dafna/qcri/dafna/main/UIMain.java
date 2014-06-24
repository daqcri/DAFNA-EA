package qcri.dafna.main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;
import qcri.dafna.experiment.ExperimentDataSetConstructor;
import qcri.dafna.voter.Cosine;
import qcri.dafna.voter.MaximumLikelihoodEstimation;
import qcri.dafna.voter.ThreeEstimates;
import qcri.dafna.voter.TwoEstimates;
import qcri.dafna.voter.SimpleLCA;
import qcri.dafna.voter.GuessLCA;
import qcri.dafna.voter.TruthFinder;
import qcri.dafna.voter.VoterParameters;
import qcri.dafna.voter.dependence.SourceDependenceModel;
import qcri.dafna.voter.latentTruthModel.LatentTruthModel;

public class UIMain {

	public static void main(String[] args) {
		double toleranceFactor = 0.01; // 0.1 max, 0 min
		String dataSetDirectory = args[1];
		String groundTruthDir = args[2];
		String outputPath = args[3];
		String delim = ",";
		DataSet ds = ExperimentDataSetConstructor.readDataSet(
				dataSetDirectory, toleranceFactor, groundTruthDir, outputPath, delim);

		// for all voters
		double cosineSimDiff = Double.parseDouble(args[4]);  // 0-1
		double startingTrust = Double.parseDouble(args[5]);  // 0-1
		double startingConf = Double.parseDouble(args[6]);  // 0-1 
		double startingErrorFactor = Double.parseDouble(args[7]);  // 0-1
		VoterParameters params = new VoterParameters(cosineSimDiff, startingTrust, startingConf, startingErrorFactor);

		// specific params
		VoterQualityMeasures q = null;
		boolean convergence100 = false;
		boolean profileMemory = false;

		String algo_name = args[0];
		switch(algo_name){
		case "Cosine":
			double dampeningFactorCosine = Double.parseDouble(args[8]); // 0-1
			startingConf = Double.parseDouble(args[9]);
			params = new VoterParameters(cosineSimDiff, startingTrust, startingConf,startingErrorFactor);
			Cosine algo1 = new Cosine(ds, params, dampeningFactorCosine);	
			q = algo1.launchVoter(convergence100 , profileMemory);

			break;
		case "2-Estimates":
			double normalizationWeight = Double.parseDouble(args[8]);
			TwoEstimates algo2 = new TwoEstimates(ds, params,normalizationWeight );
			q = algo2.launchVoter(convergence100 , profileMemory);
			break;
		case "3-Estimates":
			double ThreeNormalizationWeight = Double.parseDouble(args[8]);
			startingErrorFactor = Double.parseDouble(args[9]);
			params = new VoterParameters(cosineSimDiff, startingTrust, startingConf,startingErrorFactor);
			ThreeEstimates algo3 = new ThreeEstimates(ds, params, ThreeNormalizationWeight);
			q = algo3.launchVoter(convergence100 , profileMemory);
			break;
		case "Depen":
		case "Accu":
		case "AccuSim":
		case "AccuNoDep":
			double alfa = Double.parseDouble(args[8]);
			double c = Double.parseDouble(args[9]);
			int n = Integer.parseInt(args[10]);
			double similarityConstant = Double.parseDouble(args[11]);
			boolean considerSimilarity = args[12].equals("true");
			boolean considerSourcesAccuracy = args[13].equals("true");
			boolean considerDependency = args[14].equals("true");
			boolean orderSrcByDependence = args[15].equals("true");
			SourceDependenceModel algo4 = new SourceDependenceModel(ds, params, alfa, c, n, similarityConstant, considerSimilarity, considerSourcesAccuracy, considerDependency, orderSrcByDependence);
			q = algo4.launchVoter(convergence100, profileMemory);
			break;
		case "TruthFinder":
			double similarityConstantTF = Double.parseDouble(args[8]); // 0-1
			double dampeningFactorTF = Double.parseDouble(args[9]); // 0-1
			TruthFinder algo5 = new TruthFinder(ds, params, similarityConstantTF, dampeningFactorTF);
			q = algo5.launchVoter(convergence100, profileMemory);
			break;
		case "SimpleLCA":
			double Simplebeta1LCA = Double.parseDouble(args[8]);
			SimpleLCA algo6 = new SimpleLCA(ds, params, Simplebeta1LCA);
			q = algo6.launchVoter(convergence100, profileMemory);
			break;
		case "GuessLCA":
			double beta1LCA = Double.parseDouble(args[8]);
			GuessLCA algo7 = new GuessLCA(ds, params, beta1LCA);
			q = algo7.launchVoter(convergence100, profileMemory);
			break;
		case "MLE":
			double beta1MLE = Double.parseDouble(args[8]);
			double rMLE = Double.parseDouble(args[9]);
			MaximumLikelihoodEstimation algo8 = new MaximumLikelihoodEstimation(ds, params, beta1MLE, rMLE );
			q = algo8.launchVoter(convergence100, profileMemory);
			break;
		case "LTM":
			double b1 = Double.parseDouble(args[8]);
			double b0 = Double.parseDouble(args[9]);
			double a00 = Double.parseDouble(args[10]);
			double a01 = Double.parseDouble(args[11]);
			double a10 = Double.parseDouble(args[12]);
			double a11 = Double.parseDouble(args[13]);
			int iterationCount = Integer.parseInt(args[14]);
			int burnIn = Integer.parseInt(args[15]);
			int sampleGap = Integer.parseInt(args[16]);
			LatentTruthModel algo9 = new LatentTruthModel(ds, params, b1, b0, a00, a01,a10, a11, iterationCount, burnIn, sampleGap);
			q = algo9.launchVoter(convergence100, profileMemory);
			break;
		}

		String trustworthinessResultsFile = outputPath + System.getProperty("file.separator") + "Trustworthiness.csv";
		BufferedWriter trustworthinessWriter;
		try {
			trustworthinessWriter = Files.newBufferedWriter(Paths.get(trustworthinessResultsFile), 
					Globals.FILE_ENCODING, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
			/* add header */
			writeTrustworthiness(trustworthinessWriter, "sourceId", "trustworthiness", ',');
			HashMap<String, Source> map = ds.getSourcesHash();
			System.out.println("Sources\n\n");
			for(String key: map.keySet()){
				System.out.println(key + "\t" +  map.get(key).getTrustworthiness());
				writeTrustworthiness(trustworthinessWriter, key, String.valueOf(map.get(key).getTrustworthiness()), ',');
			}
		} catch (IOException e) {
			System.out.println("Cannot write the trustworthiness results");
			e.printStackTrace();
		}

		String confidenceResultFile = outputPath + System.getProperty("file.separator") + "Confidences.csv";
		BufferedWriter confidenceWriter;
		try {
			confidenceWriter = Files.newBufferedWriter(Paths.get(confidenceResultFile), 
					Globals.FILE_ENCODING, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

			/*header*/
			writeConfidenceResult(confidenceWriter, "claimId", "confidence", "trueOrFalse", ',');
			System.out.println("\nClaims");
			for (List<ValueBucket> bList : ds.getDataItemsBuckets().values()) {
				for (ValueBucket b : bList) {
					for (SourceClaim claim :  b.getClaims()) {
						System.out.println(claim.getId() + "\t" + b.getConfidence() + "\t" + claim.isTrueClaimByVoter());
						writeConfidenceResult(confidenceWriter, String.valueOf(claim.getId()), 
								String.valueOf(b.getConfidence()), String.valueOf(claim.isTrueClaimByVoter()), ',');
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Cannot write the confidence results");
			e.printStackTrace();
		}
		System.out.println("Finished");
	}

	private static boolean writeConfidenceResult(BufferedWriter writer, String claimId,	String confidence, String trueOrFalse, char dlim) {
		CSVWriter csvWriter = new CSVWriter(writer, dlim);
		String [] lineComponents = new String[]{claimId, confidence, trueOrFalse};
		csvWriter.writeNext(lineComponents);
		return true;
	}

	private static boolean writeTrustworthiness(BufferedWriter writer, String sourceId,	String trustworthiness, char dlim) {
		CSVWriter csvWriter = new CSVWriter(writer, dlim);
		String [] lineComponents = new String[]{sourceId, trustworthiness};
		csvWriter.writeNext(lineComponents);
		return true;
	}


}
