package qcri.dafna.dataModel.quality.voterResults;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.dataFormatter.DataComparator;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.dataFormatter.PersonsNameComparator;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;

public class SourceClaimsLogger {

	
	public void saveSourcesTrueClaims(DataSet dataSet, String experiment, String voter) {
		BufferedWriter writer = null;
		String fileName = Globals.directory_formattedDAFNADataset_SourcesTrueClaims + "/" + experiment + "/" + voter + ".txt";
		String line;
		DataItemMeasures dim;
		int lastIndex;
		double jaccardList;
		try {
			writer = Files.newBufferedWriter(Paths.get(fileName), Globals.FILE_ENCODING, 
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			for (Source source : dataSet.getSourcesHash().values()) {
				line = source.getSourceIdentifier() + "\t";
				for (SourceClaim claim : source.getClaims()) {
					dim = dataSet.getDataQualityMeasurments().getDataItemMeasures().get(claim.dataItemKey());
					if (dim.getTrueValue() == null) {
						continue;
					}
					if (claim.getValueType().equals(ValueType.ListNames)) {
						jaccardList = PersonsNameComparator.computeJaccardListofNames((String)claim.getPropertyValue(), (String)dim.getTrueValue());
						if (jaccardList > 0) {
							line = line + claim.getId() + "(" + jaccardList + ")" + Globals.cleanedListDelimiter;
						}
					} else if (claim.isTrueClaimByVoter()) {
						dim = dataSet.getDataQualityMeasurments().getDataItemMeasures().get(claim.dataItemKey());
						if (DataComparator.valueInBucket(dim.getTrueValue(), dim.isTrueValueCleaned(), claim.getBucket())) {
							line = line + claim.getId() + "(1)" +  Globals.cleanedListDelimiter;
						}
					}
				}
				if (line.length() == (source.getSourceIdentifier() + "\t").length()) {
					line = line + "\n";
				} else {
					lastIndex = line.length() - Globals.cleanedListDelimiter.length();
					line = line.substring(0, lastIndex) + "\n";
				}
				writer.write(line);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
