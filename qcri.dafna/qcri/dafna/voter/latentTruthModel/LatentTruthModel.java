package qcri.dafna.voter.latentTruthModel;


import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.UniformRandomGenerator;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.voterResults.NormalVoterQualityMeasures;
import qcri.dafna.voter.Voter;
import qcri.dafna.voter.VoterParameters;
/**
 * The Claim Observation (boolean) represented in the SourceClaim.trueClaimByVoter variable.
 * The fact truth label (boolean) represented in the ValueBucket.LTMTruthLabel variable.
 * The Prior truth probability "Theta" (double) represented in the ValueBucket. confidence variable.
 * @author dalia
 *
 */
public class LatentTruthModel extends Voter {

	double b1, b0;
	double a00, a01, a10, a11;
	HashMap<String, LTMSourceData> sourceCounters;
	int iterationCount;
	int burnIn;
	int sampleGap;// thinning
	NormalVoterQualityMeasures voterQuality;

	public LatentTruthModel
	(DataSet dataSet, VoterParameters params,double b1, double b0,double a00,double a01,double a10,double a11, 
			int iterationCount,	int burnIn,	int sampleGap) {

		super(dataSet, params);

		this.b1 = b1;
		this.b0 = b0;
		this.a00 = a00;
		this.a01 = a01;
		this.a10 = a10;
		this.a11 = a11;
		sourceCounters = new HashMap<String, LTMSourceData>();
		for (String key : dataSet.getSourcesHash().keySet()) {
			sourceCounters.put(key, new LTMSourceData());
		}

		this.iterationCount = iterationCount;
		this.burnIn = burnIn;
		this.sampleGap = sampleGap;
	}
	@Override
	protected void initParameters() {
		singlePropertyValue = true; 
		onlyMaxValueIsTrue = false;
	}
	@Override
	protected int runVoter(boolean convergence100) {
		dataSet.resetDataSet(0, 0, 0);
		initialize();
		iterate();
		LTMSourceData sCounters;
		for(Source source : dataSet.getSourcesHash().values()){
			sCounters = sourceCounters.get(source.getSourceIdentifier());
			// Sensitivity, Specificity, Precision
			//System.out.println(sCounters.getq11()/(sCounters.getq11()+sCounters.getq10()));
			//System.out.println(sCounters.getq00()/(sCounters.getq00()+sCounters.getq01()));
			//System.out.println(sCounters.getq11()/(sCounters.getq11()+sCounters.getq01()));
			//Precision as Source Trust Worthiness
			source.setTrustworthiness(sCounters.getq11()/(sCounters.getq11()+sCounters.getq01()));
		}
		
		return iterationCount;
	}

