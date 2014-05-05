package qcri.dafna.dataModel.quality.voterResults;

import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;

public class BooleanDataVoterQualityMeasures extends VoterQualityMeasures {

	public BooleanDataVoterQualityMeasures(DataSet dataSet) {
		super(dataSet);
		
	}

	double falsePositiveMeasure;
	double truePositiveMeasure;
	double falseNegativeMeasure;
	double trueNegativeMeasure;
	/**
	 * Handling only boolean type data (ONLY true facts have their associated true claim, and wrong facts don't have any associated claims )
	 */
	int negativeCount = 0;
	@Override
	void computePrecisionAccuracyRecallSpecificity(boolean singlePropertyValue) {

		int truePositive = 0;
		int trueNegative = 0;
		int falsePositive = 0;
		int falseNegative = 0;

		DataItemMeasures dataItemMeasures;
		List<ValueBucket> bucketsList;
		for (String dataItemKey : dataSet.getDataItemsBuckets().keySet()) {
			dataItemMeasures = dataSet.getDataQualityMeasurments().getDataItemMeasures().get(dataItemKey);
			bucketsList = dataSet.getDataItemsBuckets().get(dataItemKey); // the list should contains only one bucket
			if (dataItemMeasures.getTrueValue() == null) {
				negativeCount++;
			}
			if (dataItemMeasures.getTrueValue() == null || (!(Boolean)dataItemMeasures.getTrueValue())) { /** gold standard says it is false */ 
				if (bucketsList == null || bucketsList.size() == 0) { /** no body voted for it */
					trueNegative ++;
				} else if (bucketsList.get(0).getClaims().get(0).isTrueClaimByVoter()) {/** if voter says it is true */
					falsePositive ++;
				} else { /** if voter says it is false */
					trueNegative ++;
				}
			} else {/** gold standard says it is true */ 
				if (bucketsList == null || bucketsList.size() == 0) { /** no body voted for it */
					falseNegative ++;
				} else if (bucketsList.get(0).getClaims().get(0).isTrueClaimByVoter()) {/** if voter says it is true */
					truePositive ++;
				} else { /** if voter says it is false */
					falseNegative ++;
				}
			}
			
		}
		int dataItemsCount = dataSet.getDataItemsBuckets().size();
		truePositiveMeasure = ((double) truePositive) / ((double)dataItemsCount);
		trueNegativeMeasure = ((double)trueNegative)/((double)dataItemsCount);
		falsePositiveMeasure = ((double)falsePositive)/ ((double)dataItemsCount);
		falseNegativeMeasure = (double)falseNegative/ ((double)dataItemsCount);
	}
	@Override
	public void printMeasures() {
				System.out.println();
//		System.out.print("Old precision: " + this.precisionOldDefinition + "\n");
				System.out.println("true Positive: " + this.truePositiveMeasure);
				System.out.println("true Negative: " + this.trueNegativeMeasure);
				System.out.println("false Positive: " + this.falsePositiveMeasure);
				System.out.println("false Negative: " + this.falseNegativeMeasure);
				System.out.println("Number of iterations = " + this.numberOfIterations);
				System.out.println("Measures computation duration: " + timings.getMeasuresComputationDuration() + " millisecond");
				System.out.println("Voter algorithm duration: " + timings.getVoterDuration() + " millisecond");
				System.out.println();
				System.out.println("Negative count = " + negativeCount);
	}

}
