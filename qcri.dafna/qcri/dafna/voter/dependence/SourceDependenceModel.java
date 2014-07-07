package qcri.dafna.voter.dependence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataFormatter.DataComparator;
import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;
import qcri.dafna.voter.Voter;
import qcri.dafna.voter.VoterParameters;

public class SourceDependenceModel extends Voter {

	protected final double alfa;
	protected final double c;
//	final double ita;
	final int n;
	final double base_sim = 0.5;
	final double similarityConstant;
	/*------ Luna Impl----------*/
//	private double aprioriSameValueProbability;
//	private double sameValueProbability;
//	private double sameCorrectValueProbability;
//	private double sameIncorrectValueProbability;
	/** A-priori probability of two data sources being independent. */
//	private final double APRIORI_INDEPENDENCE_PROBABILITY = 0.8;
	/** A-priori probability of independence. */
//	private double aprioriIndependenceProbability = APRIORI_INDEPENDENCE_PROBABILITY;
	/** A cached result for <code>1/aprioriIndependenceProbability-1</code>. */
//	private double aprioriIndependenceProbabilityPrime = 1/aprioriIndependenceProbability-1;
	/*------ Luna Impl----------*/


	private final boolean considerSimilarity;
	private final boolean considerSourcesAccuracy;
	private final boolean computeNormalDependency;
	/**
	 * The dependencies between sources:
	 * the key is the concatenation of source1 id and source2 id;
	 */
	private HashMap<String, HashMap<String, Dependence>> sourcesDependencies;

	private final boolean orderSourcesByDependence;
	
	public SourceDependenceModel(DataSet dataSet, VoterParameters params,
			double alfa, double c, int n,
			double similarityConstant, boolean considerSimilarity, 
			boolean considerSourcesAccuracy, boolean considerDependency, boolean orderSrcByDependence) {
		super(dataSet, params);
		this.considerSimilarity = considerSimilarity;
		this.considerSourcesAccuracy = considerSourcesAccuracy;
//		this.base_sim = base_sim;
		this.similarityConstant = similarityConstant;
		this.alfa = alfa;
		this.c = c;
		this.n = n;
		this.orderSourcesByDependence = orderSrcByDependence;

		this.computeNormalDependency = considerDependency;
	}

	private void initDependency(DataSet dataSet, boolean computeNormalDependency) {
		Dependence tempDepen;
		HashMap<String, Dependence> dependencies;
		initTrueClaimMajority();
//				initTrueClaimRandom();
		if (computeNormalDependency) {
			sourcesDependencies = new HashMap<String, HashMap<String, Dependence>>();
			for (Source s1 : dataSet.getSourcesHash().values()) {
				for (Source s2 : dataSet.getSourcesHash().values()) {
					if (s1.getSourceIdentifier().equals(s2.getSourceIdentifier())) {
						continue;
					}
					if (sourcesDependencies.containsKey(s2.getSourceIdentifier())) {
						dependencies = sourcesDependencies.get(s2.getSourceIdentifier());
						if (dependencies.containsKey(s1.getSourceIdentifier())) {
							continue;
						}
					}
					dependencies = sourcesDependencies.get(s1.getSourceIdentifier());
					if (dependencies == null) {
						dependencies = new HashMap<String, Dependence>();
						sourcesDependencies.put(s1.getSourceIdentifier(), dependencies);
					} else if (dependencies.containsKey(s2.getSourceIdentifier())) {
						continue;
					}

					/* we don't compute the dependencies when 2 sources have zero common data item ?? */
					tempDepen = new Dependence(s1, s2);
					if (tempDepen.getNumOfdifferentValues() != 0 || tempDepen.getNumOfCommonFALSEValue() != 0 
							|| tempDepen.getNumOfCommonTRUEValue() != 0) {
						dependencies.put(s2.getSourceIdentifier(), tempDepen);
					}
				}
			}
		}
	}

