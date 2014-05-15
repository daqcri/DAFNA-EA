package qcri.dafna.dataModel.quality.dataQuality;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataFormatter.DataComparator;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher;
import qcri.dafna.dataModel.dataFormatter.PersonsNameComparator;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;


public class DataQualityMeasurments {
	DataSet dataSet;

	private int maxNumOfClaimsPerSources = -1;
	public int getMaxNumOfClaimsPerSources() {
		return maxNumOfClaimsPerSources;
	}
	public void setMaxNumOfClaimsPerSources(int maxNumOfClaimsPerSources) {
		this.maxNumOfClaimsPerSources = maxNumOfClaimsPerSources;
	}

	private int goldStandardTrueValueCount = -1;
	public void setGoldStandardTrueValueCount(int trueValueCount) {
		this.goldStandardTrueValueCount = trueValueCount;
	}
	public int getGoldStandardTrueValueCount() {
		return goldStandardTrueValueCount;
	}

	// the key id the hashcode of the object = 
	// (entityID+ObjectID).hashcode received from the SourceClaim.objectHashCode() method
	private HashMap<String, Double> redundancyOnObjects;
	public HashMap<String, Double> getRedundancyOnObjects() {
		return redundancyOnObjects;
	}
	private double averageRedundencyOnObject = -1;
	public double getAverageRedundencyOnObject() {
		return averageRedundencyOnObject;
	}
	private double averageRedundencyOnDataItem = -1;
	public double getAverageRedundencyOnDataItem() {
		return averageRedundencyOnDataItem;
	}

	// key = dataItem hashCode received from SourceClaim.dataItemHashCode() method
	private HashMap<String, DataItemMeasures> dataItemMeasures;
	public HashMap<String, DataItemMeasures> getDataItemMeasures() {
		return dataItemMeasures;
	}

	private double precisionOfDominantValue = -1;
	public double getPrecisionOfDominantValue() {
		return precisionOfDominantValue;
	}

	private HashMap<String, Double> sourcesAccuracies;
	public HashMap<String, Double> getSourcesAccuracies() {
		return sourcesAccuracies;
	}

	DataSetTimingMeasures timingMeasures;
	public DataSetTimingMeasures getTimingMeasures() {
		return timingMeasures;
	}
	public void setTimingMeasures(DataSetTimingMeasures timingMeasures) {
		this.timingMeasures = timingMeasures;
	}

	private double averageNumOfClaimsPerSource = -1;
	public double getAverageNumOfClaimsPerSource() {
		return averageNumOfClaimsPerSource;
	}

	private int totalNumOfClaims = -1;
	public int getTotalNumOfClaims() {
		return totalNumOfClaims;
	}

	private double averageNumOfValuesPerDataItem = -1;
	public double getAverageNumOfValuesPerDataItem() {
		return averageNumOfValuesPerDataItem;
	}

	public DataQualityMeasurments(DataSet dataSet) {
		this.dataSet = dataSet;
		averageRedundencyOnObject = 0;
		redundancyOnObjects = new HashMap<String, Double>();
		dataItemMeasures = new HashMap<String, DataItemMeasures>();
		for (String key : dataSet.getDataItemClaims().keySet()) {
			dataItemMeasures.put(key, new DataItemMeasures());
		}
	}

	public void computeDataQaulityMeasures(double toleranceFactor) {
		timingMeasures.startDataQualityMeasuresComputationTime();
		computeRedundancy();
		computeTolerance(toleranceFactor);
		computeDominantValues_DominanceFactor_PrecisionOfDominantValue_AndNumberOfValues_numOfClaimPerBucketAverage();
		computeEntropy();
		computeDeviation();
		computeSourcesAccuracies_avgNumOfClaimsPerSource();
		if (toleranceFactor != 0.0) {
			timingMeasures.startBucketsBuildingTime();
			dataSet.computeValueBuckets(true);
			timingMeasures.endBucketsBuildingTime();
		}
		timingMeasures.endDataQualityMeasuresComputationTime();
	}

