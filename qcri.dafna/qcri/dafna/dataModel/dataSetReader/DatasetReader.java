package qcri.dafna.dataModel.dataSetReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Scanner;

import au.com.bytecode.opencsv.CSVReader;
import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataFormatter.DataCleaner;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.quality.dataQuality.DataSetTimingMeasures;

public class DatasetReader {

	DataSet dataSet;
	public DatasetReader() {
		dataSet = new DataSet();
	}

	/**
	 * Read the dataSet and construct the dataSet object.
	 * if the cleanObjectId is true, then the read objectId should be cleaned before used, and the unclean value should be saved for further computation.
	 * The timing for every process is be logged in the given timings object.
	 * 
	 * @param directory
	 * @param cleanObjectId
	 * @param objectIdValueType
	 * @param timings
	 * @return
	 */
	public DataSet readDirectoryFiles(String directory, boolean cleanObjectId, ValueType objectIdValueType, 
			DataSetTimingMeasures timings, char delim) {
		try {
			timings.startDataSetReadingTime();
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory));

			for (Path readFilePath : directoryStream) {
				processFileLines(readFilePath.toString(), delim, cleanObjectId, objectIdValueType);
			}
			timings.endDataSetReadingTime();
			directoryStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataSet;
	}
	public DataSet readDirectoryFiles(String directory, DataSetTimingMeasures timings, char delim) {
		return readDirectoryFiles(directory, false, null, timings, delim);
	}
	/**
	 * 
	 * @param fileName: Full path for the data file
	 */
	public DataSet readFile(String fileName, char delim, boolean cleanObjectId, ValueType objectIdValueType) {
		Path readFilePath = Paths.get(fileName);
		processFileLines(readFilePath.toString(), delim, cleanObjectId, objectIdValueType);
		return dataSet;
	}

	private void processFileLines(String filePath, char delim, boolean cleanObjectID, ValueType objectIdValueType) {
		try (Scanner scanner = new Scanner(Paths.get(filePath), dataSet.getENCODING().name())) {
			if (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				scanner.close();
				if (line.indexOf(Globals.delimiterText_deprecated) > 0) {
					// Files with old format
					processFileLinesOldFormat(Paths.get(filePath), cleanObjectID, objectIdValueType);
				} else {
					// CSV files
					processFileLinesCSV(filePath, delim, cleanObjectID, objectIdValueType);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void processFileLinesCSV(String filePath, char delim, boolean cleanObjectID, ValueType ObjectIdValueType) {
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

			double weight = Globals.weight;

			while((record = reader.readNext()) != null) {
				int i = 0;
				claimId = new Integer(record[i]); i++;
				objectId = record[i]; i++;
				propertyName = record[i]; i++;
				stringValue = record[i]; i++;
				sourceId = record[i]; i++;
				timeStamp = record[i]; i++;

				// when the dataset is clean this should be removed
				if (stringValue.equals("Not Available") || stringValue.trim().isEmpty()) {
					continue;
				}
				if (objectId == null || objectId.trim().equalsIgnoreCase("null")) {
					continue;
				}

				if (cleanObjectID) {
					try {
						String cleanedObjId = (String)DataCleaner.clean((String)objectId, ObjectIdValueType);
						dataSet.addClaim(claimId, /*entityID,*/ cleanedObjId, objectId, propertyName, 
								stringValue, weight, timeStamp, sourceId);
					} catch (Exception e) {}

				} else {
					dataSet.addClaim(claimId, /*entityID,*/ objectId, "", propertyName, 
							stringValue, weight, timeStamp, sourceId);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processFileLinesOldFormat(Path filePath, boolean cleanObjectId, ValueType objectIdValueType) {
		try (Scanner scanner = new Scanner(filePath, dataSet.getENCODING().name())) {
			while (scanner.hasNextLine()) {
				processLineOldFormat(scanner.nextLine(), cleanObjectId, objectIdValueType);
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processLineOldFormat(String fileLine, boolean cleanObjectId, ValueType objectIdValueType) {
		Scanner scanner = new Scanner(fileLine);
		scanner.useDelimiter(Globals.delimiterRegularExpression_deprecated);
		try {
			int claimId = scanner.nextInt();
			/*String entityID =*/ scanner.next();
			String objectId = scanner.next();
			String propertyName = scanner.next();
			String stringValue = scanner.next();
			String sourceId = scanner.next();
			String timeStamp = null;
			try { timeStamp = scanner.next();} catch (NoSuchElementException e) {}

			double weight = 1.0; // TODO

			// TODO when the dataset is clean this should be removed
			if (stringValue.equals("Not Available") || stringValue.trim().isEmpty()) {
				scanner.close();
				return;
			}
			if (objectId == null || objectId.trim().equalsIgnoreCase("null")) {
				scanner.close();
				return;
			}
			if (cleanObjectId) {
				try {
					String cleanedObjId = (String)DataCleaner.clean((String)objectId, objectIdValueType);
					dataSet.addClaim(claimId, /*entityID,*/ cleanedObjId, objectId, propertyName, 
							stringValue, weight, timeStamp, sourceId);
				} catch (Exception e) {}

			} else {
				//				System.out.println(fileLine);
				dataSet.addClaim(claimId, /*entityID,*/ objectId, "", propertyName, 
						stringValue, weight, timeStamp, sourceId);
			}

		} catch (NoSuchElementException e) {
			System.out.println("Wrong Line Format");
		}
		scanner.close();
	}
}
