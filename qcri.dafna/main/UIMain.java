package main;

import java.util.HashMap;
import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.quality.dataQuality.DataQualityMeasurments;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;
import qcri.dafna.experiment.ExperimentDataSetConstructor;
import qcri.dafna.experiment.ExperimentDataSetConstructor_Development;
import qcri.dafna.experiment.ExperimentDataSetConstructor_Development.Experiment;
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
//		DataSet ds = ExperimentDataSetConstructor.readDataSet(
//				dataSetDirectory, toleranceFactor, groundTruthDir, outputPath, delim);
		
		DataSet ds = ExperimentDataSetConstructor_Development.readDataSet(
				Globals.starting_Confidence, Globals.starting_trustworthiness, 
				Globals.directory_formattedDAFNADataset_Books_Claims_Folder, 0, true, ValueType.ISBN, Experiment.Books, null, false);

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
		


//	       HashMap<String, Source> map = ds.getSourcesHash();
//	       System.out.println("Sources\n\n");
//	       for(String key: map.keySet()){
//	    	   System.out.println(key + "\t" +  map.get(key).getTrustworthiness());
//	       }
	       
//	       System.out.println("Value buckets\n\n");
//	       HashMap<String, List<ValueBucket>> map1 = ds.getDataItemsBuckets();     
//	       for(String key: map1.keySet()){
//	    	   List<ValueBucket> list = map1.get(key);
//	    	   for(ValueBucket bucket: list)
//	    		   System.out.println(key + "\t" + bucket.getConfidence());
//	    	   System.out.println();
//	       }

	       System.out.println("Source claims\n\n");
	       HashMap<String, List<SourceClaim>> map2 = ds.getDataItemClaims();
	       String bucketValue;
	       for(String key: map2.keySet()){
	    	   List<SourceClaim> list = map2.get(key);
	    	   for(SourceClaim claim: list) {
	    		   if (DataTypeMatcher.savedAsString(claim.getValueType())) {
	    			   bucketValue = claim.getBucket().getCleanedString();
	    		   } else {
	    			   bucketValue = String.valueOf(claim.getBucket().getMinValue());
	    		   }
	    		   System.out.println(key + "\t" + claim.getPropertyName() + "\t" + claim.getPropertyValueString() 
	    				   + "\t" + claim.isTrueClaimByVoter()+ "\t" + bucketValue);
	    	   }
	    	   System.out.println();
	       }

	       System.out.println("Finished");
	}

}
