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
 * Guess LCA Voter
 * @author dalia, Laure
 *
 */
public class GuessLCA extends Voter {

	private double pym;

	public GuessLCA(DataSet dataSet, VoterParameters params, double beta1LCA) {
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
		while (continueComputation  && numOfIteration < Globals.iterationCount) {
			computeConfidence(); 
			computeTrustworthiness();
			newTrustCosinSim = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			newconfCosineSim = ConvergenceTester.computeConfidenceCosineSimilarity(dataSet);
			if (convergence100) {
				if (numOfIteration > Globals.iterationCount) {
					continueComputation = false;
				}
			} else {
				continueComputation = !( (Math.abs(newconfCosineSim-oldConfCosineSim) < ConvergenceTester.convergenceThreshold) &&
						(Math.abs(newTrustCosinSim-oldTrustCosinSim) < ConvergenceTester.convergenceThreshold));
			}
			numOfIteration ++;
			computeMeasuresPerIteration(true, Math.abs(newTrustCosinSim-oldTrustCosinSim), Math.abs(newconfCosineSim-oldConfCosineSim));
			oldTrustCosinSim = newTrustCosinSim;
			oldConfCosineSim = newconfCosineSim;
		}
		return numOfIteration;
	}

	private void computeConfidence() {
		double conf;
		double temp;
		double confidenceSum;
		double pGuess;

		for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
			
			confidenceSum = 0;
			for (ValueBucket bucket : bucketsList) {
				pGuess = bucket.getErrorFactor(); // the guess probability of each claim bucket is saved in the error facto field
				conf = pym;
				for (Source s : bucket.getSources()) {
					temp = s.getTrustworthiness() + ( ( 1 - s.getTrustworthiness() ) * pGuess );
					conf = conf * temp ;
					if (Double.isNaN(conf)) {
						System.out.println();
					}
				}
				for (String key : bucket.getDisagreeingSourcesKeys()) {
					Source s = dataSet.getSourcesHash().get(key);
					conf = conf * ( (1.0 - s.getTrustworthiness()) * pGuess );
					if (Double.isNaN(conf)) {
						System.out.println();
					}
				}
				bucket.setConfidenceWithSimilarity(bucket.getConfidence());
				bucket.setConfidence(conf);
				confidenceSum = confidenceSum + conf;
				
			}
			/** Compute the expectation-step equation */
			for (ValueBucket bucket : bucketsList) {
				bucket.setConfidence(bucket.getConfidence()/confidenceSum);
				if (Double.isNaN(bucket.getConfidence())) {
					System.out.println();
				}
			}
		}
	}

	private void computeTrustworthiness() {
		double numerator, denom;
		double pGuess ;// the bucket.error factor contains the guess probability for each claim bucket

		for (Source s : dataSet.getSourcesHash().values()) {
			numerator = 0;
			denom = 0;
			for (SourceClaim claim : s.getClaims()) {
				numerator = numerator + claim.getBucket().getConfidence();
				for (ValueBucket b : dataSet.getDataItemsBuckets().get(claim.dataItemKey())) {
					pGuess = b.getErrorFactor();
					if (pGuess != 1) {
						// transfor the pguess into the needed value in the equation
						pGuess = pGuess / (1 - pGuess);
					}
					if (Double.isNaN(pGuess)) {
						System.out.println();
					}
					numerator = numerator + ( b.getConfidence() * pGuess);
					denom = denom + b.getConfidence();
				}
				/**
				 *  Remove the value for (claim.conf * pGuess), it was added in the last loop without need 
				 **/
				pGuess = claim.getBucket().getErrorFactor();
				if (pGuess != 1) {
					pGuess = pGuess / (1 - pGuess);
				}
				numerator = numerator - (claim.getBucket().getConfidence() * pGuess);
				if (Double.isNaN(numerator)) {
					System.out.println();
				}
				/* */
			}
			s.setOldTrustworthiness(s.getTrustworthiness());
			if (Double.isNaN(numerator/denom)) {
				System.out.println();
			}
			s.setTrustworthiness(numerator/denom);
		}
	}
	/**
	 * TODO choose which implementation to take for the guess probability calculation
	 */
	private void init() {
//		RandomGenerator rgA = new JDKRandomGenerator();
//		rgA.setSeed((int)(Math.random() * 100000000)); 
//		UniformRandomGenerator randomGeneratorA = new UniformRandomGenerator(rgA);
		for (Source s : dataSet.getSourcesHash().values()) {
			s.setOldTrustworthiness(0);
//			s.setTrustworthiness(normalizeNextRandom(randomGeneratorA));
			s.setTrustworthiness(startingTrust);
		}
		dataSet.initializeTheDisagreeingSources();
		// initialize the Pgess
		double numOfSrc, totalNumOfSrc;
		double pguess;
		for (List<ValueBucket>bList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : bList) {
				/* guess with the crowd */
				numOfSrc = b.getSources().size();
				totalNumOfSrc = numOfSrc + b.getDisagreeingSourcesKeys().size();
				pguess = numOfSrc / totalNumOfSrc;
				if (pguess == 0.5) {
					pguess = 0.51;
				}
				b.setErrorFactor(pguess);
				/* uniform prior guess */
//				pguess = (double)((double)1.0/(double)bList.size());
//				b.setErrorFactor(pguess); // this implementation give better results
			}
		}
	}
	private double normalizeNextRandom(UniformRandomGenerator generator) {
		double random = generator.nextNormalizedDouble() + Math.sqrt(3); /* the nextNormalizedDouble returns a value from -sqrt(3) to +sqrt(3)  */
		random = ((double)random/(2*Math.sqrt(3))); /* now random is from 0 to 1 */
		return random;
	}


}