	/**
	 * Print to the console the different dataSet measures
	 */
	public void printDataSetInfo() {

		System.out.println("Number of Claims: " + totalNumOfClaims);
		System.out.println("Gold Standard Values Count: " + this.goldStandardTrueValueCount);

		System.out.println("The number of sources :" + dataSet.getSourcesHash().size());
		System.out.println("Average Number Of Claims Per Source: " + this.averageNumOfClaimsPerSource);

		System.out.println("Number of DataItems: " + dataSet.getDataItemClaims().size());
		System.out.println("average number of Values Per DataItem: " +  averageNumOfValuesPerDataItem);
		System.out.println("Avergae redundency on dataItem: " + averageRedundencyOnDataItem);

		System.out.println("Precision Of Dominant Value: " + this.precisionOfDominantValue);
		timingMeasures.printTimingMeasures();

		System.out.println("Number of Objects: " + redundancyOnObjects.size());
		System.out.println("Avergae redundency on objects: " + averageRedundencyOnObject);


	}
	/*---------------------- Data Redundancy ----------------------*/
	/**
	 * Compute the redundancy on objects: percentage of sources providing a particular object.
	 * Compute the redundancy on data item: percentage of sources providing a particular data item.
	 */
	private void computeRedundancy() {
		HashMap<String, Set<String>> objectsSources = new HashMap<String, Set<String>>();
		HashMap<String, Set<String>> dataItemSources = new HashMap<String, Set<String>>();
		String sourceID;
		for (String objectKey : dataSet.getObjectToClaimHash().keySet()) {
			if (! objectsSources.containsKey(objectKey)) {
				objectsSources.put(objectKey, new HashSet<String>());
			}
			for (SourceClaim claim : dataSet.getObjectToClaimHash().get(objectKey)) {
				sourceID = claim.getSource().getSourceIdentifier();
				objectsSources.get(objectKey).add(sourceID);
				if (dataItemSources.containsKey(claim.dataItemKey())) {
					dataItemSources.get(claim.dataItemKey()).add(sourceID);
				} else {
					Set<String> temp = new HashSet<String>();
					temp.add(sourceID);
					dataItemSources.put(claim.dataItemKey(), temp);
				}
			}
		}
		double allSourcesSize = dataSet.getSourcesHash().size();
		redundancyOnObjects = new HashMap<String, Double>();
		double redundancy;
		averageRedundencyOnObject = 0;
		for (String key : objectsSources.keySet()) {
			redundancy = ((double) objectsSources.get(key).size()) / allSourcesSize;
			redundancyOnObjects.put(key, redundancy);
			averageRedundencyOnObject += redundancy;
		}
		averageRedundencyOnObject = averageRedundencyOnObject / (double)redundancyOnObjects.size();
		averageRedundencyOnDataItem = 0;
		for (String key : dataItemSources.keySet()) {
			redundancy = ((double) dataItemSources.get(key).size()) / allSourcesSize;
			dataItemMeasures.get(key).setRedundancyOnDataItem(redundancy);
			averageRedundencyOnDataItem += redundancy;
		}
		averageRedundencyOnDataItem = averageRedundencyOnDataItem/ (double)dataItemMeasures.size();
	}

	/*---------------------- Data Consistency ----------------------*/

	/* --------- Tolerance ---------*/
	private void computeTolerance(double toleranceFactor) {
		if (toleranceFactor == 0.0) {
			for (String dataItemKey : dataItemMeasures.keySet()) {
				dataItemMeasures.get(dataItemKey).setTolerance(0);
			}
		}
		HashMap<String, DescriptiveStatistics> numericalPropertyValues = new HashMap<String,DescriptiveStatistics>();
		ValueType vType;
		for (String dataItemKey : dataSet.getDataItemClaims().keySet()) {
			try {
				vType = dataSet.getDataItemClaims().get(dataItemKey).get(0).getValueType();
			} catch (NullPointerException e) {
				continue;
			}
			if ( ! vType.equals(ValueType.FLOAT) && ! vType.equals(ValueType.LONG)) {
				continue;
			}
			for (SourceClaim dataItemClaim : dataSet.getDataItemClaims().get(dataItemKey)) {
				if (dataItemClaim.isClean()) {
					if ( ! numericalPropertyValues.containsKey(dataItemKey)) {
						numericalPropertyValues.put(dataItemKey, new DescriptiveStatistics());
					}
					numericalPropertyValues.get(dataItemKey).addValue(Double.valueOf(dataItemClaim.getPropertyValue().toString()));
				}
			}
		}
		double tolerance;
		for (String dataItemKey : numericalPropertyValues.keySet()) {
			tolerance = toleranceFactor * numericalPropertyValues.get(dataItemKey).getPercentile(50);
			dataItemMeasures.get(dataItemKey).setTolerance(tolerance);
		}
	}




