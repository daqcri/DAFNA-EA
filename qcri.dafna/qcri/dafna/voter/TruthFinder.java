package qcri.dafna.voter;

import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataFormatter.DataComparator;
import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;

public class TruthFinder extends Voter {

	private double similarityConstant = 0.5; // as set in the paper
	private double base_sim = 0.5;// as set in the paper
	private double dampingFactor = 0.1; // as set in the paper

	@Override
	protected void initParameters() {
		singlePropertyValue = false; 
		onlyMaxValueIsTrue = true;
	}

	public TruthFinder(DataSet dataSet, VoterParameters params, double similarityConstantTF, double dampeningFactorTF) {
		super(dataSet, params);
		this.similarityConstant = similarityConstantTF;
//		this.base_sim = base_sim;
		this.dampingFactor = dampeningFactorTF;
	}

	/**
	 * Run the TruthFinder Algorithm for the given DataSet.
	 * @return the number of iterations until TruthFinder convergences.
	 */
	@Override
	protected int runVoter(boolean convergence100) {
		boolean continueComputation = true;
		double trustworthinessCosineSimilarity = 0.0;
		double newCosineSimilarity;
		double confidenceCosineSimilarity = 0.0;
		double newConfCosineSimilarity;
		int i = 0;
		while (continueComputation && i < Globals.maxIterationCount) {
			i++;

			//Compute Claims Confidence score
			computeConfidenceScore();

			//Enhance the confidence score with the similarity between claims
			computeConfidenceScoreWithSimilarity();

			//Compute the final confidence value
			computeConfidence();
			//Compute Sources Trustworthiness
			computeTrustworthiness();


			//Compute Trustworthiness cosine similarity.
			newCosineSimilarity = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			newConfCosineSimilarity = ConvergenceTester.computeConfidenceCosineSimilarity(dataSet);

			// decide when to stop.
			double cosineSimilarityDifference = Math.abs(trustworthinessCosineSimilarity - newCosineSimilarity);
			computeMeasuresPerIteration(true, cosineSimilarityDifference, Math.abs(confidenceCosineSimilarity - newConfCosineSimilarity));
			if (convergence100) {
				if (i > Globals.maxIterationCount) {
					continueComputation = false;
				}
			} //else if (cosineSimilarityDifference <= ConvergenceTester.convergenceThreshold) {
			    else if (1- newCosineSimilarity <= ConvergenceTester.convergenceThreshold) {
				continueComputation = false;
			} 
			trustworthinessCosineSimilarity = newCosineSimilarity;
			confidenceCosineSimilarity = newConfCosineSimilarity;
		}
		return i;
	}


	/**
	 * compute the confidence score of the claims without mention 
	 * for the claim similarity.
	 * this method runs first, then the @computeConfidenceWithSimilarity method 
	 * is run to enhance the confidence computation.
	 */
	private void computeConfidenceScore() {
		double ln = 0;
		double lnSum = 0;
		for (List<ValueBucket> backetsList: dataSet.getDataItemsBuckets().values()) {
			/*
			 * All claims, from different sources, for single property value.
			 */
			for (ValueBucket bucket : backetsList) {
				lnSum = 0;
				for (Source source : bucket.getSources()) {
					ln = Math.log(1 - source.getTrustworthiness());
					lnSum = lnSum - ln;
				}
				bucket.setConfidence(lnSum);
			}
		}
	}

	/**
	 * Compute the confidence score of a claim based on its already computed confidence score
	 * and the similarity between it and the other claims.
	 * Then set the confidence value to this new confidence computed with similarity measure. 
	 */
	private void computeConfidenceScoreWithSimilarity() {
		double similarity;
		double similaritySum;
		for (List<ValueBucket> bucketList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket bucket1 : bucketList) {
				similaritySum = 0;
				for (ValueBucket bucket2 : bucketList) {
					// test if same bucket and continue
					if (bucket1.getClaims().get(0).getId() == bucket2.getClaims().get(0).getId()) {
						continue;
					}
					similarity = computeClaimsSimilarity(bucket1, bucket2);
					similaritySum = similaritySum + (bucket2.getConfidence() * similarity);
				}
				/*
				 * compute the similarity based on the confidence without similarity
				 */
				similaritySum = (similarityConstant * similaritySum) + bucket1.getConfidence();
				bucket1.setConfidenceWithSimilarity(similaritySum);
			}
			/*
			 * populate the confidence computed with similarity to be the exact new value for 
			 * the claim confidence
			 */
			for (ValueBucket bucket : bucketList) {
				bucket.setConfidence(bucket.getConfidenceWithSimilarity());
			}
		}
	}

	private void computeConfidence() {
		double e, denum;
		for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : bucketsList) {
				e = -1 * dampingFactor * b.getConfidence();
				e = Math.exp(e);
				denum = 1 + e;
				b.setConfidence(1/denum);
			}
		}
	}

	/**
	 * Compute the similarity between the two given claims.
	 * @param bucket1
	 * @param bucket2
	 * @return
	 */
	private double computeClaimsSimilarity(ValueBucket bucket1, ValueBucket bucket2) {
		double result = DataComparator.computeImplication(bucket1, bucket2, bucket1.getValueType());
		result = result - base_sim;
		if (Double.isNaN(result)) {
			return 1;
		}
		// TODO : revise for the non-string values  
		return (double)result;
	}

	/**
	 * Compute every source trustworthiness.
	 * The method doesn't delete the old trustworthiness value in order to be able to compute 
	 * the cosine similarity between the old and new trustworthiness values.
	 * it rather save it in the "oldTrustworthiness" property in the Source object.
	 */
	private void computeTrustworthiness() {
		double sum;
		for (Source source : dataSet.getSourcesHash().values()) {
			sum = 0;
			for (SourceClaim claim : source.getClaims()) {

				sum = sum +
						(1 - Math.exp((-1 * dampingFactor) * claim.getBucket().getConfidence()));
			}
			source.setOldTrustworthiness(source.getTrustworthiness());
			source.setTrustworthiness(sum / source.getClaims().size());
		}
	}


}
