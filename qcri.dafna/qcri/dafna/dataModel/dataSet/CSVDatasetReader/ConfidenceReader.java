package qcri.dafna.dataModel.dataSet.CSVDatasetReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import qcri.dafna.dataModel.data.ConfValueLabel;

import au.com.bytecode.opencsv.CSVReader;

public class ConfidenceReader {

	private HashMap<Integer, ConfValueLabel> conf;
	
	public ConfidenceReader(){
		conf = new HashMap<Integer, ConfValueLabel>();
	}
	
	public HashMap<Integer, ConfValueLabel> readConfidenceFile(String confidenceFilePath, char delim)
	{
		try {
			CSVReader reader = new CSVReader(new FileReader(confidenceFilePath), delim);
			
			//read line by line
			String[] record = null;
			//skip header row
			reader.readNext();
			
			while((record = reader.readNext()) != null){
				int claimID;
				boolean isTrue;
				double confValue;
				int bucketId;
				ConfValueLabel confValLabel = new ConfValueLabel();
				
				claimID = new Integer(record[0]);
				confValue = Double.valueOf(record[1]);
				isTrue = Boolean.valueOf(record[2]);
				bucketId = Integer.valueOf(record[3]);
				confValLabel.setConfidenceValue(confValue);
				confValLabel.setLabel(isTrue);
				confValLabel.setBucketId(bucketId);
				
				conf.put(new Integer(claimID), confValLabel);
			}
			reader.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return conf;
	}
}
