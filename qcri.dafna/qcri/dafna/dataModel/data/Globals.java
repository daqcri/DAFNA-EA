package qcri.dafna.dataModel.data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Globals {
	static public HashMap<Integer, String> sourceTruthMethod = new HashMap<Integer, String>();
	static {
		sourceTruthMethod.put(-1, "No Control");
		sourceTruthMethod.put(1, "Uniform");
		sourceTruthMethod.put(2, "Fully Pessimistic");
		sourceTruthMethod.put(3, "Fully Optimistic");
		sourceTruthMethod.put(4, "80-Pessimistic");

		sourceTruthMethod.put(5, "Exponential");

		sourceTruthMethod.put(6, "80-Optimistic");
	}
	static public HashMap<Integer, String> distinctValueMethodMethod = new HashMap<Integer, String>();
	static {
		distinctValueMethodMethod.put(1, "Uniform");
		distinctValueMethodMethod.put(2, "20-80");
		distinctValueMethodMethod.put(3, "Exponential");
	}
	public static final int controlSourcesDependency_Nocontrol = -1;
	public static final int controlSourcesDependency_control = 1;

	public static final int valueSimilarity_similar = -1;
	public static final int valueSimilarity_dissSimilar = 1;

	public static final String chartsFoleder = "/home/dalia/DAFNAData/experiments/charts/chartsCollection/";
	public static final String finalChartsFolder = "/home/dalia/DAFNAData/experiments/charts/chartsCollection/final";

	public static final String voterVoting = "Voting";
	public static final String voterTruthFinder = "Truth Finder";
	public static final String voterCosine = "Cosine";
	public static final String voter2Estimates = "2-Estimates";
	public static final String voter3Estimates = "3-Estimates";
	public static final String voterSimpleLCA = "SimpleLCA";
	public static final String voterGuessLCA = "GuessLCA";
	public static final String voterAccuSim = "AccuSim";// "AccuCopy T T"
//	public static final String voterCopyTF = "AccuCopy T F";
	public static final String voterAccu = "Accu";// "AccuCopy F T"
	public static final String voterDepen = "Depen";// "AccuCopy F F"
	public static final String voterAccuNoDep = "AccuNoDep";// new
	public static final String voterLTM = "LTM";
	public static final String voterMLE = "MLE";// Maximum likelihood estimation

	public static final String syntheticDataSet_Property = "property";
	public static final String syntheticDataSet_BooleanProperty = "booleanProperty";

	public static final String flightDataSet_ExpectedDepartureTime = "ExpectedDepartureTime";
	public static final String flightDataSet_ActualDepartureTime = "ActualDepartureTime";
	public static final String flightDataSet_DepartureGate = "DepartureGate";
	public static final String flightDataSet_ExpectedArrivalTime = "ExpectedArrivalTime";
	public static final String flightDataSet_ActualArrivalTime = "ActualArrivalTime";
	public static final String flightDataSet_ArrivalGate = "ArrivalGate";

	public static final String weatherDataSet_Tempreture = "Temperature";
	public static final String weatherDataSet_ReafFeel = "RealFeel";
	public static final String weatherDataSet_Humidity = "Humidity";
	public static final String weatherDataSet_Pressure = "Pressure";
	public static final String weatherDataSet_Visibility = "Visibility";

	private static String name = "/home/dalia";
	private static final String directory_Log = name + "/DAFNAData/experiments";
	public static final String directory_datasetLog = directory_Log + "/datasetLog";
	public static final String directory_voterLog = directory_Log + "/voterLog";
	public static final String directory_dependencyReport = directory_Log + "/dependencyReport";

	private static final String directory_formattedDAFNADataset = name + "/DAFNAData/formatted";//"/home/dalia/DAFNAData/formatted";
	private static final String directory_UN_formattedDAFNADataset = name + "/DAFNAData/unformatted";
	public static final String directory_formattedDAFNADataset_SourcesTrueClaims = directory_formattedDAFNADataset + "/logs/sourcesTrueClaims";

	// Flight dataSet
	public static final String directory_UnformattedFlightFiles = directory_UN_formattedDAFNADataset + "/clean_flight";
	public static final String directory_formattedDAFNADataset_Flight = directory_formattedDAFNADataset +  "/flights";
	public static final String directory_formattedDAFNADataset_FlightFolder = directory_formattedDAFNADataset_Flight +  "/claims";
	public static final String directory_formattedDAFNADataset_FlightTruthFolder = directory_formattedDAFNADataset_Flight + "/flight_truth";
	public static final Charset Flight_DataSet_FILE_ENCODING = StandardCharsets.ISO_8859_1;
	// Weather dataSet
	public static final String directory_UnformattedWeatherFiles = directory_UN_formattedDAFNADataset + "/weather";
	public static final String directory_formattedDAFNADataset_WeatherFolder = directory_formattedDAFNADataset +  "/weather";
	public static final String directory_formattedDAFNADataset_WeatherClaims = directory_formattedDAFNADataset_WeatherFolder +  "/claims";
	public static final String directory_formattedDAFNADataset_WeatherTruthFolder = directory_formattedDAFNADataset_WeatherFolder + "/truth";

	// Books dataSet
	public static final String directory_UnformattedBooksFiles = directory_UN_formattedDAFNADataset + "/Book/Books";
	public static final String directory_formattedDAFNADataset_Books_Folder = directory_formattedDAFNADataset + "/Books";
	public static final String directory_formattedDAFNADataset_Books_Claims_Folder = directory_formattedDAFNADataset + "/Books/claims";
	public static final String directory_formattedDAFNADataset_BooksFolder_SingleClaimValue = directory_formattedDAFNADataset + "/Books/claimsSingleValue";
	public static final String directory_formattedDAFNADataset_BooksFolder_MLE = directory_formattedDAFNADataset + "/Books/claimsMLE";
	public static final String directory_formattedDAFNADataset_BooksTruthFolder = directory_formattedDAFNADataset + "/Books/truth";
	public static final String bookDataSet_AuthorsNamesList = "AuthorsNamesList";
	public static final String bookDataSet_BookName = "BookName";


	// synthetic boolean dataSet
	public static final String directory_syntheticDataSet_Boolean_base = directory_formattedDAFNADataset +  "/syntheticBoolean";
	public static final String directory_syntheticDataSet_Boolean = directory_syntheticDataSet_Boolean_base +  "/claims";
	public static final String directory_syntheticDataSet_Boolean_Truth = directory_syntheticDataSet_Boolean_base +  "/truth";
	public static final String directory_syntheticDataSet_BooleanTrueAndFalse_base = directory_formattedDAFNADataset +  "/syntheticBooleanTrueAndFasle";
	public static final String directory_syntheticDataSet_BooleanTrueAndFalse = directory_syntheticDataSet_BooleanTrueAndFalse_base +  "/claims";
	public static final String directory_syntheticDataSet_BooleanTrueAndFalse_Truth = directory_syntheticDataSet_BooleanTrueAndFalse_base +  "/truth";

	public static final String directory_syntheticDataSetMainDirectory = directory_formattedDAFNADataset +  "/synthetic";

	// Biographies dataSet
	public static final String directory_UnformattedBiographiesFiles = directory_UN_formattedDAFNADataset + "/Biographies/claims";
	public static final String directory_formattedDAFNADataset_BiographiesFolder = directory_formattedDAFNADataset +  "/Biographies";
	public static final String directory_formattedDAFNADataset_BiographiesClaimsFolder = directory_formattedDAFNADataset +  "/Biographies/claims";
	public static final String directory_formattedDAFNADataset_BiographiesTruthFolder = directory_formattedDAFNADataset + "/Biographies/truth";
	public static final String biographiesDataSet_Born = "Born";
	public static final String biographiesDataSet_Died = "Died";
	public static final String biographiesDataSet_Spouse = "Spouse";
	public static final String biographiesDataSet_Children = "Children";
	public static final String biographiesDataSet_Father = "Father";
	public static final String biographiesDataSet_Mother = "Mother";
	public static final Charset Biography_DataSet_FILE_ENCODING = StandardCharsets.UTF_8;

	//population dataSet
	public static final String directory_FormattedPopulationFolder = directory_formattedDAFNADataset + "/Population";
	public static final String directory_UnformattedPopulationFiles = directory_UN_formattedDAFNADataset + "/Population/claims";
	public static final String directory_formattedDAFNADataset_PopulationClaimsFolder = directory_FormattedPopulationFolder +  "/claims";
	public static final String directory_formattedDAFNADataset_PopulationTruthFolder = directory_FormattedPopulationFolder + "/truth";
	public static final String populationDataSet_Population = "Population";

	// Population Biography 
	public static final String directory_FormattedPopulationBiographyFolder = directory_formattedDAFNADataset + "/PopulationBiography";
	public static final String directory_formattedDAFNADataset_PopulationBiographyClaimsFolder = directory_FormattedPopulationBiographyFolder +  "/claims";
	public static final String directory_formattedDAFNADataset_PopulationBiographyTruth = directory_FormattedPopulationBiographyFolder +  "/truth";
	public static final String directory_UnformattedPopulationBiographyFiles = directory_UN_formattedDAFNADataset + "/PopulationBiography/claims";

	// FreeBase DataSet
	public static final String directory_formattedDAFNADataset_FreebaseFolder = directory_formattedDAFNADataset +  "/freebase/claims";

	public static final String peopleDataSet_dateOfBirth = biographiesDataSet_Born;
	public static final String peopleDataSet_dateOfDeith = biographiesDataSet_Died;
	public static final String peopleDataSet_countryOfBirth = "countryOfBirth";
	public static final String peopleDataSet_Children = biographiesDataSet_Children;
	public static final String peopleDataSet_parents = "parents";
	public static final String peopleDataSet_spouses = "spouse_s";
	public static final String peopleDataSet_height = "height";
	public static final String peopleDataSet_weight = "weight";

	// IMDB dataset
	public static final String directory_formattedDAFNADataset_IMDBactors = directory_formattedDAFNADataset +  "/IMDBactors/claims";
	public static final String directory_UnformattedIMDBactorsDump = directory_UN_formattedDAFNADataset + "/imdbactors/claims";

	//Illustativ Example dataset
	public static final String directory_formattedDAFNADataset_IllustativeExample = directory_formattedDAFNADataset +  "/example";
	
	public static double starting_Confidence = 5.0;
	public static double starting_trustworthiness = 0.9;
	public static double tolerance_Factor = 0.01;
	public static final int lastClaimID = 13847311;// after the IMDB 13847311 // after IMDB secnd time 14953653=>LAST
	// after freebase => 11634624
	//  After reading the flight files ( on December 2nd 3012) => 2871621
	//  After reading the books files => 2904879
	// after reading the biography file => 8687024
	// after writing the book with single property value => 8736444
	// after writing the boolean synthetic dataSet 
	//	          (both true only and true and false)=> 8780643
	public static final int MaxFileEntriesCount = 75000;


	// The delimiter used in the data file
	public static final String delimiterText =  "|\t";//",";//
	// The regular expression used to detect the delimiter
	public static final String delimiterRegularExpression ="\\|\\t";//",";//
	public static final String cleanedListDelimiter = ";";
	public static final Charset FILE_ENCODING = StandardCharsets.UTF_8;//.ISO_8859_1;

//	public static char CSVDelimiter = ',';

	public static final int iterationCount = 50;//50


	/* -------------------- database ---------------------------*/

	/**
	 * All Data Items have the same number of different claimed values by the set of sources
	 */
	public static final int db_differentValuesMethod_uniform = 0;
	/**
	 * 80% of the Data Items have 20% of the provided number of different values.
	 * 20% of the Data Items have 80% of the provided number of different values.
	 */
	public static final int db_differentValuesMethod_80_20_simple = 0;
	/**
	 * Data Items different values are exponentially distributed.
	 */
	public static final int db_differentValuesMethod_exponential= 0;

	/**
	 * No control on the number of true values provided by each source.
	 */
	public static final int db_control_source_true_value_count_method_not_used = 0;
	/**
	 * Number of true values provided by each source are all equal.
	 */
	public static final int db_control_source_true_value_count_method_not_uniform = 0;
	/**
	 * 80% of the sources, provide always true claims.
	 * 20% of the sources, provide always false claims.
	 */
	public static final int db_control_source_true_value_count_method_not_80_20_simple = 0;
	/**
	 * 80% of the sources, provide 20% true claims among all their claims.
	 * 20% of the sources, provide 80% true claims among all their claims.
	 */
	public static final int db_control_source_true_value_count_method_not_80_20_complex = 0;

	public static void log(String s) {
		System.out.println(s);
	}

}
