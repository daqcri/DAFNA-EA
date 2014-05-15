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

/**
 * The 3-Estimates Voter
 * @author dalia
 *
 */
public class ThreeEstimate_Old extends Voter {

	private final double normalizationWeight;

	private int max;
	public ThreeEstimate_Old(DataSet dataSet, int iterationCount, VoterParameters params) {
		super(dataSet, params);
		this.normalizationWeight = -1;
		max = iterationCount;
	}

	/**
	 * The error on the view is presented in the variable : source.trustworthiness
	 * The confidence of a fact is presented in: bucket.confidence
	 * the error on a fact is presented in: bucket.errorFactor
	 * @return
	 */
	@Override
	protected void initParameters() {
		singlePropertyValue = false; 
		onlyMaxValueIsTrue = true;
	}

	@Override
	protected int runVoter(boolean convergence100) {
		dataSet.resetDataSet(1,0,0.1);
		boolean continueComputation = true;
		double trustworthinessCosineSimilarity = 0.0;
		double newCosineSimilarity;
		double confidenceCosineSimilarity = 0.0;
		double newConfCosineSimilarity;
		int i = 0;

		while (i < max) {//(continueComputation) {
			i++;
			computeConfidence();
			normalizeConfidence();

			computeErrorFactor();
			normalizeErrorFactor();

			computeTrustworthiness();
			normalizeTrustWorthiness();

			newCosineSimilarity = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			newConfCosineSimilarity = ConvergenceTester.computeConfidenceCosineSimilarity(dataSet);
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
			normalizeConfidence();
			normalizeErrorFactor();
			normalizeTrustWorthiness();
		}
		return i;
	}

	private void computeConfidence() {
		Set<String> allSources;
		Set<String> positiveSources;
		Set<String> negativeSources;
		double errorFactor;
		double posSrcTrSum; 
		double negSrcTrSum;
		double conf;
		for (List<ValueBucket> buckets : dataSet.getDataItemsBuckets().values()) {
			allSources = new HashSet<String>();
			for (ValueBucket b : buckets) {
				allSources.addAll(b.getSourcesKeys());
			}
			for (ValueBucket b : buckets) {
				errorFactor = b.getErrorFactor();

				positiveSources = new HashSet<String>();
				positiveSources.addAll(b.getSourcesKeys());

				negativeSources = new HashSet<String>();
				negativeSources.addAll(allSources);
				negativeSources.removeAll(positiveSources);

				posSrcTrSum = 0;
				for (String srcKey: positiveSources) {
					posSrcTrSum = posSrcTrSum + 1 - (errorFactor * dataSet.getSourcesHash().get(srcKey).getTrustworthiness());
				}
				negSrcTrSum = 0;
				for (String srcKey: negativeSources) {
					negSrcTrSum = negSrcTrSum + (errorFactor * dataSet.getSourcesHash().get(srcKey).getTrustworthiness());
				}
				conf = (posSrcTrSum + negSrcTrSum) / allSources.size();
				b.setConfidence(conf);
			}
		}
	}

	private void computeErrorFactor() {
		Set<String> allSources;
		Set<String> positiveSources;
		Set<String> negativeSources;
		double errorFactor;
		double posSrcTrSum; 
		double negSrcTrSum;
		double conf;
		for (List<ValueBucket> buckets : dataSet.getDataItemsBuckets().values()) {
			allSources = new HashSet<String>();
			for (ValueBucket b : buckets) {
				for (SourceClaim claim : b.getClaims()) {
					if (claim.getSource().getTrustworthiness() != 0) {
						allSources.add(claim.getSource().getSourceIdentifier());//TODO
					}
				}
			}
			// TODO this is not sure the right solution
			if (allSources.size() == 0) {
				continue;
			}
			for (ValueBucket b : buckets) {
				conf = b.getConfidence();

				positiveSources = new HashSet<String>();
				for (SourceClaim claim : b.getClaims()) {
					if (claim.getSource().getTrustworthiness() != 0) {
						positiveSources.add(claim.getSource().getSourceIdentifier());//TODO
					}
				}

				negativeSources = new HashSet<String>();
				negativeSources.addAll(allSources);
				negativeSources.removeAll(positiveSources);

				posSrcTrSum = 0;
				for (String srcKey: positiveSources) {
					posSrcTrSum = posSrcTrSum + ( (1 - conf) / dataSet.getSourcesHash().get(srcKey).getTrustworthiness());
				}
				negSrcTrSum = 0;
				for (String srcKey: negativeSources) {
					negSrcTrSum = negSrcTrSum + (conf / dataSet.getSourcesHash().get(srcKey).getTrustworthiness());
				}
				if (allSources.size() == 0) {
					errorFactor = 0;
				} else  {
					errorFactor = (posSrcTrSum + negSrcTrSum) / allSources.size();
				}
				b.setErrorFactor(errorFactor);
			}
		}
	}
	private void normalizeErrorFactor() {
		double min, max;
		min = dataSet.getDataItemsBuckets().values().iterator().next().get(0).getErrorFactor(); // !! must be done to initialize the min and max !!
		max = min;
		double v1,v2;
		for (List<ValueBucket> buckets : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : buckets) {
				if (max < b.getErrorFactor()) {
					max = b.getErrorFactor();
				} else if (min > b.getErrorFactor()) {
					min = b.getErrorFactor();
				}
			}
		}
		for (List<ValueBucket> buckets : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : buckets) {
				v1 = (b.getErrorFactor() - min)/ (max - min);
				v2 = Math.round(b.getErrorFactor());
				v1 = (normalizationWeight * v1) + ((1-normalizationWeight)*v2);
				b.setErrorFactor(v1);
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
		Set<String> dataItemsKey;
		double posOneOverError;
		double posConfidenceOverError;
		double negConfidenceOverError;
		double allConfOverError;
		int numOfValues;
		double tr;
		for (Source source : dataSet.getSourcesHash().values()) {
			dataItemsKey = new HashSet<String>();
			posOneOverError = 0;
			posConfidenceOverError = 0;
			negConfidenceOverError = 0;
			allConfOverError = 0;
			numOfValues = 0;
			for (SourceClaim claim : source.getClaims()) {
				if (claim.getBucket().getErrorFactor() != 0) {
					posOneOverError = posOneOverError + (1/claim.getBucket().getErrorFactor());
					posConfidenceOverError = posConfidenceOverError + (claim.getBucket().getConfidence()/claim.getBucket().getErrorFactor());
					if ( ! dataItemsKey.contains(claim.dataItemKey())) {
						dataItemsKey.add(claim.dataItemKey());
						for (ValueBucket b : dataSet.getDataItemsBuckets().get(claim.dataItemKey())) {
							if (b.getErrorFactor() != 0) {
								allConfOverError = allConfOverError + (b.getConfidence()/b.getErrorFactor());
								numOfValues ++;
							}
						}
					}

				}
			}
			negConfidenceOverError = allConfOverError - posConfidenceOverError;
			if (numOfValues == 0) {
				tr = 0;
			} else {
				tr = ((posOneOverError - posConfidenceOverError) + negConfidenceOverError) / numOfValues;
			}
			source.setOldTrustworthiness(source.getTrustworthiness());
			source.setTrustworthiness(tr);
		}
	}
}
