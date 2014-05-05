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

public class FlightTruthReader  extends TruthReader {
	
	HashMap<String, DataItemMeasures> dataItemMeasures;
	public FlightTruthReader(HashMap<String, DataItemMeasures> dataItemMeasures) {
		super(dataItemMeasures);
	}
	public int readDirectoryFiles() {
		trueValueCount = 0;
		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Globals.directory_formattedDAFNADataset_FlightTruthFolder));


			for (Path readFilePath : directoryStream) {
				String timeStamp = readFilePath.getFileName().toString();
				timeStamp = timeStamp.substring(0, timeStamp.indexOf("-truth"));
				processFileLines(readFilePath, timeStamp);
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
		// flightNumber	expectedDeparture	ActualDeparture DepartureGate	expectedArrivale	ActualArrivale ArrivaleGate
		Scanner scanner = new Scanner(fileLine);
		scanner.useDelimiter("\t");
		try {
			String flightID = scanner.next();
			String expectedDeparture = scanner.next();
			String actualDeparture = scanner.next();
			String departureGate = scanner.next();
			String expectedArrivale = scanner.next();
			String actualArrivale = scanner.next();
			String arrivaleGate = "";
			try { arrivaleGate = scanner.next();} catch (NoSuchElementException e) {}

			// Object identifier = flight number + time stamp
			flightID = flightID + timeStamp;
			if (!expectedDeparture.trim().isEmpty()) {
				addTrueValue("", flightID, Globals.flightDataSet_ExpectedDepartureTime, expectedDeparture);
			}
			if (!actualDeparture.trim().isEmpty()) {
				addTrueValue("", flightID, Globals.flightDataSet_ActualDepartureTime, actualDeparture);
			}
			if (!departureGate.trim().isEmpty() && !departureGate.trim().equals("--")) {
				addTrueValue("", flightID, Globals.flightDataSet_DepartureGate, departureGate);
			}
			if (!expectedArrivale.trim().isEmpty()) {
				addTrueValue("", flightID, Globals.flightDataSet_ExpectedArrivalTime, expectedArrivale);
			}
			if (!actualArrivale.trim().isEmpty()) {
				addTrueValue("", flightID, Globals.flightDataSet_ActualArrivalTime, actualArrivale);
			}
			if (!arrivaleGate.trim().isEmpty() && !arrivaleGate.trim().equals("--")) {
				addTrueValue("", flightID, Globals.flightDataSet_ArrivalGate, arrivaleGate);
			}
		} catch (NoSuchElementException e) {
			System.out.println("Wrong Line Format");
		}
		scanner.close();
	}

}
