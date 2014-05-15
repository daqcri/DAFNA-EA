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

public class TwoEstimates extends Voter {
	private final double normalizationWeight; // The lower value the better results (equal result for 0.2 and less)

	public TwoEstimates(DataSet dataSet, VoterParameters params, double normalizationWeight) {
		super(dataSet, params);
		this.normalizationWeight = normalizationWeight;
	}

	@Override
	protected void initParameters() {
		singlePropertyValue = false; 
		onlyMaxValueIsTrue = true;
	}
	@Override
	protected int runVoter(boolean convergence100) {

		boolean continueComputation = true;
		double trustworthinessCosineSimilarity = 0.0;
		double newCosineSimilarity;
		double confidenceCosineSimilarity = 0.0;
		double newConfCosineSimilarity;
		int i = 0;

		while (continueComputation && i < Globals.iterationCount) {
			i++;
			computeConfidence();
			normalizeConfidence();
			computeTrustworthiness();
			normalizeTrustWorthiness();
			voterQuality.computePrecisionAndRecall();
			newCosineSimilarity = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			newConfCosineSimilarity = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			double cosineSimilarityDifference = Math.abs(trustworthinessCosineSimilarity - newCosineSimilarity);
			computeMeasuresPerIteration(true, cosineSimilarityDifference, Math.abs(confidenceCosineSimilarity - newConfCosineSimilarity));
			if (convergence100) {
				if (i > Globals.iterationCount) {
					continueComputation = false;
				}
			} else if (cosineSimilarityDifference <= ConvergenceTester.convergenceThreshold) {
				continueComputation = false;
			} 
			trustworthinessCosineSimilarity = newCosineSimilarity;
			confidenceCosineSimilarity = newConfCosineSimilarity;

		}

		return i;
	}

	private void computeConfidence() {
		double trueSrcTrustworthinessSum;
		double allSrcTrustworthinessSum;
		double falseSrcTrustworthinessSum;
		Set<String> allSrcId;
		Set<String> trueSrcId;
		double tempConfidence;
		String srcId;

		for (List<ValueBucket> dataItemBuckets : dataSet.getDataItemsBuckets().values()) {
			allSrcId = new HashSet<String>();
			allSrcTrustworthinessSum = 0.0;

			for (ValueBucket bucket : dataItemBuckets) {
				trueSrcTrustworthinessSum = 0.0;
				trueSrcId = new HashSet<String>();
				for (SourceClaim claim : bucket.getClaims()) {
					srcId = claim.getSource().getSourceIdentifier();
					if( ! allSrcId.contains(srcId)) {
						allSrcId.add(srcId);
						allSrcTrustworthinessSum = allSrcTrustworthinessSum + claim.getSource().getTrustworthiness();
					}
					if (! trueSrcId.contains(srcId)) {
						trueSrcId.add(srcId);
						trueSrcTrustworthinessSum = trueSrcTrustworthinessSum + claim.getSource().getTrustworthiness();
					}
				}
				bucket.setConfidence(trueSrcTrustworthinessSum);// = sum (srcTrustworthy_forAllArcProvidingThisValue)
			}
			for (ValueBucket bucket : dataItemBuckets) {
				falseSrcTrustworthinessSum = allSrcTrustworthinessSum - bucket.getConfidence();
				/**
				 * sum (1 - srcTrustworthy_forAllArcProvidingThisValue)
				 *  = sum (1)_forAllArcProvidingThisValue - sum (srcTrustworthy_forAllArcProvidingThisValue)
				 */
				tempConfidence = bucket.getClaims().size() - bucket.getConfidence();
				tempConfidence = (tempConfidence + falseSrcTrustworthinessSum) / allSrcId.size();
				bucket.setConfidence(tempConfidence);
			}

		}
	}

	private void normalizeConfidence() {
		double min, max;
		min = dataSet.getDataItemsBuckets().values().iterator().next().get(0).getConfidence(); // !! must be done to initialize the min and max !!
		max = min;
		double v1,v2;
		for (List<ValueBucket> buckets : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : buckets) {
				if (max < b.getConfidence()) {
					max = b.getConfidence();
				} else if (min > b.getConfidence()) {
					min = b.getConfidence();
				}
			}
		}
		for (List<ValueBucket> buckets : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : buckets) {
				v1 = (b.getConfidence() - min)/ (max - min);
				v2 = Math.round(b.getConfidence());
				v1 = (normalizationWeight * v1) + ((1-normalizationWeight)*v2);
				b.setConfidence(v1);
				//				v1 = Math.round(v1);
				//				b.setConfidence(v1);
			}
		}

	}

	private void normalizeTrustWorthiness() {
		double min, max;
		double v1,v2;
		min = dataSet.getSourcesHash().values().iterator().next().getTrustworthiness();
		max = min;
		for (Source source : dataSet.getSourcesHash().values()) {
			if (max < source.getTrustworthiness()) {
				max = source.getTrustworthiness();
			} else if (min > source.getTrustworthiness()) {
				min = source.getTrustworthiness();
			}
		}
		for (Source source : dataSet.getSourcesHash().values()) {
			v1 = (source.getTrustworthiness() - min)/ (max - min);
			v2 = Math.round(source.getTrustworthiness());
			v1 = (normalizationWeight * v1) + ((1-normalizationWeight)*v2);
			source.setTrustworthiness(v1);
		}
	}
	private void computeTrustworthiness() {
		double trueClaimsConfidenceSum;
		double falseClaimsConfidenceSum;
		int numOfClaims = 0; // the number of different values for all data items mentioned by this source
		Set<String> dataItemsKeySet;
		String dataItemsKey;
		for (Source source : dataSet.getSourcesHash().values()) {
			dataItemsKeySet = new HashSet<String>();
			trueClaimsConfidenceSum = 0.0;
			falseClaimsConfidenceSum = 0.0;
			for (SourceClaim claim : source.getClaims()) {
				dataItemsKey = claim.dataItemKey();
				trueClaimsConfidenceSum = trueClaimsConfidenceSum + 1 - claim.getBucket().getConfidence();
				if ( ! dataItemsKeySet.contains(dataItemsKey)) {
					for (ValueBucket falseBuckets : dataSet.getDataItemsBuckets().get(claim.dataItemKey())) {
						numOfClaims ++;
						falseClaimsConfidenceSum = falseClaimsConfidenceSum + falseBuckets.getConfidence();
					}
					dataItemsKeySet.add(dataItemsKey);
				} 
				falseClaimsConfidenceSum = falseClaimsConfidenceSum - claim.getBucket().getConfidence();
			}
			source.setOldTrustworthiness(source.getTrustworthiness());
			source.setTrustworthiness((trueClaimsConfidenceSum+falseClaimsConfidenceSum)/numOfClaims);
		}
	}
}
