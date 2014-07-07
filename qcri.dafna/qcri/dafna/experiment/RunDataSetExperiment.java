package qcri.dafna.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;

public class RunDataSetExperiment extends Experiment {
	private static String truthDirectory;
	private static 	String dataSetDirectory;
	private static String experimentFolderName;
	private static String datasetFolderName;
	private static String datasetId = "DATASETID";

	public static void main(String[] args) {
		datasetFolderName = args[0];
		truthDirectory = /*Globals.directory_syntheticDataSetMainDirectory + "/" +*/ datasetFolderName + "/truth";
		dataSetDirectory =/* Globals.directory_syntheticDataSetMainDirectory + "/" +*/ datasetFolderName + "/claims";
		experimentFolderName =/* Globals.directory_syntheticDataSetMainDirectory + "/" +*/ datasetFolderName + "/experimentResult";
		File experimentFolder = new File(experimentFolderName);
		experimentFolder.mkdir();

		startExperiments();
	}

	public static void startExperiments() {
		HashMap<String, VoterQualityMeasures> resultMap;
		VoterQualityMeasures tempResult;
		String insert = "INSERT INTO experiment_results ("
				+ "`dataset_id`, "
				+ "`voter_name`, "
				+ "`precision`, `accuracy`, `recall`, `specificity`, " 
				+ "`number_of_iteration`, `voter_duration_ms`) VALUES ";
		String tempInsert;

		resultMap = launchSyntheticExperiment(dataSetDirectory, truthDirectory, experimentFolderName);
		tempInsert = "";
		for (String voterName : resultMap.keySet()) {
			tempResult = resultMap.get(voterName);
			tempInsert = tempInsert + " ( '" + datasetId + "', "
					+ "'" + voterName + "', "
					+ "'" + tempResult.getPrecision() + "' , "
					+ "'" + tempResult.getAccuracy() + "' , "
					+ "'" + tempResult.getRecall() + "' , "
					+ "'" + tempResult.getSpecificity() +  "' , "
					+ "'" + tempResult.getNumberOfIterations() +  "' , "
					+ "'" + tempResult.getTimings().getVoterDuration() + "' ) ,";
		}
		tempInsert = insert + tempInsert.substring(0, tempInsert.length()-1) + ";\n" ;
		String contengencyTableInsert = contengencyTable.getInsertStatement(datasetId);

		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(experimentFolderName + "/sql.sql"), Globals.FILE_ENCODING,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			writer.write(tempInsert);
			writer.write(contengencyTableInsert);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static HashMap<String, VoterQualityMeasures> launchSyntheticExperiment(String dataSetDir, String truthDir, String experimentName) {
		DataSet dataSet = ExperimentDataSetConstructor_Development.readDataSet(Globals.starting_Confidence, Globals.starting_trustworthiness, 
				dataSetDir, 0, false, null, 
				qcri.dafna.experiment.ExperimentDataSetConstructor_Development.Experiment.Synthetic, truthDir, false);
		DataSet dataSetSinglePropertyValue = null;

		boolean convergence100 = false;
		boolean runSyntheticBoolean = false;
		boolean runLTM = false;

		HashMap<String, VoterQualityMeasures> resultsMap = runExperiment(convergence100 ,dataSet, experimentName, 
				runLTM, dataSetSinglePropertyValue, runSyntheticBoolean, null, "");
		return resultsMap;
	}
}

