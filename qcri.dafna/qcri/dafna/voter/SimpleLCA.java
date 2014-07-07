package qcri.dafna.voter;

import java.util.List;

import org.apache.commons.math3.random.UniformRandomGenerator;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;

/**
 * Simple LCA Voter.
 * @author dalia
 *
 */
public class SimpleLCA extends Voter {

	double pym;
	

	public SimpleLCA(DataSet dataSet, VoterParameters params, double beta1LCA) {
		super(dataSet, params);
		pym = beta1LCA;
	}

	@Override
	protected void initParameters() {
		singlePropertyValue = false;
		onlyMaxValueIsTrue = true;
	}

	@Override
	protected int runVoter(boolean convergence100) {
		int numOfIteration = 0;
		init();
		double oldTrustCosinSim = 0, oldConfCosineSim = 0;
		double newTrustCosinSim, newconfCosineSim;

		boolean continueComputation = true;
		while (continueComputation && numOfIteration < Globals.maxIterationCount) {
			computeConfidence();
			computeTrustworthiness();

			newTrustCosinSim = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			newconfCosineSim = ConvergenceTester.computeConfidenceCosineSimilarity(dataSet);
			continueComputation = !( (Math.abs(newconfCosineSim-oldConfCosineSim) < ConvergenceTester.convergenceThreshold) &&
					(Math.abs(newTrustCosinSim-oldTrustCosinSim) < ConvergenceTester.convergenceThreshold));
			if (convergence100) {
				if (numOfIteration > Globals.maxIterationCount) {
					continueComputation = false;
				} else {
					continueComputation = true;
				}
			}
			numOfIteration ++;
			computeMeasuresPerIteration(true, Math.abs(newTrustCosinSim-oldTrustCosinSim), Math.abs(newconfCosineSim-oldConfCosineSim));
			oldTrustCosinSim = newTrustCosinSim;
			oldConfCosineSim = newconfCosineSim;
		}

		return numOfIteration;
	}

	private void computeConfidence() {
		double temp;
		double numOfDiffClaims;
		double sumofAllConfidences;
		double tempWeight = 1.0;
		for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
			numOfDiffClaims = bucketsList.size();
			sumofAllConfidences = 0;

			for (ValueBucket bucket : bucketsList) {
				temp = pym;
				for (Source s : bucket.getSources()) {
					for (SourceClaim c : bucket.getClaims()) {
						if (c.getSource().getSourceIdentifier().equals(s.getSourceIdentifier())) {
							tempWeight = c.getWeight();
							break;
						}
					}
//					temp = temp * s.getTrustworthiness(); // without weight
					temp = temp * Math.pow(s.getTrustworthiness(), tempWeight);
				}
				for (String key : bucket.getDisagreeingSourcesKeys()) {
					for (SourceClaim c : bucket.getClaims()) {
						if (c.getSource().getSourceIdentifier().equals(key)) {
							tempWeight = c.getWeight();
							break;
						}
					}
					/*
					 * if numOfDiffClaims = 1, the it shouldn't be any disagreeing sources, then the denominator never zero.
					 */
//					temp = temp * ( (1 - dataSet.getSourcesHash().get(key).getTrustworthiness())/ (numOfDiffClaims-1)); // without weight
					temp = temp * 
							Math.pow(( (1 - dataSet.getSourcesHash().get(key).getTrustworthiness())/ (numOfDiffClaims-1)), tempWeight);
				}
				bucket.setConfidenceWithSimilarity(bucket.getConfidence());// save the old value before update
				bucket.setConfidence(temp);
				sumofAllConfidences = sumofAllConfidences + temp;
			}
			for (ValueBucket bucket : bucketsList) {
				if (Double.isNaN(bucket.getConfidence()/sumofAllConfidences)) {
					System.out.println();
				}
				bucket.setConfidence(bucket.getConfidence()/sumofAllConfidences);
			}
		}
	}

	private void computeTrustworthiness() {
		double trustworthiness;
		double weightSum;
		for (Source s : dataSet.getSourcesHash().values()) {
			trustworthiness = 0;
			weightSum = 0;
			for (SourceClaim claim : s.getClaims()) {
//				trustworthiness = trustworthiness + claim.getBucket().getConfidence(); // without weight
				trustworthiness = trustworthiness + Math.pow(claim.getBucket().getConfidence(), claim.getWeight());
				weightSum += claim.getWeight();
			}
			s.setOldTrustworthiness(s.getTrustworthiness());
			if (Double.isNaN(trustworthiness/(double)s.getClaims().size())) {
				System.out.println();
			}
			
//			s.setTrustworthiness(trustworthiness/(double)s.getClaims().size()); // without weight
			s.setTrustworthiness(trustworthiness/weightSum);
		}
	}

	private void init() {
		//		dataSet.resetDataSet(0.5, 0, 0);
//		RandomGenerator rgA = new JDKRandomGenerator();
//		rgA.setSeed(Math.round(20)); 
//		UniformRandomGenerator randomGeneratorA = new UniformRandomGenerator(rgA);
		for (Source s : dataSet.getSourcesHash().values()) {
			s.setOldTrustworthiness(0);
//			s.setTrustworthiness(normalizeNextRandom(randomGeneratorA));
			s.setTrustworthiness(startingTrust);
		}
		dataSet.initializeTheDisagreeingSources();
	}
	private double normalizeNextRandom(UniformRandomGenerator generator) {
		double random = generator.nextNormalizedDouble() + Math.sqrt(3); /* the nextNormalizedDouble returns a value from -sqrt(3) to +sqrt(3)  */
		random = ((double)random/(2*Math.sqrt(3))); /* now random is from 0 to 1 */
		return random;
	}
}
