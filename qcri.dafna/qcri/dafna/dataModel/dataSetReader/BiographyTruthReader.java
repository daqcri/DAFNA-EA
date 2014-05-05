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

public class BiographyTruthReader  extends TruthReader {
	public BiographyTruthReader(HashMap<String, DataItemMeasures> dataItemMeasures) {
		super(dataItemMeasures);
	}
	public int readDirectoryFiles() {
		trueValueCount = 0;
		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Globals.directory_formattedDAFNADataset_BiographiesTruthFolder));
			for (Path readFilePath : directoryStream) {
				processFileLines(readFilePath, "null");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("True value count = " + trueValueCount); 
		return trueValueCount;
	}

	private void processFileLines(Path filePath, String timeStamp) {

		try (Scanner scanner = new Scanner(filePath, Globals.Biography_DataSet_FILE_ENCODING.name())) {
			while (scanner.hasNextLine()) {
				processLine(scanner.nextLine(), timeStamp);
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processLine(String fileLine, String timeStamp) {
		if (fileLine.isEmpty()) {
			return;
		}
		String personName = "";
		String date = "";
		String property = "";
		try {
			if (fileLine.indexOf("born") >= 0) {
				personName = fileLine.substring(0, fileLine.indexOf("born")).trim();
				date = fileLine.substring(fileLine.indexOf("[") + 1);
				date = date.substring(0, date.indexOf(" ")).trim();
				property = Globals.biographiesDataSet_Born;
			} else if (fileLine.indexOf("died") >= 0) {
				personName = fileLine.substring(0, fileLine.indexOf("died")).trim();
				date = fileLine.substring(fileLine.indexOf("[") + 1);
				date = date.substring(0, date.indexOf(" ")).trim();
				property = Globals.biographiesDataSet_Died;
			}

			addTrueValue("", personName, property, date);
		} catch (NoSuchElementException e) {
			System.out.println("Wrong Line Format");
		}
		
	}
}