	private void initTrueClaimMajority() {
		int numOfSrc, temp;
		ValueBucket maxVoteBucket;
		for (List<ValueBucket> bucketList : dataSet.getDataItemsBuckets().values()) {
			numOfSrc = bucketList.get(0).getSourcesKeys().size();
			maxVoteBucket = bucketList.get(0);
			for (ValueBucket b : bucketList) {
				temp = b.getSourcesKeys().size();
				if (temp > numOfSrc) {
					maxVoteBucket = b;
					numOfSrc = temp;
				}
			}
			for (SourceClaim claim : maxVoteBucket.getClaims()) {
				claim.setTrueClaimByVoter(true);
			}
		}
	}

	private void initTrueClaimRandom() {
		Random rand = new Random();
		ValueBucket maxVoteBucket;
		for (List<ValueBucket> bucketList : dataSet.getDataItemsBuckets().values()) {
			int random = Math.abs(rand.nextInt() % bucketList.size());
			maxVoteBucket = bucketList.get(random);
			for (SourceClaim claim : maxVoteBucket.getClaims()) {
				claim.setTrueClaimByVoter(true);
			}
		}
	}

	@Override
	protected void initParameters() {
		singlePropertyValue = false; 
		onlyMaxValueIsTrue = true;
	}

	@Override
	protected int runVoter(boolean convergence100) {
		initDependency(dataSet, computeNormalDependency);
		int numOfIteration = 0;
		double newTrustCosinSim, oldTrustCosinSim = 0;
		double newConfCosinSim, oldConfCosinSim = 0;
		boolean continueComputation = true;

		/**
		 * The model starts by initializing the sources trustworthiness.
		 * so we need to compute the confidence.
		 */
		if (computeNormalDependency) {
			computeDependencies(true);
		}

		while (continueComputation && numOfIteration < Globals.maxIterationCount) {
			numOfIteration ++;

			computeConfidence();
			if (considerSimilarity) {
				computeConfidenceWithSimilarity();
			}
			computeSourcesTrustworthiness();

			newTrustCosinSim = ConvergenceTester.computeTrustworthinessCosineSimilarity(dataSet);
			newConfCosinSim = ConvergenceTester.computeConfidenceCosineSimilarity(dataSet);
			//			System.out.print(numOfIteration + ": \t" + newTrustCosinSim + ". \t\t");
			
			computeMeasuresPerIteration(true, Math.abs(newTrustCosinSim - oldTrustCosinSim), Math.abs(newConfCosinSim - oldConfCosinSim));
			recomputTrueValues(); // the same implementation is done in the computePrecisionPerIteration
			if (computeNormalDependency) {
				computeDependencies(false);
			}

			if (convergence100) {
				if (numOfIteration > Globals.maxIterationCount) {
					continueComputation = false;
				}
			} else if (Math.abs(newTrustCosinSim - oldTrustCosinSim) < ConvergenceTester.convergenceThreshold) {
				continueComputation = false;
			}
			oldTrustCosinSim = newTrustCosinSim;
			oldConfCosinSim = newConfCosinSim;
		}
		return numOfIteration;
	}