	private void iterate() {
		double ptf, p1minustf;
		double temp;
		LTMSourceData sCounters;
		boolean tf;

		RandomGenerator rg;
		UniformRandomGenerator random;
		double rand;

		boolean test;
		//		int testlessthanzero = 0;
		//		int testmorethanone=0;
		int sampleSize;
		sampleSize = 0;
		if (sampleGap > 0) {
			sampleSize = ((iterationCount-burnIn) / sampleGap);
		} else {
			sampleSize = iterationCount - burnIn;
		}

		for (int i = 0; i < iterationCount; i ++) {

			rg = new JDKRandomGenerator();
			rg.setSeed(Math.round((Math.random() * 100000)));
			random = new UniformRandomGenerator(rg);

			for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
				for (ValueBucket bucket : bucketsList) {
					tf = bucket.getClaims().get(0).isTrueClaimByVoter();
					if (tf) {
						ptf = b1;
						p1minustf = b0;
					} else {
						ptf = b0;
						p1minustf = b1;
					}

					/* iterate over the agreeing sources ( oc = 1 ) */
					for (SourceClaim claim : bucket.getClaims()) {
						sCounters = sourceCounters.get(claim.getSource().getSourceIdentifier());
						if (tf) {
							ptf = (ptf * (sCounters.getN11() -1 + a11 ) )
									/ 
									(sCounters.getN11() + sCounters.getN10() - 1 + a11 + a10) ;
							p1minustf = (p1minustf * (sCounters.getN01()  + a01 ))
									/
									(sCounters.getN01() + sCounters.getN00() + a01 + a00);

						} else {
							ptf = (ptf * (sCounters.getN01() -1 + a01 ) )
									/ 
									(sCounters.getN01() + sCounters.getN00() - 1 + a01 + a00) ;

							p1minustf = (p1minustf * (sCounters.getN11() + a11 ))
									/
									(sCounters.getN11() + sCounters.getN10() + a11 + a10);

						}
					}
					/* iterate over the disagreeing sources ( oc = 0 ) */
					for (String key : bucket.getDisagreeingSourcesKeys()) {
						sCounters = sourceCounters.get(key);
						if (tf) {
							ptf = (ptf * (sCounters.getN10()  -1 + a10 ) ) 
									/
									(sCounters.getN11() + sCounters.getN10() - 1 + a11 + a10);

							p1minustf = (p1minustf * (sCounters.getN00() + a00 )) 
									/ 
									(sCounters.getN01() + sCounters.getN00() + a01 + a00);
						} else {
							ptf = (ptf * (sCounters.getN00()  -1 + a00 ) )
									/ 
									(sCounters.getN01() + sCounters.getN00() - 1 + a01 + a00) ;

							p1minustf = (p1minustf * (sCounters.getN10()  + a10 ))
									/
									(sCounters.getN11() + sCounters.getN10() + a11 + a10);
						}
					}
					rand = random.nextNormalizedDouble() + Math.sqrt(3); /* the nextNormalizedDouble returns a value from -sqrt(3) to +sqrt(3)  */
					rand = ((double)rand/(2*Math.sqrt(3)));
					//					if (rand <0)testlessthanzero++;
					//					System.out.println(rand);
					temp = p1minustf/(ptf + p1minustf);
					//					System.out.println(temp);
					if (rand < temp) {
						/* iterate over agreeing sources (i.e. oc = 1)*/ 
						for (SourceClaim claim : bucket.getClaims()) {
							sCounters = sourceCounters.get(claim.getSource().getSourceIdentifier());
							if (tf) {
								claim.setTrueClaimByVoter(false);
								sCounters.decrementN11();
								sCounters.incrementN01();
							} else {
								claim.setTrueClaimByVoter(true);
								sCounters.decrementN01();
								sCounters.incrementN11();
							}
						}
						/* iterate over disagreeing sources (i.e. oc = 0)*/ 
						for (String key : bucket.getDisagreeingSourcesKeys()) {
							sCounters = sourceCounters.get(key);
							if (tf) {// tf was true and now false
								sCounters.decrementN10();
								sCounters.incrementN00();
							} else {// tf was false and now true
								sCounters.decrementN00();
								sCounters.incrementN10();
							}
						}
					}
					if (sampleGap == 0 || (sampleGap > 0 && i % sampleGap == 0)) {
						test = true;
					} else  {
						test = false;
					}
					if (i > burnIn && test) {
						if (bucket.getClaims().get(0).isTrueClaimByVoter()) {
							bucket.setConfidence(bucket.getConfidence() + (((double)1/sampleSize)));
							//bucket.setConfidence(bucket.getConfidence() + 1);
							// else add 0
						}
						
						for(Source source : dataSet.getSourcesHash().values()){
							sCounters = sourceCounters.get(source.getSourceIdentifier());
							sCounters.setq00(sCounters.getq00()+sCounters.getN00());
							sCounters.setq01(sCounters.getq01()+sCounters.getN01());
							sCounters.setq10(sCounters.getq10()+sCounters.getN10());
							sCounters.setq11(sCounters.getq11()+sCounters.getN11());
						}
					}
				}
			}
			computeMeasuresPerIteration(false,-1,-1);
		    }
		
			for(Source source : dataSet.getSourcesHash().values()){
				sCounters = sourceCounters.get(source.getSourceIdentifier());
				sCounters.setq00(sCounters.getq00()/sampleSize + a00);
				sCounters.setq01(sCounters.getq01()/sampleSize + a01);
				sCounters.setq10(sCounters.getq10()/sampleSize + a10);
				sCounters.setq11(sCounters.getq11()/sampleSize + a11);
			}
	}

	/**
	 * initialize the truth label for all facts (buckets.TrueClaimByVoter)
	 */
	private void initialize() {
		dataSet.initializeTheDisagreeingSources();
//		RandomGenerator rg = new JDKRandomGenerator();
//		rg.setSeed(17399225432l);//rg.setSeed(1);
//		UniformRandomGenerator randomGenerator = new UniformRandomGenerator(rg);
//		double random;
		boolean tf = false;
		for (List<ValueBucket> bucketList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : bucketList) {
//				random = randomGenerator.nextNormalizedDouble();
				if (!tf) {//if (random < 0) {
					for (SourceClaim claim : b.getClaims()) {
						claim.setTrueClaimByVoter(false);
						sourceCounters.get(claim.getSource().getSourceIdentifier()).incrementN01();
						for (String disagreeingSource : b.getDisagreeingSourcesKeys()) {
							sourceCounters.get(disagreeingSource).incrementN00();
						}
					}
				} else {
					for (SourceClaim claim : b.getClaims()) {
						claim.setTrueClaimByVoter(true);
						sourceCounters.get(claim.getSource().getSourceIdentifier()).incrementN11();
						for (String disagreeingSource : b.getDisagreeingSourcesKeys()) {
							sourceCounters.get(disagreeingSource).incrementN10();
						}
					}
				}
				tf = !tf;
			}
		}
	}

//	private void initializeTheDisagreeingSources() {
//		Set<String> allSources;
//		Set<String> disagreeingSources;
//		for (List<ValueBucket> bList : dataSet.getDataItemsBuckets().values()) {
//			allSources = new HashSet<String>();
//			for (ValueBucket b : bList) {
//				allSources.addAll(b.getSourcesKeys());
//			}
//			for (ValueBucket b : bList) {
//				disagreeingSources = new HashSet<String>();
//				disagreeingSources.addAll(allSources);
//				disagreeingSources.removeAll(b.getSourcesKeys());
//				b.setDisagreeingSourcesKeys(new ArrayList<String>(disagreeingSources));
//			}
//
//		}
//	}
}
