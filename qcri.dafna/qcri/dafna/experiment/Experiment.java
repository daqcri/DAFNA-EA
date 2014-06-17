package qcri.dafna.experiment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.Source;
import qcri.dafna.dataModel.data.ValueBucket;
import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;
import qcri.dafna.dataModel.quality.dataQuality.logger.DataQualityLogger;
import qcri.dafna.dataModel.quality.voterResults.NormalVoterQualityMeasures;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;
import qcri.dafna.voter.MaximumLikelihoodEstimation;
import qcri.dafna.voter.Cosine;
import qcri.dafna.voter.GuessLCA;
import qcri.dafna.voter.SimpleLCA;
import qcri.dafna.voter.ThreeEstimates;
import qcri.dafna.voter.TruthFinder;
import qcri.dafna.voter.TwoEstimates;
import qcri.dafna.voter.VoterParameters;
import qcri.dafna.voter.Voting;
import qcri.dafna.voter.dependence.SourceDependenceModel;
import qcri.dafna.voter.latentTruthModel.LatentTruthModel;

public abstract class Experiment {

	static boolean logExperimentName = true;
	static boolean profileMEmory = false;
	protected static ContengencyTable contengencyTable;
	protected static HashMap<String, VoterQualityMeasures> resultsMap;

	protected static HashMap<String, VoterQualityMeasures> runExperiment(boolean convergence100, DataSet dataSet, 
			String resultFolderName, 
			boolean runLTM, DataSet datasetLTM,
			boolean runMLE, DataSet dataSetMLE) {

		contengencyTable = new ContengencyTable();

		resultsMap = new HashMap<String, VoterQualityMeasures>();

		DataQualityLogger logger = new DataQualityLogger();
		logger.LogDataSetData(resultFolderName + "/dataSetInfo.txt", dataSet.getDataQualityMeasurments(), dataSet);
		BufferedWriter writer = getLoggerWriter(resultFolderName + "/VoterQuality.csv");
		BufferedWriter precisionWriter = getLoggerWriter(resultFolderName +  "/ParametersPerIteration.csv");
		logger.logVoterQualityLine(writer, null, true, "", precisionWriter);

		double cosineSimDiffStoppingCriteria = 0.001;//ConvergenceTester.convergenceThreshold;
		ConvergenceTester.convergenceThreshold = cosineSimDiffStoppingCriteria;
		double similarityConstant = 0.5;//0.5 as set in the paper TruthFinder
		double base_sim = 0.5;// as set in the paper TruthFinder
		double dampingFactor = 0.1; // as set in the paper comparative study
		double dampingFactorCosine = 0.2;
		
		double startingTrust = 0.8;//0.8;
		double startingConfidence = 1.0;
		double startingErrorFactor = 0.4;

		double normalizationWeight = 0.5;// used for the 2-estimates and 3-estimates normalization

		VoterParameters params = new VoterParameters(cosineSimDiffStoppingCriteria, startingTrust, startingConfidence, startingErrorFactor);
		/* ---------------------------------- Voter ---------------------------------- */

		Voting voting = new Voting(dataSet, params);
		NormalVoterQualityMeasures votingQualityMeasure = (NormalVoterQualityMeasures)voting.launchVoter(convergence100, profileMEmory);
		log(dataSet, writer, precisionWriter, logger, Globals.voterVoting, votingQualityMeasure, false);

		//		/* ---------------------------------- TruthFinder  ---------------------------------- */

		TruthFinder truthFinder = new TruthFinder(dataSet, params, similarityConstant, dampingFactor);
		NormalVoterQualityMeasures truthFinderQualityMeasure = (NormalVoterQualityMeasures)truthFinder.launchVoter(convergence100, profileMEmory);
		log(dataSet, writer, precisionWriter, logger, Globals.voterTruthFinder, truthFinderQualityMeasure, false);

	/* ---------------------------------- Cosine  ---------------------------------- */
			
		Cosine cosine = new Cosine(dataSet, params, dampingFactorCosine);
		NormalVoterQualityMeasures cosineQualityMeasure = (NormalVoterQualityMeasures)cosine.launchVoter(convergence100, profileMEmory);
		log(dataSet, writer, precisionWriter, logger, Globals.voterCosine, cosineQualityMeasure, false);
		

//		/* ---------------------------------- 2-Estimates  ---------------------------------- */
			
		TwoEstimates twoEstimates = new TwoEstimates(dataSet, params, normalizationWeight);
		NormalVoterQualityMeasures twoEstimatesQualityMeasure = (NormalVoterQualityMeasures)twoEstimates.launchVoter(convergence100, profileMEmory);
		log(dataSet, writer, precisionWriter, logger, Globals.voter2Estimates, twoEstimatesQualityMeasure, false);


//		/* ---------------------------------- 3-Estimates  ---------------------------------- */
		
		NormalVoterQualityMeasures threeEstimatesQualityMeasure = runThreeEstimates(convergence100, dataSet, normalizationWeight, params);
		log(dataSet, writer, precisionWriter, logger, Globals.voter3Estimates, threeEstimatesQualityMeasure, false);
		


//		/* ---------------------------------- Simple LCA  ---------------------------------- */
//		
		double pymB = 0.5;//1.0;
		SimpleLCA simpleLCA = new SimpleLCA(dataSet, params, pymB);
		NormalVoterQualityMeasures simpleLCAQualityMeasure = (NormalVoterQualityMeasures)simpleLCA.launchVoter(convergence100, profileMEmory);
		log(dataSet, writer, precisionWriter, logger, Globals.voterSimpleLCA, simpleLCAQualityMeasure, false);

//		/* ---------------------------------- Guess LCA  ---------------------------------- */
		GuessLCA guessLCA = new GuessLCA(dataSet, params, pymB);
		NormalVoterQualityMeasures guessLCAQualityMeasure = (NormalVoterQualityMeasures)guessLCA.launchVoter(convergence100, profileMEmory);
		log(dataSet, writer, precisionWriter, logger, Globals.voterGuessLCA, guessLCAQualityMeasure, false);

		/* ---------------------------------- AccuCopy T T  AccuSim---------------------------------- */

		double alfa = 0.2;
		double c = 0.8;
		int n = 100;
		boolean orderSrcByDependence = false;

		boolean considerSimilarity = true; 
		boolean considerSourcesAccuracy = true;
		boolean considerDependencies = true;

		considerSimilarity = true; 
		considerSourcesAccuracy = true;
		considerDependencies = true;

		NormalVoterQualityMeasures accuModelBaseQualityMeasure = runAccuModel(convergence100, dataSet, 
				similarityConstant, alfa, c, n, orderSrcByDependence, considerSimilarity,
				considerSourcesAccuracy, considerDependencies, params);
		log(dataSet, writer, precisionWriter, logger, Globals.voterAccuSim, accuModelBaseQualityMeasure, false);
////
////		/* ---------------------------------- AccuCopy F F Depen ---------------------------------- */
		considerSimilarity = false;
		considerSourcesAccuracy = false;
		considerDependencies = true;

		NormalVoterQualityMeasures accuModelBaseQualityMeasureFF = runAccuModel(convergence100, dataSet, 
				similarityConstant, alfa, c, n, orderSrcByDependence, considerSimilarity,
				considerSourcesAccuracy, considerDependencies, params);
		log(dataSet, writer, precisionWriter, logger, Globals.voterDepen, accuModelBaseQualityMeasureFF, false);

//			/* ---------------------------------- AccuNoDep  ---------------------------------- */

		considerSimilarity = false;
		considerSourcesAccuracy = true;
		considerDependencies = false;

		NormalVoterQualityMeasures accuModelBaseQualityMeasureAccuNoDep = runAccuModel(convergence100, dataSet, 
				similarityConstant, alfa, c, n, orderSrcByDependence, considerSimilarity,
				considerSourcesAccuracy, considerDependencies, params);
		log(dataSet, writer, precisionWriter, logger, Globals.voterAccuNoDep, accuModelBaseQualityMeasureAccuNoDep, false);


				/* ---------------------------------- AccuCopy F T Accu ---------------------------------- */

		considerSimilarity = false;
		considerSourcesAccuracy = true;
		considerDependencies = true;
		NormalVoterQualityMeasures accuModelBaseQualityMeasureFT = runAccuModel(convergence100, dataSet, 
				similarityConstant, alfa, c, n, orderSrcByDependence, considerSimilarity,
				considerSourcesAccuracy, considerDependencies, params);
		log(dataSet, writer, precisionWriter, logger, Globals.voterAccu, accuModelBaseQualityMeasureFT, false);
		/*------------------------------------------------------------------------------------*/
		if (runLTM) {
			runLTM(convergence100, datasetLTM, writer, precisionWriter, logger, resultsMap, contengencyTable, params);
		}
		if (runMLE) {
//			/*for synthetic dataset*/runSyntheticBooleanMLE(convergence100, dataSetSinglePropertyValue, writer, precisionWriter, logger, resultsMap, contengencyTable);
			/*for book dataset*/runSyntheticBooleanMLE(convergence100, dataSetMLE, writer, precisionWriter, logger, resultsMap, contengencyTable, params);
		}
		/*--------------------------------*/
		try {
			writer.close();
			precisionWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultsMap;
	}

	private static NormalVoterQualityMeasures runThreeEstimates(boolean convergence100, DataSet dataSet, double normalizationWeight, VoterParameters params) {
		NormalVoterQualityMeasures threeEstimatesQualityMeasure = null;

//		normalizationWeight = 0.5;// 0.1 0.3 0.5
//		startingErrorFactor = 0.1;
//		startingTrust = 0.8;
//		System.out.print("lambda =" + normalizationWeight + ", T ="+startingTrust);
//		convergence100 = false;
//for (int i = 1; i <= 10; i ++) {
//	startingErrorFactor = ((double)i)/10.0;
		ThreeEstimates threeEstimate = new ThreeEstimates(dataSet, params, normalizationWeight);
		threeEstimatesQualityMeasure = (NormalVoterQualityMeasures)threeEstimate.launchVoter(convergence100, profileMEmory);
		
//		tempLog(threeEstimatesQualityMeasure, "E ",String.valueOf(startingErrorFactor));
//}
		return threeEstimatesQualityMeasure;
	}

	private static void tempLog(NormalVoterQualityMeasures tempvoterQuality, String variableName, String variableValue) {
		DecimalFormat format = new DecimalFormat("#.####");
		System.out.println("  & " + variableName + " = " + variableValue + " & " + format.format(tempvoterQuality.getPrecision())	
				+ " & " + format.format(tempvoterQuality.getAccuracy()) 
				+ " & " + format.format(tempvoterQuality.getRecall()) + " & " + format.format(tempvoterQuality.getSpecificity()) 
				+ " & " + tempvoterQuality.getNumberOfIterations() + " & " + tempvoterQuality.getTimings().getVoterDuration());
	}
	private static NormalVoterQualityMeasures runAccuModel(boolean convergence100, DataSet dataSet, double similarityConstant,
			double alfa, double c, int n, boolean orderSrcByDependence, boolean considerSimilarity,
			boolean considerSourcesAccuracy, boolean considerDependencies, VoterParameters params) {

		SourceDependenceModel accuModel = new SourceDependenceModel(dataSet, params, alfa, c, n, similarityConstant, considerSimilarity, 
				considerSourcesAccuracy, considerDependencies, orderSrcByDependence);
		NormalVoterQualityMeasures qualityMeasure = (NormalVoterQualityMeasures) accuModel.launchVoter(convergence100, profileMEmory);
		return qualityMeasure;
	}

	private static void runLTM(boolean convergence100, DataSet dataSetSinglePropertyValue, BufferedWriter writer, 
			BufferedWriter precisionWriter, DataQualityLogger logger ,HashMap<String, VoterQualityMeasures> resultsMap, 
			ContengencyTable contengencyTable, VoterParameters params) {
		/* --------------------------------------- Latent truth model -  Special dataSet formulation ------------------------------------------ */
		int iterationCount = 500;
		int burnIn = 100;
		int sampleGap = 9;

		double a01 = 0.9;//0.10;
		double a00 = 0.1;//0.90;

		double a11 = 0.9;//0.90;
		double a10= 0.1;//0.10 ;
		double b1 = 0.5;//10;
		double b0 = 0.5;//10;


		convergence100 = false;

		LatentTruthModel latentTruthModel = new LatentTruthModel(dataSetSinglePropertyValue, params, b1, b0, a00, a01, a10, a11, 
				iterationCount, burnIn, sampleGap);
		NormalVoterQualityMeasures latentTruthModelQualityMeasure = (NormalVoterQualityMeasures)latentTruthModel.launchVoter(convergence100, profileMEmory);
		log(dataSetSinglePropertyValue, writer, precisionWriter, logger, Globals.voterLTM, latentTruthModelQualityMeasure, false);
	}

	private static void runLTMExperimetsss(double d,boolean convergence100,
			DataSet dataSetSinglePropertyValue, BufferedWriter writer,
			BufferedWriter precisionWriter, DataQualityLogger logger,
			int iterationCount, int burnIn, int sampleGap, DecimalFormat format, VoterParameters params) {
		System.out.println("k=" + iterationCount+" B="+burnIn+" T= "+ sampleGap);
		double b1;
		double b0;
		for (int b = 0 ; b < 2; b++) {
			if (b == 0) {
				b1 = 0.1;//10;
				b0 = 0.1;//10;
			} else {
				b1 = 0.5;//10;
				b0 = 0.5;//10;
			}
			
			double a01 = d;//0.10;
			double a00 = 1-d;//0.90;
//			System.out.println("& b1 = " + b1 + " & b0 = " + b0 + " a01="+a01 + " a00="+a00);
//			for (double i = 1; i < 10; i++)  {
//
//				double a11 = i/10;//0.90;
//				double a10 = 1-a11;//0.10 ;
//
//				double precision = 0;
//				double accuracy = 0;
//				double specificity = 0;
//				double recall = 0;
//				long time = 0;
//
//				DescriptiveStatistics presisionStats = new DescriptiveStatistics();
//				DescriptiveStatistics accStats = new DescriptiveStatistics();
//				DescriptiveStatistics recallStats = new DescriptiveStatistics();
//				DescriptiveStatistics specificityStats = new DescriptiveStatistics();
//				
//				System.out.print(" & a11 = " + a11 + " & a10=" + a10 + " & ");
//				for (int x = 0; x < 100; x++) {
//					LatentTruthModel latentTruthModel = new LatentTruthModel(b1, b0, a00, a01, a10, a11, dataSetSinglePropertyValue, iterationCount, burnIn, sampleGap);
//					NormalVoterQualityMeasures latentTruthModelQualityMeasure = (NormalVoterQualityMeasures)latentTruthModel.launchVoter(convergence100);
////					log(dataSetSinglePropertyValue, writer, precisionWriter, logger, Globals.voterLTM, latentTruthModelQualityMeasure, false);
//					presisionStats.addValue(latentTruthModelQualityMeasure.getPrecision());
//					accStats.addValue(latentTruthModelQualityMeasure.getAccuracy());
//					recallStats.addValue(latentTruthModelQualityMeasure.getRecall());
//					specificityStats.addValue(latentTruthModelQualityMeasure.getSpecificity());
//					
////					precision += latentTruthModelQualityMeasure.getPrecision();
////					accuracy += latentTruthModelQualityMeasure.getAccuracy();
////					specificity += latentTruthModelQualityMeasure.getSpecificity();
////					recall += latentTruthModelQualityMeasure.getRecall();
//					time += latentTruthModelQualityMeasure.getTimings().getVoterDuration();
//				} System.out.println(" " + format.format(presisionStats.getMean()) + "(+-"+format.format(presisionStats.getStandardDeviation()) + ")" 
//					+ " & " + format.format(accStats.getMean()) + "(+-"+format.format(accStats.getStandardDeviation()) + ")" 
//					+ " & " + format.format(recallStats.getMean()) + "(+-"+format.format(recallStats.getStandardDeviation()) + ")"  
//					+ " & " + format.format(specificityStats.getMean()) + "(+-"+format.format(specificityStats.getStandardDeviation()) + ")"  
//					+ " & " + iterationCount + " & " + time/100);
//			}
		}

		for (int b = 0 ; b < 2; b++) {
			if (b == 0) {
				b1 = 0.1;//10;
				b0 = 0.1;//10;
			} else {
				b1 = 0.5;//10;
				b0 = 0.5;//10;
			}
			
			double a11 = d;//0.10;
			double a10 = 1-d;//0.90;
			System.out.println("& b1 = " + b1 + " & b0 = " + b0 + " a11="+a11 + " a10="+a10);
			for (double i = 1; i < 10; i++)  {

				double a01 = i/10;//0.90;
				double a00 = 1-a01;//0.10 ;

				double precision = 0;
				double accuracy = 0;
				double specificity = 0;
				double recall = 0;
				long time = 0;
				
				DescriptiveStatistics presisionStats = new DescriptiveStatistics();
				DescriptiveStatistics accStats = new DescriptiveStatistics();
				DescriptiveStatistics recallStats = new DescriptiveStatistics();
				DescriptiveStatistics specificityStats = new DescriptiveStatistics();

				System.out.print(" & a01 = " + a01 + " & a00=" + a00 + " & ");
				for (int x = 0; x < 100; x++) {
					LatentTruthModel latentTruthModel = new LatentTruthModel(dataSetSinglePropertyValue, params, b1, b0, a00, a01, a10, a11, 
							iterationCount, burnIn, sampleGap);
					NormalVoterQualityMeasures latentTruthModelQualityMeasure = (NormalVoterQualityMeasures)latentTruthModel.launchVoter(convergence100, profileMEmory);
//					log(dataSetSinglePropertyValue, writer, precisionWriter, logger, Globals.voterLTM, latentTruthModelQualityMeasure, false);
					presisionStats.addValue(latentTruthModelQualityMeasure.getPrecision());
					accStats.addValue(latentTruthModelQualityMeasure.getAccuracy());
					recallStats.addValue(latentTruthModelQualityMeasure.getRecall());
					specificityStats.addValue(latentTruthModelQualityMeasure.getSpecificity());
					
//					precision += latentTruthModelQualityMeasure.getPrecision();
//					accuracy += latentTruthModelQualityMeasure.getAccuracy();
//					specificity += latentTruthModelQualityMeasure.getSpecificity();
//					recall += latentTruthModelQualityMeasure.getRecall();
					time += latentTruthModelQualityMeasure.getTimings().getVoterDuration();
				} System.out.println(" " + format.format(presisionStats.getMean()) + "(+-"+format.format(presisionStats.getStandardDeviation()) + ")" 
						+ " & " + format.format(accStats.getMean()) + "(+-"+format.format(accStats.getStandardDeviation()) + ")" 
						+ " & " + format.format(recallStats.getMean()) + "(+-"+format.format(recallStats.getStandardDeviation()) + ")"  
						+ " & " + format.format(specificityStats.getMean()) + "(+-"+format.format(specificityStats.getStandardDeviation()) + ")"  
						+ " & " + iterationCount + " & " + time/100);
			}
		}
	}

	private static void runSyntheticBooleanMLE(boolean convergence100, DataSet dataSet, BufferedWriter writer, 
			BufferedWriter precisionWriter, DataQualityLogger logger, HashMap<String, VoterQualityMeasures> resultsMap,
			ContengencyTable contengencyTable, VoterParameters params) {
		double priorFalseGroundTruth = 0.5;//0.4;
		double r = 0.5;
		MaximumLikelihoodEstimation em = new MaximumLikelihoodEstimation(dataSet, params, priorFalseGroundTruth, r);
		/** 
		 * this approach compute finally the sources trustworthiness (reliability) and
		 * can be used for further comparison between methods
		 **/
		VoterQualityMeasures emQualityMeasure = em.launchVoter(convergence100, profileMEmory);
//		System.out.print("d=" + priorFalseGroundTruth+ " & ");
		log(dataSet, writer, precisionWriter, logger, Globals.voterMLE, emQualityMeasure, false);
		/*-----------------------------------------------------------------------------------------*/
	}

	private static void log(DataSet dataSet, BufferedWriter writer, BufferedWriter precisionWriter, DataQualityLogger logger,
			String VoterName, VoterQualityMeasures voterQualityMeasure, boolean header) {

		if (logExperimentName) {
			Date d = new Date();
//			System.out.println("Finished:\t" + VoterName + "\t:\t" + voterQualityMeasure.getNumberOfIterations() + "\t\t" + d.toString());
			System.out.println(VoterName);
		}
		logger.logVoterQualityLine(writer, voterQualityMeasure, header, VoterName, precisionWriter);
		resultsMap.put(VoterName, voterQualityMeasure);
		contengencyTable.readTrueValues(dataSet, VoterName);
		
		System.out.print("\t");
		for (Source s : dataSet.getSourcesHash().values()) {
			System.out.print("Ts(" + s.getSourceIdentifier() + ") = " + s.getTrustworthiness() + "\t");
		} System.out.println();
		for (List<ValueBucket> bList : dataSet.getDataItemsBuckets().values()) {System.out.print("\t");
			System.out.print(bList.get(0).getClaims().get(0).getObjectIdentifier());System.out.print("\t");
			for (ValueBucket b : bList) {
				System.out.print("Cv("+b.getClaims().get(0).getPropertyValueString()+") = " + b.getConfidence() + "\t");
			}System.out.println();
		}
		
//		sclog.saveSourcesTrueClaims(dataSet, "FlightExperiment", "AccuModelBase");
	}
	private static BufferedWriter getLoggerWriter(String fileName) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), Globals.FILE_ENCODING,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			return writer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
