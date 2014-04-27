package qcri.dafna.voter.dependence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;

public class Dependence {
	private List<ValueBucket> sameValueClaims;
	//	private Set<String> overlapedDataItem;
	private int numOfCommonTRUEValue;
	private int numOfCommonFALSEValue;
	private int numOfdifferentValues;
	private double dependence;

	public Dependence(Source source1, Source source2) {
		//		this.source1 = source1;
		//		this.source2 = source2;
		sameValueClaims = new ArrayList<ValueBucket>();
		//		overlapedDataItem = new HashSet<String>();
		numOfdifferentValues = 0;

		//		HashMap<String, SourceClaim> s1Claims = new HashMap<String, SourceClaim>();
		HashMap<String, SourceClaim> s2Claims = new HashMap<String, SourceClaim>();

		//		for (SourceClaim c1 : source1.getClaims()) {
		//			s1Claims.put(c1.dataItemKey(), c1);
		//		}
		for (SourceClaim c2 : source2.getClaims()) {
			s2Claims.put(c2.dataItemKey(), c2);
		}

		SourceClaim /*c1,*/ c2;
		String c1Key;
		for (SourceClaim c1 : source1.getClaims()) {
			c1Key = c1.dataItemKey();
			if (s2Claims.keySet().contains(c1Key)) {
				//				overlapedDataItem.add(c1Key);
				/**
				 * TODO in the case when one source provide more than one value for a fact what should be the behavior
				 * here on is neglected
				 */
				//				c1 = s1Claims.get(c1Key);
				c2 = s2Claims.get(c1Key);
				if (c1.getBucket().getId() == c2.getBucket().getId()) {
					sameValueClaims.add(c1.getBucket());
//					numOfdifferentValues ++; // TODO insure this initialization is true
				} else {
					numOfdifferentValues ++;
				}
			}
		}
		//		s1Claims = null;
		s2Claims = null;
		recomputeTrueFalseCounts();
	}

	public void recomputeTrueFalseCounts() {
		numOfCommonFALSEValue = 0;
		numOfCommonTRUEValue = 0;
		for (ValueBucket b : sameValueClaims) {
			if (b.getClaims().get(0).isTrueClaimByVoter()) {
				numOfCommonTRUEValue ++;
			} else {
				numOfCommonFALSEValue ++;
			}
		}
	}

