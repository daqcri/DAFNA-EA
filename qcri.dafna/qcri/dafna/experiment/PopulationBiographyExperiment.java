package qcri.dafna.experiment;


import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.quality.dataQuality.logger.DataQualityLogger;

public class PopulationBiographyExperiment extends Experiment{

//	public static void main(String[] args) {
//		System.out.println("Start Population-Biography Experiment...");
//		launchDataSet_BiographyExperiment();
//		System.out.println("End Population-Biography Experiment.");
//	}
	static private void launchDataSet_BiographyExperiment() {

		DataSet dataSet = ExperimentDataSetConstructor_Development.readDataSet(Globals.starting_Confidence,Globals.starting_trustworthiness, 
				Globals.directory_formattedDAFNADataset_PopulationBiographyClaimsFolder, 
				Globals.tolerance_Factor, false, null ,qcri.dafna.experiment.ExperimentDataSetConstructor_Development.Experiment.PopulationBiography, null, false); 
		String dir = Globals.directory_FormattedPopulationBiographyFolder;

		boolean convergence100 = false;
		DataQualityLogger logger = new DataQualityLogger();
		boolean runLTM = false;
		boolean runSyntheticBoolean = false;
				
		logger.LogDataSetData(dir + "/experimentResult/dataSetInfo.txt", dataSet.getDataQualityMeasurments(), dataSet);
		runExperiment(convergence100, dataSet, dir, runLTM, null, runSyntheticBoolean, null, "");

	}
}


