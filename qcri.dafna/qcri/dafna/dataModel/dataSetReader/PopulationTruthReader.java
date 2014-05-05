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

public class PopulationTruthReader extends TruthReader {
	
	public PopulationTruthReader(HashMap<String, DataItemMeasures> dataItemMeasures) {
		super(dataItemMeasures);
	}
	
	public int readDirectoryFiles() {
		trueValueCount = 0;
		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Globals.directory_formattedDAFNADataset_PopulationTruthFolder));
			for (Path readFilePath : directoryStream) {
//System.out.println(readFilePath);
				processFileLines(readFilePath, "null");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		//crawfordsville, indiana, 2000, 15243
		if (fileLine.isEmpty()) {
			return;
		}
		Scanner scanner = new Scanner(fileLine);
		scanner.useDelimiter(",");
		try {
			String city = scanner.next().trim() + ", " + scanner.next().trim();
			String year = scanner.next().trim();
			String value = scanner.next().trim();

				addTrueValue("", city, Globals.populationDataSet_Population+year, value);
		} catch (NoSuchElementException e) {
			System.out.println("Wrong Line Format");
		}
		scanner.close();
	}
}
