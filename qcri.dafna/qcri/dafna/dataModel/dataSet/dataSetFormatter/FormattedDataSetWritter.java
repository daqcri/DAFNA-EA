package qcri.dafna.dataModel.dataSet.dataSetFormatter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.SourceClaim;

public class FormattedDataSetWritter {
	private DataSet dataSet;
	public FormattedDataSetWritter(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	/**
	 * This method expect that the Property Value is well formatted,
	 * if it is a list of values, they are already formatted with the appropriate delimiter.
	 * @param fileName: The Name of the data file to be created without extension. A set of files with this name 
	 * concatenated with a number are going to be created. 
	 */
	public void writeDataSetToFile(String fileName, boolean append) {
		String claimLine = "";
		String dlim = dataSet.getDelimiterText();
		BufferedWriter writer;
		int fileNumber = 1;
		int fileEntriesCount = 0;
		int numberOfClaims = 0;
		try {
			writer = openFile(fileName + fileNumber + ".txt", append);
			for (List<SourceClaim> claimsList : dataSet.getDataItemClaims().values()) {
				for (SourceClaim claim : claimsList) {
					claimLine = 
							claim.getId() + dlim +
							/*claim.getEntityId()*/ "" + dlim + 
							claim.getObjectIdentifier() + dlim + 
							claim.getPropertyName() + dlim + 
							claim.getPropertyValueString() + dlim + 
							claim.getSource().getSourceIdentifier()+ dlim + 
							claim.getTimeStamp() + dlim + "\n";
					writer.write(claimLine);
					numberOfClaims ++;
					fileEntriesCount ++;
					if (fileEntriesCount == Globals.MaxFileEntriesCount) {
						fileEntriesCount = 0;
						writer.close();
						fileNumber ++;
						writer = openFile(fileName + fileNumber + ".txt", append);
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Globals.log("Total number of Claims = " + numberOfClaims);
		Globals.log("Total number of files = " + fileNumber);

	}

	private BufferedWriter openFile(String fileName, boolean append) throws IOException{
		if (append) {
			return Files.newBufferedWriter(Paths.get(fileName), dataSet.getENCODING(), 
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} else {
			return Files.newBufferedWriter(Paths.get(fileName), dataSet.getENCODING(),
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
		}
	}

}