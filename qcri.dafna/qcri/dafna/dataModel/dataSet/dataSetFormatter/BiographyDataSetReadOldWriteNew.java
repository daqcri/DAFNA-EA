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

public class BiographyDataSetReadOldWriteNew {
	private int claimId = Globals.lastClaimID;
	private final String biographyDirectory = Globals.directory_UnformattedBiographiesFiles;
	private final String newFilesDirectory = Globals.directory_formattedDAFNADataset_BiographiesClaimsFolder 	+ "/bio-";

	public void readOldBiographyFileAndWriteNewFiles (String newFileDelimiter) {

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
		String key;
		Set<String> keys = new HashSet<String>();
		//writer
		BufferedWriter writer;
		int writtenFileNumber = 1;
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

//							key = personName+propertyName+sourceID;
//							if (keys.contains(key)) {
//								continue;
//							}
//							keys.add(key);
							
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
