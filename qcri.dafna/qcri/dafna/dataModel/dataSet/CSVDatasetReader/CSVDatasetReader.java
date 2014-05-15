package qcri.dafna.dataModel.dataSet.CSVDatasetReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import au.com.bytecode.opencsv.CSVReader;
import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.quality.dataQuality.DataSetTimingMeasures;

public class CSVDatasetReader {

	DataSet dataSet;
	public CSVDatasetReader()    {
		dataSet = new DataSet();
	}

	/**
	 * Read the dataSet files and construct the dataSet object.
	 * if the cleanObjectId is true, then the read objectId should be cleaned before used, and the unclean value should be saved for further computation.
	 * The timing for every process is be logged in the given timings object.
	 * 
	 * @param directory
	 * @param cleanObjectId
	 * @param objectIdValueType
	 * @param timings
	 * @return
	 */
	public DataSet readDirectoryFiles(String directory, DataSetTimingMeasures timings, char delim) {
		try {
			timings.startDataSetReadingTime();
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory));

			for (Path readFilePath : directoryStream) {
				readCSVFile(readFilePath.toString(), delim);
			}
			timings.endDataSetReadingTime();
			directoryStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataSet;
	}

	private void readCSVFile(String filePath, char delim) {
		try {
			CSVReader reader = new CSVReader(new FileReader(filePath), delim);

			//read line by line
			String[] record = null;
			//skip header row
			reader.readNext();

			int claimId;
			String objectId;
			String propertyName;
			String stringValue;
			String sourceId;
			String timeStamp;

			double weight = 1.0; // TODO

			while((record = reader.readNext()) != null){
				claimId = new Integer(record[0]);
				/*String entityID = record[1];*/
				objectId = record[2];
				 propertyName = record[3];
				stringValue = record[4];
				sourceId = record[5];
				timeStamp = record[6];

				// TODO when the dataset is clean this should be removed
				if (stringValue.equals("Not Available") || stringValue.trim().isEmpty()) {
					continue;
				}
				if (objectId == null || objectId.trim().equalsIgnoreCase("null")) {
					continue;
				}
				dataSet.addClaim(claimId, /*entityID,*/ objectId, "", propertyName, 
						stringValue, weight, timeStamp, sourceId);
			}

			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
