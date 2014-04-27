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
 * Guess LCA Voter
 * @author dalia
 *
 */
public class GuessLCA extends Voter {

	private double pym;
	double cosineSimilarityStoppingCondition = ConvergenceTester.convergenceThreshold;
	private double startingTrust = 0.8;
	public GuessLCA(DataSet dataSet,  double probabilityOfTruthexistance, double startingTrust) {
		super(dataSet);
		pym = probabilityOfTruthexistance;
		this.startingTrust = startingTrust;
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
				continueComputation = !( (Math.abs(newconfCosineSim-oldConfCosineSim) < cosineSimilarityStoppingCondition) &&
						(Math.abs(newTrustCosinSim-oldTrustCosinSim) < cosineSimilarityStoppingCondition));
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
				}
				for (String key : bucket.getDisagreeingSourcesKeys()) {
					Source s = dataSet.getSourcesHash().get(key);
					conf = conf * ( (1.0 - s.getTrustworthiness()) * pGuess );
				}
				bucket.setConfidenceWithSimilarity(bucket.getConfidence());
				bucket.setConfidence(conf);
				confidenceSum = confidenceSum + conf;
			}
			/** Compute the expectation-step equation */
			for (ValueBucket bucket : bucketsList) {
				bucket.setConfidence(bucket.getConfidence()/confidenceSum);
			}
		}
	}

	private void computeTrustworthiness() {
		double sumOfAllConf;
		double sumOfAllConfPerBucketList;
		double numerator, denom;
		double tempNumerator;
		double pGuess ;// the bucket.error factor contains the guess probability for each claim bucket

		boolean listContainsSource;
		for (Source s : dataSet.getSourcesHash().values()) {
			sumOfAllConf = 0;
			numerator = 0;
			denom = 0;
			for (SourceClaim claim : s.getClaims()) {
				numerator = numerator + claim.getBucket().getConfidence();
				for (ValueBucket b : dataSet.getDataItemsBuckets().get(claim.dataItemKey())) {
					pGuess = b.getErrorFactor();
//					if (pGuess != 1) {
//						// transfor the pguess into the needed value in the equation
//						pGuess = pGuess / (1 - pGuess);
//					}
					numerator = numerator + ( b.getConfidence() * pGuess);
					denom = denom + b.getConfidence();
				}
				/**
				 *  Remove the value for (claim.conf * pGuess), it was added in the last loop without need 
				 **/
				pGuess = claim.getBucket().getErrorFactor();
//				if (pGuess != 1) {
//					pGuess = pGuess / (1 - pGuess);
//				}
				numerator = numerator - (claim.getBucket().getConfidence() * pGuess);
				/* */
			}
			s.setOldTrustworthiness(s.getTrustworthiness());
			s.setTrustworthiness(numerator/denom);
//			for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
//				listContainsSource = false;
//				tempNumerator = 0;
//				sumOfAllConfPerBucketList = 0;
//				for (ValueBucket bucket : bucketsList) {
//					pGuess = bucket.getErrorFactor();
//					if (bucket.getSourcesKeys().contains(s.getSourceIdentifier())) {
//						listContainsSource = true;
//						tempNumerator= tempNumerator + bucket.getConfidence();
//					} else {
////						if (pGuess == 1) {
//							tempNumerator = tempNumerator + (bucket.getConfidence() * (pGuess));
////						} else {
////							tempNumerator = tempNumerator + (bucket.getConfidence() * (pGuess /(1-pGuess)));
////						}
//					}
//					sumOfAllConfPerBucketList = sumOfAllConfPerBucketList + bucket.getConfidence();
//				}
//				if (listContainsSource) {
//					sumOfAllConf = sumOfAllConf + sumOfAllConfPerBucketList;
//					numerator = numerator + tempNumerator;
//				}
//			}
//			s.setOldTrustworthiness(s.getTrustworthiness());
//			s.setTrustworthiness(numerator/sumOfAllConf);
		}
	}
	/**
	 * TODO choose which implementation to take for the guess probability calculation
	 */
	private void init() {
		//		dataSet.resetDataSet(0.5, 0, 0);
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
				b.setErrorFactor(numOfSrc / totalNumOfSrc);
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
