package qcri.dafna.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.quality.dataQuality.logger.DataQualityLogger;
import qcri.dafna.voter.ExperimentDataSetConstructor;

public class WeatherEperiment extends Experiment {
	public static void main(String[] args) {
		List<String> sources = new ArrayList<String>();
//		sources.add("accuweather");
		sources.add("msn");
		sources.add("herald");
		sources.add("foxnews");
		sources.add("findlocalweather");
		sources.add("nytimes");
		sources.add("unisys");
		sources.add("uswx");
		sources.add("washingtonpost");
		sources.add("weatherbug");
		sources.add("weatherforyou");
		sources.add("weather_gov");
		sources.add("wunderground");
		sources.add("yahoo");
		sources.add("cnn");
		sources.add("climaton");
		sources.add("aol");

		String source = "accuweather";
//		for (String source : sources) {
			System.out.println("Start Weather Experiment: " + source + " as ground truth...");
			launchDataSet_BiographyExperiment();
			System.out.println("End Weather Experiment.");
//		}

	}

	static private void launchDataSet_BiographyExperiment() {
		double tolerence = Globals.tolerance_Factor;
		DataSet dataSet = ExperimentDataSetConstructor.readDataSet(Globals.starting_Confidence,Globals.starting_trustworthiness, 
				Globals.directory_formattedDAFNADataset_WeatherClaims, 
				tolerence, false, null ,qcri.dafna.voter.ExperimentDataSetConstructor.Experiment.Weather, null, false); 

		boolean convergence100 = false;
		DataQualityLogger logger = new DataQualityLogger();
		String dir = Globals.directory_formattedDAFNADataset_WeatherFolder;// + "/FroundTruth-" + groundTruthSourceID;
		File experimentFolder = new File(dir);
		experimentFolder.mkdir();
		logger.LogDataSetData(dir + "/dataSetInfo.txt", dataSet.getDataQualityMeasurments(), dataSet);
		runExperiment(convergence100, dataSet, dir, false, null, false, null);

	}
}