	//	Source source1;
	//	Source source2;
	private double probabilityS1CopyingS2;
	private double probabilityS2CopyingS1;
	/*

	Universe univ = d.getUniverse();
	int numOfCommonValues = d.commonValuedFacts.size();
	int numOfDiffValues = d.overlappingFacts.size()-numOfCommonValues;

	if (!univ.validSelectedValue())
		return getAprioriDependenceProbability(univ, numOfCommonValues, numOfDiffValues);

	DataSource[] srcs = d.getSources();
	int numOfErrs = 0;
	int numOfCorrs = 0;
	if (!univ.validSelectedValue())
		numOfErrs = numOfCommonValues;
	else
		for (Iterator<Object> i = d.commonValuedFacts.iterator(); i.hasNext(); ){
			Object f = i.next();
			//if (f.toString().endsWith("ISBN")) continue;	//TODO REMOVE
			Object v = srcs[0].getVoteValue(f);
			if (v.equals(univ.getSelectedValue(f)))
				numOfCorrs ++;
			else
				numOfErrs ++;
		}

	d.numOfCommonErrors = numOfErrs;
	double a1 = srcs[0].getAvgValueProbability();
	double a2 = srcs[1].getAvgValueProbability();
	double c = univ.changePercentage;
	double t1 = a1*a2;
	double t2 = (1-a1)*(1-a2)/univ.numOfWrongValues;
	double t3 = Math.log(c) * numOfDiffValues + Math.log((1-univ.aprioriIndependenceProbability) / 2);
		double pr1 = Math.log(a1*(1-c) + t1*c)*numOfCorrs + Math.log((1-a1)*(1-c) + t2*c)*numOfErrs + t3;
	double pr2 = Math.log(a2*(1-c) + t1*c)*numOfCorrs + Math.log((1-a2)*(1-c) + t2*c)*numOfErrs + t3;
	double pr3 = Math.log(t1)*numOfCorrs + Math.log(t2)*numOfErrs + Math.log(univ.aprioriIndependenceProbability);

	double temp1 = Math.exp(pr1-pr3);
	double temp2 = Math.exp(pr2-pr3);
	if (!Double.isInfinite(temp1) && !Double.isInfinite(temp2)){
		d.probabilityS2CopyingS1 = temp1/(temp1+temp2+1);
		d.probabilityS1CopyingS2 = temp2/(temp1+temp2+1);
	}else if (Double.isInfinite(temp1) && Double.isInfinite(temp2)){
		double temp = Math.exp(pr1-pr2);
		if (Double.isInfinite(temp)){
			d.probabilityS2CopyingS1 = 1.;
			d.probabilityS1CopyingS2 = 0.;
		}else{
			d.probabilityS2CopyingS1 = temp/(temp+1);
			d.probabilityS1CopyingS2 = 1-d.probabilityS2CopyingS1;
		}
	}else if (Double.isInfinite(temp1)){
		d.probabilityS2CopyingS1 = 1.;
		d.probabilityS1CopyingS2 = 0.;
	}else{
		d.probabilityS2CopyingS1 = 0.;
		d.probabilityS1CopyingS2 = 1.;
	}

	return d.probabilityS1CopyingS2 + d.probabilityS2CopyingS1;
	 */
/*	public void computeDirectionalDependence(double c, int n, double aprioriIndependenceProbability, boolean firstRound,
			double sameValueProbability, double aprioriIndependenceProbabilityPrime, Source source1, Source source2) {
		int numOfCommonValues = numOfCommonFALSEValue + numOfCommonTRUEValue;
		if (numOfCommonValues == 0) {
			dependence = 0;
			probabilityS2CopyingS1 = 0;
			probabilityS1CopyingS2 = 0;
			return;
		}
		if (firstRound) {
			dependence = getAprioriDependenceProbability(sameValueProbability, aprioriIndependenceProbabilityPrime, 
					aprioriIndependenceProbability, numOfCommonValues);
			// TODO check the next 2 lines
			probabilityS2CopyingS1 = dependence;
			probabilityS1CopyingS2 = dependence;
			return;
		}
		double a1 = source1.getTrustworthiness();//srcs[0].getAvgValueProbability();
		double a2 = source2.getTrustworthiness();//srcs[1].getAvgValueProbability();
		double t1 = a1*a2;
		double t2 = (1-a1)*(1-a2)/n;
		double t3 = Math.log(c) * numOfdifferentValues + Math.log((1-aprioriIndependenceProbability) / 2);
		double pr1;
		//		if ( ((1-a1)*(1-c) + t2*c) == 0 && numOfCommonFALSEValue == 0) {
		//			pr1 = Math.log(a1*(1-c) + t1*c)*numOfCommonTRUEValue + t3;
		//		} else if ((a1*(1-c) + t1*c)==0 && numOfCommonTRUEValue == 0) {
		//			pr1 = Math.log((1-a1)*(1-c) + t2*c)*numOfCommonFALSEValue + t3;
		//		} else {
		pr1 = Math.log(a1*(1-c) + t1*c)*numOfCommonTRUEValue + Math.log((1-a1)*(1-c) + t2*c)*numOfCommonFALSEValue + t3;
		//		}
		double pr2;
		//		if ( ((1-a2)*(1-c) + t2*c) == 0 && numOfCommonFALSEValue == 0) {
		//			pr2 = Math.log(a2*(1-c) + t1*c)*numOfCommonTRUEValue + t3;
		//		} else if ( (a2*(1-c) + t1*c) == 0 && numOfCommonTRUEValue == 0) {
		//			pr2 = Math.log((1-a2)*(1-c) + t2*c)*numOfCommonFALSEValue + t3;
		//		} else {
		pr2 = Math.log(a2*(1-c) + t1*c)*numOfCommonTRUEValue + Math.log((1-a2)*(1-c) + t2*c)*numOfCommonFALSEValue + t3;
		//		}
		double pr3;
		//		if (t2 == 0 && numOfCommonFALSEValue ==0) {
		//			pr3 = Math.log(t1)*numOfCommonTRUEValue + Math.log(aprioriIndependenceProbability);
		//		} else if (t1 == 0 && numOfCommonTRUEValue == 0) {
		//			pr3 = Math.log(t2)*numOfCommonFALSEValue + Math.log(aprioriIndependenceProbability);
		//		} else {
		pr3 = Math.log(t1)*numOfCommonTRUEValue + Math.log(t2)*numOfCommonFALSEValue + Math.log(aprioriIndependenceProbability);
		//		}
		int test=0;
		if ( Double.isNaN(pr1) || Double.isNaN(pr2) || Double.isNaN(pr3)) {
			test =  0;
		}
		double temp1 = Math.exp(pr1-pr3);
		double temp2 = Math.exp(pr2-pr3);
		if ( Double.isNaN(temp1) || Double.isNaN(temp2)) {
			test =  0;
		}
		if (!Double.isInfinite(temp1) && !Double.isInfinite(temp2)){
			probabilityS2CopyingS1 = temp1/(temp1+temp2+1);
			probabilityS1CopyingS2 = temp2/(temp1+temp2+1);
		}else if (Double.isInfinite(temp1) && Double.isInfinite(temp2)){
			double temp = Math.exp(pr1-pr2);
			if (Double.isInfinite(temp)){
				probabilityS2CopyingS1 = 1.;
				probabilityS1CopyingS2 = 0.;
			}else{
				probabilityS2CopyingS1 = temp/(temp+1);
				probabilityS1CopyingS2 = 1-probabilityS2CopyingS1;
			}
		}else if (Double.isInfinite(temp1)){
			probabilityS2CopyingS1 = 1.;
			probabilityS1CopyingS2 = 0.;
		}else{
			probabilityS2CopyingS1 = 0.;
			probabilityS1CopyingS2 = 1.;
		}

		
		if (Double.isNaN(probabilityS1CopyingS2))
			if (Double.isNaN(probabilityS2CopyingS1))
				probabilityS1CopyingS2 = probabilityS2CopyingS1 = .5;
			else{
				probabilityS1CopyingS2 = 1;
				probabilityS2CopyingS1 = 0;
			}
		else if (Double.isNaN(probabilityS2CopyingS1)){
			probabilityS2CopyingS1 = 1;			
			probabilityS1CopyingS2 = 0;
		}
		

		dependence = probabilityS1CopyingS2 + probabilityS2CopyingS1;
	}*/
	/*
			if (null == d)
				throw new NullPointerException();

			Universe univ = d.getUniverse();
			int numOfCommonValues = d.commonValuedFacts.size();
			int numOfDiffValues = d.overlappingFacts.size()-numOfCommonValues;

			if (!univ.validSelectedValue())
				return getAprioriDependenceProbability(univ, numOfCommonValues, numOfDiffValues);

			DataSource[] srcs = d.getSources();
			double numOfErrs = 0;
			double numOfCorrs = 0;
			for (Iterator<Object> i = d.commonValuedFacts.iterator(); i.hasNext(); ){
				Object f = i.next();
				double pr = srcs[0].getValueProbability(f);
				numOfCorrs += pr;
				numOfErrs += (1-pr);
			}

			double a1 = srcs[0].getAvgValueProbability();
			double a2 = srcs[1].getAvgValueProbability();
			double c = univ.changePercentage;
			double t1 = a1*a2;
			double t2 = (1-a1)*(1-a2)/univ.numOfWrongValues;
			double t3 = univ.getChangePercentagePower(numOfDiffValues) * (1-univ.aprioriIndependenceProbability) / 2;
			double pr1 = Math.pow(a1*(1-c) + t1*c, numOfCorrs) * Math.pow((1-a1)*(1-c) + t2*c, numOfErrs) * t3;
			double pr2 = Math.pow(a2*(1-c) + t1*c, numOfCorrs) * Math.pow((1-a2)*(1-c) + t2*c, numOfErrs) * t3;
			double pr3 = Math.pow(t1, numOfCorrs) * Math.pow(t2, numOfErrs) * univ.aprioriIndependenceProbability;
			double s = pr1 + pr2 + pr3;

			d.noLoopCopying = true;
			d.probabilityS1CopyingS2 = pr2/s;
			d.probabilityS2CopyingS1 = pr1/s;
			if (Double.isNaN(d.probabilityS1CopyingS2))
				if (Double.isNaN(d.probabilityS2CopyingS1))
					d.probabilityS1CopyingS2 = d.probabilityS2CopyingS1 = .5;
				else{
					d.probabilityS1CopyingS2 = 1;
					d.probabilityS2CopyingS1 = 0;
				}
			else if (Double.isNaN(d.probabilityS2CopyingS1)){
				d.probabilityS2CopyingS1 = 1;			
				d.probabilityS1CopyingS2 = 0;
			}
			return d.probabilityS1CopyingS2 + d.probabilityS2CopyingS1;
	 */
/*	public void computeDirectionalDependence2(double c, int n, double aprioriIndependenceProbability, boolean firstRound,
			double sameValueProbability, double aprioriIndependenceProbabilityPrime, DataSet dataSet, Source source1, Source source2) {
		int numOfCommonValues = numOfCommonFALSEValue + numOfCommonTRUEValue;
		if (numOfCommonValues == 0) {
			dependence = 0;
			probabilityS2CopyingS1 = 0;
			probabilityS1CopyingS2 = 0;
			return;
		}
		if (firstRound) {
			dependence = getAprioriDependenceProbability(sameValueProbability, aprioriIndependenceProbabilityPrime, 
					aprioriIndependenceProbability, numOfCommonValues);
			// TODO check the next 2 lines
			probabilityS2CopyingS1 = dependence;
			probabilityS1CopyingS2 = dependence;
			return;
		}

		double numOfErrs = 0;
		double numOfCorrs = 0;
		double expConf, tempSum;
		for (ValueBucket b : sameValueClaims) {
			expConf = Math.exp(b.getConfidence());
			tempSum = 0;
			for (ValueBucket bucket : dataSet.getDataItemsBuckets().get(b.getClaims().get(0).dataItemKey())) {
				tempSum = tempSum + Math.exp(bucket.getConfidence());
			}
			// TODO this is the added N + 1 - |Vd|
			tempSum = tempSum + n + 1 - dataSet.getDataItemsBuckets().get(b.getClaims().get(0).dataItemKey()).size();
			if (Double.isInfinite(expConf)) {
				numOfCorrs += 1;
			} else {
				numOfCorrs += (expConf/tempSum);
				numOfErrs += 1 - (expConf/tempSum);
			}
		}

		double a1 = source1.getTrustworthiness(); //srcs[0].getAvgValueProbability();
		double a2 = source2.getTrustworthiness();//srcs[1].getAvgValueProbability();
		double t1 = a1*a2;
		double t2 = (1-a1)*(1-a2)/n;
		double t3 = Math.pow(c, numOfdifferentValues) * (1-aprioriIndependenceProbability) / 2;
		double pr1 = Math.pow(a1*(1-c) + t1*c, numOfCorrs) * Math.pow((1-a1)*(1-c) + t2*c, numOfErrs) * t3;
		double pr2 = Math.pow(a2*(1-c) + t1*c, numOfCorrs) * Math.pow((1-a2)*(1-c) + t2*c, numOfErrs) * t3;
		double pr3 = Math.pow(t1, numOfCorrs) * Math.pow(t2, numOfErrs) * aprioriIndependenceProbability;
		double s = pr1 + pr2 + pr3;

		//		d.noLoopCopying = true;
		probabilityS1CopyingS2 = pr2/s;
		probabilityS2CopyingS1 = pr1/s;
		if (Double.isNaN(probabilityS1CopyingS2))
			if (Double.isNaN(probabilityS2CopyingS1))
				probabilityS1CopyingS2 = probabilityS2CopyingS1 = .5;
			else{
				probabilityS1CopyingS2 = 1;
				probabilityS2CopyingS1 = 0;
			}
		else if (Double.isNaN(probabilityS2CopyingS1)){
			probabilityS2CopyingS1 = 1;			
			probabilityS1CopyingS2 = 0;
		}
		dependence = probabilityS1CopyingS2 + probabilityS2CopyingS1;
	}
	*/
	/**
	 * Returns the a-priori probability that two data sources are dependent.
	 * 
	 * <p>Invoked only when the correct values are not decided yet.
	 * 
	 * @param univ the universe the two sources belong to.
	 * @param numOfCommonValues number of values that the two sources share.
	 * @param numOfDiffValues number of facts that the two sources provide different values.
	 * @return the a-priori probability that two data sources are dependent.
	 */
//	private double getAprioriDependenceProbability(double sameValueProbability, 
//			double aprioriIndependenceProbabilityPrime, double aprioriIndependenceProbability, double c) {
//		int numOfCommonValues = numOfCommonFALSEValue + numOfCommonTRUEValue;
//		double probability = Math.pow(sameValueProbability, numOfCommonValues);//univ.getSameValuePower(numOfCommonValues);
//		probability *= aprioriIndependenceProbabilityPrime;
//		probability *= Math.pow(c, numOfdifferentValues);//univ.getChangePercentagePower(numOfDiffValues);
//		probability = probability / (1+probability);
//		return Double.isNaN(probability) ? 1-aprioriIndependenceProbability : probability;
//	}

