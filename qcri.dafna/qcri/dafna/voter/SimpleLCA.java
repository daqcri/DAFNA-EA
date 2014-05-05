package qcri.dafna.voter;

import java.util.List;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
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
	private final double cosineSimilarityStoppingCondition = 0.001;
	private double startingTrust = 0.8;

	public SimpleLCA(DataSet dataSet, double probabilityOfTruthexistance, double startingTrust) {
		super (dataSet);
		pym = probabilityOfTruthexistance;
		this.startingTrust = startingTrust;
	}

	@Override
	protected void initParameters() {
		singlePropertyValue = false;
		onlyMaxValueIsTrue = true;// TODO check this
	}

	@Override
	protected int runVoter(boolean convergence100) {
		int numOfIteration = 0;
		init();
		double oldTrustCosinSim = 0, oldConfCosineSim = 0;
		double newTrustCosinSim, newconfCosineSim;

		boolean continueComputation = true;
		while (continueComputation && numOfIteration < Globals.iterationCount) {
			computeConfidence();
			computeTrustworthiness();

			newTrustCosinSim = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			newconfCosineSim = ConvergenceTester.computeConfidenceCosineSimilarity(dataSet);
			continueComputation = !( (Math.abs(newconfCosineSim-oldConfCosineSim) < cosineSimilarityStoppingCondition) &&
					(Math.abs(newTrustCosinSim-oldTrustCosinSim) < cosineSimilarityStoppingCondition));
			if (convergence100) {
				if (numOfIteration > Globals.iterationCount) {
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
		for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
			numOfDiffClaims = bucketsList.size();
			sumofAllConfidences = 0;

			for (ValueBucket bucket : bucketsList) {
				temp = pym;
				for (Source s : bucket.getSources()) {
					temp = temp * s.getTrustworthiness();
				}
				for (String key : bucket.getDisagreeingSourcesKeys()) {
					/*
					 * if numOfDiffClaims = 1, the it shouldn't be any disagreeing sources, then the denominator never zero.
					 */
					temp = temp * ( (1 - dataSet.getSourcesHash().get(key).getTrustworthiness())/ (numOfDiffClaims-1));
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
		for (Source s : dataSet.getSourcesHash().values()) {
			trustworthiness = 0;
			for (SourceClaim claim : s.getClaims()) {
				trustworthiness = trustworthiness + claim.getBucket().getConfidence();
			}
			s.setOldTrustworthiness(s.getTrustworthiness());
			if (Double.isNaN(trustworthiness/(double)s.getClaims().size())) {
				System.out.println();
			}
			s.setTrustworthiness(trustworthiness/(double)s.getClaims().size());
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
