package qcri.dafna.dataModel.quality.dataQuality.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.quality.dataQuality.DataQualityMeasurments;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;

public class DataQualityLogger {
	public void LogDataSetData(String fileName, DataQualityMeasurments dqm, DataSet dataSet) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), Globals.FILE_ENCODING,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

			writer.write("Nb of sources :" + dataSet.getSourcesHash().keySet().size()+ "\n");
			writer.write("NB of Claims: " + dqm.getTotalNumOfClaims() + "\n");
			writer.write("Nb of Objects: " + dqm.getRedundancyOnObjects().keySet().size()+ "\n");
			writer.write("Nb of DataItems: " + dataSet.getDataItemClaims().keySet().size()+ "\n");
			writer.write("Gold Standard Values Count: " + dqm.getGoldStandardTrueValueCount()+ "\n");
			writer.write("Data Items Coverage: " + dqm.getAverageNumOfClaimsPerSource()/dataSet.getDataItemClaims().keySet().size() + "\n");
			
			writer.write("Avg Nb. of Claims Per Source: " + dqm.getAverageNumOfClaimsPerSource()+ "\n");
			writer.write("Avg Nb. of Values Per DataItem: " +  dqm.getAverageNumOfValuesPerDataItem()+ "\n");
			writer.write("Avg Redundency on dataItem: " + dqm.getAverageRedundencyOnDataItem() + "\n");
			writer.write("Avg Redundency on objects: " + dqm.getAverageRedundencyOnObject()+ "\n");
			writer.write("Precision Of Dominant Value: " + dqm.getPrecisionOfDominantValue()+ "\n");

			writer.write("dataSet Reading Time: " + dqm.getTimingMeasures().getDataSetReadingTime() + " m.s. (" + dqm.getTimingMeasures().getDataSetReadingTime()/60000.0 + "mins. )\n");
//			writer.write("truth values Reading Time: " + dqm.getTimingMeasures().getTruthReadingTime() + " m.s. (" + dqm.getTimingMeasures().getTruthReadingTime()/1000.0 + "sec. )\n");
//			writer.write("buckets Building Time: " + dqm.getTimingMeasures().getBucketsBuildingTime() + " m.s. (" + dqm.getTimingMeasures().getBucketsBuildingTime()/1000.0 + "sec. )\n");
//			writer.write("data-Quality Measures Computation Time: " + 
//					dqm.getTimingMeasures().getDataQualityMeasuresComputationTime() + "m.s.(" + dqm.getTimingMeasures().getDataQualityMeasuresComputationTime()/60000.0 + "mins. )\n");
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logVoterQualityLine(BufferedWriter writer, VoterQualityMeasures measures, boolean header, 
			String experiment, BufferedWriter precisionPerIterationWriter) {
		DecimalFormat format = new DecimalFormat("#.####");
		String line = "";
		if (header) {
			line = "Voter & ";
			line = line + "Precision & ";
			line = line + "Accuracy & ";
			line = line + "Recall & ";
			line = line + "Specificity & ";
			line = line + "N. of Iterations & ";
			line = line + "Voter duration & ";
			line = line + "Memory Consumtion(MB) & \n";
		} else {
			line = experiment + " & ";
			line = line + format.format(measures.getPrecision())  + " & ";
			line = line + format.format(measures.getAccuracy())  + " & ";
			line = line + format.format(measures.getRecall())  + " & ";
			line = line + format.format(measures.getSpecificity())  + " & ";
			line = line + measures.getNumberOfIterations()  + " & ";
			line = line + measures.getTimings().getVoterDuration()  + " & ";
			line = line + measures.getMaxMemoryConsumption()  + " & \n";
			
//			System.out.print(line);
		}
		try {
			writer.write(line);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (header) {
			line = "method ID & ";
			line = line + " Iteration # &";
			line = line + " time &";
			line = line + " precision & ";
			line = line + " accuracy &";
			line = line + " recall & ";
			line = line + " specificity & ";
			line = line + " F-Measure & ";
			line = line + " TP & ";
			line = line + " TN & ";
			line = line + " FP & ";
			line = line + " FN & ";
			line = line + " trust cosine similarity & ";
			line = line + " conf cosine similarity & \n";
			
			
		} else {
			double fmeasure, precision, recall;
			int size = measures.getPrecisionPerIteration().size();
			line = "";
			for (int i = 0; i < size; i ++) {
				precision = measures.getPrecisionPerIteration().get(i);
				recall = measures.getRecallPerIteration().get(i);
				fmeasure =  2 * precision * recall / (precision + recall);

				line = line + experiment + " & ";
				if (i == (size - 1)) {
					line = line + " -1 & ";
				} else {
					line = line + (i+1) + " & ";
				}
				line = line + measures.getIterationEndingTime().get(i)  + " & ";
				line = line + precision  + " & ";
				line = line + measures.getAccuracyPerIteration().get(i)  + " & ";
				line = line + recall  + " & ";
				line = line + measures.getSpecificityPerIteration().get(i)  + " & ";
				line = line + fmeasure  + " & ";
				line = line + measures.getTruePositivePerIteration().get(i)  + " & ";
				line = line + measures.getTrueNegativePerIteration().get(i)  + " & ";
				line = line + measures.getFalsePositivePerIteration().get(i)  + " & ";
				line = line + measures.getFalseNegativePerIteration().get(i)  + " & ";
				line = line + measures.getTrustCosineSimilarityPerIteration().get(i)  + " & ";
				line = line + measures.getConfCosineSimilarityPerIteration().get(i)  + " & \n";
			}
		}
		try {
			precisionPerIterationWriter.write(line);
			precisionPerIterationWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
