package qcri.dafna.experiment;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.experiment.ExperimentDataSetConstructor_Development.Experiment;

public class BooksExperiment extends qcri.dafna.experiment.Experiment {

	public static void main(String[] args) {
		System.out.println("Start Book Experiment");
//		for (int i = 0; i < 100; i++) {
			Runtime.getRuntime().gc();
			launchBookExperiment();
//		}
		System.out.println("End of Book Experiment");
	}

	private static void launchBookExperiment() {
		DataSet dataSet = ExperimentDataSetConstructor_Development.readDataSet(Globals.starting_Confidence, Globals.starting_trustworthiness, 
				Globals.directory_formattedDAFNADataset_Books_Claims_Folder, 0, true, ValueType.ISBN, Experiment.Books, null, false);

		DataSet dataSetSinglePropertyValue = null;
//		dataSetSinglePropertyValue = ExperimentDataSetConstructor.readDataSet(0, 0, 
//				Globals.directory_formattedDAFNADataset_BooksFolder_SingleClaimValue, 0, true, ValueType.ISBN, Experiment.Books, null, false);

		DataSet dataSetMLE = null;
//		dataSetMLE = ExperimentDataSetConstructor_test.readDataSet(0.0, 0.0, Globals.directory_formattedDAFNADataset_BooksFolder_MLE, 
//				0.0, true, ValueType.ISBN, Experiment.Books, null, true);


		boolean convergence100 = false;
		boolean runMLE = false;
		boolean runLTM = false;
		String resultDir = Globals.directory_formattedDAFNADataset_Books_Folder + "/experimentResult";
		runExperiment(convergence100 ,dataSet, resultDir, runLTM, dataSetSinglePropertyValue, runMLE, dataSetMLE, "");
//		runExperiment(convergence100 ,dataSetMLE, resultDir, true, null, true, dataSetMLE, ""); // only for the MLE experiment
	}

}
