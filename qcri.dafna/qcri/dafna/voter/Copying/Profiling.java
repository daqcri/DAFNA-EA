package qcri.dafna.voter.Copying;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;

public class Profiling {

	/**
	 * key = source identifier
	 * value = percentage of objects covered by the source.
	 */
	private HashMap<String, Double> objectLevelSourceCoverage;
	/**
	 * key = source identifier
	 * value =  key   = attribute (property) name
	 * 			value = number of objects for which the source provide value for this attribute (divided by) set of objects this source provide.
	 * 			if an attribute ID does not exist, the coverage is zero
	 */
	private HashMap<String, HashMap<String, Double>> attributeLevelSourceCoverage;
	/**
	 * key = object ID
	 * value = percentage of sources providing this object
	 */
	private HashMap<String, Double> objectCoverage;// TODO recomputed using dependence probability
	/**
	 * key = dataItem ID
	 * value = number of sources providing this data item (divided by) number of sources providing the associated object 
	 */
	private HashMap<String, Double> dataItemCoverage;
	/**
	 * initialized in the evaluateObjectandDataitemCoverage method
	 */
	private Set<String> objectIds;
	/**
	 * initialized in the evaluateObjectandDataitemCoverage method
	 */
	private Set<String> dataItems;
	private double sumofAllSourcesObjectLevelCoverage;
	private double sumofAlldataItemCoverage;

	private double totalSourceNumber;
	private double totalDataItemNumber;
	private double totalObjectsNumber;

	/**
	 * Key = attribute ID
	 * value = number of objects for which there exist a source providing a value for the associated attribute.
	 */
	private HashMap<String, Double> numOfObjectsPerAttribute;

	public Profiling(DataSet dataSet) {
		objectLevelSourceCoverage = new HashMap<String, Double>();
		attributeLevelSourceCoverage = new HashMap<String, HashMap<String,Double>>();
		dataItemCoverage = new HashMap<String, Double>();
		objectCoverage = new HashMap<String, Double>();
		evaluate(dataSet);
	}

	private void evaluate(DataSet dataSet) {
		evaluateSourceCoverage(dataSet);
		evaluateObjectandDataitemCoverage(dataSet);
		totalSourceNumber = dataSet.getSourcesHash().size();
		totalDataItemNumber = dataSet.getDataItemClaims().size();
		totalObjectsNumber = dataSet.getObjectToClaimHash().size();
	}

	private void evaluateObjectandDataitemCoverage(DataSet dataSet) {
		sumofAlldataItemCoverage = 0;
		double coverage;
		objectIds = new HashSet<String>();
		dataItems = new HashSet<String>();
		HashMap<String, Set<String>> objectToSourcesMap = new HashMap<String, Set<String>>();
		double totalSourceNumber = dataSet.getSourcesHash().size();
		for (String objectKey : dataSet.getObjectToClaimHash().keySet()) {
			objectToSourcesMap.put(objectKey, new HashSet<String>());
			objectIds.add(objectKey);
		}

		Set<String> sources;

		for (List<SourceClaim> claimList : dataSet.getObjectToClaimHash().values()) {
			sources = objectToSourcesMap.get(claimList.get(0).getObjectIdentifier());
			for (SourceClaim claim : claimList) {
				sources.add(claim.getSource().getSourceIdentifier());
			}
		}
		for (String objectID : objectToSourcesMap.keySet()) {
			sources = objectToSourcesMap.get(objectID);
			coverage = ((double)sources.size()) / totalSourceNumber;
			objectCoverage.put(objectID, coverage);
		}

		String objectID;
		String dataItemKey;
		for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
			objectID = bucketsList.get(0).getClaims().get(0).getObjectIdentifier();
			dataItemKey = bucketsList.get(0).getClaims().get(0).dataItemKey();
			dataItems.add(dataItemKey);
			sources = new HashSet<String>();
			for (ValueBucket b : bucketsList) {
				sources.addAll(b.getSourcesKeys());
			}
			coverage = ((double)sources.size()) / ((double)objectToSourcesMap.get(objectID).size());
			dataItemCoverage.put(dataItemKey, coverage);
			sumofAlldataItemCoverage += coverage;
		}
	}

	private void evaluateSourceCoverage(DataSet dataSet) {
		sumofAllSourcesObjectLevelCoverage = 0;

		numOfObjectsPerAttribute = new HashMap<String, Double>();
		HashMap<String, Set<String>> ObjectsPerAttributeMap = new HashMap<String, Set<String>>();

		double coverage;
		double totalNumberOfObjects = dataSet.getObjectToClaimHash().size();
		Set<String> objects;
		HashMap<String, Double> attributes;
		double objetcsSize;
		for (Source source : dataSet.getSourcesHash().values()) {
			objects = new HashSet<String>();
			attributes = new HashMap<String, Double>();
			for (SourceClaim claim : source.getClaims()) {
				objects.add(claim.getObjectIdentifier());
				attributes.put(claim.getPropertyName(), 0.0);
			}
			objetcsSize = objects.size();
			coverage = objetcsSize / totalNumberOfObjects;
			objectLevelSourceCoverage.put(source.getSourceIdentifier(), coverage);
			sumofAllSourcesObjectLevelCoverage += coverage;

			for (SourceClaim claim : source.getClaims()) {
				attributes.put(claim.getPropertyName(), attributes.get(claim.getPropertyName())+(1/objetcsSize));

				if ( ! ObjectsPerAttributeMap.containsKey(claim.getPropertyName())) {
					ObjectsPerAttributeMap.put(claim.getPropertyName(), new HashSet<String>());
				}
				ObjectsPerAttributeMap.get(claim.getPropertyName()).add(claim.getObjectIdentifier());
			}
			attributeLevelSourceCoverage.put(source.getSourceIdentifier(), attributes);
		}
		for (String attrId : ObjectsPerAttributeMap.keySet()) {
			numOfObjectsPerAttribute.put(attrId, (double)ObjectsPerAttributeMap.get(attrId).size());
		}
	}

	/**
	 * key = source identifier
	 * value =  key   = attribute (property) name
	 * 			value = number of objects for which the source provide value for this attribute (divided by) set of objects this source provide.
	 * 			if an attribute ID does not exist, the coverage is zero
	 */
	public HashMap<String, HashMap<String, Double>> getAttributeLevelSourceCoverage() {
		return attributeLevelSourceCoverage;
	}
	public HashMap<String, Double> getDataItemCoverage() {
		return dataItemCoverage;
	}
	public HashMap<String, Double> getObjectCoverage() {
		return objectCoverage;
	}
	public HashMap<String, Double> getObjectLevelSourceCoverage() {
		return objectLevelSourceCoverage;
	}
	public double getTotalSourceNumber() {
		return totalSourceNumber;
	}
	public Set<String> getDataItems() {
		return dataItems;
	}
	public Set<String> getObjectIds() {
		return objectIds;
	}
	public double getSumofAlldataItemCoverage() {
		return sumofAlldataItemCoverage;
	}
	public double getSumofAllSourcesObjectLevelCoverage() {
		return sumofAllSourcesObjectLevelCoverage;
	}
	public double getTotalDataItemNumber() {
		return totalDataItemNumber;
	}
	public double getTotalObjectsNumber() {
		return totalObjectsNumber;
	}
	public HashMap<String, Double> getNumOfObjectsPerAttribute() {
		return numOfObjectsPerAttribute;
	}
}
