package qcri.dafna.experiment;


import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.quality.dataQuality.logger.DataQualityLogger;
import qcri.dafna.voter.ExperimentDataSetConstructor;

public class BiographiesExperiment extends Experiment{

	public static void main(String[] args) {
		System.out.println("Start Biography Experiment...");
		launchDataSet_BiographyExperiment();
		System.out.println("End Biography Experiment.");
	}
	static private void launchDataSet_BiographyExperiment() {

		DataSet dataSet = ExperimentDataSetConstructor.readDataSet(Globals.starting_Confidence,Globals.starting_trustworthiness, 
				Globals.directory_formattedDAFNADataset_BiographiesClaimsFolder, 
				Globals.tolerance_Factor, false, null ,qcri.dafna.voter.ExperimentDataSetConstructor.Experiment.Biography, null, false); 

		boolean convergence100 = false;
		DataQualityLogger logger = new DataQualityLogger();
		String dir = Globals.directory_formattedDAFNADataset_BiographiesFolder;
		logger.LogDataSetData(dir + "/dataSetInfo.txt", dataSet.getDataQualityMeasurments(), dataSet);
		runExperiment(convergence100, dataSet, dir, false, null, false, null);
	}
}
