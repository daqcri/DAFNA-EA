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
import qcri.dafna.dataModel.dataFormatter.DataComparator;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.quality.dataQuality.ConvergenceTester;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;
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

	static boolean logExperimentName = false;
	static boolean profileMEmory = false;


	protected static ContengencyTable contengencyTable;
	protected static HashMap<String, VoterQualityMeasures> resultsMap;

	/**
	 * @param convergence100 True if the model should not stop at convergence, but continue till the maximum number of Iterations.
	 * @param dataSet The normal dataset to be used.
	 * @param resultFolderName The Folder where all result should be  written in.
	 * @param runLTM Whether to run LTM or no.
	 * @param datasetLTM The dataset in the LTM format
	 * @param runMLE Whether to run MLE or not.
	 * @param dataSetMLE The dataset in the MLE format
	 * @paramvoterName
	 * @return
	 */
	protected static HashMap<String, VoterQualityMeasures> runExperiment(boolean convergence100, DataSet dataSet, 
			String resultFolderName, 
			boolean runLTM, DataSet datasetLTM,
			boolean runMLE, DataSet dataSetMLE,
			String voterName) {

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
		double dampingFactor = 0.1; // as set in the paper comparative study
		double dampingFactorCosine = 0.2;

		double startingTrust = 0.8;//0.8;
		double startingConfidence = 1.0;
		double startingErrorFactor = 0.4;

		double normalizationWeight = 0.5;// used for the 2-estimates and 3-estimates normalization

		VoterParameters params = new VoterParameters(cosineSimDiffStoppingCriteria, startingTrust, startingConfidence, startingErrorFactor);
		/* ---------------------------------- Voter ---------------------------------- */

		if (voterName.equals(Globals.voterVoting) || voterName.isEmpty()) {
			Voting voting = new Voting(dataSet, params);
			NormalVoterQualityMeasures votingQualityMeasure = (NormalVoterQualityMeasures)voting.launchVoter(convergence100, profileMEmory);
			log(dataSet, writer, precisionWriter, logger, Globals.voterVoting, votingQualityMeasure, false);
		}

		/**
		 * For synthetic experiment,
		 * To select only 100 DI that are not selected by the Voter Voting to be in the ground truth
		 */
		//		int remaining = dataSet.getDataQualityMeasurments().getGoldStandardTrueValueCount();
		//		DataItemMeasures dim;
		//		for (String diKey: dataSet.getDataItemsBuckets().keySet()) {
		//			dim = dataSet.getDataQualityMeasurments().getDataItemMeasures().get(diKey);
		//			for (ValueBucket b : dataSet.getDataItemsBuckets().get(diKey)) {
		//				if (bucketContainsValue(b, dim) ) {
		//					if (remaining > 100) {
		//						dataSet.getDataQualityMeasurments().getDataItemMeasures().get(diKey).setTrueValue(null);
		//					} else {
		//						break;
		//					}
		//				}
		//			}
		//		}
		//		
		//		voting = new Voting(dataSet, params);
		//		votingQualityMeasure = (NormalVoterQualityMeasures)voting.launchVoter(convergence100, profileMEmory);
		//		log(dataSet, writer, precisionWriter, logger, Globals.voterVoting, votingQualityMeasure, false);
		/** ----------------------------------------------------------------------------------------------------- */

		//		/* ---------------------------------- TruthFinder  ---------------------------------- */

		if (voterName.equals(Globals.voterTruthFinder) || voterName.isEmpty()) {
			TruthFinder truthFinder = new TruthFinder(dataSet, params, similarityConstant, dampingFactor);
			NormalVoterQualityMeasures truthFinderQualityMeasure = (NormalVoterQualityMeasures)truthFinder.launchVoter(convergence100, profileMEmory);
			log(dataSet, writer, precisionWriter, logger, Globals.voterTruthFinder, truthFinderQualityMeasure, false);
		}
		/* ---------------------------------- Cosine  ---------------------------------- */
		if (voterName.equals(Globals.voterCosine) || voterName.isEmpty()) {
			Cosine cosine = new Cosine(dataSet, params, dampingFactorCosine);
			NormalVoterQualityMeasures cosineQualityMeasure = (NormalVoterQualityMeasures)cosine.launchVoter(convergence100, profileMEmory);
			log(dataSet, writer, precisionWriter, logger, Globals.voterCosine, cosineQualityMeasure, false);
		}

		//		/* ---------------------------------- 2-Estimates  ---------------------------------- */
		if (voterName.equals(Globals.voter2Estimates) || voterName.isEmpty()) {
			TwoEstimates twoEstimates = new TwoEstimates(dataSet, params, normalizationWeight);
			NormalVoterQualityMeasures twoEstimatesQualityMeasure = (NormalVoterQualityMeasures)twoEstimates.launchVoter(convergence100, profileMEmory);
			log(dataSet, writer, precisionWriter, logger, Globals.voter2Estimates, twoEstimatesQualityMeasure, false);
		}

		//		/* ---------------------------------- 3-Estimates  ---------------------------------- */
		if (voterName.equals(Globals.voter3Estimates) || voterName.isEmpty()) {
			NormalVoterQualityMeasures threeEstimatesQualityMeasure = runThreeEstimates(convergence100, dataSet, normalizationWeight, params);
			log(dataSet, writer, precisionWriter, logger, Globals.voter3Estimates, threeEstimatesQualityMeasure, false);
		}


		//		/* ---------------------------------- Simple LCA  ---------------------------------- */
		double pymB = 0.5;//1.0;
		if (voterName.equals(Globals.voterSimpleLCA) || voterName.isEmpty()) {
			SimpleLCA simpleLCA = new SimpleLCA(dataSet, params, pymB);
			NormalVoterQualityMeasures simpleLCAQualityMeasure = (NormalVoterQualityMeasures)simpleLCA.launchVoter(convergence100, profileMEmory);
			log(dataSet, writer, precisionWriter, logger, Globals.voterSimpleLCA, simpleLCAQualityMeasure, false);
		}
		//		/* ---------------------------------- Guess LCA  ---------------------------------- */
		if (voterName.equals(Globals.voterGuessLCA) || voterName.isEmpty()) {
			GuessLCA guessLCA = new GuessLCA(dataSet, params, pymB);
			NormalVoterQualityMeasures guessLCAQualityMeasure = (NormalVoterQualityMeasures)guessLCA.launchVoter(convergence100, profileMEmory);
			log(dataSet, writer, precisionWriter, logger, Globals.voterGuessLCA, guessLCAQualityMeasure, false);
		}
		/* ---------------------------------- AccuCopy T T  AccuSim---------------------------------- */

		double alfa = 0.2;
		double c = 0.8;
		int n = 100;
		boolean orderSrcByDependence = false;

		boolean considerSimilarity = true; 
		boolean considerSourcesAccuracy = true;
		boolean considerDependencies = true;



		if (voterName.equals(Globals.voterAccuSim) || voterName.isEmpty()) {
			considerSimilarity = true; 
			considerSourcesAccuracy = true;
			considerDependencies = true;
			NormalVoterQualityMeasures accuModelBaseQualityMeasure = runAccuModel(convergence100, dataSet, 
					similarityConstant, alfa, c, n, orderSrcByDependence, considerSimilarity,
					considerSourcesAccuracy, considerDependencies, params);
			log(dataSet, writer, precisionWriter, logger, Globals.voterAccuSim, accuModelBaseQualityMeasure, false);
		}
		////		/* ---------------------------------- AccuCopy F F Depen ---------------------------------- */


		if (voterName.equals(Globals.voterDepen) || voterName.isEmpty()) {
			considerSimilarity = false;
			considerSourcesAccuracy = false;
			considerDependencies = true;
			NormalVoterQualityMeasures accuModelBaseQualityMeasureFF = runAccuModel(convergence100, dataSet, 
					similarityConstant, alfa, c, n, orderSrcByDependence, considerSimilarity,
					considerSourcesAccuracy, considerDependencies, params);
			log(dataSet, writer, precisionWriter, logger, Globals.voterDepen, accuModelBaseQualityMeasureFF, false);
		}
		//			/* ---------------------------------- AccuNoDep  ---------------------------------- */
		if (voterName.equals(Globals.voterAccuNoDep) || voterName.isEmpty()) {
			considerSimilarity = false;
			considerSourcesAccuracy = true;
			considerDependencies = false;

			NormalVoterQualityMeasures accuModelBaseQualityMeasureAccuNoDep = runAccuModel(convergence100, dataSet, 
					similarityConstant, alfa, c, n, orderSrcByDependence, considerSimilarity,
					considerSourcesAccuracy, considerDependencies, params);
			log(dataSet, writer, precisionWriter, logger, Globals.voterAccuNoDep, accuModelBaseQualityMeasureAccuNoDep, false);
		}

		/* ---------------------------------- AccuCopy F T Accu ---------------------------------- */
		if (voterName.equals(Globals.voterAccu) || voterName.isEmpty()) {
			considerSimilarity = false;
			considerSourcesAccuracy = true;
			considerDependencies = true;
			NormalVoterQualityMeasures accuModelBaseQualityMeasureFT = runAccuModel(convergence100, dataSet, 
					similarityConstant, alfa, c, n, orderSrcByDependence, considerSimilarity,
					considerSourcesAccuracy, considerDependencies, params);
			log(dataSet, writer, precisionWriter, logger, Globals.voterAccu, accuModelBaseQualityMeasureFT, false);
		}
		/*------------------------------------------------------------------------------------*/
		if (runLTM) {
			runLTM(convergence100, datasetLTM, writer, precisionWriter, logger, resultsMap, contengencyTable, params);
		}
		if (runMLE) {
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

		ThreeEstimates threeEstimate = new ThreeEstimates(dataSet, params, normalizationWeight);
		threeEstimatesQualityMeasure = (NormalVoterQualityMeasures)threeEstimate.launchVoter(convergence100, profileMEmory);

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

		double a01 = 0.9;
		double a00 = 0.1;

		double a11 = 0.9;
		double a10= 0.1;
		double b1 = 0.5;
		double b0 = 0.5;

		convergence100 = false;

		LatentTruthModel latentTruthModel = new LatentTruthModel(dataSetSinglePropertyValue, params, b1, b0, a00, a01, a10, a11, 
				 iterationCount, burnIn, sampleGap);
		NormalVoterQualityMeasures latentTruthModelQualityMeasure = (NormalVoterQualityMeasures)latentTruthModel.launchVoter(convergence100, profileMEmory);
		log(dataSetSinglePropertyValue, writer, precisionWriter, logger, Globals.voterLTM, latentTruthModelQualityMeasure, false);
	}

	private static void runSyntheticBooleanMLE(boolean convergence100, DataSet dataSet, BufferedWriter writer, 
			BufferedWriter precisionWriter, DataQualityLogger logger, HashMap<String, VoterQualityMeasures> resultsMap,
			ContengencyTable contengencyTable, VoterParameters params) {
		double priorFalseGroundTruth = 0.5;//0.4;
		double r = 0.5;

		for (int i=0; i < 10; i++) {

			r = ((double)i)/10.0;
			MaximumLikelihoodEstimation em = new MaximumLikelihoodEstimation(dataSet, params, priorFalseGroundTruth, r);
			/** 
			 * this approach compute finally the sources trustworthiness (reliability) and
			 * can be used for further comparison between methods
			 **/
			VoterQualityMeasures emQualityMeasure = em.launchVoter(convergence100, profileMEmory);
			System.out.print("r =" + r+ " & ");
			log(dataSet, writer, precisionWriter, logger, Globals.voterMLE, emQualityMeasure, false);
		}
		/*-----------------------------------------------------------------------------------------*/
	}

	/**
	 * 
	 * @param dataSet: the dataset
	 * @param writer: the voter quality writer.
	 * @param precisionWriter The precision per iteration writer.
	 * @param logger the logger object
	 * @param VoterName: the voter name
	 * @param voterQualityMeasure: the result of the voter.
	 * @param header whether this line to be logged in the file header or a data line, this is always false, 
	 * only true for logging the header of the file at the beginning of the launchVoter Method
	 */
	private static void log(DataSet dataSet, BufferedWriter writer, BufferedWriter precisionWriter, DataQualityLogger logger,
			String VoterName, VoterQualityMeasures voterQualityMeasure, boolean header) {

		if (logExperimentName) {
			Date d = new Date();
			System.out.println("Finished:\t" + VoterName + "\t:\t" + voterQualityMeasure.getNumberOfIterations() + "\t\t" + d.toString());
		}
		logger.logVoterQualityLine(writer, voterQualityMeasure, header, VoterName, precisionWriter);
		resultsMap.put(VoterName, voterQualityMeasure);
		contengencyTable.readTrueValues(dataSet, VoterName);

		/*  To log results for the illustrative example */
		//		logIllustartiveExample(dataSet);

	}

	private static void logIllustartiveExample(DataSet dataSet) {
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
