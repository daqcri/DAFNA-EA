package qcri.dafna.dataModel.quality.dataQuality;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.ValueBucket;

public class ConvergenceTester {
	public  static double convergenceThreshold = 0.001;
	/**
	 * Compute the cosine similarity between the trustworthiness computed in the current iteration 
	 * and the trustworthiness computed in the previous iteration. 
	 * @return the cosine similarity
	 */
	public static double computeTrustworthinessCosineSimilarity(DataSet dataSet) {
		double a,b;
		double sumAB = 0;
		double sumA2=0;
		double sumB2 = 0;
		for (Source source : dataSet.getSourcesHash().values()) {
			a = source.getOldTrustworthiness();
			b = source.getTrustworthiness();
			sumAB = sumAB + (a*b);
			sumA2 = sumA2 + (a*a);
			sumB2 = sumB2 + (b*b);
			if (Double.isInfinite(sumAB)) {
				System.out.println();
			}
		}
		sumA2 = Math.pow(sumA2, 0.5);
		sumB2 = Math.pow(sumB2, 0.5);
		if ((sumA2 * sumB2) == 0) {
			return Double.MAX_VALUE;
		}
		if (Double.isInfinite(sumAB)) {
			if (Double.isInfinite((sumA2 * sumB2))) {
				return 1.0;
			}
		}
		double cosineSimilarity = sumAB / (sumA2 * sumB2);
		return cosineSimilarity;
	}
	/**
	 * This method use the bucket.confidence as the new value, and the bucket.confidence with similarity as the old value
	 * @param dataSet
	 * @return
	 */
	public static double computeConfidenceCosineSimilarity(DataSet dataSet) {
		double a,b;
		double sumAB = 0;
		double sumA2=0;
		double sumB2 = 0;
		for (List<ValueBucket> blist : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket bucket : blist) {
				a = bucket.getConfidenceWithSimilarity();
				b = bucket.getConfidence();
				sumAB = sumAB + (a*b);
				sumA2 = sumA2 + (a*a);
				sumB2 = sumB2 + (b*b);
			}
		}
		sumA2 = Math.pow(sumA2, 0.5);
		sumB2 = Math.pow(sumB2, 0.5);
		if ((sumA2 * sumB2) == 0) {
			return Double.MAX_VALUE;
		}
		double cosineSimilarity = sumAB / (sumA2 * sumB2);
		return cosineSimilarity;
	}

	public static double computeValuesCosineSimilarity(HashMap<String, Double> oldVal, HashMap<String, Double> newVal, Set<String> keySet) {
		double a,b;
		double sumAB = 0;
		double sumA2=0;
		double sumB2 = 0;
		for (String key : keySet) {
			a = oldVal.get(key);
			b = newVal.get(key);
			sumAB = sumAB + (a*b);
			sumA2 = sumA2 + (a*a);
			sumB2 = sumB2 + (b*b);
		}
		sumA2 = Math.pow(sumA2, 0.5);
		sumB2 = Math.pow(sumB2, 0.5);
		if ((sumA2 * sumB2) == 0) {
			return Double.MAX_VALUE;
		}
		double cosineSimilarity = sumAB / (sumA2 * sumB2);
		return cosineSimilarity;
	}
}
