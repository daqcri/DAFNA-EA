package qcri.dafna.allegation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.voter.Cosine;
import qcri.dafna.voter.GuessLCA;
import qcri.dafna.voter.MaximumLikelihoodEstimation;
import qcri.dafna.voter.SimpleLCA;
import qcri.dafna.voter.ThreeEstimates;
import qcri.dafna.voter.TruthFinder;
import qcri.dafna.voter.TwoEstimates;
import qcri.dafna.voter.Voter;
import qcri.dafna.voter.VoterParameters;
import qcri.dafna.voter.dependence.SourceDependenceModel;
import qcri.dafna.voter.latentTruthModel.LatentTruthModel;
import qcri.dafna.dataModel.data.ConfValueLabel;
import qcri.dafna.dataModel.dataSet.CSVDatasetReader.ConfidenceReader;
import qcri.dafna.dataModel.dataSet.CSVDatasetReader.TrustWorthinessReader;

public class Allegate extends Voter {
	
	private String[] algoParams;
	private String claimID;
	private String confidenceFilePath;
	private String trustWorthinessFilePath;
	private List<DataItemAllegatorObject> orderedDataItems;
	private VoterParameters params;
	private List<String> sourcesToBeMimicked; // TODO Remove this variable if old idea completely discarded - without breaking nbSS.
	private int fakeSourceCount;
	private int nbSS; // Don't remove any unused variables. They give important information for experiments - currently commented.
	private int nbC;
	private ValueBucket secondMaxBucket;
	private int nbFS;
	private String algoName;
	private double startingConfidenceForNewBucket;
	private String uncleanedObjectId;
	
	public Allegate(DataSet dataSet, VoterParameters params, String[] algoParams, String claimID, String confidenceFilePath, String trustWorthinessFilePath)
	{
		super(dataSet, params);
		this.algoName = algoParams[0]; // Name of the algorithm
		this.algoParams = algoParams; // and its parameters
		this.claimID = claimID; // Claim which is to be falsified.
		this.confidenceFilePath = confidenceFilePath; // Results as shown to the user
		this.trustWorthinessFilePath = trustWorthinessFilePath; // Results as shown to the user
		this.orderedDataItems = new ArrayList<DataItemAllegatorObject>(); // Dalia's implementation to arrange resulting dataset in an ordered fashion
		this.params = params; // Voter Parameters (General Ones)
		this.sourcesToBeMimicked = new ArrayList<String>();
		this.fakeSourceCount =  0;
		this.nbSS = 0;
		this.nbC = 0;
		this.secondMaxBucket =  null;
		this.nbFS = 0;
		this.startingConfidenceForNewBucket =  Double.valueOf(algoParams[6]);
	}
	
	protected void initParameters() {
		singlePropertyValue = false;
		onlyMaxValueIsTrue = false;
	}
	
