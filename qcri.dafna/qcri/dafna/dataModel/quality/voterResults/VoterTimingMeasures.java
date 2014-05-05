package qcri.dafna.dataModel.quality.voterResults;

public class VoterTimingMeasures {
	private long voterDuration;
	private long measuresComputationDuration;
	private long startingTime;

	/* ------------- voterDuration ------------- */
	public void startVoterDuration() {
		this.voterDuration = System.currentTimeMillis();
		startingTime = voterDuration;
	}
	public void endVoterDuration() {
		this.voterDuration = System.currentTimeMillis() - this.voterDuration;
	}
	public long getVoterDuration() {
		return voterDuration;
	}
	/* ------------- voterDuration ------------- */
	
	/* ------------- measuresComputationDuration ------------- */
	public void startMeasuresComputationDuration() {
		this.measuresComputationDuration = System.currentTimeMillis();
	}
	public void endMeasuresComputationDuration() {
		this.measuresComputationDuration = System.currentTimeMillis() - this.measuresComputationDuration;
	}
	public long getMeasuresComputationDuration() {
		return measuresComputationDuration;
	}
	/* ------------- measuresComputationDuration ------------- */

	public long getStartingTime() {
		return startingTime;
	}
}
