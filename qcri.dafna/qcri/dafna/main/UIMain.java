package qcri.dafna.main;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.quality.dataQuality.DataQualityMeasurments;
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
import qcri.dafna.voter.latentTruthModel.LatentTruthModel;

public class UIMain {

	public static void main(String[] args) {
		double toleranceFactor = 0.01; // 0.1 max, 0 min
		String dataSetDirectory = args[1];
		String groundTruthDir = args[2];
		String outputPath = args[3];
		String delim = ",";
		// TODO Auto-generated method stub
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
			Cosine algo1 = new Cosine(ds, params, dampeningFactorCosine);	
			q = algo1.launchVoter(convergence100 , profileMemory);
			
			break;
		case "2-Estimates":
			double normalizationWeight = Double.parseDouble(args[9]);
			TwoEstimates algo2 = new TwoEstimates(ds, params,normalizationWeight );
			q = algo2.launchVoter(convergence100 , profileMemory);
			break;
		case "3-Estimates":
			double ThreeNormalizationWeight = Double.parseDouble(args[10]);
			ThreeEstimates algo3 = new ThreeEstimates(ds, params, ThreeNormalizationWeight);
			q = algo3.launchVoter(convergence100 , profileMemory);
			break;
		case "Depen":
			break;
		case "Accu":
			break;
		case "AccuSim":
			break;
		case "AccuNoDep":
			break;
		case "TruthFinder":
			double similarityConstantTF = Double.parseDouble(args[11]); // 0-1
			double dampeningFactorTF = Double.parseDouble(args[12]); // 0-1
			TruthFinder algo8 = new TruthFinder(ds, params, similarityConstantTF, dampeningFactorTF);
			q = algo8.launchVoter(convergence100, profileMemory);
			break;
		case "SimpleLCA":
			double Simplebeta1LCA = Double.parseDouble(args[13]);
			SimpleLCA algo9 = new SimpleLCA(ds, params, Simplebeta1LCA);
			q = algo9.launchVoter(convergence100, profileMemory);
			break;
		case "GuessLCA":
			double beta1LCA = Double.parseDouble(args[14]);
			GuessLCA algo10 = new GuessLCA(ds, params, beta1LCA);
			q = algo10.launchVoter(convergence100, profileMemory);
			break;
		case "MLE":
			double beta1MLE = Double.parseDouble(args[15]);
			double rMLE = Double.parseDouble(args[16]);
			MaximumLikelihoodEstimation algo11 = new MaximumLikelihoodEstimation(ds, params, beta1MLE, rMLE );
			q = algo11.launchVoter(convergence100, profileMemory);
			break;
		case "LTM":
			double b1 = Double.parseDouble(args[17]);
			double b0 = Double.parseDouble(args[18]);
			double a00 = Double.parseDouble(args[19]);
			double a01 = Double.parseDouble(args[20]);
			double a10 = Double.parseDouble(args[21]);
			double a11 = Double.parseDouble(args[22]);
			int iterationCount = Integer.parseInt(args[23]);
			int burnIn = Integer.parseInt(args[24]);
			int sampleGap = Integer.parseInt(args[25]);
			LatentTruthModel algo12 = new LatentTruthModel(ds, params, b1, b0, a00, a01,a10, a11, iterationCount, burnIn, sampleGap);
			q = algo12.launchVoter(convergence100, profileMemory);
			break;
		}
		System.out.println(q.getAccuracy());
		System.out.println(q.getRecall());
		System.out.println(q.getPrecision());
					
	}

}
