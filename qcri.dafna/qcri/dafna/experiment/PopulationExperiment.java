package qcri.dafna.experiment;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.experiment.ExperimentDataSetConstructor_test.Experiment;

public class PopulationExperiment extends qcri.dafna.experiment.Experiment{

	private static final double TOLERANCE_FACTOR = 0.01;
	/**
	 * for good performance enlarge the Java virtual machine memory:
	 * set the argument: -Xmx4G
	 */
	public static void main(String[] args) {
		System.out.println("Start Population Experiment...");
		Globals.tolerance_Factor = TOLERANCE_FACTOR;
//		for (int i = 0; i < 10; i ++) {
			launchDataSet_PopulationExperiment();
//		}
		System.out.println("End Population Experiment.");
//		FlightExperiment.main(null);
//		BooksExperiment.main(null);
	}

	static private void launchDataSet_PopulationExperiment() {
//		Date d = new Date();System.out.println(d.toString());
		DataSet dataSet = ExperimentDataSetConstructor_test.readDataSet(Globals.starting_Confidence,Globals.starting_trustworthiness, 
				Globals.directory_formattedDAFNADataset_PopulationClaimsFolder, Globals.tolerance_Factor, false, null ,Experiment.Population, null, false);

		
		boolean convergence100 = false;
		boolean runSyntheticBoolean = false;
		boolean runLTM = false;
		String dir = Globals.directory_FormattedPopulationFolder + "/experimentResult";

		runExperiment(convergence100, dataSet, dir, runLTM, null, runSyntheticBoolean, null);
		resultsMap = null;
		contengencyTable = null;
		Runtime.getRuntime().gc();

	}
}