	private void recomputTrueValues() {
		for (List<SourceClaim> claimList : dataSet.getDataItemClaims().values()) {
			for (SourceClaim claim : claimList) {
				claim.setTrueClaimByVoter(false);
			}
		}
		voterQuality.computeTruth(true);
		//		voterQuality.computeVoterQualityMeasures(singlePropertyValue);
		//		voterQuality.printMeasures();
	}
	/**
	 * source trustworthiness = source accuracy
	 */
	private void computeSourcesTrustworthiness() {
		double expConf;
		double tempSum;
		double sumOfProbOfBeingTrue;

		for (Source src : dataSet.getSourcesHash().values()) {
			sumOfProbOfBeingTrue = 0;
			for (SourceClaim claim : src.getClaims()) {
				expConf = Math.exp(claim.getBucket().getConfidence());
				tempSum = 0;
				for (ValueBucket bucket : dataSet.getDataItemsBuckets().get(claim.dataItemKey())) {
					tempSum = tempSum + Math.exp(bucket.getConfidence());
				}
				// this is the added N + 1 - |Vd|
				tempSum = tempSum + n + 1 - dataSet.getDataItemsBuckets().get(claim.dataItemKey()).size();
				if (Double.isInfinite(expConf)) {
					sumOfProbOfBeingTrue = sumOfProbOfBeingTrue + 1;
					if (Double.isNaN(sumOfProbOfBeingTrue)) {
						System.out.println();
					}
				} else {
					sumOfProbOfBeingTrue = sumOfProbOfBeingTrue + (expConf / tempSum);
					if (Double.isNaN(sumOfProbOfBeingTrue)) {
						System.out.println();
					}
				}
			}
			src.setOldTrustworthiness(src.getTrustworthiness());
			src.setTrustworthiness(sumOfProbOfBeingTrue/((double)src.getClaims().size()));
			//			System.out.println(src.getSourceIdentifier() + ": " + src.getTrustworthiness());
		}
		//		System.out.println("-------------------------");
	}

