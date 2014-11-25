package qcri.dafna.voter;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.UniformRandomGenerator;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;

public class MistakeLCA extends Voter {

	private double pym;
	double cosineSimilarityStoppingCondition = 0.001;
	HashMap<String , Double> newD;
	HashMap<String , Double> oldD;
	public MistakeLCA(DataSet dataSet, VoterParameters params) {
		super(dataSet, params);
		pym = -1;
		newD = new HashMap<String, Double>();
		oldD = new HashMap<String, Double>();
	}
	@Override
	protected void initParameters() {
		singlePropertyValue = false;
		onlyMaxValueIsTrue = true;
	}

	@Override
	protected int runVoter(boolean convergence100) {
		init();
		double oldTrustCosinSim = 0, oldConfCosineSim = 0, oldDCosineSim = 0;
		double newTrustCosinSim, newconfCosineSim, newDCosineSim;
		int numOfIteration = 0;
		boolean continueComputation = true;
		while (continueComputation) {

			computeConfidence();
			computeSourceParameters();

			newTrustCosinSim = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			newconfCosineSim = ConvergenceTester.computeConfidenceCosineSimilarity(dataSet);
			newDCosineSim = ConvergenceTester.computeValuesCosineSimilarity(oldD, newD, dataSet.getSourcesHash().keySet());
			//continueComputation = !( (Math.abs(newconfCosineSim-oldConfCosineSim) < cosineSimilarityStoppingCondition) &&
			//		(Math.abs(newTrustCosinSim-oldTrustCosinSim) < cosineSimilarityStoppingCondition) && 
			//		(Math.abs(newDCosineSim-oldDCosineSim) < cosineSimilarityStoppingCondition));
			continueComputation = !( (1-newconfCosineSim < cosineSimilarityStoppingCondition) &&
					(1-newTrustCosinSim < cosineSimilarityStoppingCondition) && 
					(1-newDCosineSim < cosineSimilarityStoppingCondition));
			oldTrustCosinSim = newTrustCosinSim;
			oldConfCosineSim = newconfCosineSim;
			oldDCosineSim = newDCosineSim;
			numOfIteration ++;
			computeMeasuresPerIteration(true,0,0);
		}
		return numOfIteration;
	}

	private void computeConfidence() {
		double tempConf;
		double pError;
		double hd;
		double d;
		for (List<ValueBucket> bList : dataSet.getDataItemsBuckets().values()) {
			tempConf = pym;
			d = newD.get(bList.get(0).getClaims().get(0).dataItemKey());
			for (ValueBucket bucket : bList) {
				for (Source s : bucket.getSources()) {
					hd = s.getTrustworthiness() * d;
					tempConf = tempConf * hd;
				}
				double errorProbability = 1;
				List<ValueBucket> bucketsList = dataSet.getDataItemsBuckets().get(bucket.getClaims().get(0).dataItemKey());
				if (bucketsList.size() > 1) {
					for (ValueBucket b : bucketsList) {
						pError = b.getErrorFactor(); // the error probability is saved in the error factor field
						for (Source s : b.getSources()) {
							hd = s.getTrustworthiness() * d;
							errorProbability = errorProbability * (pError*(1-hd));
						}
					}
					if (errorProbability > 0) {
						double tobeDeducted = 1;
						for (Source s : bucket.getSources()) {
							pError = bucket.getErrorFactor(); // the error probability is saved in the error factor field
							hd = s.getTrustworthiness() * d;
							tobeDeducted = tobeDeducted * (pError*(1-hd));
						}
						errorProbability = errorProbability / tobeDeducted;
					}
				}
				tempConf = tempConf * errorProbability;
				bucket.setConfidenceWithSimilarity(bucket.getConfidence());
				bucket.setConfidence(tempConf);
			}
		}
	}

	private void computeSourceParameters() {
		double allSrcALLClaimsConf = 0;
		double allSrcClaimsConf = 0;
		for (Source src : dataSet.getSourcesHash().values()) {
			for (SourceClaim claim : src.getClaims()) {
				allSrcClaimsConf = allSrcClaimsConf + claim.getBucket().getConfidence();
				for (ValueBucket b : dataSet.getDataItemsBuckets().get(claim.dataItemKey())) {
					allSrcALLClaimsConf = allSrcALLClaimsConf + b.getConfidence();
				}
			}
		}
		double partOfH = allSrcALLClaimsConf  / allSrcClaimsConf;
		double h;
		double d;
		for (Source src : dataSet.getSourcesHash().values()) {
			h = computeH(src, partOfH);
			src.setOldTrustworthiness(src.getTrustworthiness());
			src.setTrustworthiness(h);

			if (h > 0) {
				d = 1 / (partOfH * h);
			} else {
				d = 1;
			}
			oldD.put(src.getSourceIdentifier(), newD.get(src.getSourceIdentifier()));
			newD.put(src.getSourceIdentifier(), d);
		}
	}
	private double computeH(Source src, double partOfH) {
		double h = 0;
		double srcALLClaimsConf = 0;
		double srcClaimsConf = 0;
		int x = 0 ;
		if (src.getSourceIdentifier().equals("B?cher Th?ne")) {
			x++;
		}
		for (SourceClaim claim : src.getClaims()) {
			srcClaimsConf = srcClaimsConf + claim.getBucket().getConfidence();
			for (ValueBucket b : dataSet.getDataItemsBuckets().get(claim.dataItemKey())) {
				srcALLClaimsConf = srcALLClaimsConf + b.getConfidence();
			}
		}
		if (srcClaimsConf == 0 && srcALLClaimsConf == 0) {
			return 0;
		}
		h = partOfH * srcClaimsConf / srcALLClaimsConf;
		h = Math.sqrt(h);
		if (Double.isNaN(h) ) {
			x++;
		}
		return h;
	}

	private void init() {
		RandomGenerator rgA = new JDKRandomGenerator();
		rgA.setSeed((int)(Math.random() * 100000000)); 
		UniformRandomGenerator randomGeneratorA = new UniformRandomGenerator(rgA);

		RandomGenerator rgB = new JDKRandomGenerator();
		rgB.setSeed((int)(Math.random() * 100000000)); 
		UniformRandomGenerator randomGeneratorB = new UniformRandomGenerator(rgB);

		for (Source s : dataSet.getSourcesHash().values()) {
			s.setOldTrustworthiness(0);
			s.setTrustworthiness(normalizeNextRandom(randomGeneratorA));
		}
		for (String key : dataSet.getDataItemsBuckets().keySet()) {
			oldD.put(key, 0.0);
			newD.put(key, normalizeNextRandom(randomGeneratorB));
		}
		dataSet.initializeTheDisagreeingSources();
		// initialize the Perror
		double numOfSrc, totalNumOfSrc;
		double perror;
		for (List<ValueBucket>bList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : bList) {
				numOfSrc = b.getSources().size();
				totalNumOfSrc = b.getDisagreeingSourcesKeys().size();
				if (totalNumOfSrc == 0) {
					perror = 0;
				} else {
					perror = numOfSrc / totalNumOfSrc;
				}
				b.setErrorFactor(perror);
			}
		}
	}
	private double normalizeNextRandom(UniformRandomGenerator generator) {
		double random = generator.nextNormalizedDouble() + Math.sqrt(3); /* the nextNormalizedDouble returns a value from -sqrt(3) to +sqrt(3)  */
		random = ((double)random/(2*Math.sqrt(3))); /* now random is from 0 to 1 */
		return random;
	}
}
