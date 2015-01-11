package qcri.dafna.dataModel.dataSet.CSVDatasetReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

public class TrustWorthinessReader {
	
private HashMap<String, Double> trust;
	
	public TrustWorthinessReader(){
		trust = new HashMap<String, Double>();
	}
	
	public HashMap<String, Double> readTrustFile(String trustWorthinessFilePath, char delim)
	{
		try {
			CSVReader reader = new CSVReader(new FileReader(trustWorthinessFilePath), delim);
			
			//read line by line
			String[] record = null;
			//skip header row
			reader.readNext();
			
			while((record = reader.readNext()) != null){
				String sourceName;
				double trustValue;
				
				sourceName = record[0];
				trustValue = Double.valueOf(record[1]);
				
				trust.put(sourceName, trustValue);
			}
			reader.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return trust;
	}
}
