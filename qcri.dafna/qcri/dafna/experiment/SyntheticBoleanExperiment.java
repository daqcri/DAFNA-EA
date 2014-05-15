package qcri.dafna.experiment;

import java.util.ArrayList;
import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.experiment.ExperimentDataSetConstructor_test.Experiment;

public class SyntheticBoleanExperiment extends qcri.dafna.experiment.Experiment{

	public static void main(String[] args) {
		System.out.println("Start Boolean Synthetic Experiment...");
		launchSyntheticBoleanExperiment();
		System.out.println("End Boolean Synthetic Experiment.");
	}
	static private void launchSyntheticBoleanExperiment() {
		List<String> folders = new ArrayList<String>(); 
		String numSrcNumDI ;
//		numSrcNumDI = "/1000s100di"; folders.add(numSrcNumDI);
//		numSrcNumDI = "/1000s1000di";folders.add(numSrcNumDI);
//		numSrcNumDI = "/1000s10000di";folders.add(numSrcNumDI);
//		numSrcNumDI = "/5000s100di";folders.add(numSrcNumDI);
//		numSrcNumDI = "/5000s1000di";folders.add(numSrcNumDI);
		numSrcNumDI = "/10000s100di";folders.add(numSrcNumDI);
		numSrcNumDI = "/10000s1000di";folders.add(numSrcNumDI);
		
		for (String f : folders) {
			Runtime.getRuntime().gc();
			String dir = Globals.directory_syntheticDataSet_Boolean_base + f + "/claims";
			String dir_truth = Globals.directory_syntheticDataSet_Boolean_base + f + "/truth";

			DataSet dataSet = ExperimentDataSetConstructor_test.readDataSet(0.0, 0.0, dir, 
					0.0, false, null, Experiment.BooleanSynthetic, dir_truth, true);

			//		DataSet dataSet = ExperimentDataSetConstructor.readDataSet(0.0, 0.0, Globals.directory_syntheticDataSet_Boolean, 
			//				0.0, false, null, Experiment.BooleanSynthetic, null, true);

			//		DataSet dataSetTrueFalse = ExperimentDataSetConstructor.readDataSet(0.0, 0.0, Globals.directory_syntheticDataSet_BooleanTrueAndFalse, 
			//				0.0, false, null, Experiment.BooleanSyntheticTF, null, true);

			boolean convergence100 = false;
			boolean runSyntheticBoolean = true;
			boolean runLTM = false;
			runExperiment(convergence100, dataSet/*dataSetTrueFalse*/, Globals.directory_syntheticDataSet_Boolean_base + f, runLTM, 
					dataSet, runSyntheticBoolean, dataSet);
		}
	}
}
