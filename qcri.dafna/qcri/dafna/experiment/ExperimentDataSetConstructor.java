package qcri.dafna.experiment;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataSet.CSVDatasetReader.CSVDatasetReader;
import qcri.dafna.dataModel.dataSetReader.SyntheticDataSetTruthReader;
import qcri.dafna.dataModel.quality.dataQuality.DataQualityMeasurments;
import qcri.dafna.dataModel.quality.dataQuality.DataSetTimingMeasures;
import qcri.dafna.dataModel.quality.dataQuality.logger.DataQualityLogger;

public class ExperimentDataSetConstructor {

	/**
	 * Construct the dataSet object by reading the files in the given  dataSetDirectory.
	 * Initialize it with the given tolerance factor.
	 * Read the true values from the true values directory depending on the experiment.
	 * 
	 * @param dataSetDirectory
	 * @param toleranceFactor
	 * @return The built dataSet
	 */
	public static DataSet readDataSet(String dataSetDirectory, double toleranceFactor, String groundTruthDir, String outputPath,char delim) {
		Globals.tolerance_Factor = toleranceFactor;
		DataSetTimingMeasures timings = new DataSetTimingMeasures();
		CSVDatasetReader reader = new CSVDatasetReader();
		/**
		 *  Construct the dataSet without bucketing.
		 */
		DataSet dataSet = reader.readDirectoryFiles(dataSetDirectory, timings, delim);
		/**
		 *  Compute buckets with exact values.
		 *  The computed buckets building time will be overwrite if the the buckets 
		 *  are recomputed using tolerance.
		 */
		timings.startBucketsBuildingTime();
		dataSet.computeValueBuckets(false);
		timings.endBucketsBuildingTime();

		/**
		 *  Initialization for dataSet quality measures objects
		 */
		DataQualityMeasurments dqm = new DataQualityMeasurments(dataSet);
		dqm.setTimingMeasures(timings);
		dataSet.setDataQualityMeasurments(dqm);

		int trueValueCount = 0;
		/**
		 * If the ground truth exist, read it
		 */
		if (groundTruthDir != null && ! groundTruthDir.isEmpty()) {

			timings.startTruthReadingTime();
			SyntheticDataSetTruthReader truthReader = new SyntheticDataSetTruthReader(dqm.getDataItemMeasures());
			trueValueCount = truthReader.readDirectoryFiles(groundTruthDir);

		}
		dqm.setGoldStandardTrueValueCount(trueValueCount);
		timings.endTruthReadingTime();
		dqm.computeDataQaulityMeasures(Globals.tolerance_Factor);

		DataQualityLogger logger = new DataQualityLogger();
		logger.LogDataSetData(outputPath + "/dataSetInfo.txt", dataSet.getDataQualityMeasurments(), dataSet);

		return dataSet;
	}
}
