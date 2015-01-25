package qcri.dafna.explaination;


public class LaunchExplaination {
	public static void main(String args[])
	{
<<<<<<< HEAD
		TextGeneration t = new TextGeneration();
		//String[] metrics = {"0.5256384699966441","0.05162170698017994","0.05046583432262075","0.05200505818102974","75.0","25.0","24.0","3.0","85.77981651376147","100.0","69.6969696969697","87.5","TRUE"};
		String[] metrics = {"0.5103789981355668","0.05054878730020672","0.05026218084207576","0.0512463476752496","57.142857142857146","42.857142857142854","14.0","2.0","53.669724770642205","50.0","26.83982683982684","50.0","FALSE"};
		double[] scores = {0.10548401989643952,0.005206719276570372,0.06174001196074881,0.10080315039443996,0.2772473330874408,0.2772473330874408,0.10295689348058405,0.168638187140844,0.09440913844421205,0.49593058170899856,0.008459286566193196,-0.00638440146804976};
		String[] header = {"CLAIM-ID", "DI", "VALUE", "SOURCE ID", "RUN ID"};
		// metrics - entire row of file
		// scores - Relief Scores
		// header - claimid, di, value string, source-id, run-id
		String temp = t.generateText(metrics, scores, header);
		System.out.println(temp);
=======
		launchAllegator();
	}

	public static void launchAllegator()
	{
		double toleranceFactor = 0.01; // 0.1 max, 0 min
		String dataSetDirectory = Globals.directory_formattedDAFNADataset_Books_Folder + "/claims";
		String groundTruthDir = Globals.directory_formattedDAFNADataset_Books_Folder + "/truth";
		String outputPath = Globals.directory_formattedDAFNADataset_Books_Folder + "/experimentResult";
		String delim = ",";
		DataSet ds = ExperimentDataSetConstructor.readDataSet(dataSetDirectory, toleranceFactor, groundTruthDir, outputPath, delim);
		
		VoterQualityMeasures q = null;
		boolean convergence100 = false;
		boolean profileMemory = false;
		double cosineSimDiff = 0.001;  // 0-1
		double startingTrust = 0.8;  // 0-1
		double startingConf = 1;  // 0-1 
		double startingErrorFactor = 0.4;  // 0-1
		VoterParameters params = new VoterParameters(cosineSimDiff, startingTrust, startingConf, startingErrorFactor);
		String[] algoParams = new String[2];
		algoParams[0] = "0.5";
		algoParams[1] = "0.1";
		
		String confidenceFilePath = "/home/dalia/Desktop/Backups/results/4/Confidences.csv";
		String trustWorthinessFilePath = "/home/dalia/Desktop/Backups/results/4/Trustworthiness.csv";
		//String claimID = "24326";
		//Allegate algo5 = new Allegate(ds, params, "Truth Finder", algoParams, claimID, confidenceFilePath, trustWorthinessFilePath);
		//q = algo5.launchVoter(convergence100, profileMemory);
		
		for(int i = 24432; i<26436; i++){
			ds = ExperimentDataSetConstructor.readDataSet(dataSetDirectory, toleranceFactor, groundTruthDir, outputPath, delim);
			String claimID = String.valueOf(i);
			MetricsGenerator algo5 = new MetricsGenerator(ds, params, claimID, confidenceFilePath, trustWorthinessFilePath);
			q = algo5.launchVoter(convergence100, profileMemory);
		}
		
		try {
			Dataset data = FileHandler.loadDataset(new File("/home/dalia/Desktop/explaination.data"),11, ",");
			RELIEF reliefF = new RELIEF();
			reliefF.build(data);
			for (int i = 0; i < reliefF.noAttributes(); i++)
	            System.out.println(reliefF.score(i));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    			
		//System.out.println(q.getPrecision());
		//System.out.println(q.getRecall());
		//System.out.println(q.getAccuracy());
		//System.out.println(q.getSpecificity());
		
		String confidenceResultFile = outputPath + System.getProperty("file.separator") + "Confidences.csv";
		BufferedWriter confidenceWriter;
		try {
			confidenceWriter = Files.newBufferedWriter(Paths.get(confidenceResultFile), 
					Globals.FILE_ENCODING, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			CSVWriter csvWriter = new CSVWriter(confidenceWriter, ',');

			/*header*/
			writeConfidenceResult(csvWriter, "ClaimID", "Confidence", "IsTrue", "BucketId");
		for (List<ValueBucket> bList : ds.getDataItemsBuckets().values()) {
			for (ValueBucket b : bList) {
				for (SourceClaim claim :  b.getClaims()) {
					writeConfidenceResult(csvWriter, String.valueOf(claim.getId()), 
							String.valueOf(b.getConfidence()), String.valueOf(claim.isTrueClaimByVoter()), String.valueOf(b.getId()));
				}
			}
		}
		confidenceWriter.close();
		} catch (IOException e) {
			System.out.println("Cannot write the confidence results");
			e.printStackTrace();
		}
	}
	
	private static void writeConfidenceResult(CSVWriter writer, String claimId,	String confidence, String trueOrFalse, String bucketValue) {
		String [] lineComponents = new String[]{claimId, confidence, trueOrFalse, bucketValue};
		writer.writeNext(lineComponents);
>>>>>>> 2394d422a53a224fe04091786301b8623cd9d9ad
	}
}
