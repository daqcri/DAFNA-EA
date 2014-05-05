package qcri.dafna.voter.Copying;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;

public class SourceProfiling {
//	private Source source;
	/**
	 * key = object Id
	 */
	private HashMap<String, Double> independent_probability_for_not_provided_objects;
	/**
	 * key = dataItem key
	 */
	private HashMap<String, Double> independent_probability_for_provided_objects_not_provided_dataItem;

	private Set<String> objects;
	private Set<String> dataItems;
	/**
	 * key = Attribute Id
	 * value = Set of Objects Id for which this source provide a value for the corresponding attribute.
	 */
	private HashMap<String, Set<String>> objectsPerAttributeMap;

	public SourceProfiling(Profiling profiling, Source source, DataSet dataSet) {
		independent_probability_for_not_provided_objects = new HashMap<String, Double>();
		independent_probability_for_provided_objects_not_provided_dataItem = new HashMap<String, Double>();
		objects = new HashSet<String>();
		dataItems = new HashSet<String>();
		initializeIndependentProbabilities(profiling, dataSet, source);
		initObjectsPerAttribute(source);
	}
	private void initObjectsPerAttribute(Source source) {
		objectsPerAttributeMap = new HashMap<String, Set<String>>();
		String attrId;
		for (SourceClaim claim : source.getClaims()) {
			attrId = claim.getPropertyName();
			if ( ! objectsPerAttributeMap.containsKey(attrId)) {
				objectsPerAttributeMap.put(attrId, new HashSet<String>());
			}
			objectsPerAttributeMap.get(attrId).add(claim.getObjectIdentifier());
		}
	}

	private void initializeIndependentProbabilities(Profiling profiling, DataSet dataSet, Source source) {
		for (SourceClaim claim : source.getClaims()) {
			objects.add(claim.getObjectIdentifier());
			dataItems.add(claim.dataItemKey());
		}
		String obj;
		String attr;
		String dataItemKey;
		for (List<SourceClaim> claims : dataSet.getDataItemClaims().values()) {
			obj = claims.get(0).getObjectIdentifier();
			if ( ! objects.contains(obj)) {
			/* compute 	probability_for_not_provided_objects */
				initialize_probability_for_not_provided_objects(obj, profiling, source);
			} else 
			/* if ( ! dataItems.contains(claims.get(0).dataItemKey()))*/ 
				// -- > this value also needed in the computation of the provided values, so it is computed in all cases 
				{
				/* compute probability_for_provided_objects_not_provided_dataItem */
				attr = claims.get(0).getPropertyName();
				dataItemKey = claims.get(0).dataItemKey();
				initialize_probability_for_provided_objects_not_provided_dataItem(attr, dataItemKey, profiling, source);
			}  // -- > this value also needed in the computation of the provided values 
			// so it is also computes/* else if (it contains this dataItem) then do not initialize any value */
		}
	}

	private void initialize_probability_for_not_provided_objects(String objectId, Profiling profiling, Source source) {
		double val1 = profiling.getObjectCoverage().get(objectId);
		double val2 = profiling.getObjectLevelSourceCoverage().get(source.getSourceIdentifier());
		double prob = profiling.getTotalSourceNumber() * val1 * val2 / profiling.getSumofAllSourcesObjectLevelCoverage();
		prob = 1 - prob;
		independent_probability_for_not_provided_objects.put(objectId, prob);
	}
	private void initialize_probability_for_provided_objects_not_provided_dataItem(String attributeId, String dataItemKey, Profiling profiling, Source source) {
		double val1 = 0;
		try {
		 val1 = profiling.getAttributeLevelSourceCoverage().get(source.getSourceIdentifier()).get(attributeId);
		} catch (NullPointerException e) {
			val1 = 0;
		}
		double val2 = profiling.getDataItemCoverage().get(dataItemKey);
		double prob = profiling.getTotalDataItemNumber() * val1 * val2 / profiling.getSumofAlldataItemCoverage();
		prob = 1 - prob;
		independent_probability_for_provided_objects_not_provided_dataItem.put(dataItemKey, prob);
	}
	public Set<String> getDataItems() {
		return dataItems;
	}
	public Set<String> getObjects() {
		return objects;
	}
	public HashMap<String, Double> getIndependent_Probability_for_not_provided_objects() {
		return independent_probability_for_not_provided_objects;
	}
	public HashMap<String, Double> getIndependent_Probability_for_provided_objects_not_provided_dataItem() {
		return independent_probability_for_provided_objects_not_provided_dataItem;
	}
	/**
	 * key = Attribute Id
	 * value = Set of Objects Id for which this source provide a value for the corresponding attribute.
	 */
	public HashMap<String, Set<String>> getObjectsPerAttributeMap() {
		return objectsPerAttributeMap;
	}
}
