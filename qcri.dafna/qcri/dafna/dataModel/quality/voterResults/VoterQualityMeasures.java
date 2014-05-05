package qcri.dafna.dataModel.quality.voterResults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataFormatter.DataComparator;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher;
import qcri.dafna.dataModel.dataFormatter.PersonsNameComparator;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;

public abstract class VoterQualityMeasures {

	DataSet dataSet;
	VoterTimingMeasures timings;
	public List<Double> truePositivePercentagePerIteration;
	public List<Double> precisionPerIteration;
	public List<Double> accuracyPerIteration;
	public List<Double> recallPerIteration;
	public List<Double> specificityPerIteration;
	public List<Integer> truePositivePerIteration;
	public List<Integer> trueNegativePerIteration;
	public List<Integer> falsePositivePerIteration;
	public List<Integer> falseNegativePerIteration;
	private int truePositive = 0;
	private int trueNegative = 0;
	private int falsePositive = 0;
	private int falseNegative = 0;
	public List<Double> iterationEndingTime;
	public List<Double> trustCosineSimilarityPerIteration;
	public List<Double> confCosineSimilarityPerIteration;
	int numberOfIterations;
	double truePositivePercentage;
	double precision;
	double accuracy;
	double recall;
	double specificity;

	private long maxMemoryConsumption;
	/**
	 * The percentage of the output values that are 
	 * consistent with the gold standard.
	 */
	private double precisionOldDefinition;