	private void computeConfidence() {
		double conf;
		double SrcTrustScore;
		double voteCount;
		List<Source> orderedSources;
		List<Source> pre;

		for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket bucket : bucketsList) {
				pre = new ArrayList<Source>();
				if (computeNormalDependency) {
					if (orderSourcesByDependence) {
						orderedSources = orderListByDependence(bucket.getSources());
					} else {
						orderedSources = orderListByName(bucket.getSources());
					} 
				} else {
					orderedSources = new ArrayList<Source>(bucket.getSources());
				}

				conf = 0;
				for (int i = orderedSources.size()-1; i >=0; i--) {
					Source src = orderedSources.get(i);
					if (considerSourcesAccuracy) {
						SrcTrustScore = computeTrustworthinessScore(src);
					} else {
						SrcTrustScore = 1;
					}
					if (computeNormalDependency) {
						voteCount = calculateVoteCount(src, pre);
					} else {
						voteCount = 1;
					}
					conf = conf + (SrcTrustScore * voteCount);
					
					if (Double.isNaN(conf)) {
						System.out.println();
					}
					pre.add(src);
				}
				bucket.setConfidence(conf);
			}
		}
	}

	protected double calculateVoteCount(Source src1, List<Source> pre) {
		if (pre.isEmpty()) {
			return 1.0;
		}
		double vote = 1;
		Dependence depen;
		HashMap<String, Dependence> tempDepen;
		double copyingProbability;
		for (Source s2 : pre) {
			tempDepen = sourcesDependencies.get(src1.getSourceIdentifier());
			if (tempDepen != null && tempDepen.containsKey(s2.getSourceIdentifier())) {
				depen = tempDepen.get(s2.getSourceIdentifier());
			} else {
				depen = sourcesDependencies.get(s2.getSourceIdentifier()).get(src1.getSourceIdentifier());
			}
			copyingProbability = depen.getDependence();
			vote = vote * (1 - (c * copyingProbability));
			if (vote < 0) {
				System.out.println("-");
			}
		}
		return vote;
	}
	private void computeConfidenceWithSimilarity() {
		double similarity;
		double similaritySum;
		for (List<ValueBucket> bucketList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket bucket1 : bucketList) {
				if (Double.isInfinite(bucket1.getConfidence())) {
					continue;  /* As Per Luna's implementation */
				}
				similaritySum = 0;
				for (ValueBucket bucket2 : bucketList) {
					if (Double.isInfinite(bucket2.getConfidence())) {
						continue;  /* As Per Luna's implementation */
					}
					// test if same bucket and continue
					if (bucket1.getId() == bucket2.getId()) {
						continue;
					}
					similarity = computeClaimsSimilarity(bucket1, bucket2);
					if(similarity != 0) {
						similaritySum = similaritySum + (bucket2.getConfidence() * similarity);
						if(Double.isNaN(similaritySum)) {
							System.out.println();
						}
					}
				}
				/*
				 * compute the similarity based on the confidence without similarity
				 */
				similaritySum = (similarityConstant * similaritySum) + bucket1.getConfidence();
				if(Double.isNaN(similaritySum)) {
					System.out.println();
				}
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
		return (double)result;
	}
	private List<Source> orderListByName(Set<Source> sList) {
		List<Source> sourcesList = new ArrayList<Source>(sList);
		List<Source> pre = new ArrayList<Source>();
		String keyMax = sourcesList.get(0).getSourceIdentifier();
		int imax = 0;
		Source max;
		while( ! sourcesList.isEmpty()) {
			keyMax = sourcesList.get(0).getSourceIdentifier();
			imax = 0;
			for (int i = 0 ; i < sourcesList.size(); i++) {
				if (sourcesList.get(i).getSourceIdentifier().compareTo(keyMax) >= 0) {
					keyMax = sourcesList.get(i).getSourceIdentifier();
					imax = i;
				}
			}
			max = sourcesList.get(imax);
			sourcesList.remove(imax);
			pre.add(max);
		}
		return pre;
	}
	private List<Source> orderListByDependence(Set<Source> sList) {
		List<Source> sourcesList = new ArrayList<Source>(sList);
		List<Source> pre = new ArrayList<Source>();
		pre.add(getSrcWithMaxDependence(sourcesList, sourcesList));
		Source tempSource;
		boolean duplicate;
		while (! sourcesList.isEmpty()) {
			tempSource = getSrcWithMaxDependence(sourcesList, pre);
			duplicate = false;
			for (Source s : pre) {
				if (tempSource.getSourceIdentifier().equals(s.getSourceIdentifier())) {
					duplicate = true;
					break;
				}
			}
			if (! duplicate) {
				pre.add(tempSource);
			}
		}
		return pre;
	}


	/**
	 * Return the source from the sourcesList with maximal dependency with another source from preList .
	 * remove this source from the given list
	 * @param sList
	 * @param preList
	 * @return
	 */
	protected Source getSrcWithMaxDependence(List<Source> sourcesList, List<Source> preList) {
		Source source1 = sourcesList.get(0);
		if (sourcesList.size() == 1) {
			sourcesList.remove(0);
			return source1;
		}
		Source source2 = preList.get(0);
		if (source2.getSourceIdentifier().equals(source1.getSourceIdentifier())) {
			/*This is only for the first call of this method when we assign the same list for the 2 parameters
			 * we are sure preList.size > 1 as it passed the check at the beginning of the method*/
			source2 = preList.get(1);
		}
		int maxSourceIndex = 0;
		HashMap<String, Dependence> tempDepen;
		tempDepen = sourcesDependencies.get(source1.getSourceIdentifier());
		Dependence depen;
		double maxDep;
		if (tempDepen != null && tempDepen.containsKey(source2.getSourceIdentifier())) {
			depen = tempDepen.get(source2.getSourceIdentifier());
			maxDep = depen.getProbabilityS1CopyingS2();
		} else {
			depen = sourcesDependencies.get(source2.getSourceIdentifier()).get(source1.getSourceIdentifier());
			maxDep = depen.getProbabilityS2CopyingS1();
		}


		double dep;
		for ( int i = 0 ; i < sourcesList.size(); i ++) {
			source1 = sourcesList.get(i);
			for (Source s2 : preList) {
				if (source1.getSourceIdentifier().equals(s2.getSourceIdentifier())) { // this shouldn't happen
					continue;
				}
				tempDepen = sourcesDependencies.get(source1.getSourceIdentifier());
				if (tempDepen != null && tempDepen.containsKey(s2.getSourceIdentifier())) {
					depen = tempDepen.get(s2.getSourceIdentifier());
					dep = depen.getProbabilityS1CopyingS2();
				} else {
					depen = sourcesDependencies.get(s2.getSourceIdentifier()).get(source1.getSourceIdentifier());
					dep = depen.getProbabilityS2CopyingS1();
				}
				if (dep > maxDep) {
					maxDep = dep;
					maxSourceIndex = i;
				}
			}
		}
		//		/////////////////////
		List<Source> sources = new ArrayList<Source>();
		for ( int i = 0 ; i < sourcesList.size(); i ++) {
			source1 = sourcesList.get(i);
			for (Source s2 : preList) {
				if (source1.getSourceIdentifier().equals(s2.getSourceIdentifier())) {
					continue;
				}
				tempDepen = sourcesDependencies.get(source1.getSourceIdentifier());
				if (tempDepen != null && tempDepen.containsKey(s2.getSourceIdentifier())) {
					depen = tempDepen.get(s2.getSourceIdentifier());
					dep = depen.getProbabilityS1CopyingS2();
				} else {
					depen = sourcesDependencies.get(s2.getSourceIdentifier()).get(source1.getSourceIdentifier());
					dep = depen.getProbabilityS2CopyingS1();
				}
				if (dep == maxDep) {
					sources.add(source1);
				}
			}
		}
		//
		source1 = sources.get(0);
		source2 = preList.get(0);
		if (source2.getSourceIdentifier().equals(source1.getSourceIdentifier())) {
			source2 = preList.get(1);
		}
		tempDepen = sourcesDependencies.get(source1.getSourceIdentifier());
		if (tempDepen != null && tempDepen.containsKey(source2.getSourceIdentifier())) {
			depen = tempDepen.get(source2.getSourceIdentifier());
		} else {
			depen = sourcesDependencies.get(source2.getSourceIdentifier()).get(source1.getSourceIdentifier());
		}
		maxDep = depen.getDependence();
		maxSourceIndex = 0;
		for ( int i = 0 ; i < sources.size(); i ++) {
			source1 = sources.get(i);
			for (Source s2 : preList) {
				if (source1.getSourceIdentifier().equals(s2.getSourceIdentifier())) { // this shouldn't happen
					continue;
				}
				tempDepen = sourcesDependencies.get(source1.getSourceIdentifier());
				if (tempDepen != null && tempDepen.containsKey(s2.getSourceIdentifier())) {
					depen = tempDepen.get(s2.getSourceIdentifier());
				} else {
					depen = sourcesDependencies.get(s2.getSourceIdentifier()).get(source1.getSourceIdentifier());
				}
				dep = depen.getDependence();
				if (dep > maxDep) {
					maxDep = dep;
					maxSourceIndex = i;
				}
			}
		}
		String maxDepenSrcId = sources.get(maxSourceIndex).getSourceIdentifier();
		for (int i = 0 ; i < sourcesList.size(); i++) {
			if (sourcesList.get(i).getSourceIdentifier().equals(maxDepenSrcId)) {
				source1 = sourcesList.get(i);
				sourcesList.remove(i);
				return source1;
			}
		}
		///////////////////
		//		sourcesList.remove(maxSourceIndex);
		return null;
	}
	protected void computeDependencies(boolean firstRound) {
		HashMap<String, Dependence> depen;
		for (String src1 : sourcesDependencies.keySet()) {
			depen = sourcesDependencies.get(src1);
			for (String src2 : depen.keySet()) {
				//				depen.get(src2).computeUndDirectionalDependency(n, alfa, ita, c);
				depen.get(src2).computeDirectionalDependency3(n, alfa, c, 
						dataSet.getSourcesHash().get(src1), dataSet.getSourcesHash().get(src2));
				//				depen.get(src2).computeDirectionalDependence2( c, n, aprioriIndependenceProbability, firstRound,
				//						sameValueProbability, aprioriIndependenceProbabilityPrime, dataSet, 
				//						dataSet.getSourcesHash().get(src1), dataSet.getSourcesHash().get(src2));
			}
		}
	}




	/**
	 * 
	 * @param source
	 * @param n: number of false values for the data item we are computing the confidence for.
	 * @return
	 */
	private double computeTrustworthinessScore(Source source) {
		double score = ((double)n) * source.getTrustworthiness() / (1 - source.getTrustworthiness());
		score = Math.log(score);
		return score;
	}
}