	/* --------- Dominant-Value and Dominance-Factor---------*/
	/**
	 * Compute parameters: 
	 * 		- Dominant value for a data item: the value with the largest number of provider.
	 * 		- Dominance Factor: Percentage of sources that provide the dominant value for 
	 * data item among all providers for the same data item.
	 *  	- Number of values for a data item: the number of different values provided for a data item.
	 * 
	 */
	private void computeDominantValues_DominanceFactor_PrecisionOfDominantValue_AndNumberOfValues_numOfClaimPerBucketAverage() {
		int numOfSrc;
		int numOfClaimsPerBucket;
		double dominanceFactor;
		Object dominantValue;
		boolean cleaned;
		Set<String> dataItemProviders;
		DataItemMeasures dim;
		List<ValueBucket> bucketsList;
		ValueType valueType;
		precisionOfDominantValue = 0;
		int tempSize;
		for (String dataItemKey : dataSet.getDataItemsBuckets().keySet()) {
			bucketsList = dataSet.getDataItemsBuckets().get(dataItemKey);
			if (bucketsList.size() == 0) {
				System.out.println();
			}
			numOfClaimsPerBucket = 0;
			dataItemProviders = new HashSet<String>();
			numOfSrc = bucketsList.get(0).getSourcesKeys().size();
			valueType = bucketsList.get(0).getValueType();
			// the max and min values in the buckets are the exact value for all claims as we didn't compute tolerant buckets yet
			if ( bucketsList.get(0).isClean() && !DataTypeMatcher.savedAsString(valueType)) {
				dominantValue = bucketsList.get(0).getMaxValue();
				cleaned = true;
			} else if ( DataTypeMatcher.savedAsString(valueType)) {
				dominantValue =  bucketsList.get(0).getCleanedString();
				cleaned = true;
			} else {
				dominantValue =  bucketsList.get(0).getCleanedString();
				cleaned = false;
			}
			for (ValueBucket b : bucketsList) {
				dataItemProviders.addAll(b.getSourcesKeys());
				tempSize =  b.getSourcesKeys().size();
				if (numOfSrc < tempSize) {
					numOfSrc = tempSize;
					if ( b.isClean() && !DataTypeMatcher.savedAsString(valueType)) {
						dominantValue = b.getMaxValue();
						cleaned = true;
					} else if (DataTypeMatcher.savedAsString(valueType)) {
						dominantValue =  b.getCleanedString();
						cleaned = true;
					} else {
						dominantValue =  b.getCleanedString();
						cleaned = false;
					}
				}
				numOfClaimsPerBucket = numOfClaimsPerBucket + b.getClaims().size();
			}
			dim = dataItemMeasures.get(dataItemKey);
			dim.setDominantValue(dominantValue);
			dim.setDominantValueCleaned(cleaned);
			dominanceFactor = numOfSrc / dataItemProviders.size();
			dim.setDominanceFactor(dominanceFactor);
			// set the number of values
			dim.setNumberOfValues(bucketsList.size());
			dim.setAverageNumOfValuesPerBucket((double)numOfClaimsPerBucket/(double)bucketsList.size());
			if (dim.getTrueValue() != null) {
				if (DataComparator.hasSameValue(dominantValue, dim.getTrueValue(), valueType)) {
					precisionOfDominantValue ++;
				}
			}
		}
		averageNumOfValuesPerDataItem = 0;
		for (List<ValueBucket> bList : dataSet.getDataItemsBuckets().values()) {
			averageNumOfValuesPerDataItem += bList.size();
		}
		averageNumOfValuesPerDataItem = averageNumOfValuesPerDataItem/(double)dataItemMeasures.size();
		precisionOfDominantValue = precisionOfDominantValue/goldStandardTrueValueCount;
	}

	/* --------- Entropy ---------*/

	private void computeEntropy() {
		double entropy;
		List<Integer> providersCount;
		Set<String> allProviders;
		Set<String> tempProviders;
		int allProvidersCount;
		for (String dataItemKey : dataSet.getDataItemsBuckets().keySet()) {
			providersCount = new ArrayList<Integer>();
			allProviders = new HashSet<String>();
			for (ValueBucket b : dataSet.getDataItemsBuckets().get(dataItemKey)) {
				tempProviders = b.getSourcesKeys();
				providersCount.add(tempProviders.size());
				allProviders.addAll(tempProviders);
			}
			allProvidersCount = allProviders.size();
			entropy = 0;
			for (int n : providersCount) {
				entropy = entropy - ( (n * allProvidersCount) * Math.log(n * allProvidersCount) );
			}
			dataItemMeasures.get(dataItemKey).setEntropy(entropy);
		}
	}

	/* --------- Deviation ---------*/

