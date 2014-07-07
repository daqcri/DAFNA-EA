package qcri.dafna.experiment.allegator;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.experiment.ExperimentDataSetConstructor_Development;
import qcri.dafna.experiment.ExperimentDataSetConstructor_Development.Experiment;

public class AllegatorExperiment {

	public static void main(String[] args) {

//		book(0);
//		weather(0);
		book100(0);
	}

	private static void book(double tolerance) {
		DataSet dataSet = ExperimentDataSetConstructor_Development.readDataSet(Globals.starting_Confidence, Globals.starting_trustworthiness, 
				Globals.directory_formattedDAFNADataset_Books_Claims_Folder, tolerance, true, ValueType.ISBN, Experiment.Books, null, false);
		allegate1SrcPercenteDI(dataSet, Globals.directory_formattedDAFNADataset_Books_Folder, Globals.voterTruthFinder, 10, 1.0);
	}
	private static void book100(double tolerance) {
		DataSet dataSet = ExperimentDataSetConstructor_Development.readDataSet(Globals.starting_Confidence, 
				Globals.starting_trustworthiness, 
				"/home/dalia/DAFNAData/formatted/Books100/claims", tolerance, true, ValueType.ISBN, 
				Experiment.Books, null, false);

		allegateOneSourceDIOneByOne(dataSet, "/home/dalia/DAFNAData/formatted/Books100", Globals.voterAccuNoDep);
		
//		allegate1SrcPercenteDI(dataSet, Globals.directory_formattedDAFNADataset_Books_Folder, Globals.voterTruthFinder, 10, 1.0);
		
	}
	private static void weather(double tolerance) {
		DataSet dataSet = ExperimentDataSetConstructor_Development.readDataSet(Globals.starting_Confidence,
				Globals.starting_trustworthiness, 
				Globals.directory_formattedDAFNADataset_WeatherClaims, 
				tolerance, false, null ,qcri.dafna.experiment.ExperimentDataSetConstructor_Development.Experiment.Weather, null, false);
		allegate1SrcPercenteDI(dataSet, Globals.directory_formattedDAFNADataset_WeatherFolder, Globals.voterTruthFinder, 10, 1.0);

	}

	private static void allegateOneSourceDIOneByOne(DataSet dataset, String dsDir, String voterName) {
		AllegateOneSourceDIOneByOne alg = new AllegateOneSourceDIOneByOne();
		String allegatorFolderName = "allegatorDIOneByOne-orderBy" + "-voter" + voterName + "-orderByMinConflicts";
		boolean neglictDIWithNoConflict = true;
		boolean orderByMaxConfidence = false; // order by min conflicts
		alg.allegate(dataset, dsDir, allegatorFolderName, voterName, neglictDIWithNoConflict, orderByMaxConfidence);
	}

	private static void allegate1SrcPercenteDI(DataSet dataset, String dsDir, String voterName, int numNewSources, double newDIPercentage) {
		AllegateDIPercentageSourcesRecursively alg = new AllegateDIPercentageSourcesRecursively();
		alg.allegate(dataset, dsDir, voterName, numNewSources, newDIPercentage);
	}
}
