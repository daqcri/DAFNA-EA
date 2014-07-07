package qcri.dafna.experiment.allegator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.dataSet.ClaimWriter;

public class AllegateDIPercentageSourcesRecursively  extends qcri.dafna.experiment.Experiment {
	/**
	 * The key = data item key
	 * the value = the data item value to be added as a claim by the allegator source.
	 */
	private HashMap<String, String> newDataItems;

	public void allegate(DataSet dataset, String seedDir, String voterName, int numNewSources, double newDIPercentage ) {
		boolean convergence100 = false;
		boolean runMLE = false;
		boolean runLTM = false;
		
		String resultDir = seedDir + "/experimentResult";

		runExperiment(convergence100 ,dataset, resultDir, runLTM, null, runMLE, null, voterName);

		newDataItems = new HashMap<String, String>();

		int numDI = (int)((double)dataset.getDataItemsBuckets().size() * (double)newDIPercentage);
		for (int i = 0 ; i < numNewSources; i++) {
			newDataItems = new HashMap<String, String>();
			/*create folders*/
			String allegateDir = seedDir+"/allegate"+i;
			File allegateFolder = new File(allegateDir);
			allegateFolder.mkdir();
			resultDir = allegateDir + "/experimentResult";
			File experimentFolder = new File(resultDir);
			experimentFolder.mkdir();
			/*create folders*/

			dataset = addSource(dataset, numDI, allegateDir +"/claims.txt");
			runExperiment(convergence100 ,dataset, resultDir, runLTM, null, runMLE, null, voterName);
		}
	}

	private DataSet addSource(DataSet dataset, int numNewDataItemsClaims, String newFileName) {

		String newSourceName = "NewSource" + System.currentTimeMillis();
		BufferedWriter writer = null;
		try {
			writer = ClaimWriter.openFile(newFileName, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SourceClaim chosenClaim = null;
		ValueBucket chosenBucket = null;
		SourceClaim tempChosenClaim = null;
		ValueBucket tempChosenBucket = null;

		int tempMaxNumSrc;
		int maxNumSrc = 0;

		int counter = 0;
		int maxid = 0;
		boolean maxIdCalculated = false;
		while (counter < numNewDataItemsClaims) {

			for (String dikey : dataset.getDataItemsBuckets().keySet()) {
				tempMaxNumSrc = 0;
				if ( ! newDataItems.containsKey(dikey)) {
					int size = dataset.getDataItemsBuckets().get(dikey).size();
					for (ValueBucket b : dataset.getDataItemsBuckets().get(dikey)) {
						if ( ! b.getClaims().get(0).isTrueClaimByVoter() || size == 1) {
							if (b.getSourcesKeys().size() > tempMaxNumSrc) {
								tempMaxNumSrc = b.getSourcesKeys().size();
								tempChosenClaim = b.getClaims().get(0);
								tempChosenBucket = b;
							}
						}
						if ( ! maxIdCalculated) {
							for (SourceClaim claim : b.getClaims()) {
								if (claim.getId() > maxid) {
									maxid = claim.getId();
								}
							}
						}
					}
				}
				if (tempMaxNumSrc > 0) {
					if (tempMaxNumSrc > maxNumSrc) {
						maxNumSrc = tempMaxNumSrc;
						chosenClaim = tempChosenClaim;
						chosenBucket = tempChosenBucket;
					} else if (tempMaxNumSrc == maxNumSrc) {
						if (dataset.getDataItemClaims().get(tempChosenClaim.dataItemKey()).size() >
						dataset.getDataItemClaims().get(chosenClaim.dataItemKey()).size()) {
							chosenClaim = tempChosenClaim;
							chosenBucket = tempChosenBucket;
						}
					}
				}
			}
			/* add the claim */
			maxIdCalculated = true;
			maxid ++;
			newDataItems.put(chosenClaim.dataItemKey(), chosenClaim.getPropertyValueString());
			SourceClaim newClaim = dataset.addClaim(maxid, chosenClaim.getObjectIdentifier(), 
					chosenClaim.getUncleanedObjectIdentifier(), chosenClaim.getPropertyName(), 
					chosenClaim.getPropertyValueString(), chosenClaim.getWeight(), 
					chosenClaim.getTimeStamp(), newSourceName);
			dataset.addClaimToBucket(newClaim, chosenBucket);
			ClaimWriter.writeClaim(writer, maxid, chosenClaim.getObjectIdentifier(), chosenClaim.getPropertyName(), 
					chosenClaim.getPropertyValueString(), chosenClaim.getTimeStamp(), newSourceName, Globals.delimiterText);
			counter ++;

			chosenClaim = null;
			chosenBucket = null;
			maxNumSrc = 0;
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dataset;
	}
}
