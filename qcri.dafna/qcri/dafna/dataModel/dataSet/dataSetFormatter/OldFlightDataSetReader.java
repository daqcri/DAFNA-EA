package qcri.dafna.dataModel.dataSet.dataSetFormatter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Scanner;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataSet.ClaimWriter;


public class OldFlightDataSetReader {

	private int claimId = Globals.lastClaimID;
	private final String flightDirectory = Globals.directory_UnformattedFlightFiles;
	private final String newFilesDirectory = Globals.directory_formattedDAFNADataset_FlightFolder 	+ "/2013-12-02-Flight-";

	public void readOldFlightFileAndWriteNewFiles (String newFileDelimiter) {

		Globals.log("Start reading the Flight Files");

		int numberOfReadFiles = 0, numberOfClaims = 0;

		String sourceID;
		String flight_objectId;
		String expectedDepartureTime;
		String actualDeparture;
		String departGate;
		String expectedArrivalTime;
		String actualArrival;
		String arrivalGate = "";
		String timeStamp;
//		String timeStampFormat = "YYYY-MM-DD";

		//writer
		BufferedWriter writer;
		int writtenFileNumber = 1;
		int fileEntriesCount = 0;
		boolean lineWriten;
		boolean append = false;
		try {
			writer = ClaimWriter.openFile(newFilesDirectory + writtenFileNumber + ".txt", append);
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(flightDirectory));
			for (Path readFilePath : directoryStream) {
				timeStamp = readFilePath.getFileName().toString();
				timeStamp = timeStamp.substring(0, timeStamp.indexOf("-data"));

				/**
				 * Scanner is not thread safe, not synchronized. 
				 */
				try (Scanner scanner = new Scanner(readFilePath, StandardCharsets.ISO_8859_1.name())) {
					System.out.println("reading file: " + readFilePath.getFileName());
					int numofline = 0;
					while (scanner.hasNextLine()) {
						numofline ++;
						String line = scanner.nextLine();
						Scanner s2 = new Scanner(line);
						s2.useDelimiter( "\t");
						try {
							sourceID = s2.next();
							flight_objectId = s2.next();
							expectedDepartureTime = s2.next();
							actualDeparture = s2.next();
							departGate = s2.next();
							expectedArrivalTime = s2.next();
							actualArrival = s2.next();
							arrivalGate = "";
							try { arrivalGate = s2.next(); } catch (NoSuchElementException e) {}

							flight_objectId = flight_objectId + timeStamp;

							lineWriten = ClaimWriter.writeClaim(writer, claimId, flight_objectId, /*timeStamp,*/ Globals.flightDataSet_ExpectedDepartureTime, 
									expectedDepartureTime, timeStamp, sourceID, Globals.delimiterText /*Globals.delimiterText*/);
							if (lineWriten) {
								claimId ++;
								numberOfClaims ++; 
								fileEntriesCount ++;
							}
							lineWriten = ClaimWriter.writeClaim(writer, claimId, flight_objectId, /*timeStamp,*/ Globals.flightDataSet_ActualDepartureTime, 
									actualDeparture, timeStamp, sourceID, Globals.delimiterText /*Globals.delimiterText*/);
							if (lineWriten) {
								claimId ++;
								numberOfClaims ++;
								fileEntriesCount ++;
							}

							lineWriten = ClaimWriter.writeClaim(writer, claimId, flight_objectId, /*timeStamp,*/ Globals.flightDataSet_DepartureGate, 
									departGate, timeStamp, sourceID, Globals.delimiterText /*Globals.delimiterText*/);
							if (lineWriten) {
								claimId ++;
								numberOfClaims ++;
								fileEntriesCount ++;
							}

							lineWriten = ClaimWriter.writeClaim(writer, claimId, flight_objectId, /*timeStamp,*/ Globals.flightDataSet_ExpectedArrivalTime, 
									expectedArrivalTime, timeStamp, sourceID, Globals.delimiterText /*Globals.delimiterText*/);
							if (lineWriten) {
								claimId ++;
								numberOfClaims ++;
								fileEntriesCount ++;
							}

							lineWriten = ClaimWriter.writeClaim(writer, claimId, flight_objectId, /*timeStamp,*/ Globals.flightDataSet_ActualArrivalTime, 
									actualArrival, timeStamp, sourceID, Globals.delimiterText /*Globals.delimiterText*/);
							if (lineWriten) {
								claimId ++;
								numberOfClaims ++;
								fileEntriesCount ++;
							}

							lineWriten = ClaimWriter.writeClaim(writer, claimId, flight_objectId, /*timeStamp,*/ Globals.flightDataSet_ArrivalGate, 
									arrivalGate, timeStamp, sourceID, Globals.delimiterText /*Globals.delimiterText*/);
							if (lineWriten) {
								claimId ++;
								numberOfClaims ++;
								fileEntriesCount ++;
							}

							if (fileEntriesCount >= Globals.MaxFileEntriesCount) {
								fileEntriesCount = 0;
								writer.close();
								writtenFileNumber ++;
								writer = ClaimWriter.openFile(newFilesDirectory + writtenFileNumber + ".txt", append);
							}
							/**
							 * A flight data is identified by both the flight number and the day of the flight, 
							 * as each flight data is captured for many different days.
							 */

							s2.close();
						} catch (NoSuchElementException e) {
							e.printStackTrace();
						}

					}
					scanner.close();
					System.out.println("number of lines = " + numofline);
				} catch (IOException e) {
					e.printStackTrace();
				}
				numberOfReadFiles++;
			}
			writer.close();
		} catch (IOException e)  {
			e.printStackTrace();
		}
		Globals.log(numberOfReadFiles + " readed files.\n" + numberOfClaims + " extracted claims.");
		Globals.log("Last Claim ID = " + claimId);
	}
}