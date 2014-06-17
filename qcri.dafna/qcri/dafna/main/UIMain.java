package qcri.dafna.main;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.quality.dataQuality.DataQualityMeasurments;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;
import qcri.dafna.experiment.ExperimentDataSetConstructor;
import qcri.dafna.voter.Cosine;
import qcri.dafna.voter.TruthFinder;
import qcri.dafna.voter.VoterParameters;

public class UIMain {

	public static void main(String[] args) {
		double toleranceFactor = 0.01; // 0.1 max, 0 min
		String dataSetDirectory = "data/Books_CSV/claims";
		String groundTruthDir = "data/Books_CSV/truth";
		String outputPath = "data/Books_CSV/output";
		String delim = ",";
		// TODO Auto-generated method stub
		DataSet ds = ExperimentDataSetConstructor.readDataSet(
				dataSetDirectory, toleranceFactor, groundTruthDir, outputPath, delim);
		// for all voters
		double cosineSimDiff = 0.001; // 0-1
		double startingTrust = 0.8; // 0-1
		double startingConf = 1; // 0-1
		double startingErrorFactor = 0.1; // 0-1
		VoterParameters params = new VoterParameters(cosineSimDiff, startingTrust, startingConf, startingErrorFactor);
		
		// specific params
		double dampeningFactorCosine = 0.2; // 0-1
		Cosine algo1 = new Cosine(ds, params, dampeningFactorCosine);
		
		boolean convergence100 = false;
		boolean profileMemory = false;
		VoterQualityMeasures q = algo1.launchVoter(convergence100 , profileMemory);
		System.out.println(q.getAccuracy());
		System.out.println(q.getRecall());
		System.out.println(q.getPrecision());
		
		double similarityConstantTF = 0.5; // 0-1
		double dampeningFactorTF = 0.2; // 0-1
		TruthFinder algo2 = new TruthFinder(ds, params, similarityConstantTF, dampeningFactorTF);
		q = algo2.launchVoter(convergence100, profileMemory);
		System.out.println(q.getAccuracy());
		System.out.println(q.getRecall());
		System.out.println(q.getPrecision());
		
	}

}
