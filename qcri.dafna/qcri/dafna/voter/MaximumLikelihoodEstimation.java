package qcri.dafna.voter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;

/**
 * The MLE Voter
 * @author dalia
 *
 */
public class MaximumLikelihoodEstimation extends Voter {

	private HashMap<String, Double> aOld;
	private HashMap<String, Double> bOld;
	private HashMap<String, Double> a;
	private HashMap<String, Double> b;

	private double bita;
	private double r;

	/**
	 * This method assume that the observation is only true or false.
	 * i.e. If a source claim the occurrence of an event ( then true by this source) a claim should exist for this source.
	 * If the Source doesn't claim the occurrence of the event (then false by the source) no claim should exist for this source for this dataItem.
	 * 
	 * The final estimation for a claim confidence exists in the bucket.confidence field
	 * The final source reliability in the source,trustworthiness field
	 * @param dataSet
	 */
	public MaximumLikelihoodEstimation(DataSet dataSet, VoterParameters params, double beta1MLE, double rMLE) {
		super(dataSet, params);

		aOld = new HashMap<String, Double>();
		bOld = new HashMap<String, Double>();
		a = new HashMap<String, Double>();
		b = new HashMap<String, Double>();
		bita = beta1MLE;
		this.r = rMLE;
	}

	@Override
	protected void initParameters() {
		singlePropertyValue = false;
		onlyMaxValueIsTrue = false;
	}

	@Override
	protected int runVoter(boolean convergence100) {

		int numberOfIteration = 0;
		init();
		while (continueComputation(convergence100, numberOfIteration)  && numberOfIteration < Globals.maxIterationCount) {
//			System.out.print("iteration " +  + " , ");
			/* compute the confidence as function of the old a and b parameters*/
			computeLatentConfidence();
			/* Save the a and b values to the old hashmap */
			propagateOldParametersValues();
			/* compute the new a and b values */
			computeSourceParameters();
			
			numberOfIteration ++;
			// the a parameter cosine sim is saved in the trust-cosine sim,
			// the b parameter cosine sim is saved in the conf-cosine sim,
			computeMeasuresPerIteration(true, trustCosineSimDiff, confCosineSimDiff);
		}

		computeSourcesTrustworthiness();
		return numberOfIteration;
	}

	private void propagateOldParametersValues() {
		for (String key : a.keySet()) {
			aOld.put(key, a.get(key));
			bOld.put(key, b.get(key));
		}
	}

