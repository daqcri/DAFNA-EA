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

public class PopulationBiographyDataSetGenerator {
	private int claimId = Globals.lastClaimID;
	private final String unformattedPopulationDirectory = Globals.directory_UnformattedPopulationFiles;// Population
	private final String biographyDirectory = Globals.directory_UnformattedBiographiesFiles;
	private final String newFilesDirectory = Globals.directory_formattedDAFNADataset_PopulationBiographyClaimsFolder 	+ "/population-Biography";

	private int writtenFileNumber = 1;
	private Set<String> sourcesKeys = new HashSet<String>();
	public void readOldPopulationAndBiographyFilesAndWriteNewFiles (String newFileDelimiter) {

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
		String sourceKey;
		//writer
		BufferedWriter writer;
//		int writtenFileNumber = 1;
		int fileEntriesCount = 0;
		boolean append = false;
		try {
			writer = ClaimWriter.openFile(newFilesDirectory + writtenFileNumber + ".txt", append);
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(unformattedPopulationDirectory));
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
							sourcesKeys.add(sourceID);
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
			writtenFileNumber++;
		} catch (IOException e)  {
			e.printStackTrace();
		}
		Globals.log(numberOfReadFiles + " readed files.\n" + numberOfClaims + " extracted claims.");
		Globals.log("Last Claim ID = " + claimId);
		readOldBiographyFileAndWriteNewFiles(newFileDelimiter);
	}
	private void readOldBiographyFileAndWriteNewFiles (String newFileDelimiter) {

		Globals.log("Start reading the Biographies File");

		int numberOfReadFiles = 0, numberOfClaims = 0;

		String sourceID;
		String sourceID1;
		String sourceID2 = "";
		String personName;
		String value;
		String propertyName;
		String date;

		Set<String> diffProp = new HashSet<String>();
		//writer
		BufferedWriter writer;
		
		int fileEntriesCount = 0;
		boolean lineWriten;
		boolean append = false;
		try {
			writer = ClaimWriter.openFile(newFilesDirectory + writtenFileNumber + ".txt", append);
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(biographyDirectory));
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
							
							//Abraham Lincoln	Born	Infobox President	R	Contributor #36171: Davidcannon	19708275	[2/12/1809 12:00:00 AM to 2/12/1809 11:59:59 PM]
							personName = s2.next();
							propertyName = s2.next();
							diffProp.add(propertyName);
							String ss;
							ss = s2.next();

							try {s2.next();} catch (Exception e){
								System.out.println(line);
							}
							sourceID1 = s2.next();
						try {	sourceID2 = s2.next();}
						catch(Exception e) {
							System.out.println(line);
							continue;
						}
							value = s2.next();

							sourceID = sourceID1.substring(sourceID1.indexOf("#") + 1);// + sourceID2;

							if ( ! sourcesKeys.contains(sourceID)) {
								continue;
							}
							
							lineWriten = false;
							if (propertyName.equals("Born")) {
								date = value.substring(1,value.indexOf(" "));
								lineWriten = ClaimWriter.writeClaim(writer, claimId, personName, Globals.biographiesDataSet_Born, date, 
										"null", sourceID, newFileDelimiter);
							} else if (propertyName.equals("Died")){
								date = value.substring(1,value.indexOf(" "));
								lineWriten = ClaimWriter.writeClaim(writer, claimId, personName, Globals.biographiesDataSet_Died, date, 
										"null", sourceID, newFileDelimiter);
							} else if (propertyName.equals("Spouse")){
								lineWriten = ClaimWriter.writeClaim(writer, claimId, personName, Globals.biographiesDataSet_Spouse, value, 
										"null", sourceID, newFileDelimiter);
							} else if (propertyName.equals("Mother")){
								lineWriten = ClaimWriter.writeClaim(writer, claimId, personName, Globals.biographiesDataSet_Mother, value, 
										"null", sourceID, newFileDelimiter);
							} else if (propertyName.equals("Father")){
								lineWriten = ClaimWriter.writeClaim(writer, claimId, personName, Globals.biographiesDataSet_Father, value, 
										"null", sourceID, newFileDelimiter);
							} else if (propertyName.equals("Children")){
								while (s2.hasNext()) {
									value = value + Globals.cleanedListDelimiter + (s2.next().replaceAll("," , " "));
								}
								lineWriten = ClaimWriter.writeClaim(writer, claimId, personName, Globals.biographiesDataSet_Children, value, 
										"null", sourceID, newFileDelimiter);
							}

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
//							e.printStackTrace();
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
		for (String p : diffProp) {
			System.out.println(p);
		}
		Globals.log(numberOfReadFiles + " readed files.\n" + numberOfClaims + " extracted claims.");
		Globals.log("Last Claim ID = " + claimId);
	}
}
