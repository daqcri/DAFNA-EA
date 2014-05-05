package qcri.dafna.dataModel.dataSetReader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataFormatter.DataCleaner;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;

public class BooksTruthReader extends TruthReader {
	
	private boolean MLE;
	public BooksTruthReader(HashMap<String, DataItemMeasures> dataItemMeasures, boolean MLE) {
		super(dataItemMeasures);
		this.MLE = MLE;
	}
	
	public int readDirectoryFiles() {
		trueValueCount = 0;
		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Globals.directory_formattedDAFNADataset_BooksTruthFolder));
			for (Path readFilePath : directoryStream) {
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
		// uncleaned isbn	authors list
		if (fileLine.isEmpty()) {
			return;
		}
		Scanner scanner = new Scanner(fileLine);
		scanner.useDelimiter("\t");
		try {
			String isbn = scanner.next();
			String authors = scanner.next();

			isbn = (String)DataCleaner.clean(isbn, ValueType.ISBN);
			// Entity ID = isbn
			// Object identifier = isbn
			if (!authors.trim().isEmpty()) {
				if (MLE) {
					String authorsList = (String) DataCleaner.clean(authors, ValueType.ListNames);
					List<String> names = Arrays.asList(authorsList.split(Globals.cleanedListDelimiter));
					for (String name : names) {
						addTrueValue(isbn, isbn, Globals.bookDataSet_AuthorsNamesList+name, "True");
					}
				} else {
					addTrueValue(isbn, isbn, Globals.bookDataSet_AuthorsNamesList, authors);
				}
			}
		} catch (NoSuchElementException e) {
			System.out.println("Wrong Line Format");
		}
		scanner.close();
	}
}
