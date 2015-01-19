package qcri.dafna.allegation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import qcri.dafna.dataModel.data.ConfValueLabel;
import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataSet.CSVDatasetReader.ConfidenceReader;
import qcri.dafna.dataModel.dataSet.CSVDatasetReader.TrustWorthinessReader;
import qcri.dafna.voter.Voter;

public class Allegator {
	
	// All Experimental Code is in Allegate.java - including ideas that didn't work !!
	
	private DataSet dataSet;
	private Voter algo;
	private String claimID;
	private int fakeSourceCount;
	
	public Allegator(DataSet ds, Voter algo, String claimID){
		this.dataSet = ds;
		this.algo =  algo;
		this.claimID = claimID;
		this.fakeSourceCount =  0;
	}
	
	public int Allegate(String confidenceFilePath, String trustFilePath){
		
		HashMap<Integer, ConfValueLabel> conf;
		HashMap<String, Double> trust;
		ConfidenceReader confidenceReader = new ConfidenceReader();
		TrustWorthinessReader trustWorthinessReader = new TrustWorthinessReader();
		conf = confidenceReader.readConfidenceFile(confidenceFilePath, ',');
		trust = trustWorthinessReader.readTrustFile(trustFilePath, ',');
		intializeWithResults(conf, trust);
		
		int iterationCount = 0;	
		int claimIdMax = (int) Collections.max(conf.keySet());
		
		String targetObjectId = "";
		String targetPropertyName = "";
		String targetDataItemKey = "";
		
		LOOP:
		while(iterationCount < 50)
		{
			for (String DIKey : dataSet.getDataItemsBuckets().keySet()){
					for (ValueBucket b : dataSet.getDataItemsBuckets().get(DIKey)) {
							for (SourceClaim claim : b.getClaims()) {
								if(claim.isTrueClaimByVoter() == false && Integer.valueOf(claimID) == claim.getId()){
									// Already False or has been falsified
									break LOOP;
								}
								else if(iterationCount == 0 && claim.isTrueClaimByVoter() == true && Integer.valueOf(claimID) == claim.getId()){
									targetObjectId = claim.getObjectIdentifier();
									targetPropertyName = claim.getPropertyName();
									targetDataItemKey = DIKey;
								}
							}
					}
			}
			if(targetDataItemKey == ""){
				// Claim not found
				break LOOP;
			}
			
			//add new claims
		    int claimsAdded = addNewClaim(claimIdMax, targetObjectId, targetPropertyName, targetDataItemKey, trust);
		    if(claimsAdded == -1) return 0;
		    iterationCount++;
		    claimIdMax = claimIdMax + claimsAdded;
		    
		    // Run the algorithm again to update Truth Labels after introduction new claims
		    algo.launchVoter(false, false);	    
		}
		if(iterationCount == 50){
			return 0;
		}
		else
			return fakeSourceCount;
	}

	public int addNewClaim(int claimIdMax, String targetObjectId, String targetPropertyName, String targetDataItemKey, HashMap<String, Double> trust){
		int claimsAdded = 0; // For now, only one claim is introduced per call
		String newSourceName = Globals.fakeSourceName + String.valueOf(fakeSourceCount);
		double max = -1.0 * Double.MAX_VALUE;
		ValueBucket secondMax = null;
			
		// Second Max is selected based on number of conflicts
		for (ValueBucket b : dataSet.getDataItemsBuckets().get(targetDataItemKey)) {
			if ( ! b.getClaims().get(0).isTrueClaimByVoter() && dataSet.getDataItemsBuckets().get(targetDataItemKey).size() != 1) {
				if (max < b.getClaims().size()) {
					max = b.getClaims().size();
					secondMax = b;
				}
			}
		}
		
		if(secondMax == null) 
		{
			return -1;
		}
		
		SourceClaim newClaim = dataSet.addClaim(claimIdMax+1, targetObjectId, dataSet.getDataItemsBuckets().get(targetDataItemKey).get(0).getClaims().get(0).getUncleanedObjectIdentifier(), targetPropertyName, secondMax.getClaims().get(0).getPropertyValueString() , Globals.weight, "Now", newSourceName);
		dataSet.addClaimToBucket(newClaim, secondMax);
		claimsAdded = claimsAdded + 1;
		fakeSourceCount++;
		return claimsAdded;
	}
	
	public void intializeWithResults(HashMap<Integer, ConfValueLabel> conf, HashMap<String, Double> trust){
		for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : bucketsList) {
				int claim_id = b.getClaims().get(0).getId();
				double confidence = conf.get(claim_id).getConfidenceValue();
					b.setConfidence(confidence);
					for (SourceClaim claim : b.getClaims()){
						claim.setTrueClaimByVoter(conf.get(claim.getId()).getLabel());
					}
			}
		}
		for(Source source : dataSet.getSourcesHash().values()){
			try
			{
				double source_trust = trust.get(source.getSourceIdentifier());
				source.setTrustworthiness(source_trust);
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}
	}
}
