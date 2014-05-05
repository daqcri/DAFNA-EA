package qcri.dafna.voter;

import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.voterResults.NormalVoterQualityMeasures;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;
import qcri.dafna.experiment.profiling.Profiler;

/**
 * Super class for all Voters
 * @author dalia
 *
 */
public abstract class Voter {

	protected DataSet dataSet;
	protected VoterQualityMeasures voterQuality;
	protected boolean singlePropertyValue; 
	protected boolean onlyMaxValueIsTrue;
	
	public Voter(DataSet dataSet) {
		this.dataSet = dataSet;
		initParameters();
	}
	protected abstract void initParameters();
	/**
	 * 
	 * @param singlePropertyValue : list data type is provided as single value over multiple claims, or one claim with multiple values
	 * @param onlyMaxValueIsTrue
	 * @return
	 */
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

		voterQuality.computeTruth(onlyMaxValueIsTrue);

		voterQuality.setNumberOfIterations(iterationCount);
		voterQuality.computeVoterQualityMeasures(singlePropertyValue); // list data type is provided as single value over multiple claims

		return voterQuality;
	}

	public void computeMeasuresPerIteration(boolean resetTrueValues, double trustCosineSimDiff, double confCosineSimDiff){
		if (resetTrueValues) {
			voterQuality.computeTruth(onlyMaxValueIsTrue);
		}
		voterQuality.computeVoterQualityMeasures(singlePropertyValue);
		voterQuality.getPrecisionPerIteration().add(voterQuality.getPrecision());
		voterQuality.getAccuracyPerIteration().add(voterQuality.getAccuracy());
		voterQuality.getRecallPerIteration().add(voterQuality.getRecall());
		voterQuality.getSpecificityPerIteration().add(voterQuality.getSpecificity());
		voterQuality.getTruePositivePercentagePerIteration().add(voterQuality.getTruePositivePercentage());

		voterQuality.getTruePositivePerIteration().add(voterQuality.getTruePositive());
		voterQuality.getTrueNegativePerIteration().add(voterQuality.getTrueNegative());
		voterQuality.getFalsePositivePerIteration().add(voterQuality.getFalsePositive());
		voterQuality.getFalseNegativePerIteration().add(voterQuality.getFalseNegative());

		voterQuality.getIterationEndingTime().add(new Double(System.currentTimeMillis()) - voterQuality.getTimings().getStartingTime());

		voterQuality.getTrustCosineSimilarityPerIteration().add(trustCosineSimDiff);
		voterQuality.getConfCosineSimilarityPerIteration().add(confCosineSimDiff);
//		computeMemoryConsumption();
		
	}
//	private static void computeMemoryConsumption() {
//		// Get the Java runtime
//	    Runtime runtime = Runtime.getRuntime();
//	    // Run the garbage collector
////	    runtime.gc();
//	    // Calculate the used memory
//	    long memory = runtime.totalMemory() - runtime.freeMemory();
//	    System.out.println("Used memory is bytes: " + memory);
//	    System.out.println("Used memory is megabytes: "
//	        + bytesToMegabytes(memory));
//	}
//	private static final long MEGABYTE = 1024L * 1024L;
//
//	  public static long bytesToMegabytes(long bytes) {
//	    return bytes / MEGABYTE;
//	  }
	protected abstract int runVoter(boolean convergence100);
}
