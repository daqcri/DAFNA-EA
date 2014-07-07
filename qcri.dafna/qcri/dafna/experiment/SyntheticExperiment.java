package qcri.dafna.experiment;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;

public class SyntheticExperiment extends Experiment{
	private static Connection connect = null;
	private static 	Statement readingStatement = null;
	private static 	Statement writingStatement = null;

	public static void main(String[] args) {
//		boolean runLTM = true;// only true for scalability
//		startExperiments(getSelectQueryForScalability(), runLTM);
			
		startExperiments(getSyntheticExperimentSelectQuery(false), false);
			
	}

	public static String getSelectQueryForScalability() {
		String select = "SELECT dataset_id, dataset_file_name, num_of_sources, num_of_data_item  FROM dataset ";
		String where = " WHERE  num_of_sources IN ( 1000, 5000, 10000) ";
		String where2 = " AND dataset_id NOT IN (SELECT dataset_id from "
				+ "experiment_results where voter_name = 'LTM')";

		String orderBy = " ORDER BY num_of_sources, num_of_data_item";
		
		String selectQuery = select + where + where2 + orderBy;//+ " AND " + where2 ;//+ " ORDER BY num_of_sources, num_of_data_item";

		return selectQuery;
	}
	


	private static String getSyntheticExperimentSelectQuery(boolean runLTM){
		int numOfSources = 50;
		int numOfObjects = 200;
		int numOfProperties = 5;

		List<Integer> numOfDistinctValues_List = new ArrayList<Integer>();
		for (int i = 2; i <=20; i++) {
			numOfDistinctValues_List.add(i);
		}

		int numOfSourcesPerValue = -1;
		int dependencyID = Globals.controlSourcesDependency_Nocontrol;
		int similarityID = Globals.valueSimilarity_dissSimilar;

		List<Double> coverage_list = new ArrayList<Double>();
//		coverage_list.add(0.25);
		coverage_list.add(0.75);
		int data_items_coverage_method_id = 2;// 2->expo

		int numOfDiferentValueGenerationMethod = 3;
		int controlSrcTrueValMethod = 2; // 3 = FO, 2 PF, 
		double percentageOfTruValPerSrc = 0.5;// only used for uniform method 
		int controlNumOfSrcPerValMethod = -1;

		String select = "SELECT dataset_id, dataset_file_name, num_of_sources, num_of_data_item  FROM dataset ";
		String where = " WHERE ";
				where = where + "num_of_sources =  " + numOfSources;
				where = where + " AND num_of_data_item = " + (numOfProperties * numOfObjects);
				
				where = where + " AND data_items_coverage_method_id = " + data_items_coverage_method_id;
				if (data_items_coverage_method_id == 1) {
					where = where + " AND ( ";
					for (double dataItemCoverage : coverage_list) {
						where = where + "coverage > " + (dataItemCoverage - 0.0001) + 
								" AND coverage < " + (dataItemCoverage + 0.0001) + " OR ";
					}
					where = where.trim().substring(0, where.length() - 4) + " ) AND ";
				}

				where = where + " AND control_src_true_value_method_id = " + controlSrcTrueValMethod;
				if (controlSrcTrueValMethod == 1) {
					where = where + " AND percentage_true_val_per_src > " + (percentageOfTruValPerSrc - 0.0001) + 
							" AND percentage_true_val_per_src < " + (percentageOfTruValPerSrc + 0.0001);
				}
				
				where = where + " AND di_different_values_method_id = " + numOfDiferentValueGenerationMethod;
				if ( ! numOfDistinctValues_List.isEmpty()) {
					where = where + " AND num_of_different_values in (";
					for (int i : numOfDistinctValues_List) {
						where = where + i + ", ";
					}
					where = where.substring(0, where.length()-2) + ") ";
					
				}

//				where = where + " AND control_num_of_src_per_value_method_id = " + controlNumOfSrcPerValMethod + " AND ";
//				where = where + " AND num_of_sources_per_value = " + numOfSourcesPerValue + " AND ";

				where = where + " AND similarity_id = " + similarityID;// + " AND ";
				where = where + " AND dependency_id = " + dependencyID;
				if (dependencyID == 1) {
					where = where + " AND num_of_independent_sources " + " IN " + " (10, 20, 30, 40, 50) " ;
					where = where + " AND percentage_of_copied_values > 0.99 ";
				}


		String whereNotAlreadyRun = " dataset_id NOT IN (SELECT dataset_id from experiment_results)";
		String selectQuery = select + where 
				;
//				+ " AND " + whereNotAlreadyRun ;

		return selectQuery; 
	}

