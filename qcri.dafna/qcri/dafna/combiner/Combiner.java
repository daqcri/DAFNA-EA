package qcri.dafna.combiner;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.voter.Voter;
import qcri.dafna.voter.VoterParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Combiner extends Voter {
	
	private int n;
	private String[] confidenceFilePaths;
	private int[][][] confusionMatrices;
	private int[] trainingCount;
	List<HashMap<Integer, Boolean>> allClaimsToIsTrue;
	
	public Combiner(DataSet dataSet, VoterParameters params, int n, String[] confidenceFilePaths)
	{
		super(dataSet, params);
		this.n = n;
		this.confidenceFilePaths = confidenceFilePaths;
		this.confusionMatrices = new int[2][2][n];
		this.allClaimsToIsTrue = new ArrayList<HashMap<Integer, Boolean>>();
		this.trainingCount = new int[2];
	}
	
	protected void initParameters() {
		singlePropertyValue = false;
		onlyMaxValueIsTrue = false;
	}
	
	protected int runVoter(boolean convergence100) {
		dataSet.resetDataSet(0, 0, 0);
		int i = 0;
		while(i < n)
		{
			Object[] temp = ConfusionMatrixCalculator.calculateConfusionMatrix(dataSet, confidenceFilePaths[i],',', singlePropertyValue);
			int confusionMatrix[][] = (int[][])temp[0];
			allClaimsToIsTrue.add((HashMap<Integer, Boolean>)temp[1]);
			//TODO The following two variable will be overwritten n times with the same value every time because they are dependent on the dataset and not on the model
			trainingCount[0] = (int)temp[2];
			trainingCount[1] = (int)temp[3];
			confusionMatrices[0][0][i] = confusionMatrix[0][0];
			confusionMatrices[0][1][i] = confusionMatrix[0][1];
			confusionMatrices[1][0][i] = confusionMatrix[1][0];
			confusionMatrices[1][1][i] = confusionMatrix[1][1];
			i++;
		}
		computeConfidence(allClaimsToIsTrue, confusionMatrices, n, trainingCount);
		return 1;
	}
	
	private void computeConfidence(List<HashMap<Integer, Boolean>> allClaimsToIsTrue, int[][][] confusionMatrices, int n, int[] trainingCount) {
		for (List<ValueBucket> bucketsList : dataSet.getDataItemsBuckets().values()) {
			for (ValueBucket b : bucketsList) {
				int claimId = b.getClaims().get(0).getId();
				int i = 0;
				int s = 0;
				double u0 = 1;
				double u1 = 1;
				while(i < n)
				{
					if(allClaimsToIsTrue.get(i).get(claimId))
						s = 0;
					else if(! allClaimsToIsTrue.get(i).get(claimId))
						s = 1;
					// for true class, correction for zeros included
					u0 = u0 * (confusionMatrices[0][s][i]+0.5)/(trainingCount[0]+1);
					// for false class, , correction for zeros included
					u1 = u1 * (confusionMatrices[1][s][i]+0.5)/(trainingCount[1]+1);
					i++;
				}
				// correction for zeros included
				u0 = u0*trainingCount[0]/(trainingCount[0]+trainingCount[1]); //  support for true class
				u1 = u1*trainingCount[1]/(trainingCount[0]+trainingCount[1]); //  support for false class
				if(u0 > u1)
				{
					b.setConfidence(1.0);
				}
				else
				{
					b.setConfidence(0.0);
				}
			}
		}
	}
	
}
