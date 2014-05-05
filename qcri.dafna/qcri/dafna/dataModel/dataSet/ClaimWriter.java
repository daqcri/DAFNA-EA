package qcri.dafna.dataModel.dataSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import qcri.dafna.dataModel.data.Globals;

public class ClaimWriter {
	public static boolean writeClaim(BufferedWriter writer, int claimId ,/*String entityId,*/ String objectIdentifier,	
			String propertyName, String propertyValueString, String timeStamp, String sourceId, 
			String dlim) {
		if (propertyValueString == null || propertyValueString == "" || propertyValueString == "Not Available" ||
				propertyValueString.isEmpty() || propertyValueString.length() == 0) {
			return false;
		}
		String line  = claimId + dlim + 
				/*entityId*/ "" + dlim + 
				objectIdentifier + dlim + 
				propertyName + dlim + 
				propertyValueString + dlim + 
				sourceId + dlim + 
				timeStamp + dlim + "\n";
		claimId ++;
		try {
			writer.write(line);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				writer.flush();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			return false;
		}
		return true;
	}

	public static boolean writeTrueValue(BufferedWriter writer, String objectIdentifier, String propertyName, String propertyValueString) {
		boolean result = true;
		String line = objectIdentifier + "\t" + propertyName + "\t" + propertyValueString + "\n";
		try {
			writer.write(line);
		} catch (IOException e) {
			try {
				writer.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return result;
	}
	public static BufferedWriter openFile(String fileName, boolean append) throws IOException {
		if (append) {
			return Files.newBufferedWriter(Paths.get(fileName), Globals.FILE_ENCODING, 
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} else {
			return Files.newBufferedWriter(Paths.get(fileName), Globals.FILE_ENCODING,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
		}
	}
}