	protected int runVoter(boolean convergence100) {
		HashMap<Integer, ConfValueLabel> conf;
		HashMap<String, Double> trust;
		int iterationCount = 0;
		
		ConfidenceReader confidenceReader = new ConfidenceReader();
		TrustWorthinessReader trustWorthinessReader = new TrustWorthinessReader();
		
		conf = confidenceReader.readConfidenceFile(confidenceFilePath, ',');
		trust = trustWorthinessReader.readTrustFile(trustWorthinessFilePath, ',');
		
		int claimIdMax = (int) Collections.max(conf.keySet());
		
		intializeWithResults(conf, trust);
		initDIAllegatorObjects();
		String targetObjectId = "";
		String targetPropertyName = "";
		String targetDataItemKey = "";
		String targetSource = "";
		ValueBucket targetBucket = null;
		
		String originalTrust = "";
		String changedTrust = "";
		
		String originalConfidence = "";
		String changedConfidence = "";
		
		String secondMaxOriginalConfidence = "";
		
		LOOP:
		while(iterationCount < 50)
		{
			for (String DIKey : dataSet.getDataItemsBuckets().keySet()){
					for (ValueBucket b : dataSet.getDataItemsBuckets().get(DIKey)) {
							for (SourceClaim claim : b.getClaims()) {
								if(claim.isTrueClaimByVoter() == false && Integer.valueOf(claimID) == claim.getId()){
									//Conditional Print 1 - Commented
									//System.out.println(claim.getId()+" has been falsified or is already false!!");
									//changedConfidence = String.valueOf(b.getConfidence());
									String fakeSourceTrust = "";
									for(Source source : dataSet.getSourcesHash().values()){
										//for(int i = 0; i <= fakeSourceCount; i++){
											//if(source.getSourceIdentifier().equals(Globals.fakeSourceName+String.valueOf(i)))
												//fakeSourceTrust = fakeSourceTrust+","+String.valueOf(source.getTrustworthiness());
											if(source.getSourceIdentifier().equals(Globals.fakeSourceName+String.valueOf(0))) // All fake sources maintain same Ts
												fakeSourceTrust = String.valueOf(source.getTrustworthiness());
											if(source.getSourceIdentifier().equals(targetSource)){
												changedTrust = String.valueOf(source.getTrustworthiness());
											}
										//}
									}
									/*
									if(iterationCount > 0 && secondMaxBucket != null)
									System.out.println(claimID+","+nbSS+","+nbC+","+nbFS+","+originalTrust+","+changedTrust+","+fakeSourceCount+","+fakeSourceTrust+","+originalConfidence+","+changedConfidence+","+secondMaxOriginalConfidence+","+secondMaxBucket.getConfidence());
									*/
									break LOOP;
								}
								else if(claim.isTrueClaimByVoter() == true && Integer.valueOf(claimID) == claim.getId() && iterationCount == 0){
									targetObjectId = claim.getObjectIdentifier();
									targetPropertyName = claim.getPropertyName();
									targetDataItemKey = DIKey;
									targetSource = claim.getSource().getSourceIdentifier();
									targetBucket = b;
								}
							}
					}
			}
			if(targetDataItemKey == ""){
				//Conditional Print 2 - Commented
				//System.out.println("Claim Not Found");
				break LOOP;
			}
			
			if(originalTrust == ""){
				originalTrust = String.valueOf(trust.get(targetSource));
			}
			
			if(originalConfidence == "" && targetBucket != null){
				originalConfidence = String.valueOf(targetBucket.getConfidence());
			}
			
			//add new claims
		    int claimsAdded = addNewClaimIterationBYTrust(iterationCount, claimIdMax, targetObjectId, targetPropertyName, targetDataItemKey, trust);
		    
		    if(claimsAdded == -1) return 0;
		    
		    iterationCount++;
			
		    claimIdMax = claimIdMax + claimsAdded;
		    
		    //run experiment
		    boolean profileMemory = false;
		    
		    double cosineSimDiff = Double.parseDouble(algoParams[4]);  // 0-1
			double startingTrust = Double.parseDouble(algoParams[5]);  // 0-1
			double startingConf = Double.parseDouble(algoParams[6]);  // 0-1 
			double startingErrorFactor = Double.parseDouble(algoParams[7]);  // 0-1
			switch(algoName){
			case "Cosine":
				double dampeningFactorCosine = Double.parseDouble(algoParams[8]); // 0-1
				startingConf = Double.parseDouble(algoParams[9]);
				params = new VoterParameters(cosineSimDiff, startingTrust, startingConf,startingErrorFactor);
				Cosine algo1 = new Cosine(dataSet, params, dampeningFactorCosine);
				algo1.launchVoter(convergence100 , profileMemory);
				break;
			case "2-Estimates":
				double normalizationWeight = Double.parseDouble(algoParams[8]);
				TwoEstimates algo2 = new TwoEstimates(dataSet, params,normalizationWeight );
				algo2.launchVoter(convergence100 , profileMemory);
				break;
			case "3-Estimates":
				double ThreeNormalizationWeight = Double.parseDouble(algoParams[8]);
				startingErrorFactor = Double.parseDouble(algoParams[9]);
				params = new VoterParameters(cosineSimDiff, startingTrust, startingConf,startingErrorFactor);
				ThreeEstimates algo3 = new ThreeEstimates(dataSet, params, ThreeNormalizationWeight);
				algo3.launchVoter(convergence100 , profileMemory);
				break;
			case "Depen":
			case "Accu":
			case "AccuSim":
			case "AccuNoDep":
				double alfa = Double.parseDouble(algoParams[8]);
				double c = Double.parseDouble(algoParams[9]);
				int n = Integer.parseInt(algoParams[10]);
				double similarityConstant = Double.parseDouble(algoParams[11]);
				boolean considerSimilarity = algoParams[12].equals("true");
				boolean considerSourcesAccuracy = algoParams[13].equals("true");
				boolean considerDependency = algoParams[14].equals("true");
				boolean orderSrcByDependence = algoParams[15].equals("true");
				SourceDependenceModel algo4 = new SourceDependenceModel(dataSet, params, alfa, c, n, similarityConstant, considerSimilarity, considerSourcesAccuracy, considerDependency, orderSrcByDependence);
				algo4.launchVoter(convergence100, profileMemory);
				break;
			case "TruthFinder":
				double similarityConstantTF = Double.parseDouble(algoParams[8]); // 0-1
				double dampeningFactorTF = Double.parseDouble(algoParams[9]); // 0-1
				TruthFinder algo5 = new TruthFinder(dataSet, params, similarityConstantTF, dampeningFactorTF);
				algo5.launchVoter(convergence100, profileMemory);
				break;
			case "SimpleLCA":
				double Simplebeta1LCA = Double.parseDouble(algoParams[8]);
				SimpleLCA algo6 = new SimpleLCA(dataSet, params, Simplebeta1LCA);
				algo6.launchVoter(convergence100, profileMemory);
				break;
			case "GuessLCA":
				double beta1LCA = Double.parseDouble(algoParams[8]);
				GuessLCA algo7 = new GuessLCA(dataSet, params, beta1LCA);
				algo7.launchVoter(convergence100, profileMemory);
				break;
			case "MLE":
				double beta1MLE = Double.parseDouble(algoParams[8]);
				double rMLE = Double.parseDouble(algoParams[9]);
				MaximumLikelihoodEstimation algo8 = new MaximumLikelihoodEstimation(dataSet, params, beta1MLE, rMLE );
				algo8.launchVoter(convergence100, profileMemory);
				break;
			case "LTM":
				double b1 = Double.parseDouble(algoParams[8]);
				double b0 = Double.parseDouble(algoParams[9]);
				double a00 = Double.parseDouble(algoParams[10]);
				double a01 = Double.parseDouble(algoParams[11]);
				double a10 = Double.parseDouble(algoParams[12]);
				double a11 = Double.parseDouble(algoParams[13]);
				int iterationCountLTM = Integer.parseInt(algoParams[14]);
				int burnIn = Integer.parseInt(algoParams[15]);
				int sampleGap = Integer.parseInt(algoParams[16]);
				LatentTruthModel algo9 = new LatentTruthModel(dataSet, params, b1, b0, a00, a01,a10, a11, iterationCountLTM, burnIn, sampleGap);
				algo9.launchVoter(convergence100, profileMemory);
				break;
			default:
				throw new RuntimeException("Unknown algorithm specified '" + algoName + "'");
			}
			
			if(targetBucket != null){
					changedConfidence = String.valueOf(targetBucket.getConfidence());
				}
			if(secondMaxOriginalConfidence == "" && secondMaxBucket != null)
				secondMaxOriginalConfidence = String.valueOf(secondMaxBucket.getConfidence());
		}
		if(iterationCount == 50)
			return -1;
		else
			return 1;
	}
	
