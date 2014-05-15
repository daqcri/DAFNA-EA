package qcri.dafna.experiment;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.dataFormatter.DataCleaner;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.dataSetReader.BiographyTruthReader;
import qcri.dafna.dataModel.dataSetReader.BooksTruthReader;
import qcri.dafna.dataModel.dataSetReader.DatasetReader;
import qcri.dafna.dataModel.dataSetReader.FlightTruthReader;
import qcri.dafna.dataModel.dataSetReader.PopulationTruthReader;
import qcri.dafna.dataModel.dataSetReader.SyntheticDataSetTruthReader;
import qcri.dafna.dataModel.dataSetReader.WeatherTruthReader;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;
import qcri.dafna.dataModel.quality.dataQuality.DataQualityMeasurments;
import qcri.dafna.dataModel.quality.dataQuality.DataSetTimingMeasures;

public class ExperimentDataSetConstructor_test {
	public static enum Experiment {
		Flight, Books, Biography, BooleanSynthetic, Population, PopulationBiography,
		BooleanSyntheticTF, Synthetic, Weather;
	};

	/**
	 * Construct the dataSet object by reading the files in the given  dataSetDirectory.
	 * Initialize it with the given starting confidence, starting trustworthiness, and tolerance factor.
	 * Read the true values from the true values directory depending on the experiment.
	 * 
	 * @param startingConfidence
	 * @param statingTrustworthiness
	 * @param dataSetDirectory
	 * @param toleranceFactor
	 * @param cleanObjectId
	 * @param objectIdValueType
	 * @param experiment
	 * @param MLE: wheter the book dataset is running for MLE or not
	 * @return The built dataSet
	 */
	public static DataSet readDataSet(double startingConfidence, double statingTrustworthiness, String dataSetDirectory, double toleranceFactor, 
			boolean cleanObjectId, ValueType objectIdValueType, Experiment experiment, String syntheticDirectory, boolean MLE) {
		Globals.tolerance_Factor = toleranceFactor;
		DataSetTimingMeasures timings = new DataSetTimingMeasures();
		DatasetReader reader = new DatasetReader();
		/**
		 *  Construct the dataSet without bucketing.
		 */
		DataSet dataSet = reader.readDirectoryFiles(dataSetDirectory, cleanObjectId, objectIdValueType, timings);
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
		timings.startTruthReadingTime();
		if (experiment == Experiment.PopulationBiography) {
			PopulationTruthReader truthReader = new PopulationTruthReader(dqm.getDataItemMeasures());
			trueValueCount = truthReader.readDirectoryFiles();
			BiographyTruthReader truthReader2 = new BiographyTruthReader(dqm.getDataItemMeasures());
			trueValueCount += truthReader2.readDirectoryFiles();
		} else if (experiment == Experiment.Population) {
			PopulationTruthReader truthReader = new PopulationTruthReader(dqm.getDataItemMeasures());
			trueValueCount = truthReader.readDirectoryFiles();
		} else if (experiment == Experiment.Flight) {
			FlightTruthReader truthReader = new FlightTruthReader(dqm.getDataItemMeasures());
			trueValueCount = truthReader.readDirectoryFiles(); 
		} else if (experiment == Experiment.Books) {
			BooksTruthReader truthReader = new BooksTruthReader(dqm.getDataItemMeasures(), MLE);
			trueValueCount = truthReader.readDirectoryFiles();
		} else if (experiment == Experiment.Biography) {
			BiographyTruthReader truthReader = new BiographyTruthReader(dqm.getDataItemMeasures());
			trueValueCount = truthReader.readDirectoryFiles();
		} else if (experiment == Experiment.BooleanSynthetic) {
			SyntheticDataSetTruthReader truthReader = new SyntheticDataSetTruthReader(dqm.getDataItemMeasures());
//			trueValueCount = truthReader.readDirectoryFiles(Globals.directory_syntheticDataSet_Boolean_Truth);
			trueValueCount = truthReader.readDirectoryFiles(syntheticDirectory);
		} else if (experiment == Experiment.BooleanSyntheticTF) {
			SyntheticDataSetTruthReader truthReader = new SyntheticDataSetTruthReader(dqm.getDataItemMeasures());
//			trueValueCount = truthReader.readDirectoryFiles(Globals.directory_syntheticDataSet_BooleanTrueAndFalse_Truth);
			trueValueCount = truthReader.readDirectoryFiles(syntheticDirectory);
		} else if (experiment == Experiment.Synthetic) {
			SyntheticDataSetTruthReader truthReader = new SyntheticDataSetTruthReader(dqm.getDataItemMeasures());
			trueValueCount = truthReader.readDirectoryFiles(syntheticDirectory);
		} else if (experiment == Experiment.Weather) {
			WeatherTruthReader truthReader = new WeatherTruthReader(dqm.getDataItemMeasures());
			trueValueCount = truthReader.readDirectoryFiles(Globals.directory_formattedDAFNADataset_WeatherTruthFolder);
//					getGroundTruthFromSource(syntheticDirectory_OR_Weather_GroundTruth_SourceName, dqm.getDataItemMeasures(), dataSet);
		}
		dqm.setGoldStandardTrueValueCount(trueValueCount);
		timings.endTruthReadingTime();

		dqm.computeDataQaulityMeasures(Globals.tolerance_Factor);

		return dataSet;
	}
}
