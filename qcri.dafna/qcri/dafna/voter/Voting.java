package qcri.dafna.voter;

import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.voterResults.NormalVoterQualityMeasures;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;
import qcri.dafna.experiment.profiling.Profiler;

public class Voting extends Voter{

	public Voting(DataSet dataSet, VoterParameters params) {
		super(dataSet, params);
	}
	@Override
	public int runVoter(boolean convergence100) {
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
		return 1;
	}
	@Override
	public VoterQualityMeasures launchVoter(boolean convergence100, boolean profileMemory) {
		voterQuality = new NormalVoterQualityMeasures(dataSet);
		Profiler profiler = null;
		if (profileMemory) {
			profiler = new Profiler();
			Thread profilerThread = new Thread(profiler);
			profilerThread.start();
		}
		voterQuality.getTimings().startVoterDuration();
		int iterationCount = runVoter(convergence100);
		voterQuality.getTimings().endVoterDuration();
		if (profileMemory) {
			profiler.stopProfiling(voterQuality);
//			System.out.println(voterQuality.getMaxMemoryConsumption());
		}
		

		voterQuality.setNumberOfIterations(iterationCount);
		voterQuality.computeVoterQualityMeasures(singlePropertyValue); // list data type is provided as single value over multiple claims

		return voterQuality;
	}
	@Override
	protected void initParameters() {
		singlePropertyValue = false;
		onlyMaxValueIsTrue = true;
	}
}