	//TODO Name of the function needs to be changed - Trust Idea didn't work
	public int addNewClaimIterationBYTrust(int iterationCount, int claimIdMax, String targetObjectId, String targetPropertyName, String targetDataItemKey, HashMap<String, Double> trust){
		
		int claimsAdded = 0;
		
		String newSourceName = Globals.fakeSourceName + String.valueOf(fakeSourceCount);
		
		double max = -1.0 * Double.MAX_VALUE;
		ValueBucket secondMax = null;
		String fakeValue = "";
		
		// When Second Max is selected based on Confidence Value
		/*
		for (ValueBucket b : dataSet.getDataItemsBuckets().get(targetDataItemKey)) {
			if ( ! b.getClaims().get(0).isTrueClaimByVoter() && dataSet.getDataItemsBuckets().get(targetDataItemKey).size() != 1) {
				if (max < b.getConfidence() || Double.isInfinite(b.getConfidence())) {
					max = b.getConfidence();
					secondMax = b;
				}
			}
		}
		*/
		
		// When second Max is selected based on number of conflicts
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
			//Conditional Print 3 - Commented
			//secondMax = new ValueBucket(startingConfidenceForNewBucket , true);
			//fakeValue = "Fake Author Name";
			//System.out.println("Not Yet Implemented");
			return -1;
		}
		else{
			fakeValue = secondMax.getClaims().get(0).getPropertyValueString();
		}
		
		uncleanedObjectId = dataSet.getDataItemsBuckets().get(targetDataItemKey).get(0).getClaims().get(0).getUncleanedObjectIdentifier();
		
		secondMaxBucket = secondMax;
		
		if(iterationCount == 0)
		nbFS = secondMax.getClaims().size();
		
		if(iterationCount == 0)
		{
			for (ValueBucket b : dataSet.getDataItemsBuckets().get(targetDataItemKey)) {
				if ( b.getClaims().get(0).isTrueClaimByVoter()){
						//for (SourceClaim claim : secondMax.getClaims()) {
						for (SourceClaim claim : b.getClaims()) {
							if(! sourcesToBeMimicked.contains(claim.getSource().getSourceIdentifier())) //Not required really but some data may be uncleaned
								sourcesToBeMimicked.add(claim.getSource().getSourceIdentifier());
					}
				}
				else
					//nbC+=b.getClaims().size(); // Number of opposing Sources
					nbC++; // Number of Conflicting Values
			}
			nbSS = sourcesToBeMimicked.size();
		}		
		
