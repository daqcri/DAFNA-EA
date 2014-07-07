package qcri.dafna.dataModel.dataSet.CSVDatasetReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;
import qcri.dafna.dataModel.dataSetReader.TruthReader;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;

public class CSVTruthReader extends TruthReader {
	/*
	 * File format
	 * object Id \t propertyName \t value
	 */
	public CSVTruthReader(HashMap<String, DataItemMeasures> dataItemMeasures) {
		super(dataItemMeasures);
	}
	public int readDirectoryFiles(String directory, char delim) {
		setTrueValueCount(0);
		try {
			DirectoryStream<Path> directoryStream;

			directoryStream = Files.newDirectoryStream(Paths.get(directory));
			for (Path readFilePath : directoryStream) {
				readCSVFile(readFilePath.toString(), delim);
			}
			directoryStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return getTrueValueCount();
	}

	private void readCSVFile(String filePath, char delim) {
		// line format:
		// object Id \t propertyName \t value
		try {
			CSVReader reader = new CSVReader(new FileReader(filePath), delim);

			//read line by line
			String[] record = null;
			//skip header row
			reader.readNext();

			while((record = reader.readNext()) != null){
				String objectID = record[0];
				String propertyName = record[1];
				String value = record[2];
				addTrueValue("", objectID, propertyName, value);
			    setTrueValueCount(getTrueValueCount() + 1 );
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