	public long getMaxMemoryConsumption() {
		return maxMemoryConsumption;
	}
	public void setMaxMemoryConsumption(long maxMemoryConsumtion) {
		this.maxMemoryConsumption = maxMemoryConsumtion;
	}
	public double getPrecisionOldDefinition() {
		return precisionOldDefinition;
	}
	public VoterTimingMeasures getTimings() {
		return timings;
	}
	public void setNumberOfIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}
	public int getNumberOfIterations() {
		return numberOfIterations;
	}
	public double getPrecision() {
		return precision;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public double getRecall() {
		return recall;
	}
	public double getSpecificity() {
		return specificity;
	}
	public double getTruePositivePercentage() {
		return truePositivePercentage;
	}
	public List<Double> getPrecisionPerIteration() {
		return precisionPerIteration;
	}
	public List<Double> getAccuracyPerIteration() {
		return accuracyPerIteration;
	}
	public List<Double> getRecallPerIteration() {
		return recallPerIteration;
	}
	public List<Double> getSpecificityPerIteration() {
		return specificityPerIteration;
	}
	public List<Double> getIterationEndingTime() {
		return iterationEndingTime;
	}
	public List<Integer> getTruePositivePerIteration() {
		return truePositivePerIteration;
	}
	public List<Integer> getTrueNegativePerIteration() {
		return trueNegativePerIteration;
	}
	public List<Integer> getFalsePositivePerIteration() {
		return falsePositivePerIteration;
	}
	public List<Integer> getFalseNegativePerIteration() {
		return falseNegativePerIteration;
	}
	public int getTruePositive() {
		return truePositive;
	}
	public int getTrueNegative() {
		return trueNegative;
	}
	public int getFalsePositive() {
		return falsePositive;
	}
	public int getFalseNegative() {
		return falseNegative;
	}
	public List<Double> getTrustCosineSimilarityPerIteration() {
		return trustCosineSimilarityPerIteration;
	}
	public List<Double> getConfCosineSimilarityPerIteration() {
		return confCosineSimilarityPerIteration;
	}
	public List<Double> getTruePositivePercentagePerIteration() {
		return truePositivePercentagePerIteration;
	}


	public VoterQualityMeasures(DataSet dataSet) {
		this.dataSet = dataSet;
		timings = new VoterTimingMeasures();

		precisionPerIteration = new ArrayList<Double>();
		recallPerIteration = new ArrayList<Double>();
		specificityPerIteration = new ArrayList<Double>();
		accuracyPerIteration = new ArrayList<Double>();
		truePositivePercentagePerIteration = new ArrayList<Double>();

		truePositivePerIteration = new ArrayList<Integer>();
		trueNegativePerIteration = new ArrayList<Integer>();
		falsePositivePerIteration = new ArrayList<Integer>();
		falseNegativePerIteration = new ArrayList<Integer>();

		trustCosineSimilarityPerIteration = new ArrayList<Double>();
		confCosineSimilarityPerIteration = new ArrayList<Double>();

		iterationEndingTime = new ArrayList<Double>();
	}

	public void computeVoterQualityMeasures(boolean singlePropertyValue) {
		timings.startMeasuresComputationDuration();
		computePrecisionAndRecall();
		computePrecisionAccuracyRecallSpecificity(singlePropertyValue);
		timings.endMeasuresComputationDuration();
	}

	/*--------------  --------------*/
	void computePrecisionAccuracyRecallSpecificity(boolean singlePropertyValue) {
		truePositive = 0;
		trueNegative = 0;
		falsePositive = 0;
		falseNegative = 0;

		DataItemMeasures dataItemMeasures;
		List<ValueBucket> bucketsList;
		List<String> wrongNames;
		List<String> goldStandardNames;
		List<String> namesChosenByTheVoter = new ArrayList<String>();
		int common;
		for (String dataItemKey : dataSet.getDataItemsBuckets().keySet()) {
			dataItemMeasures = dataSet.getDataQualityMeasurments().getDataItemMeasures().get(dataItemKey);
			if (dataItemMeasures.getTrueValue() == null) {
				continue;
			}
			bucketsList = dataSet.getDataItemsBuckets().get(dataItemKey);
			if (bucketsList.get(0).getValueType().equals(ValueType.ListNames)) {
				namesChosenByTheVoter = new ArrayList<String>();
				wrongNames = new ArrayList<String>();
				goldStandardNames = new ArrayList<String>(Arrays.asList(((String)dataItemMeasures.getTrueValue()).split(Globals.cleanedListDelimiter)));
				/**
				 * this iteration compute both the TruePositive and the FalsePositive for the given data item.
				 */
				for (ValueBucket b : bucketsList) {
					if (b.getClaims().get(0).isTrueClaimByVoter()) {
						if ( ! singlePropertyValue) {
							namesChosenByTheVoter = new ArrayList<String>(Arrays.asList((b.getCleanedString()).split(Globals.cleanedListDelimiter)));
						} else {
							namesChosenByTheVoter.add(b.getCleanedString());
						}
					} else {
						for (String name : Arrays.asList((b.getCleanedString()).split(Globals.cleanedListDelimiter))) {
							if (PersonsNameComparator.containName(goldStandardNames, name)) {
								continue;
							} else if ( ! PersonsNameComparator.containName(wrongNames, name)) {
								wrongNames.add(name);
							}
						}
					}
				}
				for (String name : namesChosenByTheVoter) {
					if (PersonsNameComparator.containName(goldStandardNames, name)) {  /** Gold standard says yes AND voter says yes */
						truePositive ++;
					} else { /** Gold standard says NO AND voter says yes */
						falsePositive ++;
						if (! PersonsNameComparator.containName(wrongNames, name)) {
							wrongNames.add(name);
						}
					}
				}

				common = PersonsNameComparator.commonCount(goldStandardNames, namesChosenByTheVoter);
				falseNegative += goldStandardNames.size() - common; // we increment the FN with the intersection of Gold Standard and names chosen by the voter
				common = PersonsNameComparator.commonCount(wrongNames, namesChosenByTheVoter);
				trueNegative += wrongNames.size() - common;
			} else {
				for (ValueBucket b : bucketsList) {
					if (bucketContainsValue(b, dataItemMeasures)) {/** if gold standard says it is true */
						if (b.getClaims().get(0).isTrueClaimByVoter()) {/** if voter says it is true */
							truePositive++;
						} else {/** if voter says it is false */
							falseNegative ++;
						}
					} else {/** gold standard says and it is false */
						if (b.getClaims().get(0).isTrueClaimByVoter()) {/** if voter says it is true */
							falsePositive ++;
						} else { /** if voter says and it is false */
							trueNegative ++;
						}
					}
				}
			}
		}
		precision = (double) truePositive / ((double)truePositive+(double)falsePositive);
		accuracy = ((double)truePositive+(double)trueNegative)/((double)truePositive+(double)trueNegative+(double)falsePositive+(double)falseNegative);
		recall = (double)truePositive/((double)truePositive+(double)falseNegative);
		specificity = (double)trueNegative/((double)falsePositive+(double)trueNegative);
		if (Double.isNaN(precision)) {
			precision = 0;
		}
		if (Double.isNaN(accuracy)) {
			accuracy = 0;
		}
		if (Double.isNaN(recall)) {
			recall = 0;
		}
		if (Double.isNaN(specificity)) {
			specificity = 0;
		}
		truePositivePercentage = ((double)truePositive)/(double)dataSet.getDataQualityMeasurments().getGoldStandardTrueValueCount();
	}
	/*-------------- precision - accuracy - recall - specificity --------------*/

	/*-------------- Precision - old definition --------------*/
	public void computePrecisionAndRecall() {
		int trueVoteCount = 0;
		double listAccuracy = 0.0;
		DataItemMeasures dataItemMeasures;
		List<ValueBucket> bucketsList;
		boolean trueChoice;
		int size1,size2;
		String s1,s2;
		for (String dataItemKey : dataSet.getDataItemsBuckets().keySet()) {
			dataItemMeasures = dataSet.getDataQualityMeasurments().getDataItemMeasures().get(dataItemKey);
			if (dataItemMeasures.getTrueValue() == null) {
				continue;
			}
			bucketsList = dataSet.getDataItemsBuckets().get(dataItemKey);
			if (bucketsList.get(0).getValueType().equals(ValueType.ListNames)) {
				s1 = "";
				for (ValueBucket b : bucketsList) {

					if (b.getClaims().get(0).isTrueClaimByVoter()) {

						s1 = b.getCleanedString();
						size1 = s1.split(Globals.cleanedListDelimiter).length;
						s2 = (String)dataItemMeasures.getTrueValue();
						size2 = s2.split(Globals.cleanedListDelimiter).length;
						if (size1 > size2) {
							listAccuracy = listAccuracy + 
									PersonsNameComparator.computePersonsNamesListSimilarity(s2, s1);
						} else {
							listAccuracy = listAccuracy + 
									PersonsNameComparator.computePersonsNamesListSimilarity(s1, s2);
						}
					}
				}
			} else {
				trueChoice = false;
				for (ValueBucket b : bucketsList) {
					if (b.getClaims().get(0).isTrueClaimByVoter()) {
						if (bucketContainsValue(b, dataItemMeasures)) {
							trueChoice = true;
							break;
						}
					}
				}
				if (trueChoice) {
					trueVoteCount ++;
				}
			}
		}
		precisionOldDefinition = ((double)trueVoteCount+listAccuracy) / (double)dataSet.getDataQualityMeasurments().getGoldStandardTrueValueCount();
	}
	/*-------------- Precision - old definition --------------*/

	/**
	 * returns true is the given bucket contain the true value associated with the given dataItemMeasures object
	 * @param b
	 * @param dataItemMeasures
	 * @return
	 */
	boolean bucketContainsValue(ValueBucket b, DataItemMeasures dataItemMeasures) {
		if(DataTypeMatcher.savedAsString(b.getValueType()) ||
				(! b.isClean() && !dataItemMeasures.isTrueValueCleaned())) {
			return DataComparator.hasSameValue(b.getCleanedString(), dataItemMeasures.getTrueValue(), ValueType.STRING);
		}
		if ( (! b.isClean() && dataItemMeasures.isTrueValueCleaned()) ||  (b.isClean() && !dataItemMeasures.isTrueValueCleaned())) {
			return false;
		}
		return DataComparator.valueInRange(dataItemMeasures.getTrueValue(), b.getMinValue(), b.getMaxValue(), b.getValueType());
	}

	/*------------------------------*/
	/**
	 * 
	 * @param onlyMaxValue If true, only one value will be considered true, the value with maximum confidence.
	 * If false, all values with confidence greater than or equal to 0.5 will be considered true by the voter.
	 */
	public void computeTruth(boolean onlyMaxValue) {
		for (List<SourceClaim> claimList : dataSet.getDataItemClaims().values()) {
			for (SourceClaim c : claimList) {
				c.setTrueClaimByVoter(false);
			}
		}
		ValueBucket max;
		if (onlyMaxValue) {
			for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
				max = bucketsList.get(0);
				for (ValueBucket b : bucketsList) {
					if (b.getConfidence() > max.getConfidence()) {
						max = b;
					}
				}
				for (SourceClaim claim : max.getClaims()) {
					claim.setTrueClaimByVoter(true);
				}
			}
		} else {
			for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
				for (ValueBucket b : bucketsList) {
					if (b.getConfidence() >= 0.5) {
						for (SourceClaim claim : b.getClaims()) {
							claim.setTrueClaimByVoter(true);
						}
					}
				}
			}
		}
	}
	/*------------------------------*/
	public void printMeasures() {
		//				System.out.println();
		System.out.println("precision: " + this.precision + "\n");
		System.out.println("accuracy: " + this.accuracy + "\n");
		System.out.println("recall: " + this.recall + "\n");
		System.out.println("specificity: " + this.specificity + "\n");
		System.out.println("Number of iterations = " + this.numberOfIterations);
		System.out.println("Measures computation duration: " + timings.getMeasuresComputationDuration() + " millisecond");
		System.out.println("Voter algorithm duration: " + timings.getVoterDuration() + " millisecond");
		System.out.println();
	}

}
