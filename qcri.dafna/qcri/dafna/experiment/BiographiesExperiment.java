package qcri.dafna.experiment;


import java.util.HashMap;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.quality.dataQuality.logger.DataQualityLogger;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;
import qcri.dafna.experiment.ExperimentDataSetConstructor_Development.Experiment;

public class BiographiesExperiment extends qcri.dafna.experiment.Experiment{

	public static void main(String[] args) {
		System.out.println("Start Biography Experiment...");
		launchDataSet_BiographyExperiment(Globals.directory_formattedDAFNADataset_BiographiesClaimsFolder, Globals.tolerance_Factor, false, null ,
				qcri.dafna.experiment.ExperimentDataSetConstructor_Development.Experiment.Biography, null, false);
		System.out.println("End Biography Experiment.");
	}
	static private void launchDataSet_BiographyExperiment(String dirDS, double toleranceFactor, 
			boolean cleanObjectId, ValueType objectIdValueType, Experiment experiment, String syntheticDirectory, boolean runMLE) {

		DataSet dataSet = ExperimentDataSetConstructor_Development.readDataSet(
				Globals.starting_Confidence,
				Globals.starting_trustworthiness, 
				dirDS, 
				toleranceFactor, cleanObjectId, objectIdValueType ,
				experiment, syntheticDirectory, runMLE); 

		boolean convergence100 = false;
		DataQualityLogger logger = new DataQualityLogger();
		String dir = Globals.directory_formattedDAFNADataset_BiographiesFolder;
		logger.LogDataSetData(dir + "/dataSetInfo.txt", dataSet.getDataQualityMeasurments(), dataSet);
		HashMap<String, VoterQualityMeasures>  result = 
				runExperiment(convergence100, dataSet, dir, false, null, false, null,"");
		
//		boolean convergence100, DataSet dataSet, 
//		String resultFolderName, 
//		boolean runLTM, DataSet datasetLTM,
//		boolean runMLE, DataSet dataSetMLE
	}
}
