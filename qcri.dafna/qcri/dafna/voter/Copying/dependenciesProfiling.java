package qcri.dafna.voter.Copying;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;

public class dependenciesProfiling {

	private double objectsCoverage_dependent;
	/**
	 * Key   = attribute key
	 * value = dependent dataItem coverage
	 */
	private HashMap<String, Double> attributeLevelSourceCoverage_dependent;

	private double selectivity_object;
	private double selectivity_overlap = 0.8;

	/**
	 * creating a dependenciesProfiling object for Source1 dependent on Source2
	 * @param source1
	 * @param sp1
	 * @param source2
	 * @param sp2
	 */
	public dependenciesProfiling(Source source1, SourceProfiling sp1, Source source2, SourceProfiling sp2, Profiling profiling, DataSet dataSet) {
		attributeLevelSourceCoverage_dependent = new HashMap<String, Double>();
		initValues(source1, sp1, source2, sp2, profiling, dataSet);
	}

	private void initValues(Source source1, SourceProfiling sp1, Source source2, SourceProfiling sp2, Profiling profiling, DataSet dataSet) {
		initObjectCoveragetandSelectivity(source1, sp1, source2, sp2, profiling);
		initAttributeLevelCoverageDependent(source1, sp1, source2, sp2, profiling, dataSet);
	}
	private void initAttributeLevelCoverageDependent(Source source1, SourceProfiling sp1, Source source2, SourceProfiling sp2, Profiling profiling, DataSet dataSet) {
		Set<String> intersection;
		double coverage;
		for (String attrId : sp1.getObjectsPerAttributeMap().keySet()) {
			if (sp2.getObjectsPerAttributeMap().get(attrId) != null) {
				intersection = new HashSet<String>(sp1.getObjectsPerAttributeMap().get(attrId));
				intersection.retainAll(sp2.getObjectsPerAttributeMap().get(attrId));
				coverage = sp1.getObjectsPerAttributeMap().get(attrId).size() - intersection.size();
				coverage = coverage / ( profiling.getNumOfObjectsPerAttribute().get(attrId) - sp1.getObjectsPerAttributeMap().get(attrId).size());
			} else {
				coverage = sp1.getObjectsPerAttributeMap().get(attrId).size()
						/ ( profiling.getNumOfObjectsPerAttribute().get(attrId) - sp1.getObjectsPerAttributeMap().get(attrId).size());
			}
			attributeLevelSourceCoverage_dependent.put(attrId, coverage);
		}
	}
	private void initObjectCoveragetandSelectivity(Source source1, SourceProfiling sp1, Source source2, SourceProfiling sp2, Profiling profiling) {
		Set<String> intersection = new HashSet<String>(sp1.getObjects());
		intersection.retainAll(sp2.getObjects());
		objectsCoverage_dependent = (sp1.getObjects().size() - intersection.size() ) / ( profiling.getTotalObjectsNumber() - sp2.getObjects().size() );

		Set<String> sp1MinusSp2 = new HashSet<String>(sp1.getObjects());
		sp1MinusSp2.removeAll(sp2.getObjects());
		double tempN = sp2.getObjects().size() * sp1MinusSp2.size() / (profiling.getTotalObjectsNumber() -  sp2.getObjects().size());

		// TODO remove comment
		//		selectivity_overlap = 1 - ( tempN / intersection.size());
		//		if (Double.isNaN(selectivity_overlap)) {
		//			int n = 0;
		//		}
		//		selectivity_object = ( intersection.size() - tempN ) / sp2.getObjects().size();
		//		if (Double.isNaN(selectivity_object)) {
		//			int n = 0;
		//		}
	}

	public double computeProb_Source1ProvideDataItem(Profiling dataSetProfile, String attributeId, String dataItemKey) {
		try {
			double x = dataSetProfile.getTotalDataItemNumber() 
					* attributeLevelSourceCoverage_dependent.get(attributeId) // => is null if the source does not provide the attribute, thus the coverage is zero
					* dataSetProfile.getDataItemCoverage().get(dataItemKey)
					/ dataSetProfile.getSumofAlldataItemCoverage();
			return x;
		} catch (NullPointerException e) {
			return 0;
		}

	}
	public HashMap<String, Double> getAttributeLevelSourceCoverage_dependent() {
		return attributeLevelSourceCoverage_dependent;
	}
	public double getObjectsCoverage_dependent() {
		return objectsCoverage_dependent;
	}
	public double getSelectivity_object() {
		return selectivity_object;
	}
	public double getSelectivity_overlap() {
		return selectivity_overlap;
	}
}
