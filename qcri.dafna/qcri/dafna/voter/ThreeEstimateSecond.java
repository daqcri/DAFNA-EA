package qcri.dafna.voter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;
import qcri.dafna.dataModel.quality.voterResults.NormalVoterQualityMeasures;

public class ThreeEstimateSecond extends Voter {


	private final double normalizationWeight = 0.1;

	int max;
	public ThreeEstimateSecond(DataSet dataSet, int maxIterationCount, VoterParameters params) {
		super(dataSet, params);
		max = maxIterationCount;
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

	protected int runVoter(boolean convergence100) {
		dataSet.resetDataSet(1,0, 0.9);
		boolean continueComputation = true;
		double trustworthinessCosineSimilarity = 0.0;
		double newCosineSimilarity;
		int i = 0;

		while (i<max){// (continueComputation) {
			i++;
			computeConfidence();
			// TODO check this order is true (conf -> trustw. - > error) not (conf ->  error -> trustw. )
			computeTrustworthiness();
			computeErrorFactor();
//			normalizeErrorFactor();


			//			voterQuality.computePrecisionAndRecall();

			newCosineSimilarity = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			double cosineSimilarityDifference = Math.abs(trustworthinessCosineSimilarity - newCosineSimilarity);
			if (cosineSimilarityDifference <= ConvergenceTester.convergenceThreshold) {
				continueComputation = false;
			} else {
				trustworthinessCosineSimilarity = newCosineSimilarity;
			}
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
					posSrcTrSum = posSrcTrSum + (errorFactor * dataSet.getSourcesHash().get(srcKey).getTrustworthiness());
				}
				negSrcTrSum = 0;
				for (String srcKey: negativeSources) {
					negSrcTrSum = negSrcTrSum + 1 - (errorFactor * dataSet.getSourcesHash().get(srcKey).getTrustworthiness());
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
					if (claim.getSource().getTrustworthiness() != 1) {
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
					if (claim.getSource().getTrustworthiness() != 1) {
						positiveSources.add(claim.getSource().getSourceIdentifier());//TODO
					}
				}

				negativeSources = new HashSet<String>();
				negativeSources.addAll(allSources);
				negativeSources.removeAll(positiveSources);

				posSrcTrSum = 0;
				for (String srcKey: positiveSources) {
					posSrcTrSum = posSrcTrSum + ( conf / (1 - dataSet.getSourcesHash().get(srcKey).getTrustworthiness()));
				}
				negSrcTrSum = 0;
				for (String srcKey: negativeSources) {
					negSrcTrSum = negSrcTrSum + ((1-conf) / (1-dataSet.getSourcesHash().get(srcKey).getTrustworthiness()));
				}
				errorFactor = (posSrcTrSum + negSrcTrSum) / allSources.size();
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

	private void computeTrustworthiness() {
		Set<String> dataItemsKey;
		double posPart;
		double negpart;
		int numOfValues;
		double tr;
		for (Source source : dataSet.getSourcesHash().values()) {
			dataItemsKey = new HashSet<String>();
			posPart = 0;
			negpart = 0;
			numOfValues = 0;

			double temp;
			for (SourceClaim claim : source.getClaims()) {
				if (claim.getBucket().getErrorFactor() != 1) {
					posPart = posPart + (claim.getBucket().getConfidence()/(1-claim.getBucket().getErrorFactor()));
					temp = (1-claim.getBucket().getConfidence()) / (1-claim.getBucket().getErrorFactor());
					if ( ! dataItemsKey.contains(claim.dataItemKey())) {
						dataItemsKey.add(claim.dataItemKey());
						for (ValueBucket b : dataSet.getDataItemsBuckets().get(claim.dataItemKey())) {
							if (b.getErrorFactor() != 1) {
								negpart = negpart + ((1-b.getConfidence())/(1-b.getErrorFactor()));
								numOfValues ++;
							}
						}
					}
					negpart = negpart - temp;
				}

			}
			tr = (posPart + negpart)/numOfValues;
			source.setOldTrustworthiness(source.getTrustworthiness());
			source.setTrustworthiness(tr);
		}
	}




























}
