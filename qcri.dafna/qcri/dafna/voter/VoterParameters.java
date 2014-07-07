package qcri.dafna.voter;

import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;

public class VoterParameters {

	/*
	 * The difference beteween the Sources trustworthiness cosine similarity between 2 iterations must be  less than this value for a convergence sign.
	 */
	private double cosineSimDiffStoppingCriteria = 0.001;//ConvergenceTester.convergenceThreshold;

	/*
	 * The value used for initializing all sources trustworthiness
	 */
	private double startingTrust = 0.8;
	/*
	 * The value used for initializing all Values Error Factor
	 */
	private double startingErrorFactor = 0.4;
	/*
	 * The value used for initializing all Values Confidence
	 */
	private double startingConfidence = 1.0;

	public VoterParameters(double cosineSimDiffStoppingCriteria, 
			double startingTrust, double startingConfidence, double startingErrorFactor) {
		if (cosineSimDiffStoppingCriteria > 0) {
			this.cosineSimDiffStoppingCriteria = cosineSimDiffStoppingCriteria;
		}
		if (startingTrust > 0) {
			this.startingTrust = startingTrust;
		}
		if (startingErrorFactor > 0) {
			this.startingErrorFactor = startingErrorFactor;
		}
		if (startingConfidence > 0) {
			this.startingConfidence = startingConfidence;
		}
		ConvergenceTester.convergenceThreshold = cosineSimDiffStoppingCriteria;
	}
	public double getCosineSimDiffStoppingCriteria() {
		return cosineSimDiffStoppingCriteria;
	}
	public double getStartingErrorFactor() {
		return startingErrorFactor;
	}
	public double getStartingTrust() {
		return startingTrust;
	}
	public double getStartingConfidence() {
		return startingConfidence;
	}
}