	public static void startExperiments(String selectQuery, boolean runLTM) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager.getConnection("jdbc:mysql://localhost/DAFNAdb?user=root&password=root");
			// Statements allow to issue SQL queries to the database
			readingStatement = connect.createStatement();
			writingStatement = connect.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}


		String datasetFolderName;
		int datasetId;
		HashMap<String, VoterQualityMeasures> resultMap;
		VoterQualityMeasures tempResult;
		String insert = "INSERT INTO "
				+ "experiment_results ("
				+ "`dataset_id`, "
				+ "`voter_name`, "
				+ "`precision`, `accuracy`, `recall`, `specificity`, " 
				+ "`number_of_iteration`, `voter_duration_ms`) VALUES ";
		String s = "";
		String tempInsert;
		try {
			System.out.println(selectQuery);
			ResultSet resultSet = readingStatement.executeQuery(selectQuery);
			int experimentCounter = 0;
			System.out.print("Start synthetic experiment: ");
			while (resultSet.next()) {
				System.out.print(experimentCounter + ", ");
				experimentCounter ++;
				if (experimentCounter % 30 == 0) {
					System.out.println();
				}
				datasetId = resultSet.getInt(1);
				datasetFolderName = resultSet.getString(2);

				String truthDirectory = Globals.directory_syntheticDataSetMainDirectory + "/" + datasetFolderName + "/truth";
				String dataSetDirectory = Globals.directory_syntheticDataSetMainDirectory + "/" + datasetFolderName + "/claims";
				String experimentFolderName = Globals.directory_syntheticDataSetMainDirectory + "/" + datasetFolderName + "/experimentResult";
				File experimentFolder = new File(experimentFolderName);
				experimentFolder.mkdir();

				resultMap = launchSyntheticExperiment(dataSetDirectory, truthDirectory, experimentFolderName, runLTM);
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
				tempInsert = insert + tempInsert.substring(0, tempInsert.length()-1) + ";" ;
				s = tempInsert;
				writingStatement.executeUpdate(tempInsert);

				// contengency table does not contain the accnodep
				String contengencyTableInsert = contengencyTable.saveTrueValuesToDatabase(connect, datasetId);
			}
		} catch (SQLException e) {
			System.out.println(s);
			e.printStackTrace();
		}
	}

	private static HashMap<String, VoterQualityMeasures> launchSyntheticExperiment(String dataSetDir, String truthDir, String experimentName, boolean runLTM) {
//		System.out.println("Start synthetic experiment...");
		DataSet dataSet = ExperimentDataSetConstructor_Development.readDataSet(Globals.starting_Confidence, Globals.starting_trustworthiness, 
				dataSetDir, 0, false, null, 
				qcri.dafna.experiment.ExperimentDataSetConstructor_Development.Experiment.Synthetic, truthDir, false);
		DataSet dataSetSinglePropertyValue = null;
		//		DataSet dataSetSinglePropertyValue = ExperimentDataSetConstructor.readDataSet(0, 0, 
		//				Globals.directory_formattedDAFNADataset_BooksFolder_SingleClaimValue, 0, true, 
		//				ValueType.ISBN, qcri.dafna.voter.ExperimentDataSetConstructor.Experiment.Synthetic, directory);

		boolean convergence100 = false;
		boolean runSyntheticBoolean = false;
//		boolean runLTM = false;

		HashMap<String, VoterQualityMeasures> resultsMap = runExperiment(convergence100 ,dataSet, experimentName, 
				runLTM, dataSet, runSyntheticBoolean, null, "");
//		System.out.println("Synthetic experiment finished.");
		return resultsMap;
	}
}
