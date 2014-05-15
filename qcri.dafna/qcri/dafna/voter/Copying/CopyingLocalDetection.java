package qcri.dafna.voter.Copying;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.voter.VoterParameters;
import qcri.dafna.voter.dependence.SourceDependenceModel;

public class CopyingLocalDetection extends SourceDependenceModel {

	private Profiling dataSetProfile;
	private HashMap<String, SourceProfiling> sourcesProfiling;
	/**
	 * key   = source 1 Id
	 * value =  Key   = source 2 Id
	 * 			value = dependence object of source 1 on source 2
	 */
	private HashMap<String, HashMap<String, dependenciesProfiling>> sourcesDependencies;
	/**
	 * key   = source 1 Id
	 * value =  Key   = source 2 Id
	 * 			value = dependence of source 1 on source 2
	 */
	private HashMap<String, HashMap<String, Double>> sourcesDependenciesValues;

	public CopyingLocalDetection(DataSet dataSet, double alfa, double c, double ita, int n, double cosineSimStoppingCondition, 
			double base_sim, double similarityConstant, boolean considerSimilarity, boolean considerSourcesAccuracy, VoterParameters params) {
		super(dataSet, params, alfa, c, n, similarityConstant, considerSimilarity, considerSourcesAccuracy, false, true);
		dataSetProfile = new Profiling(dataSet);
		sourcesProfiling = new HashMap<String, SourceProfiling>();
		sourcesDependencies = new HashMap<String, HashMap<String,dependenciesProfiling>>();
		sourcesDependenciesValues = new HashMap<String, HashMap<String,Double>>();

		for (Source s : dataSet.getSourcesHash().values()) {
			sourcesProfiling.put(s.getSourceIdentifier(), new SourceProfiling(dataSetProfile, s, dataSet));
		}

		HashMap<String,dependenciesProfiling> dependenceMap;
		SourceProfiling sp1, sp2;
		for (Source s1 : dataSet.getSourcesHash().values()) {
			dependenceMap = new HashMap<String,dependenciesProfiling>();
			sp1 = sourcesProfiling.get(s1.getSourceIdentifier());
			for (Source s2 : dataSet.getSourcesHash().values()) {
				sp2 = sourcesProfiling.get(s2.getSourceIdentifier());
				dependenceMap.put(s2.getSourceIdentifier(), new dependenciesProfiling(s1, sp1, s2, sp2, dataSetProfile, dataSet));
			}
			sourcesDependencies.put(s1.getSourceIdentifier(), dependenceMap);
		}
	}

	/**
	 * Return the source from the sourcesList with maximal dependency with another source from preList .
	 * remove this source from the given list
	 * TODO test the removal of the source from the list given as parameter
	 * @param sList
	 * @param preList
	 * @return
	 */
	@Override
	protected Source getSrcWithMaxDependence(List<Source> sourcesList, List<Source> preList) {
		Source source1 = sourcesList.get(0);
		if (sourcesList.size() == 1) {
			sourcesList.remove(0);
			return source1;
		}
		Source source2 = preList.get(0);
		if (source2.getSourceIdentifier().equals(source1.getSourceIdentifier())) {
			/*This is only for the first call of this method when we assign the same list for the 2 parameters
			 * we are sure preList.size > 1 as it passed the check at the beginning of the method*/
			source2 = preList.get(1);
		}

		int maxSourceIndex = 0;
		double maxDep = sourcesDependenciesValues.get(source1.getSourceIdentifier()).get(source2.getSourceIdentifier());

		double dep;
		Source s1;
		for ( int i = 0 ; i < sourcesList.size(); i ++) {
			s1 = sourcesList.get(i);
			for (Source s2 : preList) {
				if (s1.getSourceIdentifier().equals(s2.getSourceIdentifier())) { // this should only happen in the first call
					continue;
				}
				dep = sourcesDependenciesValues.get(s1.getSourceIdentifier()).get(s2.getSourceIdentifier());
				if (dep > maxDep) {
					source1 = s1;
					source2 = s2;
					maxDep = dep;
					maxSourceIndex = i;
				}
			}
		}
		sourcesList.remove(maxSourceIndex);
		return source1;
	}

	@Override
	protected void computeDependencies(boolean firstRound) {
		double p1, p2;
		List<Double> dependencies;
		for (Source s : dataSet.getSourcesHash().values()) {
			sourcesDependenciesValues.put(s.getSourceIdentifier(), new HashMap<String, Double>());
		}
		Source s1;
		List<Source> sourceList = new ArrayList<Source>(dataSet.getSourcesHash().values());
		for (int i = 0; i < sourceList.size(); i++) {
			s1 = sourceList.get(i);
			for (Source s2 : sourceList) {
				if ( ! s1.getSourceIdentifier().equals(s2.getSourceIdentifier())) {
					dependencies = computeProbabilityOfDependence(s1, s2);
					p1 = dependencies.get(0);
					p2 = dependencies.get(1);
					sourcesDependenciesValues.get(s1.getSourceIdentifier()).put(s2.getSourceIdentifier(), p1);
					sourcesDependenciesValues.get(s2.getSourceIdentifier()).put(s1.getSourceIdentifier(), p2);
				}
			}
			sourceList.remove(i);
			i--;
		}
	}