	/**
	 * The last and exact implementation
	 * @param n
	 * @param alfa
	 * @param ita
	 * @param c
	 * @param s1
	 * @param s2
	 */
	public void computeDirectionalDependency3(double n, double alfa, double c, Source s1, Source s2) {
		double e1 = 1 - s1.getTrustworthiness();
		double e2 = 1 - s1.getTrustworthiness();
		double pt = (1 - e1) * (1 - e2);
		double pf = e1 * e2 / n;
		double pS1NODepnS2 = Math.pow(pt, numOfCommonTRUEValue) * Math.pow(pf, numOfCommonFALSEValue) 
				* Math.pow(( 1 - pt - pf), numOfdifferentValues); 
		double partTrue = ((1 - e1) * c) + ( pt * (1 - c));
		double partFalse = ( e1 * c ) + (pf * (1 - c));
		double partDiff = ( 1 - pt - pf ) * (1 - c);
		double pS1DepenS2 = Math.pow(partTrue, numOfCommonTRUEValue) * Math.pow(partFalse, numOfCommonFALSEValue) 
				* Math.pow(partDiff, numOfdifferentValues);

		partTrue = ((1 - e2) * c) + ( pt * (1 - c));
		partFalse = ( e2 * c ) + (pf * (1 - c));
		partDiff = ( 1 - pt - pf ) * (1 - c);
		double pS2DepenS1 = Math.pow(partTrue, numOfCommonTRUEValue) * Math.pow(partFalse, numOfCommonFALSEValue) 
				* Math.pow(partDiff, numOfdifferentValues);

		double denom = alfa * pS1DepenS2;
		denom += alfa * pS2DepenS1;
		denom += ( 1 - ( 2 * alfa) ) * (pS1NODepnS2);

		if (Double.isInfinite(pS1DepenS2)) {
			if (Double.isInfinite(pS2DepenS1)) {
				probabilityS1CopyingS2 = 0.5;
				probabilityS2CopyingS1 = 0.5;
			} else {
				probabilityS1CopyingS2 = 1;
				probabilityS2CopyingS1 = 0;
			}
		} else if (Double.isInfinite(pS2DepenS1)) {
			probabilityS1CopyingS2 = 0;
			probabilityS2CopyingS1 = 1;
		} else {
			probabilityS1CopyingS2 = (alfa * pS1DepenS2) / denom;
			probabilityS2CopyingS1 = (alfa * pS2DepenS1) / denom;
		}

		if (Double.isNaN(probabilityS1CopyingS2))
			if (Double.isNaN(probabilityS2CopyingS1))
				probabilityS1CopyingS2 = probabilityS2CopyingS1 = .5;
			else{
				probabilityS1CopyingS2 = 1;
				probabilityS2CopyingS1 = 0;
			}
		else if (Double.isNaN(probabilityS2CopyingS1)){
			probabilityS2CopyingS1 = 1;			
			probabilityS1CopyingS2 = 0;
		}

		if (probabilityS1CopyingS2 > 1 || probabilityS2CopyingS1 > 1) {
			System.out.println("-");
		}
		dependence = probabilityS1CopyingS2 + probabilityS2CopyingS1;
		if (dependence > 1) {
			System.out.println("*");
		}
	}
	/**
	 * TODO
	 * In the equation for dependence computation depen from s1 to s2 is equal to depen from s2 to s1 !!
	 * but this method recompute the 2 values separately until this question is solved
	 * @param s1
	 * @param s2
	 * @param n
	 * @return
	 */
//	public void computeUndDirectionalDependency(double n, double alfa, double ita, double c) {
//		double part1, part2, part3, part4;
//
//		double dependenceValue;
//
//
//		int kt = numOfCommonTRUEValue;
//		int	kf = numOfCommonFALSEValue;
//		int kd = numOfdifferentValues;
//
//		part1 = (1-alfa)/alfa;
//
//		part2 = 1 - ita  + (c * ita);
//		part2 = (1 - ita) / part2;
//		part2 = Math.pow(part2, kt);
//
//		part3 = (c * n) + ita - (c * ita);
//		part3 = ita / part3;
//		part3 = Math.pow(part3, kf);
//
//		part4 = 1 / (1 - c);
//		part4 = Math.pow(part4, kd);
//
//		dependenceValue = 1 / (1 + (part1 * part2 * part3 * part4));
//		if (Double.isNaN(dependenceValue)) {
//			dependenceValue = 1;
//		}
//		dependence = dependenceValue;
//		probabilityS1CopyingS2 = dependence;
//		probabilityS2CopyingS1 = dependence;
//	}

	public void setDependence(double dependence) {
		this.dependence = dependence;
	}
	public double getDependence() {
		return dependence;
	}
	public double getProbabilityS1CopyingS2() {
		return probabilityS1CopyingS2;
	}
	public double getProbabilityS2CopyingS1() {
		return probabilityS2CopyingS1;
	}
	//	public Source getSource1() {
	//		return source1;
	//	}
	//	public Source getSource2() {
	//		return source2;
	//	}
	public List<ValueBucket> getSameValueClaims() {
		return sameValueClaims;
	}

	public int getNumOfCommonFALSEValue() {
		return numOfCommonFALSEValue;
	}
	public int getNumOfCommonTRUEValue() {
		return numOfCommonTRUEValue;
	}
	public int getNumOfdifferentValues() {
		return numOfdifferentValues;
	}
}
