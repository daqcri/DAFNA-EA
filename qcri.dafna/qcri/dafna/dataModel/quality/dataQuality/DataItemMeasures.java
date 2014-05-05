package qcri.dafna.dataModel.quality.dataQuality;

public class DataItemMeasures {

	private Object trueValue;
	private boolean trueValueCleaned;

	private double redundancyOnDataItem;
	private int numberOfValues;
	private double entropy;
	private double deviation = 0;
	/**
	 * The clean property describe whether the dominant value is cleaned or not.
	 * If cleaned, the value will be casted as the value type parameter.
	 * If not the value will be used as a String value.
	 */
	private boolean dominantValueCleaned;
	private Object dominantValue;
	private double dominanceFactor;
	private double tolerance;

	private double averageNumOfValuesPerBucket;

	public void setRedundancyOnDataItem(double redundancyOnDataItem) {
		this.redundancyOnDataItem = redundancyOnDataItem;
	}
	public double getRedundancyOnDataItem() {
		return redundancyOnDataItem;
	}
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}
	public double getTolerance() {
		return tolerance;
	}
	public Object getDominantValue() {
		return dominantValue;
	}
	public double getDominanceFactor() {
		return dominanceFactor;
	}
	public void setDominanceFactor(double dominanceFactor) {
		this.dominanceFactor = dominanceFactor;
	}
	public void setDominantValue(Object dominantValue) {
		this.dominantValue = dominantValue;
	}
	public boolean isDominantValueCleaned() {
		return dominantValueCleaned;
	}
	public void setDominantValueCleaned(boolean cleaned) {
		this.dominantValueCleaned = cleaned;
	}
	public void setNumberOfValues(int numberOfValues) {
		this.numberOfValues = numberOfValues;
	}
	public int getNumberOfValues() {
		return numberOfValues;
	}
	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}
	public double getEntropy() {
		return entropy;
	}
	public void setDeviation(double deviation) {
		this.deviation = deviation;
	}
	public double getDeviation() {
		return deviation;
	}
	public void setTrueValueCleaned(boolean trueValueCleaned) {
		this.trueValueCleaned = trueValueCleaned;
	}
	public boolean isTrueValueCleaned() {
		return trueValueCleaned;
	}
	public void setTrueValue(Object trueValue) {
		this.trueValue = trueValue;
	}
	public Object getTrueValue() {
		return trueValue;
	}
	public void setAverageNumOfValuesPerBucket(double averageNumOfClaimsPerBucket) {
		this.averageNumOfValuesPerBucket = averageNumOfClaimsPerBucket;
	}
	public double getAverageNumOfValuesPerBucket() {
		return averageNumOfValuesPerBucket;
	}

}