	@Override
	protected double calculateVoteCount(Source src1, List<Source> pre) {
		if (pre.isEmpty()) {
			return 1.0;
		}
		double vote = 1;
		double copyingProbability;
		for (Source s2 : pre) {
			copyingProbability = sourcesDependenciesValues.get(src1.getSourceIdentifier()).get(s2.getSourceIdentifier());
			copyingProbability += sourcesDependenciesValues.get(s2.getSourceIdentifier()).get(src1.getSourceIdentifier());
			vote = vote * (1 - c * copyingProbability);
		}
		return vote;
	}

	private List<Double> computeProbabilityOfDependence(Source s1, Source s2) {
		double probS1DependsonS2_final;
		double probS2DependsonS1_final;

		double denom;
		double ps1DepenS2 = 1;
		double ps2DepenS1 = 1;

		double pNotDepen = 1;

		double ps1NotDepenS2 = 1;
		double ps2NotDepenS1 = 1;

		List<Double> independentProbabilities;

		for (List<ValueBucket> bList : dataSet.getDataItemsBuckets().values()) {

			independentProbabilities = computeIndependantProbabilityForDataItem(bList, s1, s2);
			ps1NotDepenS2 = independentProbabilities.get(0);
			ps2NotDepenS1 = independentProbabilities.get(1);

			pNotDepen *= ps1NotDepenS2 * ps2NotDepenS1;
/*TODO infinity * - infinity = NAN*/
			ps1DepenS2 *= computeS1DependsonS2ProbabilityForDataItem(bList, s1, s2) * ps2NotDepenS1;
			ps2DepenS1 *= computeS1DependsonS2ProbabilityForDataItem(bList, s2, s1) * ps1NotDepenS2;
		}

		denom = alfa * ps1DepenS2;
		denom += alfa * ps2DepenS1;
		denom += (1 - ( 2 * alfa ) ) * pNotDepen;
		if (Double.isInfinite(ps1DepenS2)) {
			probS1DependsonS2_final = 1.0;
		} else {
			probS1DependsonS2_final = alfa * ps1DepenS2 / denom;
		}
		denom = alfa * ps2DepenS1;
		denom += alfa * ps1DepenS2;
		denom += (1 - (2* alfa) ) * pNotDepen;
		if (Double.isInfinite(ps2DepenS1)) {
			probS2DependsonS1_final = 1;
		} else {
			probS2DependsonS1_final = alfa * ps2DepenS1 / denom;
		}
		/* from Luna's */
		if (Double.isNaN(probS1DependsonS2_final))
			if (Double.isNaN(probS2DependsonS1_final))
				probS1DependsonS2_final = probS2DependsonS1_final = .5;
			else{
				probS1DependsonS2_final = 1;
				probS2DependsonS1_final = 0;
			}
		else if (Double.isNaN(probS2DependsonS1_final)){
			probS2DependsonS1_final = 1;			
			probS1DependsonS2_final = 0;
		}
		/* from Luna's */
		List<Double> depen = new ArrayList<Double>();
		depen.add(probS1DependsonS2_final);
		depen.add(probS2DependsonS1_final);
		return depen;
	}


