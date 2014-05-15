package qcri.dafna.dataModel.dataSetReader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;

public class WeatherTruthReader extends TruthReader {
	public WeatherTruthReader(HashMap<String, DataItemMeasures> dataItemMeasures) {
		super(dataItemMeasures);
	}
	public int readDirectoryFiles(String directory) {
		trueValueCount = 0;
		try {
			DirectoryStream<Path> directoryStream;

			directoryStream = Files.newDirectoryStream(Paths.get(directory));
			for (Path readFilePath : directoryStream) {
				processFileLines(readFilePath, "null");
			}
			directoryStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ground truth count = " + trueValueCount);
		return trueValueCount;
	}

	private void processFileLines(Path filePath, String timeStamp) {
		
		try (Scanner scanner = new Scanner(filePath, Globals.Flight_DataSet_FILE_ENCODING.name())) {
			while (scanner.hasNextLine()) {
				processLine(scanner.nextLine(), timeStamp);
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void processLine(String fileLine, String timeStamp) {
		// line format:
		// 14113255|	|	sanjosefrijan2908:46:382010|	Temperature|	48|	accuweather|	fri jan 29 08:46:38 2010|
		Scanner scanner = new Scanner(fileLine);
		scanner.useDelimiter(Globals.delimiterRegularExpression);
		try {
			/*claimid*/ scanner.next();
			/*entity id */ scanner.next();
			String objectID = scanner.next();
			String propertyName = scanner.next();
			String value = scanner.next();
			addTrueValue("", objectID, propertyName, value);
		} catch (NoSuchElementException e) {
			System.out.println("Wrong Line Format : " + fileLine + " . ");
		}
		scanner.close();
	}
}
