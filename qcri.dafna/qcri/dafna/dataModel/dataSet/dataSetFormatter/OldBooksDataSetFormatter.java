package qcri.dafna.dataModel.dataSet.dataSetFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataFormatter.DataCleaner;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.dataSet.ClaimWriter;

public class OldBooksDataSetFormatter {
	private static int claimId = Globals.lastClaimID;
	private static final String booksDirectory = Globals.directory_UnformattedBooksFiles;
	private static String newFilesDirectory = Globals.directory_formattedDAFNADataset_Books_Claims_Folder + "/claim";

	public static void main(String[] args) {
		readOldBooksWriteFormattedBooks(",", false, false, true);
	}

	public static void readOldBooksWriteFormattedBooks (String newFileDelimiter, boolean singleClaimValue, boolean MLE, boolean cleanListValues) {

		if (singleClaimValue) {
			newFilesDirectory = Globals.directory_formattedDAFNADataset_BooksFolder_SingleClaimValue + "/claim";
		}
		if (MLE) {
			newFilesDirectory = Globals.directory_formattedDAFNADataset_BooksFolder_MLE + "/claim";
		}
		Globals.log("Start reading the Books Files");

		int numberOfReadFiles = 0, numberOfClaims = 0;

		String sourceID;
		String isbn;
		//		String bookName;
		String authorsList;
		String timeStamp = "null";
		List<String> names;

		//writer
		BufferedWriter writer;
		int writtenFileNumber = 1;
		int fileEntriesCount = 0;
		boolean lineWriten;
		boolean append = false;
		try {
			writer = ClaimWriter.openFile(newFilesDirectory + writtenFileNumber + ".txt", append);
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(booksDirectory));
			for (Path readFilePath : directoryStream) {
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
							isbn = s2.next(); 
							
							isbn = (String) DataCleaner.clean(isbn, ValueType.ISBN);
							
							
							s2.next(); // bookName = s2.next();
							authorsList = "";
							try { authorsList = s2.next(); } catch (NoSuchElementException e) {}

							if (! authorsList.trim().equals("")) {
								if ( ! singleClaimValue) {
									// normal
									if (cleanListValues) {
										authorsList = (String) DataCleaner.clean(authorsList, ValueType.ListNames);
									}
									lineWriten = ClaimWriter.writeClaim(writer,claimId, isbn, Globals.bookDataSet_AuthorsNamesList, 
											authorsList, timeStamp, sourceID, Globals.delimiterText /*Globals.delimiterText*/);
									if (lineWriten) {
										claimId ++;
										numberOfClaims ++; 
										fileEntriesCount ++;
									}

								} else if (MLE){
									authorsList = (String) DataCleaner.clean(authorsList, ValueType.ListNames);
									names = Arrays.asList(authorsList.split(Globals.cleanedListDelimiter));
									for (String name : names) {
										lineWriten = ClaimWriter.writeClaim(writer,claimId, isbn, Globals.bookDataSet_AuthorsNamesList+name, 
												"True", timeStamp, sourceID, Globals.delimiterText /*Globals.delimiterText*/);
										if (lineWriten) {
											claimId ++;
											numberOfClaims ++; 
											fileEntriesCount ++;
										}
									}
								} else {
									authorsList = (String) DataCleaner.clean(authorsList, ValueType.ListNames);
									names = Arrays.asList(authorsList.split(Globals.cleanedListDelimiter));
									for (String name : names) {
										lineWriten = ClaimWriter.writeClaim(writer,claimId, isbn, Globals.bookDataSet_AuthorsNamesList, 
												name, timeStamp, sourceID, Globals.delimiterText /*Globals.delimiterText*/);
										if (lineWriten) {
											claimId ++;
											numberOfClaims ++; 
											fileEntriesCount ++;
										}
									}
								
								}
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
