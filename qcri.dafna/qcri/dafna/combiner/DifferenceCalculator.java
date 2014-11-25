package qcri.dafna.combiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataFormatter.DataComparator;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher;
import qcri.dafna.dataModel.dataFormatter.PersonsNameComparator;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;
import qcri.dafna.dataModel.data.SourceClaim;

public class DifferenceCalculator {
	
	
	DataSet dataSet;
	
	DataItemMeasures dataItemMeasures;
	List<ValueBucket> bucketsList;
	List<String> wrongNames;
	List<String> goldStandardNames;
	List<String> namesInTheBucket;
	HashMap<Integer, Boolean> claimTruthHash;
	boolean singlePropertyValue;
	
	public DifferenceCalculator(DataSet dataSet, boolean singlePropertyValue) {
		this.dataSet = dataSet;
		claimTruthHash = constructClaimTruthHash();
		this.singlePropertyValue = singlePropertyValue;
	}
	
	public int getDifference(int claimID, boolean isTrue) {
		int ifDifferent =  -1;
		
		if(claimTruthHash.get(claimID) != null)
		{
			if(claimTruthHash.get(claimID) == true && isTrue == true)
			{
				//true positive
				ifDifferent = 0;
			}
			else if(claimTruthHash.get(claimID) == false && isTrue == true)
			{
				// false positive
				ifDifferent = 1;
			}
			else if(claimTruthHash.get(claimID) == false && isTrue == false)
			{
				// true negative
				ifDifferent = 2;
			}
			else if(claimTruthHash.get(claimID) == true && isTrue == false)
			{
				//false negative
				ifDifferent = 3;
			}
		}
		
		return ifDifferent;
	}
	
	public HashMap<Integer, Boolean> constructClaimTruthHash()
	{
		HashMap<Integer, Boolean> claimTruthHash =  new HashMap<Integer, Boolean>();
	
		for (String dataItemKey : dataSet.getDataItemsBuckets().keySet()) {
			dataItemMeasures = dataSet.getDataQualityMeasurments().getDataItemMeasures().get(dataItemKey);
			if (dataItemMeasures.getTrueValue() == null) {
				continue;
			}
			bucketsList = dataSet.getDataItemsBuckets().get(dataItemKey);
			
			
			if (bucketsList.get(0).getValueType().equals(ValueType.ListNames)) {
				wrongNames = new ArrayList<String>();
				goldStandardNames = new ArrayList<String>(Arrays.asList(((String)dataItemMeasures.getTrueValue()).split(Globals.cleanedListDelimiter)));
				
				for (ValueBucket b : bucketsList) {
						if ( ! singlePropertyValue) {
							namesInTheBucket = new ArrayList<String>(Arrays.asList((b.getCleanedString()).split(Globals.cleanedListDelimiter)));
						} else {
							//namesInTheBucket.add(b.getCleanedString());
							namesInTheBucket = new ArrayList<String>(Arrays.asList(b.getCleanedString()));
						}
						int truePositive = 0;
						int falsePositive = 0;
						for (String name : namesInTheBucket) {
							if (PersonsNameComparator.containName(goldStandardNames, name)) {
									truePositive++;
							} else { 
								falsePositive ++;
							}
						}
						if(truePositive > falsePositive+(goldStandardNames.size())/2)
						{
							for(SourceClaim claim : b.getClaims())
								claimTruthHash.put(claim.getId(), true);
						}
						else
						{
							for(SourceClaim claim : b.getClaims())
								claimTruthHash.put(claim.getId(), false);
						}
					}
		} else {
				for (ValueBucket b : bucketsList) {
					if (bucketContainsValue(b, dataItemMeasures)) {
						for(SourceClaim claim : b.getClaims()){
							claimTruthHash.put(claim.getId(), true);
						}
					} else {
						for(SourceClaim claim : b.getClaims()){
							claimTruthHash.put(claim.getId(), false);
						}
					}
				}
			}
		}
		return claimTruthHash;
	}
	
	boolean bucketContainsValue(ValueBucket b, DataItemMeasures dataItemMeasures) {
		if(DataTypeMatcher.savedAsString(b.getValueType()) ||
				(! b.isClean() && !dataItemMeasures.isTrueValueCleaned())) {
			return DataComparator.hasSameValue(b.getCleanedString(), dataItemMeasures.getTrueValue(), ValueType.STRING);
		}
		if ( (! b.isClean() && dataItemMeasures.isTrueValueCleaned()) ||  (b.isClean() && !dataItemMeasures.isTrueValueCleaned())) {
			return false;
		}
		return DataComparator.valueInRange(dataItemMeasures.getTrueValue(), b.getMinValue(), b.getMaxValue(), b.getValueType());
	}
}
