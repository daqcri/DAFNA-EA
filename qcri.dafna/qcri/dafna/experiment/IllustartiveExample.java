package qcri.dafna.experiment;

import java.util.HashMap;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;
import qcri.dafna.dataModel.quality.dataQuality.DataQualityMeasurments;
import qcri.dafna.dataModel.quality.dataQuality.DataSetTimingMeasures;

public class IllustartiveExample extends qcri.dafna.experiment.Experiment{

	public static void main(String[] args) {
		DataSet dataset = getDataSet(0.8, 0.8, 0.01);
		
		boolean convergence100 = false;
		boolean runSyntheticBoolean = false;
		boolean runLTM = false;
		
		String dir = Globals.directory_formattedDAFNADataset_IllustativeExample + "/experimentResult";
		runExperiment(convergence100, dataset, dir, runLTM, null, runSyntheticBoolean, null);
	}
	public static DataSet getDataSet(double startingConfidence, double statingTrustworthiness, double toleranceFactor) {
		Globals.tolerance_Factor = toleranceFactor;
		DataSetTimingMeasures timings = new DataSetTimingMeasures();
		DataSet dataSet = composeDatasetValues(startingConfidence, statingTrustworthiness);
		/**
		 *  Compute buckets with exact values.
		 *  The computed buckets building time will be overwrite if the the buckets 
		 *  are recomputed using tolerance.
		 */
		timings.startBucketsBuildingTime();
		dataSet.computeValueBuckets(false);
		timings.endBucketsBuildingTime();

		/**
		 *  Initialization for dataSet quality measures objects
		 */
		DataQualityMeasurments dqm = new DataQualityMeasurments(dataSet);
		dqm.setTimingMeasures(timings);
		dataSet.setDataQualityMeasurments(dqm);

		int trueValueCount = 4;
		timings.startTruthReadingTime();

		addTrueValue(dqm.getDataItemMeasures(), "stonebraker", "AffiliatedTo", "MIT");
		addTrueValue(dqm.getDataItemMeasures(), "Bermstein", "AffiliatedTo", "MSR");
		addTrueValue(dqm.getDataItemMeasures(), "Carey", "AffiliatedTo", "UCT");
		addTrueValue(dqm.getDataItemMeasures(), "Halevy", "AffiliatedTo", "Google");
		dqm.setGoldStandardTrueValueCount(trueValueCount);
		timings.endTruthReadingTime();

		dqm.computeDataQaulityMeasures(Globals.tolerance_Factor);

		return dataSet;
	}
	private static DataSet composeDatasetValues(double startingConfidence, double statingTrustworthiness) {
		DataSet dataset = new DataSet(startingConfidence, statingTrustworthiness);
		int claimId = 1;
		String propertyName = "AffiliatedTo";
		String timeStamp = null;
		double weight = 0.0;
		
		String sourceId = "S1";
		String objectId = "stonebraker";
		String stringValue = "MIT";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;
		objectId = "Bermstein";
		stringValue = "MSR";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;
		objectId = "Carey";
		stringValue = "UCI";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;
		objectId = "Halevy";
		stringValue = "Google";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;
		
		
		sourceId = "S2";
		objectId = "stonebraker";
		stringValue = "UWisc";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;
		
		sourceId = "S3";
		objectId = "Bermstein";
		stringValue = "AT&T";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;
		objectId = "Carey";
		stringValue = "BEA";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;
		objectId = "Halevy";
		stringValue = "UW";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;

		
		sourceId = "S4";
		objectId = "stonebraker";
		stringValue = "MIT";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;
		objectId = "Carey";
		stringValue = "BEA";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;
		objectId = "Halevy";
		stringValue = "MSR";
		dataset.addClaim(claimId, /*entityID,*/ objectId, objectId, propertyName, stringValue, weight , timeStamp, sourceId); claimId++;

		return dataset;
	}

	private static void addTrueValue(HashMap<String, DataItemMeasures> dataItemMeasures ,String onjectId, String propertyName, String propertyValue) {
		String dataItemKey = SourceClaim.dataItemKey(/*entityId,*/ onjectId, propertyName);
		DataItemMeasures dim = dataItemMeasures.get(dataItemKey);
		Object trueValue = propertyValue;
		dim.setTrueValueCleaned(true);
		dim.setTrueValue(trueValue);
	}
}
