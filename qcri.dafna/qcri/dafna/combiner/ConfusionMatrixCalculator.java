package qcri.dafna.combiner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import qcri.dafna.dataModel.data.DataSet;
import au.com.bytecode.opencsv.CSVReader;


public class ConfusionMatrixCalculator {
		
	public static Object[] calculateConfusionMatrix(DataSet dataSet, String confidenceFilePath, char delim, boolean singlePropertyValue){
		
		int[][] confusionMatrix  = new int[2][2];
		int tp = 0;
		int fp = 0;
		int tn = 0;
		int fn = 0;
		int ifDifferent;
		int trainingPositive = 0;
		int trainingNegative = 0;
		
		HashMap<Integer, Boolean> claimToIsTrue = new HashMap<Integer, Boolean>();
		DifferenceCalculator diffCalc =  new DifferenceCalculator(dataSet, singlePropertyValue);
		try {
			CSVReader reader = new CSVReader(new FileReader(confidenceFilePath), delim);
			
			//read line by line
			String[] record = null;
			//skip header row
			reader.readNext();
			
			while((record = reader.readNext()) != null){
				int claimID;
				boolean isTrue;
				
				claimID = new Integer(record[0]);
				isTrue = Boolean.valueOf(record[2]);
				claimToIsTrue.put(new Integer(claimID), isTrue);
				
				ifDifferent = diffCalc.getDifference(claimID, isTrue);		
				if (ifDifferent == 0)
					{tp++; trainingPositive++;}
				if(ifDifferent == 1)
					{fp++; trainingNegative++;}
				if(ifDifferent == 2)
					{tn++; trainingNegative++;}
				if(ifDifferent == 3)
					{fn++; trainingPositive++;}
				if(ifDifferent == -1)
					continue;
			}
			reader.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		confusionMatrix[0][0] = tp;
		confusionMatrix[1][0] = fp;
		confusionMatrix[0][1] = fn;
		confusionMatrix[1][1] = tn;
		
		return new Object[]{confusionMatrix, claimToIsTrue, trainingPositive, trainingNegative};
	}

}
