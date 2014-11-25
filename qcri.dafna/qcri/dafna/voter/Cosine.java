package qcri.dafna.voter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;

public class Cosine extends Voter {

	private double dampingFactor = 0.2;

	@Override
	protected void initParameters() {
		singlePropertyValue = false; 
		onlyMaxValueIsTrue = true;
	}

	/**
	 * 
	 * @param dataSet: The Data set
	 * @param params
	 * @param dampeningFactorCosine: from zero to one, default value 0.2.
	 */
	public Cosine(DataSet dataSet, VoterParameters params, double dampeningFactorCosine) {
		super(dataSet, params);
		if (dampeningFactorCosine > 0) {
			this.dampingFactor = dampeningFactorCosine;
		} else {
			this.dampingFactor = 0.2;
		}
	}


	@Override
	protected int runVoter(boolean convergence100) {
		initializeTrustworthiness();
		int i=0;
		boolean continueComputation = true;
		double trustworthinessCosineSimilarity = 0.0;
		double newCosineSimilarity;
		double confidenceCosineSimilarity = 0.0;
		double newConfCosineSimilarity;
		while (continueComputation && i < Globals.maxIterationCount) {
			i++;
			computeTrustworthiness();
			computeConfidence();

			voterQuality.computePrecisionAndRecall();

			newCosineSimilarity = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			newConfCosineSimilarity = ConvergenceTester.computeConfidenceCosineSimilarity(dataSet);
			double cosineSimilarityDifference = Math.abs(trustworthinessCosineSimilarity - newCosineSimilarity);
			if (convergence100) {
				if (i > Globals.maxIterationCount) {
					continueComputation = false;
				}
			} //else if (cosineSimilarityDifference <= ConvergenceTester.convergenceThreshold) {
				else if (1- newCosineSimilarity <= ConvergenceTester.convergenceThreshold) {
				continueComputation = false;
			}

			trustworthinessCosineSimilarity = newCosineSimilarity;
			computeMeasuresPerIteration(true, cosineSimilarityDifference, Math.abs(confidenceCosineSimilarity - newConfCosineSimilarity));
			confidenceCosineSimilarity = newConfCosineSimilarity;
		}
		return i;
	}

	/**
	 * Initialize the trustworthiness of sources.
	 */
	private void initializeTrustworthiness() {
		int trueClaim = 0;
		int falseClaim = 0;
		int tempFalseClaims;
		String dataItemKey;
		double trustworthiness;
		Set<String> dataItemsKeySet;
		for (Source source : dataSet.getSourcesHash().values()) {
			trueClaim = 0;
			falseClaim = 0;
			dataItemsKeySet = new HashSet<String>();
			for (SourceClaim claim : source.getClaims()) {
				dataItemKey = claim.dataItemKey();
				trueClaim ++;
				if (dataItemsKeySet.contains(dataItemKey)) {
					// the source provide more than one value for the same data item
					falseClaim --;
				} else {
					tempFalseClaims = dataSet.getDataItemsBuckets().get(dataItemKey).size() - 1; // the remaining values, for the data item, that this source did not claim 
					falseClaim = falseClaim + tempFalseClaims;
					dataItemsKeySet.add(dataItemKey);
				}
			}
			trustworthiness = ((double)trueClaim - (double)falseClaim) / ((double)trueClaim + (double)falseClaim);
			source.setTrustworthiness(trustworthiness);
		}
	}

	private void computeTrustworthiness() {
		double trueClaimConfidence;
		double tempTRUEClaimConfidence;
		double falseClaimConfidence;
		double tempFALSEClaimsConfidence;
		double sumConfSqr;
		int numOfClaims;

		double denum;
		double conf;
		String dataItemKey;
		double tr1, tr2;

		Set<String> dataItemsKeySet;

		for (Source source : dataSet.getSourcesHash().values()) {
			numOfClaims = 0;
			trueClaimConfidence = 0;
			falseClaimConfidence = 0;
			sumConfSqr = 0;

			dataItemsKeySet = new HashSet<String>();
			for (SourceClaim claim : source.getClaims()) {
				dataItemKey = claim.dataItemKey();
				tempTRUEClaimConfidence = claim.getBucket().getConfidence();
				trueClaimConfidence = trueClaimConfidence + tempTRUEClaimConfidence;

				if (dataItemsKeySet.contains(dataItemKey)) {
					falseClaimConfidence = falseClaimConfidence - tempTRUEClaimConfidence; // this claim confidence was previously added in the false claims confidence
				} else {
					tempFALSEClaimsConfidence = 0;
					/**
					 * Instead of comparing every time whether this is the claim for the current source or not,
					 * all claims confidences are added and finally the current source claim confidence is subtracted.
					 */
					for (ValueBucket falseBucket : dataSet.getDataItemsBuckets().get(dataItemKey)) {
						conf = falseBucket.getConfidence();
						tempFALSEClaimsConfidence = tempFALSEClaimsConfidence + conf;
						sumConfSqr = sumConfSqr + (conf * conf);
						numOfClaims ++;
					}
					tempFALSEClaimsConfidence = tempFALSEClaimsConfidence - tempTRUEClaimConfidence;
					falseClaimConfidence = falseClaimConfidence + tempFALSEClaimsConfidence;

					dataItemsKeySet.add(dataItemKey);
				}
			}
			denum = Math.sqrt((double)numOfClaims * sumConfSqr);
			tr1 = (1 - dampingFactor) * source.getTrustworthiness();
			if (denum != 0) {
				tr2 = dampingFactor * (trueClaimConfidence - falseClaimConfidence ) / denum;
			} else {
				tr2 = Double.MAX_VALUE;
			}
			source.setOldTrustworthiness(source.getTrustworthiness());
			source.setTrustworthiness(tr1 + tr2);
		}
	}

	private void computeConfidence() {
		double sumTrueSrcCube;
		double sumAllSrcCube;
		double tr;
		double confTrue, confFalse;
		for (List<ValueBucket> dataItemBuckets : dataSet.getDataItemsBuckets().values()) {
			sumAllSrcCube = 0;
			for (ValueBucket bucket : dataItemBuckets) {
				sumTrueSrcCube = 0;
				for (SourceClaim claim : bucket.getClaims()) {
					tr = claim.getSource().getTrustworthiness();
					sumTrueSrcCube = sumTrueSrcCube + (tr * tr * tr);
					sumAllSrcCube = sumAllSrcCube  + (tr * tr * tr);
				}
				/**
				 *  we save the cube of the sum of the trustworthiness of the true value sources 
				 *  in the confidence field of the bucket in order to reuse it again after computing the sum of all sources confidences.
				 */
				if (Double.isInfinite(sumTrueSrcCube)) {
					bucket.setConfidence(1.0);
				} else {
					bucket.setConfidence(sumTrueSrcCube);
				}
			}
			for (ValueBucket bucket : dataItemBuckets) {
				confTrue = bucket.getConfidence();
				confFalse = sumAllSrcCube - confTrue;
				if (sumAllSrcCube == 0) {
					bucket.setConfidence(1.0);
				} else if (Double.isInfinite(sumAllSrcCube)) {
					if (Double.isInfinite(confFalse)) {
						bucket.setConfidence(confTrue);
					} else  {
						bucket.setConfidence(0.0);
					}
				} else {
					bucket.setConfidence((confTrue - confFalse)/sumAllSrcCube);
				}
			}
		}
	}
}