	private void computeLatentConfidence() {
		double aClaim, bClaim;
		double tempConf;
		for (List<ValueBucket> bucketList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket bucket : bucketList) {
				aClaim = 1;
				bClaim = 1;
				for (String src : bucket.getSourcesKeys()) {
					aClaim = aClaim * a.get(src);
					bClaim = bClaim * b.get(src);
				}
				for (String src : bucket.getDisagreeingSourcesKeys()) {
					aClaim = aClaim * (1 - a.get(src));
					bClaim = bClaim * (1 - b.get(src));
				}
				tempConf = (aClaim * bita) / ( (aClaim * bita) + ( bClaim * (1-bita) ) );
				bucket.setConfidence(tempConf);
			}
		}
	}

	private void computeSourceParameters() {
		double sumOfSourceClaimsConf;
		double numOfAllClaims = dataSet.getDataItemsBuckets().size();
		double sumOfAllConfidence = 0;
		int claimsCount;
		for (List<ValueBucket> bucketList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket bucket : bucketList) {
				sumOfAllConfidence = sumOfAllConfidence + bucket.getConfidence();
			}
		}
		String srcKey;
		double tempA, tempB;
		for (Source source : dataSet.getSourcesHash().values()) {
			srcKey = source.getSourceIdentifier();
			sumOfSourceClaimsConf = 0;
			claimsCount = 0;
			for (SourceClaim claim : source.getClaims()) {
				sumOfSourceClaimsConf = sumOfSourceClaimsConf + claim.getBucket().getConfidence();
				claimsCount++;
			}
			tempA = sumOfSourceClaimsConf / sumOfAllConfidence;
			tempB = ( ((double)claimsCount) - sumOfSourceClaimsConf) / (numOfAllClaims - sumOfAllConfidence);

			a.put(srcKey, tempA);
			b.put(srcKey, tempB);
		}
	}

	private void computeSourcesTrustworthiness() {
		int numOfClaims;
		int numOfAllCalims = dataSet.getDataItemsBuckets().size();
		double tempTrust;
		for (Source source : dataSet.getSourcesHash().values()) {
			numOfClaims = source.getClaims().size();
			tempTrust = a.get(source.getSourceIdentifier()) * bita / ((double)((double)numOfClaims/(double)numOfAllCalims));
			source.setTrustworthiness(tempTrust);
		}
	}

	private double aCosineSim = 0;
	private double bCosineSim = 0;
	private double trustCosineSimDiff;
	private double confCosineSimDiff;

	private boolean continueComputation(boolean convergence100, int i) {
		double acs = ConvergenceTester.computeValuesCosineSimilarity(aOld, a, dataSet.getSourcesHash().keySet());
		double bcs = ConvergenceTester.computeValuesCosineSimilarity(bOld, b, dataSet.getSourcesHash().keySet());

		trustCosineSimDiff = Math.abs(aCosineSim - acs);
		confCosineSimDiff = Math.abs(bCosineSim - bcs);
		aCosineSim = acs;
		bCosineSim = bcs;

		if (convergence100) {
			if (i < Globals.maxIterationCount) {
				return true;
			} else {
				return false;
			}
		} //else if (trustCosineSimDiff < ConvergenceTester.convergenceThreshold && confCosineSimDiff < ConvergenceTester.convergenceThreshold) {
			else if ((1-acs < ConvergenceTester.convergenceThreshold) && (1-bcs < ConvergenceTester.convergenceThreshold)) {
			return false;
		}
		return true;
	}

	/**
	 * Initialize the Disagreeing Sources.
	 * Initialize the a and b parameters.
	 */
	private void init() {
		/* This method is different from the initialize disagreeing sources
		 *  implemented for other models*/
		initializeTheDisagreeingSources();

		double totalClaimsCount = dataSet.getDataItemsBuckets().size();
		double sourceClaimsCount;
		double s;
		for (String key : dataSet.getSourcesHash().keySet()) {
			sourceClaimsCount = dataSet.getSourcesHash().get(key).getClaims().size();
			s = sourceClaimsCount/totalClaimsCount;
			aOld.put(key, 0.0);
			bOld.put(key, 0.0);
			a.put(key, (r*s/bita));//a.put(key,0.51);//a.put(key, normalizeNextRandom(randomGeneratorA));
			b.put(key, ((1-r)*s/(1-bita)));//b.put(key, 0.49); //b.put(key, normalizeNextRandom(randomGeneratorB));
		}
	}

//	private double normalizeNextRandom(UniformRandomGenerator generator) {
//		double random = generator.nextNormalizedDouble() + Math.sqrt(3); /* the nextNormalizedDouble returns a value from -sqrt(3) to +sqrt(3)  */
//		random = ((double)random/(2*Math.sqrt(3))); /* now random is from 0 to 1 */
//		return random;
//	}


	/**
	 * 
	 */
	public void initializeTheDisagreeingSources() {
		Set<String> allSources = dataSet.getSourcesHash().keySet();
		Set<String> disagreeingSources;
		for (List<ValueBucket> bList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : bList) {
				disagreeingSources = new HashSet<String>();
				disagreeingSources.addAll(allSources);
				disagreeingSources.removeAll(b.getSourcesKeys());
				b.setDisagreeingSourcesKeys(new ArrayList<String>(disagreeingSources));
			}

		}
	}
}