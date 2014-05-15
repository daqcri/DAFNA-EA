package qcri.dafna.voter;

import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;

public class VoterParameters {

	private double cosineSimDiffStoppingCriteria = 0.001;//ConvergenceTester.convergenceThreshold;

	private double startingTrust = 0.8;
	private double startingErrorFactor = 0.4;
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
