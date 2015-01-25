package qcri.dafna.explaination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import qcri.dafna.dataModel.data.ConfValueLabel;
import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataSet.CSVDatasetReader.ConfidenceReader;
import qcri.dafna.dataModel.dataSet.CSVDatasetReader.TrustWorthinessReader;

public class MetricsGenerator {
	
	private DataSet ds;
	
	public MetricsGenerator(DataSet dataSet){
		this.ds = dataSet;
	}
	
	public List<Metrics> generateMetrics(String confidenceFilePath, String trustFilePath){
		List<Metrics> allClaimsMetrics = new ArrayList<Metrics>();
		HashMap<Integer, ConfValueLabel> conf;
		HashMap<String, Double> trust;
		
		ConfidenceReader confidenceReader = new ConfidenceReader();
		TrustWorthinessReader trustWorthinessReader = new TrustWorthinessReader();
		
		conf = confidenceReader.readConfidenceFile(confidenceFilePath, ',');
		trust = trustWorthinessReader.readTrustFile(trustFilePath, ',');
		
		// Commenting Normalization Code - Assuming Files Supplied are already Normalised
		
		/*
		// Normalization
		
		HashMap<Integer, Double> confValues = new HashMap<Integer, Double>();
		HashMap<String, Double> trustValues =  new HashMap<String, Double>();
			
		for(int claimIDForConf : conf.keySet()){
			confValues.put(claimIDForConf,conf.get(claimIDForConf).getConfidenceValue());
		}
				
		for(String sourceIDForTrust : trust.keySet()){
			trustValues.put(sourceIDForTrust,trust.get(sourceIDForTrust));
		}
				
		double maxConfValue = Collections.max(confValues.values());
		double minConfValue = Collections.min(confValues.values());
		double maxTrustValue = Collections.max(trustValues.values());
		double minTrustValue = Collections.min(trustValues.values());
		
		for(int claimIDForConf : confValues.keySet()){
			double normalizedValue = (confValues.get(claimIDForConf)-minConfValue)/(maxConfValue - minConfValue);
			confValues.put(claimIDForConf, normalizedValue);
		}
		
		for(String sourceIDForTrust : trustValues.keySet()){
			double normalizedValue = (trustValues.get(sourceIDForTrust)-minTrustValue)/(maxTrustValue - minTrustValue);
			trustValues.put(sourceIDForTrust, normalizedValue);
		}
		
		for(int claimIDForConf: conf.keySet()){
			ConfValueLabel newValue = new ConfValueLabel();
			newValue.setLabel(conf.get(claimIDForConf).getLabel());
			newValue.setBucketId(conf.get(claimIDForConf).getBucketId());
			newValue.setConfidenceValue(confValues.get(claimIDForConf));
			conf.put(claimIDForConf, newValue);
		}
		
		for(String sourceIDForTrust: trust.keySet()){
			trust.put(sourceIDForTrust, trustValues.get(sourceIDForTrust));
		}
		
		//Normalization end
		
		*/
		intializeWithResults(conf, trust);
		
		String targetDataItemKey ;
		String targetSource;
		ValueBucket targetBucket;
		String claimID;
		
		double maxTrustWorthySourceDI;
		double minTrustWorthySourceDI;
		double originalTrust;
		double originalConfidence;
		double globalTrustWorthinessComparison;
		double localTrustWorthinessComparison;
		int	totalNumberOfClaims;
		int nbSS;
		int nbC;
		int nbVal;
		String truthLabel;
		int nbValDI;
		double globalConfidenceComparison;
		double localConfidenceComparison;
		
		for (String DIKey : ds.getDataItemsBuckets().keySet())
		{
			for (ValueBucket b : ds.getDataItemsBuckets().get(DIKey))
			{
					for (SourceClaim claim : b.getClaims())
					{		
							// Pick each claim one by one
							targetDataItemKey = DIKey;
							targetSource = claim.getSource().getSourceIdentifier();
							targetBucket = b;
							
							// Start Computing Metrics
							claimID = String.valueOf(claim.getId());
							originalTrust = trust.get(targetSource);
							originalConfidence = targetBucket.getConfidence();
							nbSS = targetBucket.getNumberOfClaims();
							
							maxTrustWorthySourceDI = -1*Double.MAX_VALUE;
							minTrustWorthySourceDI = Double.MAX_VALUE;
							for(SourceClaim claimTemp : targetBucket.getClaims())
							{
								if(claimTemp.getSource().getTrustworthiness() > maxTrustWorthySourceDI)
									maxTrustWorthySourceDI = claimTemp.getSource().getTrustworthiness();
								if(claimTemp.getSource().getTrustworthiness() < minTrustWorthySourceDI)
									minTrustWorthySourceDI = claimTemp.getSource().getTrustworthiness();
							}							
							
							totalNumberOfClaims = 0;
							localTrustWorthinessComparison = 0;
							localConfidenceComparison = 0;
							truthLabel = "FALSE";
							for (ValueBucket bTemp : ds.getDataItemsBuckets().get(targetDataItemKey)) {
								totalNumberOfClaims = totalNumberOfClaims + bTemp.getNumberOfClaims();
								for(SourceClaim claimTemp : bTemp.getClaims()){
									if(trust.get(claimTemp.getSource().getSourceIdentifier()) <= trust.get(targetSource))
										++localTrustWorthinessComparison;
									if(claimTemp.getSource().getSourceIdentifier() == targetSource && claimTemp.isTrueClaimByVoter())
										truthLabel = "TRUE";
								}
								if(bTemp.getConfidence() <= targetBucket.getConfidence())
									++localConfidenceComparison;
							}
							nbC = totalNumberOfClaims - nbSS;
							
							globalTrustWorthinessComparison = 0;
							for(String sourceId : trust.keySet()){
								if(trust.get(sourceId) <= trust.get(targetSource))
									++globalTrustWorthinessComparison;
							}
							
							globalConfidenceComparison = 0;
							nbVal = 0;
							for (String DIKeyTemp : ds.getDataItemsBuckets().keySet()){
								for (ValueBucket bTemp : ds.getDataItemsBuckets().get(DIKeyTemp)) {
									if(bTemp.getConfidence() <= targetBucket.getConfidence())
										++globalConfidenceComparison;
										++nbVal;
								}
							}
							
							nbValDI = ds.getDataItemsBuckets().get(targetDataItemKey).size();
							
							// Add the generated metrics to the list
							Metrics metrics = new Metrics(claimID, originalConfidence, originalTrust, minTrustWorthySourceDI, maxTrustWorthySourceDI, nbSS*100.0/(nbSS+nbC), nbC*100.0/(nbSS+nbC), nbSS+nbC, nbValDI, globalConfidenceComparison*100.0/nbVal, localConfidenceComparison*100.0/nbValDI, globalTrustWorthinessComparison*100.0/trust.size(), localTrustWorthinessComparison*100.0/totalNumberOfClaims, truthLabel);
							allClaimsMetrics.add(metrics);
					}
			}
		}
		return allClaimsMetrics;
	}
	
	public void intializeWithResults(HashMap<Integer, ConfValueLabel> conf, HashMap<String, Double> trust){
		for (List<ValueBucket> bucketsList : ds.getDataItemsBuckets().values()) {
			for (ValueBucket b : bucketsList) {
				int claim_id = b.getClaims().get(0).getId();
				double confidence = conf.get(claim_id).getConfidenceValue();
					b.setConfidence(confidence);
					for (SourceClaim claim : b.getClaims()){
						claim.setTrueClaimByVoter(conf.get(claim.getId()).getLabel());
				}
		}
	}
		
		for(Source source : ds.getSourcesHash().values()){
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
