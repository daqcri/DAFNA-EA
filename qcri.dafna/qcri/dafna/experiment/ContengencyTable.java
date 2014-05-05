package qcri.dafna.experiment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.ValueBucket;

public class ContengencyTable {



	List<HashMap<String, String>> trueValues;
	List<String> modelsName;

	List<List<Integer>> contengencyTable = new ArrayList<List<Integer>>();
	public ContengencyTable() {
		for (int i = 0 ; i < 11; i ++) {
			contengencyTable.add(new ArrayList<Integer>());
			trueValues = new ArrayList<HashMap<String,String>>();
			modelsName = new ArrayList<String>();
		}
	}

	public void readTrueValues(DataSet dataset, String modelName) {
		HashMap<String, String> modelTrueValues = new HashMap<String, String>();
		for (List<ValueBucket> list : dataset.getDataItemsBuckets().values()) {
			for (ValueBucket b : list) {
				if (b.getClaims().get(0).isTrueClaimByVoter()) {
					modelTrueValues.put(b.getClaims().get(0).dataItemKey(), b.getCleanedString());
				}
			}
		}
		modelsName.add(modelName);
		trueValues.add(modelTrueValues);
	}


	public String saveTrueValues(Connection connect, int dataset_database_id) {
		Statement writingStatement = null;
		try {
			if (connect == null) {
				Class.forName("com.mysql.jdbc.Driver");
				// Setup the connection with the DB
				connect = DriverManager.getConnection("jdbc:mysql://localhost/DAFNAdb?user=root&password=root");
			}
			// Statements allow to issue SQL queries to the database
			writingStatement = connect.createStatement();
			String insert = getInsertStatement(String.valueOf(dataset_database_id));
System.out.println(insert);
			writingStatement.executeUpdate(insert);
			return insert;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	public String getInsertStatement(String dataset_database_id) {
		try {
			String insert = "INSERT INTO DAFNAdb.contengency (dataset_id, dataItem_key ";
			for (String voterName : modelsName) {
				if (voterName.equals(Globals.voterVoting)) {
					insert = insert + ", voting_value";
				} else if (voterName.equals(Globals.voterTruthFinder)) {
					insert = insert + ", truthFinder_value";
				} else if (voterName.equals(Globals.voterCosine)) {
					insert = insert + ", cosine_value";
				} else if (voterName.equals(Globals.voter2Estimates)) {
					insert = insert + ", twoEstimates_value";
				} else if (voterName.equals(Globals.voter3Estimates)) {
					insert = insert + ", threeEstimates_value";
				} else if (voterName.equals(Globals.voterSimpleLCA)) {
					insert = insert + ", simpleLCA_value";
				} else if (voterName.equals(Globals.voterGuessLCA)) {
					insert = insert + ", guessLCA_value";
				} else if (voterName.equals(Globals.voterAccuSim)) {
					insert = insert + ", copySimAccu_value";
				} else if (voterName.equals(Globals.voterDepen)) {
					insert = insert + ", copyNoSimNoAccu_value";
				} else if (voterName.equals(Globals.voterAccu)) {
					insert = insert + ", copyNoSimAccu_value";
				} else if (voterName.equals("AccuCopy T F"/*Globals.voterCopyTF*/)) {
					insert = insert + ", copySimNoAccu_value";
				} else if (voterName.equals(Globals.voterLTM)) {
					insert = insert + ", LTM_value";
				} else if (voterName.equals(Globals.voterMLE)) {
					insert = insert + ", MLE_value";
				}
			}

			insert = insert + ") VALUES ";
			String value;
			Set<String> diKies = trueValues.get(0).keySet();
			for (String dataItemKey : diKies) {
				insert = insert + " ( " + dataset_database_id + ", '" + dataItemKey + "'";
				for (HashMap<String, String> truthMap : trueValues) {
					value = truthMap.get(dataItemKey);
					if (value == null) {
						value = "";
					}
					value = "'" + value + "'";
					insert = insert + ", " + value;
				}
				insert = insert + "), ";
			}

			insert = insert.trim();
			insert = insert.substring(0,insert.length()-1) + ";";
			return insert;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
