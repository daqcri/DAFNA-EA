package qcri.dafna.dataModel.dataSetReader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Scanner;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.dataFormatter.DataCleaner;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.quality.dataQuality.DataSetTimingMeasures;

public class DatasetReader {

	DataSet dataSet;
	public DatasetReader(double startingConfidence, double startingTrustworthiness) {
		dataSet = new DataSet(startingConfidence, startingTrustworthiness);
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
	public DataSet readDirectoryFiles(String directory, boolean cleanObjectId, ValueType objectIdValueType, DataSetTimingMeasures timings) {
		try {
			timings.startDataSetReadingTime();
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory));

			for (Path readFilePath : directoryStream) {
				processFileLines(readFilePath, cleanObjectId, objectIdValueType);
			}
			timings.endDataSetReadingTime();
			directoryStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataSet;
	}
	/**
	 * 
	 * @param fileName: Full path for the data file
	 */
	public DataSet readFile(String fileName, boolean cleanObjectId, ValueType objectIdValueType) {
		Path readFilePath = Paths.get(fileName);
		processFileLines(readFilePath, cleanObjectId, objectIdValueType);
		return dataSet;
	}

	private void processFileLines(Path filePath, boolean cleanObjectId, ValueType objectIdValueType) {
		try (Scanner scanner = new Scanner(filePath, dataSet.getENCODING().name())) {
			while (scanner.hasNextLine()) {
				processLine(scanner.nextLine(), cleanObjectId, objectIdValueType);
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processLine(String fileLine, boolean cleanObjectId, ValueType objectIdValueType) {
		Scanner scanner = new Scanner(fileLine);
		scanner.useDelimiter(dataSet.getDelimiterRegularExpression());
		try {
			int claimId = scanner.nextInt();
			/*String entityID =*/ scanner.next();
			String objectId = scanner.next();
			String propertyName = scanner.next();
			String stringValue = scanner.next();
			String sourceId = scanner.next();
			String timeStamp = null;
			try { timeStamp = scanner.next();} catch (NoSuchElementException e) {}
			
			double weight = 0.0; // TODO

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