	private void computeDeviation() {
		for (String dataItemKey : dataSet.getDataItemsBuckets().keySet()) {
			ValueType type = dataSet.getDataItemsBuckets().get(dataItemKey).get(0).getValueType();
			if (type.equals(ValueType.LONG) || type.equals(ValueType.FLOAT)) {
				computeDeviationForNumber(dataItemKey);
			}
			/**
			 * The absolute deviation is computed for Date/Time values.
			 */
			else if (type.equals(ValueType.DATE) || type.equals(ValueType.TIME)) {
				computeDeviationForDateandTime(dataItemKey);
			}
			else if (type.equals(ValueType.STRING) || type.equals(ValueType.Name)) {
				computeDeviationForString(dataItemKey);
			}
			else if (type.equals(ValueType.ListNames)) {
				computeDeviationForListofString(dataItemKey);
			}
		}
	}
	private void computeDeviationForString(String dataItemKey) {
		double deviation = 0.0;
		int numberOfCleanValues = 0;
		DataItemMeasures dim = dataItemMeasures.get(dataItemKey);;
		String dominantValue = (String) dim.getDominantValue();
		for (ValueBucket b : dataSet.getDataItemsBuckets().get(dataItemKey)) {
			numberOfCleanValues ++;
			deviation = deviation + DataComparator.computeSimilarity(b.getCleanedString(), dominantValue);;
		}
		deviation = deviation / numberOfCleanValues;
		dim.setDeviation(deviation);
	}
	private void computeDeviationForListofString(String dataItemKey) {
		double deviation = 0.0;
		int numberOfCleanValues = 0;
		DataItemMeasures dim = dataItemMeasures.get(dataItemKey);;
		String dominantValue = (String) dim.getDominantValue();
		for (ValueBucket b : dataSet.getDataItemsBuckets().get(dataItemKey)) {
			numberOfCleanValues ++;
			deviation = deviation + PersonsNameComparator.computePersonsNamesListSimilarity(b.getCleanedString(), dominantValue);
		}
		deviation = deviation / numberOfCleanValues;
		dim.setDeviation(deviation);
	}
	private void computeDeviationForDateandTime(String dataItemKey) {
		double deviation;
		int numberOfCleanValues;
		DataItemMeasures dim;
		dim = dataItemMeasures.get(dataItemKey);
		if (dim.isDominantValueCleaned()) {
			deviation = 0;
			numberOfCleanValues = 0;
			double value;
			double dominantValue = ((Date)dim.getDominantValue()).getTime()/60000; // get the time in minutes
			for (ValueBucket b : dataSet.getDataItemsBuckets().get(dataItemKey)) {
				if (b.isClean()) {
					numberOfCleanValues ++;
					// cast the Date or Time object into Date;

					value = ((Date)b.getMaxValue()).getTime() / 60000;
					// the buckets contains only exact equal values, minVal and maxVal equals to the exact value;
					deviation = deviation + Math.abs(value -  dominantValue);
				}
			}
			deviation = deviation / numberOfCleanValues;
			dim.setDeviation(deviation);
		} else {
			dim.setDeviation(-1);
		}
	}
	private void computeDeviationForNumber(String dataItemKey) {
		double deviation;
		double temp;
		int numberOfCleanValues;
		DataItemMeasures dim;
		dim = dataItemMeasures.get(dataItemKey);
		if (dim.isDominantValueCleaned()) {
			deviation = 0;
			numberOfCleanValues = 0;
			double dominantValue = Double.valueOf(dim.getDominantValue().toString());
			for (ValueBucket b : dataSet.getDataItemsBuckets().get(dataItemKey)) {
				if (b.isClean()) {
					numberOfCleanValues ++;
					// the buckets contains only exact equal values, minVal and maxVal equals to the exact value;
					temp =  Math.abs(Double.valueOf(b.getMaxValue().toString()) -  dominantValue) / Double.valueOf(dim.getDominantValue().toString());
					temp = temp * temp;
					deviation = deviation + temp;
				}
			}
			deviation = Math.sqrt(deviation / numberOfCleanValues);
			dim.setDeviation(deviation);
		} else {
			dim.setDeviation(-1);
		}
	}

	/* --------- Source Accuracy ---------*/
	/**
	 * The source accuracy is the percentage of provided values that are consistent with the given gold standard. 
	 */
	private void computeSourcesAccuracies_avgNumOfClaimsPerSource() {
		maxNumOfClaimsPerSources = 0;
		sourcesAccuracies = new HashMap<String, Double>();
		totalNumOfClaims = 0;
		int numOfClaims = 0;
		int numOfTrueClaims = 0;
		String dataItemKey;
		double accuracy;

		for (Source source : dataSet.getSourcesHash().values()) {
			numOfClaims = 0;
			numOfTrueClaims = 0;
			totalNumOfClaims += source.getClaims().size();
			if (source.getClaims().size() > maxNumOfClaimsPerSources) {
				maxNumOfClaimsPerSources = source.getClaims().size();
			}
			for (SourceClaim claim : source.getClaims()) {
				dataItemKey = claim.dataItemKey();
				DataItemMeasures dim = dataItemMeasures.get(dataItemKey);
				if (dim.getTrueValue() != null) {
					numOfClaims ++;
					if (DataComparator.hasSameValue(claim.getPropertyValue(), dim.getTrueValue(), claim.getValueType())) {
						numOfTrueClaims ++;
					}
				}
			}
			accuracy = ((double)numOfTrueClaims/(double)numOfClaims);
			sourcesAccuracies.put(source.getSourceIdentifier(), accuracy);
		}
		averageNumOfClaimsPerSource = totalNumOfClaims /(double)dataSet.getSourcesHash().size();
	}


}
