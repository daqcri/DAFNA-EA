package qcri.dafna.voter;

import java.util.List;

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
public class ThreeEstimates extends Voter {
	private final double normalizationWeight; 

	public ThreeEstimates(DataSet dataSet, VoterParameters params, double normalizationWeight) {
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

			computeErrorFactor();
			normalizeErrorFactor();

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
		double pos = 0, neg = 0, numSrc = 0;
		double errorFactor;
		for (List<ValueBucket> bList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : bList) {
				numSrc = 0;
				pos = 0;
				neg = 0;
				errorFactor = b.getErrorFactor();
				for (Source s : b.getSources()) {
					pos = pos + 1 - (s.getTrustworthiness() * errorFactor);
					numSrc = numSrc +1;
				}
				for (ValueBucket b2 : bList) {
					if (b2.getId() == b.getId()) {
						continue;
					}
					for (Source s : b2.getSources()) {
						neg = neg + (s.getTrustworthiness() * errorFactor);
						numSrc = numSrc +1;
					}
				}
				double d = (pos+neg)/numSrc;
				if (Double.isNaN(d)) {
					System.out.println();// just for testing
				}
				b.setConfidence((pos+neg)/numSrc);
			}
		}
	}
	private void computeErrorFactor() {
		double pos = 0, neg = 0, numSrc = 0;
		double conf;
		for (List<ValueBucket> bList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : bList) {
				numSrc = 0;
				pos = 0;
				neg = 0;
				conf = b.getConfidence();
				for (Source s : b.getSources()) {
					if (s.getTrustworthiness() != 0) {
						pos = pos + ((1 - conf)/s.getTrustworthiness());
					}
					numSrc = numSrc +1;
				}
				for (ValueBucket b2 : bList) {
					if (b2.getId() == b.getId()) {
						continue;
					}
					for (Source s : b2.getSources()) {
						if (s.getTrustworthiness() != 0) {
							neg = neg + (conf/s.getTrustworthiness());
						}
						numSrc = numSrc + 1;
					}
					
				}
				b.setErrorFactor((pos+neg)/numSrc);
			}
		}
	}

	private void computeTrustworthiness() {
		double claimsNum;
		double pos = 0, neg = 0;
		for (Source src : dataSet.getSourcesHash().values()) {
			claimsNum = 0;
			pos = 0;
			neg = 0;
			for (SourceClaim claim : src.getClaims()) {
				if (claim.getBucket().getErrorFactor() != 0) {
					pos = pos + (1-(claim.getBucket().getConfidence()/claim.getBucket().getErrorFactor()));
				}
				claimsNum++;
				for (ValueBucket b : dataSet.getDataItemsBuckets().get(claim.dataItemKey())) {
					if (b.getId() == claim.getBucket().getId()) {
						continue;
					}
					if (b.getErrorFactor() != 0) {
						neg = neg + (b.getConfidence()/b.getErrorFactor());
					}
					claimsNum++;
				}
			}
			src.setOldTrustworthiness(src.getTrustworthiness());
			src.setTrustworthiness((pos+neg)/claimsNum);
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

}
