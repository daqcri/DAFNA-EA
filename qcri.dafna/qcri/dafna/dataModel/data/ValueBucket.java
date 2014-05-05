package qcri.dafna.dataModel.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;

public class ValueBucket {

	private int id;
	private List<SourceClaim> claims;

	private ValueType valueType;
	// used for Date and numerical values
	private Object maxValue;
	private Object minValue;

	private String cleanedString;

	private Double confidence = 0.5;
	private Double confidenceWithSimilarity = 0.5;// used in the TruthFinder algorithm
	private Double errorFactor = 0.5; // used in the Three estimate algorithm
	private double probabilityBeingTrue;

	private boolean clean;

	private List<String> disagreeingSourcesKeys;

	public ValueBucket(double startingConfidence, boolean clean) {
		claims = new ArrayList<SourceClaim>();
		confidence = startingConfidence;
		confidenceWithSimilarity = startingConfidence;
		this.clean = clean;
	}

	public Double getConfidence() {
		return confidence;
	}
	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}
	public void setConfidenceWithSimilarity(Double confidenceWithSimilarity) {
		this.confidenceWithSimilarity = confidenceWithSimilarity;
	}
	public Double getConfidenceWithSimilarity() {
		return confidenceWithSimilarity;
	}
	public void setErrorFactor(Double errorFactor) {
		this.errorFactor = errorFactor;
	}
	public Double getErrorFactor() {
		return errorFactor;
	}
	public String getCleanedString() {
		return cleanedString;
	}
	public void setCleanedString(String cleanedString) {
		this.cleanedString = cleanedString;
	}
	public List<SourceClaim> getClaims() {
		return claims;
	}
	void addClaim(SourceClaim claim) {
		claims.add(claim);
	}
	public ValueType getValueType() {
		return valueType;
	}
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	public void setMaxValue(Object maxValue) {
		this.maxValue = maxValue;
	}
	public Object getMaxValue() {
		return maxValue;
	}
	public void setMinValue(Object minValue) {
		this.minValue = minValue;
	}
	public Object getMinValue() {
		return minValue;
	}
	public int getNumberOfClaims() {
		return claims.size();
	}
	public boolean isClean() {
		return clean;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setProbabilityBeingTrue(double probabilityBeingTrue) {
		this.probabilityBeingTrue = probabilityBeingTrue;
	}
	public double getProbabilityBeingTrue() {
		return probabilityBeingTrue;
	}

	public Set<String> getSourcesKeys() {
		Set<String> sources = new HashSet<String>();
		for (SourceClaim claim : claims) {
			sources.add(claim.getSource().getSourceIdentifier());
		}
		return sources;
	}
	public Set<Source> getSources() {
		Set<Source> sources = new HashSet<Source>();
		for (SourceClaim claim : claims) {
			sources.add(claim.getSource());
		}
		return sources;
	}
	public void setDisagreeingSourcesKeys(List<String> disagreeingSourcesKeys) {
		this.disagreeingSourcesKeys = disagreeingSourcesKeys;
	}
	public void addDisagreeingSourceKey(String disagreeingSourceKey) {
		this.disagreeingSourcesKeys.add(disagreeingSourceKey);
	}
	public List<String> getDisagreeingSourcesKeys() {
		return disagreeingSourcesKeys;
	}
	

	public void resetClaimsConfidence(double value, double error) {
		confidence = value;
		confidenceWithSimilarity = value;
		errorFactor = error;
		probabilityBeingTrue = 0.0;
		for (SourceClaim c : claims) {
			c.setTrueClaimByVoter(false);
		}
	}
}