		String temp;
		
		
		// Old Idea (Didn't work - SourcesToBeMimicked contains all the sources that provided true value for this DI sorted by their trust)
		for (int i = 0; i < sourcesToBeMimicked.size(); i++) {
			for (int j = 1; j < sourcesToBeMimicked.size() - i; j++) {
				if (trust.get(sourcesToBeMimicked.get(j)) > trust.get(sourcesToBeMimicked.get(j-1))) {
					temp = sourcesToBeMimicked.get(j-1);
					sourcesToBeMimicked.set(j - 1, sourcesToBeMimicked.get(j));
					sourcesToBeMimicked.set(j, temp);
				}
			}
		}
		
		if ( !dataSet.getSourcesHash().containsKey(newSourceName))
		{
			SourceClaim newClaim = dataSet.addClaim(claimIdMax+1, uncleanedObjectId, targetObjectId, targetPropertyName, fakeValue, Globals.weight, "Now", newSourceName);
			dataSet.addClaimToBucket(newClaim, secondMax);
			claimsAdded = claimsAdded + 1;
			fakeSourceCount++;
			// Print the fake Claims introduced
			System.out.println(String.valueOf(claimIdMax+1)+" "+targetObjectId+" "+secondMax.getClaims().get(0).getPropertyValueString()+" "+newSourceName);
		}
		return 1;
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
	
	private void orderDataItemsByMaxConfidence() { // Dalia's bubble sort
		DataItemAllegatorObject temp;
		for (int i = 0; i < orderedDataItems.size(); i++) {
			for (int j = 1; j < orderedDataItems.size() - i; j++) {
				/* data item with only one value are put at or keep it the end of the list */
				if (orderedDataItems.get(j).getNumberOfCOnflicts() == 1) {
					continue;
				}
				if (orderedDataItems.get(j - 1).getNumberOfCOnflicts() == 1) {
					temp = orderedDataItems.get(j-1);
					orderedDataItems.set(j - 1, orderedDataItems.get(j));
					orderedDataItems.set(j, temp);
				} else if (orderedDataItems.get(j).getMaxConfidence() > orderedDataItems.get(j-1).getMaxConfidence()) {
					temp = orderedDataItems.get(j-1);
					orderedDataItems.set(j - 1, orderedDataItems.get(j));
					orderedDataItems.set(j, temp);

				} else if (orderedDataItems.get(j).getMaxConfidence() == orderedDataItems.get(j-1).getMaxConfidence()) {
					if (orderedDataItems.get(j).getMinConfidence() > orderedDataItems.get(j-1).getMinConfidence()) {
						temp = orderedDataItems.get(j-1);
						orderedDataItems.set(j - 1, orderedDataItems.get(j));
						orderedDataItems.set(j, temp);
					}
				}
			}
		}
	}
	
	private void initDIAllegatorObjects() {

		double minConfidence;
		double maxConfidence;
		for (String diKey : dataSet.getDataItemsBuckets().keySet()) {
			List<ValueBucket> bList = dataSet.getDataItemsBuckets().get(diKey);
			if (bList.size() == 1) {
				continue;
			}
			DataItemAllegatorObject diAO = new DataItemAllegatorObject();
			diAO.setDataItemKey(diKey);
			minConfidence = Double.MAX_VALUE;
			maxConfidence = -1.0 * Double.MAX_VALUE;
			HashMap<String, Double> valueConfidence = new HashMap<String, Double>();
			for (ValueBucket b : bList) {
				if(b.getClaims().get(0).isTrueClaimByVoter()){
				valueConfidence.put(b.getClaims().get(0).getPropertyValueString(), b.getConfidence());
				if (minConfidence > b.getConfidence()) {
					minConfidence = b.getConfidence();
				}
				if (maxConfidence < b.getConfidence()) {
					maxConfidence = b.getConfidence();
				}
				if (b.getClaims().get(0).isTrueClaimByVoter()) {
					diAO.setFirstTrueValueByVoter(b.getClaims().get(0).getPropertyValueString());
					diAO.setTrueValueByVoter(b.getClaims().get(0).getPropertyValueString());
				}
				}
			}
			diAO.setValueConfidence(valueConfidence);
			diAO.setMaxConfidence(maxConfidence);
			diAO.setMinConfidence(minConfidence);
			diAO.setNumberOfCOnflicts(bList.size());
			orderedDataItems.add(diAO);
		}
		
		orderDataItemsByMaxConfidence();
	}

}
