package qcri.dafna.dataModel.dataSet.dataSetFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataSet.ClaimWriter;

public class PopulationDatasetReadOldWriteNew {
	private int claimId = Globals.lastClaimID;
	private final String populationDirectory = Globals.directory_UnformattedPopulationFiles;
	private final String newFilesDirectory = Globals.directory_formattedDAFNADataset_PopulationClaimsFolder 	+ "/population-";

	public void readOldPopulationFileAndWriteNewFiles (String newFileDelimiter) {

		Globals.log("Start reading the Population File");

		int numberOfReadFiles = 0, numberOfClaims = 0;

		String sourceID;
		String sourceID1;
		String sourceID2 = "";
		String location;
		String fromToDate;
		String value;
		String propertyName;
		String date;

		//		Set<String> diffProp = new HashSet<String>();
		String key;
		Set<String> keys = new HashSet<String>();
		//writer
		BufferedWriter writer;
		int writtenFileNumber = 1;
		int fileEntriesCount = 0;
		boolean append = false;
		try {
			writer = ClaimWriter.openFile(newFilesDirectory + writtenFileNumber + ".txt", append);
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(populationDirectory));
			for (Path readFilePath : directoryStream) {

				/**
				 * Scanner is not thread safe, not synchronized. // ISO_8859_1
				 */
				try (Scanner scanner = new Scanner(readFilePath, StandardCharsets.UTF_8.name())) {
					System.out.println("reading file: " + readFilePath.getFileName());
					int numofline = 0;
					while (scanner.hasNextLine()) {
						numofline ++;
						String line = scanner.nextLine();
						Scanner s2 = new Scanner(line);
						s2.useDelimiter( "\t");
						try {
							//Abu Dhabi	Population	Infobox City	F	Contributor #0 (68.162.248.83)	64429757	[1/1/2006 12:00:00 AM to 12/31/2006 11:59:59 PM]
							location = s2.next().trim().toLowerCase(); // location
							propertyName = s2.next().trim(); // population
							s2.next(); // infobox city
							s2.next(); // F
							sourceID1 = s2.next().trim(); // source id
							sourceID2 = s2.next().trim();// revision
							fromToDate = s2.next().trim();// from-to data
							value = s2.next().trim();
							sourceID = sourceID1.substring(sourceID1.indexOf("#") + 1);// + sourceID2;

							/////////////////////// TODO decide
//							key = location+propertyName+sourceID;
//							if (keys.contains(key)) {
//								continue;
//							}
//							keys.add(key);
							/////////////////////

							date = fromToDate.substring(1,fromToDate.indexOf(" ")).substring(fromToDate.indexOf("/")).substring(fromToDate.indexOf("/"));
							propertyName = Globals.populationDataSet_Population + date.trim();

							if (ClaimWriter.writeClaim(writer, claimId, location, propertyName, value, "null", 
									sourceID, newFileDelimiter)) {
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
