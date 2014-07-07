package qcri.dafna.experiment.allegator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import au.com.bytecode.opencsv.CSVWriter;
import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;

public class AllegateOneSourceDIOneByOne extends qcri.dafna.experiment.Experiment {

	private int numTrueValChanged = 0;
	private boolean changed = false;
	private List<DataItemAllegatorObject> orderedDataItems;
	List<String> sourcesId;
	List<DescriptiveStatistics> sourcesStatistics;
	private int maxClaimID = 0;


	/**
	 * The new value, to be changed for every iteration
	 */
	private String newValue;

	public void allegate(DataSet dataset, String seedDir, String allegotarFolderName, String voterName, boolean neglictDIWithNoConflict, boolean orderByMaxConfidence) {
		boolean convergence100 = false;
		boolean runMLE = false;
		boolean runLTM = false;

		String resultDir = seedDir + "/experimentResult";

		HashMap<String , VoterQualityMeasures> voterQuality = runExperiment(convergence100 ,dataset, resultDir, runLTM, null, runMLE, null, voterName);

		initDIAllegatorObjects(dataset, neglictDIWithNoConflict, orderByMaxConfidence);
		initMaxClaimID(dataset);

		String newSourceName = "NewSourceFirst" + System.currentTimeMillis();
		String newSourceName2 = "NewSourceSecond" + System.currentTimeMillis();
		initSourcesStatisticsAfterInitialRun(dataset, newSourceName, newSourceName2);

		/*create folders*/
		String allegateDir = seedDir+"/allegateDIOneByOne";
		File allegateFolder = new File(allegateDir);
		allegateFolder.mkdir();
		resultDir = allegateDir + "/experimentResult";
		File experimentFolder = new File(resultDir);
		/*create folders*/
		experimentFolder.mkdir();
		BufferedWriter logWriter = null;
		BufferedWriter newWriter = null;
		try {
			logWriter = Files.newBufferedWriter(Paths.get(allegateDir + "/CompleteAllegationLog.csv"), Globals.FILE_ENCODING,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
			newWriter = Files.newBufferedWriter(Paths.get(allegateDir + "/newLog.csv"), Globals.FILE_ENCODING,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		CSVWriter csvWriter = new CSVWriter(logWriter,',');
		logHeaderAndFirstResults(voterName, voterQuality, csvWriter);

		CSVWriter newScvWriter = new CSVWriter(newWriter,',');
		logSourcesStatisticsHeader(newScvWriter);

		int numDI = 0;
		for (DataItemAllegatorObject o : orderedDataItems) {
			numDI ++;
			dataset = addClaimWithSecondMaxConfidence(dataset, o.getDataItemKey(), newSourceName, newSourceName2);
//			dataset = addClaimWithSecondMaxConfidence(dataset, o.getDataItemKey(), newSourceName2);
			voterQuality = runExperiment(convergence100 ,dataset, resultDir, runLTM, null, runMLE, null, voterName);
			updateSourcesStatistics(dataset);
			logResults(dataset, voterName, voterQuality, csvWriter, o.getDataItemKey(), newValue, o.getNumberOfCOnflicts());
			if (changed) {
				logSourcesStatistics(newScvWriter, o, numDI);
				changed = false;
			}
			try {
				logWriter.flush();
				newWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			logWriter.close();
			newWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void logSourcesStatisticsHeader(CSVWriter w) {
		String[] line = new String[sourcesId.size()+4];
		line[0] = "NbSrc";
		line[1] = "NbDI";
		for (int i = 0; i < sourcesId.size(); i ++) {
			line[2+i] = sourcesId.get(i);
		}
		line[sourcesId.size()+2] = "Confidences";
		line[sourcesId.size()+3] = "NumConlicts";
		w.writeNext(line);
	}
	private void logSourcesStatistics(CSVWriter w, DataItemAllegatorObject diAO, int numDI) {
		String[] line = new String[sourcesId.size()+4];
		line[0] = "2";
		line[1] = "" + numDI;
		for (int i = 0; i < sourcesId.size(); i ++) {
			line[i+2] = sourcesStatistics.get(i).getMean() + "(" + sourcesStatistics.get(i).getStandardDeviation() + ")";
		}
		String vals = "";
		DecimalFormat format = new DecimalFormat("#.###");
		for (String val : diAO.getValueConfidence().keySet()) {
			vals = vals + "(" + val + "=" + format.format(diAO.getValueConfidence().get(val)) + ")";
		}
		line[sourcesId.size()+2] = vals;
		line[sourcesId.size()+3] = diAO.getValueConfidence().size() + "";
		w.writeNext(line);
	}
	private void logHeaderAndFirstResults(String voterName,
			HashMap<String, VoterQualityMeasures> voterQuality,
			CSVWriter csvWriter) {
		// 7 for -> newDI, diValue, numConflicts, precision, accuracy, recall , specificity,
		String[] header = new String[7 + orderedDataItems.size()*2];
		String[] firstResult = new String[7 + orderedDataItems.size()*2];

		header[0] = "New Data Item"; firstResult[0] = "";
		header[1] = "DI Value";      firstResult[1] = "";
		header[2] = "DI Conflicts";      firstResult[2] = "";
		int i = 3;
		DecimalFormat format = new DecimalFormat("#.###");
		for (int j = 0, index = 0; j < orderedDataItems.size(); j++, index = index + 2) {
			header[i + index] = "DI " + j; firstResult[i + index] = orderedDataItems.get(j).getFirstTrueValueByVoter();
			header[i + index + 1] = "Cnfdnc" + j; firstResult[i + index + 1] = format.format(orderedDataItems.get(j).getValueConfidence().get(orderedDataItems.get(j).getFirstTrueValueByVoter()));
		}
		i = i + orderedDataItems.size();
		header[i] = "Precision"; firstResult[i] = "" + voterQuality.get(voterName).getPrecision(); i++;
		header[i] = "Accuracy"; firstResult[i] = "" + voterQuality.get(voterName).getAccuracy(); i++;
		header[i] = "Recall"; firstResult[i] = "" + voterQuality.get(voterName).getRecall(); i++;
		header[i] = "Specificity"; firstResult[i] = "" + voterQuality.get(voterName).getSpecificity(); i++;

		csvWriter.writeNext(header);
		csvWriter.writeNext(firstResult);
	}

	private void logResults(DataSet dataset, String voterName,
			HashMap<String, VoterQualityMeasures> voterQuality,
			CSVWriter csvWriter, String newDI, String diValue, int numConflicts) {
		// 7 for -> newDI, diValue, numConflicts, precision, accuracy, recall , specificity,
		String[] result = new String[7 + orderedDataItems.size()*2];
		
		result[0] = newDI;
		result[1] = diValue;
		result[2] = numConflicts + "";
		int i = 3;
		
		String value;
		String conf;

		DecimalFormat format = new DecimalFormat("#.###");
		for (int j = 0, index = 0; j < orderedDataItems.size(); j++, index = index+2) {
			value = "";
			conf = "";
			for (ValueBucket b : dataset.getDataItemsBuckets().get(orderedDataItems.get(j).getDataItemKey())) {
				if (b.getClaims().get(0).isTrueClaimByVoter()) {
					value = b.getClaims().get(0).getPropertyValueString();
					if ( ! value.equals(orderedDataItems.get(j).getTrueValueByVoter())) {
						orderedDataItems.get(j).setChanged(true);
						changed = true;
						orderedDataItems.get(j).setTrueValueByVoter(value);
					}
					if (Double.isInfinite(b.getConfidence())) {
						conf = "Infinite";
					} else {
						conf = format.format(b.getConfidence());
					}
				}
			}
			result[i + index] = value;
			result[i + index + 1] = conf;
		}
		i = i + orderedDataItems.size();
		result[i] = "" + voterQuality.get(voterName).getPrecision(); i++;
		result[i] = "" + voterQuality.get(voterName).getAccuracy(); i++;
		result[i] = "" + voterQuality.get(voterName).getRecall(); i++;
		result[i] = "" + voterQuality.get(voterName).getSpecificity(); i++;

		csvWriter.writeNext(result);
	}
	
	private DataSet addClaimWithSecondMaxConfidence(DataSet dataset, String dataItemKey, String sourceName, String secondSource) {
		double max = -1.0 * Double.MAX_VALUE;
		ValueBucket secondMax = null;
		for (ValueBucket b : dataset.getDataItemsBuckets().get(dataItemKey)) {
			if ( ! b.getClaims().get(0).isTrueClaimByVoter() || dataset.getDataItemsBuckets().get(dataItemKey).size() == 1) {
				if (max < b.getConfidence() || Double.isInfinite(b.getConfidence())) {
					max = b.getConfidence();
					secondMax = b;
				}
			}
		}

		SourceClaim newClaim = dataset.addClaim(maxClaimID, secondMax.getClaims().get(0).getObjectIdentifier(), 
				secondMax.getClaims().get(0).getUncleanedObjectIdentifier(), secondMax.getClaims().get(0).getPropertyName(), 
				secondMax.getClaims().get(0).getPropertyValueString(), secondMax.getClaims().get(0).getWeight(), 
				secondMax.getClaims().get(0).getTimeStamp(), sourceName);
		dataset.addClaimToBucket(newClaim, secondMax);
		maxClaimID ++;
		if (secondSource!=null && !secondSource.isEmpty() ) {
			newClaim = dataset.addClaim(maxClaimID, secondMax.getClaims().get(0).getObjectIdentifier(), 
					secondMax.getClaims().get(0).getUncleanedObjectIdentifier(), secondMax.getClaims().get(0).getPropertyName(), 
					secondMax.getClaims().get(0).getPropertyValueString(), secondMax.getClaims().get(0).getWeight(), 
					secondMax.getClaims().get(0).getTimeStamp(), secondSource);
			dataset.addClaimToBucket(newClaim, secondMax);
			maxClaimID ++;
		}
		newValue = secondMax.getClaims().get(0).getPropertyValueString();

		return dataset;
	}
	private void initMaxClaimID(DataSet dataset) {
		for (List<SourceClaim> l : dataset.getDataItemClaims().values() ) {
			for (SourceClaim c : l) {
				if (maxClaimID < c.getId()) {
					maxClaimID = c.getId();
				}
			}
		}
		maxClaimID ++;
	}

	private void orderDataItemsByMaxConfidence(DataSet dataset) {
		DataItemAllegatorObject temp;
		for (int i = 0; i < orderedDataItems.size(); i++) {
			for (int j = 1; j < orderedDataItems.size() - i; j++) {
				/* data item with only one value are put at or keep it the end of the list */
				if (orderedDataItems.get(j).getNumberOfCOnflicts() == 1) {
					continue;
				}
				if (orderedDataItems.get(j - 1).getNumberOfCOnflicts() == 1) {
					temp = orderedDataItems.get(j-1);
					orderedDataItems.set(j - 1, orderedDataItems.get(j));
					orderedDataItems.set(j, temp);
				} else if (orderedDataItems.get(j).getMaxConfidence() > orderedDataItems.get(j-1).getMaxConfidence()) {
					temp = orderedDataItems.get(j-1);
					orderedDataItems.set(j - 1, orderedDataItems.get(j));
					orderedDataItems.set(j, temp);

				} else if (orderedDataItems.get(j).getMaxConfidence() == orderedDataItems.get(j-1).getMaxConfidence()) {
					if (orderedDataItems.get(j).getMinConfidence() > orderedDataItems.get(j-1).getMinConfidence()) {
						temp = orderedDataItems.get(j-1);
						orderedDataItems.set(j - 1, orderedDataItems.get(j));
						orderedDataItems.set(j, temp);
					}
				}
			}
		}
	}

	private void orderDataItemsByConflictsAssending(DataSet dataset) {
		DataItemAllegatorObject temp;
		for (int i = 0; i < orderedDataItems.size(); i++) {
			for (int j = 1; j < orderedDataItems.size() - i; j++) {
				if (orderedDataItems.get(j).getNumberOfCOnflicts() < orderedDataItems.get(j-1).getNumberOfCOnflicts()) {
					temp = orderedDataItems.get(j-1);
					orderedDataItems.set(j - 1, orderedDataItems.get(j));
					orderedDataItems.set(j, temp);
				}
			}
		}
	}
	private void initDIAllegatorObjects(DataSet dataset, boolean neglictDIWithNoConflict, boolean orderByMaxConfidence) {
		orderedDataItems = new ArrayList<DataItemAllegatorObject>();

		double minConfidence;
		double maxConfidence;
		for (String diKey : dataset.getDataItemsBuckets().keySet()) {
			List<ValueBucket> bList = dataset.getDataItemsBuckets().get(diKey);
			if (bList.size() == 1 && neglictDIWithNoConflict) {
				continue;
			}
			DataItemAllegatorObject diAO = new DataItemAllegatorObject();
			diAO.setDataItemKey(diKey);
			minConfidence = Double.MAX_VALUE;
			maxConfidence = Double.MIN_VALUE;
			HashMap<String, Double> valueConfidence = new HashMap<String, Double>();
			for (ValueBucket b : bList) {
				valueConfidence.put(b.getClaims().get(0).getPropertyValueString(), b.getConfidence());
				if (minConfidence > b.getConfidence()) {
					minConfidence = b.getConfidence();
				}
				if (maxConfidence < b.getConfidence()) {
					maxConfidence = b.getConfidence();
				}
				if (b.getClaims().get(0).isTrueClaimByVoter()) {
					diAO.setFirstTrueValueByVoter(b.getClaims().get(0).getPropertyValueString());
					diAO.setTrueValueByVoter(b.getClaims().get(0).getPropertyValueString());
				}
			}
			diAO.setValueConfidence(valueConfidence);
			diAO.setMaxConfidence(maxConfidence);
			diAO.setMinConfidence(minConfidence);
			diAO.setNumberOfCOnflicts(bList.size());
			orderedDataItems.add(diAO);
		}

		if (orderByMaxConfidence) {
			orderDataItemsByMaxConfidence(dataset);
		} else {
			orderDataItemsByConflictsAssending(dataset);
		}
	}

	private void initSourcesStatisticsAfterInitialRun(DataSet dataset, String newSrcName1, String newSrcName2) {
		sourcesId = new ArrayList<String>();
		sourcesStatistics = new ArrayList<DescriptiveStatistics>();
		for (Source s : dataset.getSourcesHash().values()) {
			sourcesId.add(s.getSourceIdentifier());
			DescriptiveStatistics ds = new DescriptiveStatistics();
			ds.addValue(s.getTrustworthiness());
			sourcesStatistics.add(ds);
		}
		sourcesId.add(newSrcName1);
		sourcesStatistics.add(new DescriptiveStatistics());
		sourcesId.add(newSrcName2);
		sourcesStatistics.add(new DescriptiveStatistics());
	}
	private void updateSourcesStatistics(DataSet dataset) {
		for (int i = 0; i < sourcesId.size(); i ++) {
			sourcesStatistics.get(i).addValue(dataset.getSourcesHash().get(sourcesId.get(i)).getTrustworthiness());
		}
	}
}
