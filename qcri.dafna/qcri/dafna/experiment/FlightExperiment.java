package qcri.dafna.experiment;

import java.util.Date;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.voter.ExperimentDataSetConstructor;
import qcri.dafna.voter.ExperimentDataSetConstructor.Experiment;

public class FlightExperiment extends qcri.dafna.experiment.Experiment{

	private static final double TOLERANCE_FACTOR = 0.01;
	/**
	 * for good performance enlarge the Java virtual machine memory:
	 * set the argument: -Xmx4G
	 */
	public static void main(String[] args) {
		System.out.println("Start Flight Experiment...");
//		for (int i = 0; i < 10; i++) {
			Globals.tolerance_Factor = TOLERANCE_FACTOR;
			launchDataSet_FlightExperiment();
			Runtime.getRuntime().gc();
//		}
		System.out.println("End Flight Experiment.");
	}

	static private void launchDataSet_FlightExperiment() {
//		Date d = new Date();System.out.println(d.toString());
		DataSet dataSet = ExperimentDataSetConstructor.readDataSet(Globals.starting_Confidence,Globals.starting_trustworthiness, 
				Globals.directory_formattedDAFNADataset_FlightFolder, Globals.tolerance_Factor, false, null ,Experiment.Flight, null, false);
		boolean convergence100 = false;
		boolean runMLE = false;
		boolean runLTM = false;
		String dir = Globals.directory_formattedDAFNADataset_Flight + "/experimentResult";
		runExperiment(convergence100, dataSet, dir, runLTM, null, runMLE, null);
	}
}