	private double computeS1DependsonS2ProbabilityForDataItem(List<ValueBucket> dataItemBucketList, Source source1, Source source2) {
		double prob = 1;

		String objectId = dataItemBucketList.get(0).getClaims().get(0).getObjectIdentifier();
		String attributeId = dataItemBucketList.get(0).getClaims().get(0).getPropertyName();
		String dataItemKey = objectId + attributeId;

		SourceProfiling sp1 = sourcesProfiling.get(source1.getSourceIdentifier());
		SourceProfiling sp2 = sourcesProfiling.get(source2.getSourceIdentifier());
		dependenciesProfiling depenProfile = sourcesDependencies.get(source1.getSourceIdentifier()).get(source2.getSourceIdentifier());
		ValueBucket b1 = null, b2 = null;

		if (sp1.getDataItems().contains(dataItemKey) || sp2.getDataItems().contains(dataItemKey)) {
			for (ValueBucket b : dataItemBucketList) {
				if (b.getSourcesKeys().contains(source1.getSourceIdentifier())) {
					b1 = b;
				}
				if (b.getSourcesKeys().contains(source2.getSourceIdentifier())) {
					b2 = b;
				}
			}
		}

		double numOfWrongValue = dataItemBucketList.size() - 1;
		/**
		 * either S1 or S2 does not provide a value for the data item
		 */
		if (b1 == null || b2 == null) {
			/**
			 * s1 does not provide the data item;
			 */
			if (b1 == null) {
				/**
				 * if s1 does not provide the object.
				 */
				if (!sp1.getObjects().contains(objectId)) {
					prob = dataSetProfile.getTotalObjectsNumber() *  depenProfile.getObjectsCoverage_dependent() / dataSetProfile.getSumofAllSourcesObjectLevelCoverage();
					prob = 1 - prob;
					return prob;
				}
				/**
				 * if s1 does provide the object.
				 */
				else {
					prob = 1 - depenProfile.computeProb_Source1ProvideDataItem(dataSetProfile, attributeId, dataItemKey);
					return prob;
				}
			} else {
				prob = b1.getProbabilityBeingTrue() * source1.getTrustworthiness();
				prob += ( 1 - b1.getProbabilityBeingTrue()) * ( 1 - source1.getTrustworthiness()) / numOfWrongValue;
				prob *= depenProfile.computeProb_Source1ProvideDataItem(dataSetProfile, attributeId, dataItemKey); 
				return prob;
			}
		}

		if (b1.getId() != b2.getId()) {
			prob = b1.getProbabilityBeingTrue() * source1.getTrustworthiness();
			prob += ( 1 - b1.getProbabilityBeingTrue()) * ( 1 - source1.getTrustworthiness()) / numOfWrongValue;
			prob *= depenProfile.computeProb_Source1ProvideDataItem(dataSetProfile, attributeId, dataItemKey); 
			prob *= 1 - depenProfile.getSelectivity_overlap();
			return prob;
		} else {
			prob = b1.getProbabilityBeingTrue() * source1.getTrustworthiness();
			prob += ( 1 - b1.getProbabilityBeingTrue()) * ( 1 - source1.getTrustworthiness()) / numOfWrongValue;
			prob *= depenProfile.computeProb_Source1ProvideDataItem(dataSetProfile, attributeId, dataItemKey); 
			prob *= 1 - depenProfile.getSelectivity_overlap();
			prob += depenProfile.getSelectivity_overlap();
			return prob;
		}
	}

	/**
	 * 
	 * @param dataItemBucketList
	 * @param source1
	 * @param source2
	 * @return List of independence probabilities:
	 * 			list.get(0) = probability source 1 not dependent on source 2
	 * 			list.get(1) = probability source 2 not dependent on source 1
	 */
	private List<Double> computeIndependantProbabilityForDataItem(List<ValueBucket> dataItemBucketList, Source source1, Source source2) {
		String objectId = dataItemBucketList.get(0).getClaims().get(0).getObjectIdentifier();
		String attributeId = dataItemBucketList.get(0).getClaims().get(0).getPropertyName();
		String dataItemKey = objectId + attributeId;
		SourceProfiling sp1 = sourcesProfiling.get(source1.getSourceIdentifier());
		SourceProfiling sp2 = sourcesProfiling.get(source2.getSourceIdentifier());
		ValueBucket b1 = null, b2 = null;
		if (sp1.getDataItems().contains(dataItemKey) || sp2.getDataItems().contains(dataItemKey)) {
			for (ValueBucket b : dataItemBucketList) {
				if (b.getSourcesKeys().contains(source1.getSourceIdentifier())) {
					b1 = b;
				}
				if (b.getSourcesKeys().contains(source2.getSourceIdentifier())) {
					b2 = b;
				}
			}
		}
		double prob1 = 1;
		double prob2 = 1;
		double numOfWrongValue = dataItemBucketList.size() - 1;
		if (b1 == null) {
			if ( ! sp1.getObjects().contains(objectId)) {
				prob1 = sp1.getIndependent_Probability_for_not_provided_objects().get(objectId);
			} else {
				prob1 = sp1.getIndependent_Probability_for_provided_objects_not_provided_dataItem().get(dataItemKey);
			}
		} else {
			prob1 = b1.getProbabilityBeingTrue() * source1.getTrustworthiness();
			prob1 += ( ( 1 - b1.getProbabilityBeingTrue()) * ( 1 - source1.getTrustworthiness()) / numOfWrongValue);
			prob1 = prob1 * ( 1 - sp1.getIndependent_Probability_for_provided_objects_not_provided_dataItem().get(dataItemKey));
		}

		if (b2 == null) {
			if ( ! sp2.getObjects().contains(objectId)) {
				prob2 = sp2.getIndependent_Probability_for_not_provided_objects().get(objectId);
			} else {
				prob2 = sp2.getIndependent_Probability_for_provided_objects_not_provided_dataItem().get(dataItemKey);
			}
		} else {
			prob2 = b2.getProbabilityBeingTrue() * source2.getTrustworthiness();
			prob2 += ( ( 1 - b2.getProbabilityBeingTrue()) * ( 1 - source2.getTrustworthiness()) / numOfWrongValue);
			prob2 = prob2 * ( 1 - sp2.getIndependent_Probability_for_provided_objects_not_provided_dataItem().get(dataItemKey));
		}

		List<Double> probabilities = new ArrayList<Double>();
		probabilities.add(prob1);
		probabilities.add(prob2);
		return probabilities;
	}
}
