package qcri.dafna.allegation;

import java.util.HashMap;

public class DataItemAllegatorObject {

	private String dataItemKey;
	/**
	 * All values provided for this data item along with its confidence.
	 */
	private HashMap<String, Double> valueConfidence;
	private double minConfidence;
	private double maxConfidence;
	private String firstTrueValueByVoter;
	
	private String trueValueByVoter;
	private boolean changed = false;

	private int numberOfCOnflicts;

	public String getDataItemKey() {
		return dataItemKey;
	}
	public void setDataItemKey(String dataItemKey) {
		this.dataItemKey = dataItemKey;
	}
	public HashMap<String, Double> getValueConfidence() {
		return valueConfidence;
	}
	public void setValueConfidence(HashMap<String, Double> valueConfidence) {
		this.valueConfidence = valueConfidence;
	}
	public double getMinConfidence() {
		return minConfidence;
	}
	public void setMinConfidence(double minConfidence) {
		this.minConfidence = minConfidence;
	}
	public double getMaxConfidence() {
		return maxConfidence;
	}
	public void setMaxConfidence(double maxConfidence) {
		this.maxConfidence = maxConfidence;
	}
	public void setFirstTrueValueByVoter(String firstTrueValueByVoter) {
		this.firstTrueValueByVoter = firstTrueValueByVoter;
	}
	public String getFirstTrueValueByVoter() {
		return firstTrueValueByVoter;
	}
	public void setNumberOfCOnflicts(int numberOfCOnflicts) {
		this.numberOfCOnflicts = numberOfCOnflicts;
	}
	public int getNumberOfCOnflicts() {
		return numberOfCOnflicts;
	}
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	public String getTrueValueByVoter() {
		return trueValueByVoter;
	}
	public void setTrueValueByVoter(String trueValueByVoter) {
		this.trueValueByVoter = trueValueByVoter;
	}
	public boolean isChanged() {
		return changed;
	}
}
