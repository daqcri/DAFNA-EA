package qcri.dafna.experiment.profiling;

import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;

public class Profiler implements Runnable {

	private boolean continueProfiling = true;
	private long maxMemoryConsumption = -1;
	private long mainMemory = -1;

	public void startProfiling() {
		Runtime.getRuntime().gc();
		mainMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		while (continueProfiling) {
			long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if (maxMemoryConsumption < mem) {
				maxMemoryConsumption = mem;
			}
		}
	}

	public void stopProfiling(VoterQualityMeasures qualityMeasures) {
		continueProfiling = false;
		qualityMeasures.setMaxMemoryConsumption((maxMemoryConsumption-mainMemory)/(1024*1024));
//		System.out.println(qualityMeasures.getMaxMemoryConsumption());
	}

	@Override
	public void run() {
		startProfiling();
	}
}
