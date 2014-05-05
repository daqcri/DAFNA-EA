package qcri.dafna.dataModel.dataSet.dataSetFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataSet.ClaimWriter;


public class WeatherReadOldWriteNew {

	private int claimId = Globals.lastClaimID;
	private final String weatherDirectory = Globals.directory_UnformattedWeatherFiles;
	private final String newFilesDirectory = Globals.directory_formattedDAFNADataset_WeatherClaims 	+ "/weather-";
	private int numberOfClaims = 0;

	Set<String> allLocations = new HashSet<String>();
	Set<String> alltimes = new HashSet<String>();
	
	
	public void readOldWeatherFileAndWriteNewFiles (String newFileDelimiter) {

		Globals.log("Start reading the Weather Files");

		int numberOfReadFiles = 0;

		String sourceID;

		String location = null;
		String timeStamp = null;
		String temp = null;
		String realFeel = null;
		String humidity = null;
		String pressure = null;
		String visibility = null;

		//writer
		BufferedWriter writer;
		boolean append = false;
		try {
			
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(weatherDirectory));
			for (Path readFilePath : directoryStream) {
				sourceID = readFilePath.getFileName().toString();
				sourceID = sourceID.substring(0, sourceID.indexOf(".txt"));
				writer = ClaimWriter.openFile(newFilesDirectory + sourceID + ".txt", append);
				/**
				 * Scanner is not thread safe, not synchronized. 
				 */
				try (Scanner scanner = new Scanner(readFilePath, StandardCharsets.ISO_8859_1.name())) {
					System.out.println("reading file: " + readFilePath.getFileName());
					scanner.nextLine(); // the first line
					scanner.nextLine(); //  the second line

					if (sourceID.equalsIgnoreCase("myforecast")) {
						continue;
					}
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						Scanner s2 = new Scanner(line);
						s2.useDelimiter( "\t");
						try {
							if (sourceID.equalsIgnoreCase("accuweather")) {
								timeStamp = s2.next();
								location = s2.next();//location
								temp = s2.next(); // F
								realFeel = s2.next();
								s2.next();//wind
								s2.next();// at (mph)
								s2.next();//condition
								humidity = s2.next();//himidity
								s2.next();//dew point
								pressure = s2.next();// inch
								visibility = s2.next();//miles
							} else if (sourceID.equalsIgnoreCase("climaton")) {
								timeStamp = s2.next(); 
								location = s2.next();
								s2.next();// condition
								temp = s2.next();// F C
								temp = temp.trim().split(" ")[0];// to get the F
								realFeel = s2.next(); // F C
								realFeel = realFeel.trim().split(" ")[0];// to get the F
								s2.next();//wind speed
								s2.next();// wind
								s2.next();//dew point
								humidity = s2.next();
								pressure =  s2.next();// inches
								try {
									visibility = s2.next(); // miles
								} catch (NoSuchElementException e) {
									visibility = null;
								}
							} else if (sourceID.equalsIgnoreCase("cnn")) {
								timeStamp = s2.next();
								location = s2.next();//location
								s2.next();//current condition
								temp = s2.next(); // F
								s2.next();//hi
								s2.next();//low
								s2.next();//condition
								pressure = s2.next();// inch
								realFeel = s2.next();// F
								humidity = s2.next();//himidity
								visibility = null;//miles
							} else if (sourceID.equalsIgnoreCase("findlocalweather")) {
								timeStamp = s2.next();
								location = s2.next();//location
								s2.next();// condition
								temp = s2.next(); // F
								humidity = s2.next();//himidity
								s2.next();//wind direction
								s2.next();//wind speed
								pressure = s2.next();// inch
								s2.next();// dewpoint
								realFeel = null;// F
								visibility = null;//miles
							} else if (sourceID.equalsIgnoreCase("foxnews")) {
								timeStamp = s2.next();
								location = s2.next();//location
								s2.next();// condition
								realFeel = s2.next();// F
								pressure = s2.next();// inch
								humidity = s2.next();//himidity
								visibility = s2.next();
								s2.next();// dewpoint
								s2.next();//wind speed
								s2.next();//wind 
								s2.next();//sunrise
								s2.next();//sunset
								temp = s2.next();//F
							} else if (sourceID.equalsIgnoreCase("herald")) {
								timeStamp = s2.next();
								location = s2.next();//location
								s2.next();// condition
								temp = s2.next();// F
								s2.next();// wind
								s2.next();//wind speed
								s2.next();//dewpoint 
								pressure = s2.next();// inch
								s2.next();// wind chill
								humidity = s2.next();//himidity
								visibility = s2.next();//miles
								realFeel = null;
							} else if (sourceID.equalsIgnoreCase("msn")) {
								timeStamp = s2.next();
								location = s2.next();//location
								s2.next();// condition
								temp = s2.next();// F
								realFeel = s2.next();
								pressure = s2.next();// inch
								s2.next();// dewpoint
								humidity = s2.next();//himidity
								visibility = s2.next();//miles
							} else if (sourceID.equalsIgnoreCase("nytimes")) {
								timeStamp = s2.next();
								location = s2.next();//location
								temp = s2.next().trim().split(" ")[0];// F
								s2.next();// condition
								humidity = s2.next();//himidity
								pressure = s2.next();// inch
								visibility = s2.next();//miles
								realFeel = null;
							} else if (sourceID.equalsIgnoreCase("unisys")) {
								timeStamp = s2.next();
								location = s2.next();//location
								temp = s2.next().trim().split(" ")[0];// F
								s2.next();// dewpt
								humidity = s2.next();//himidity
								s2.next();//wind
								s2.next();//wind speed
								s2.next();//wind chill
								pressure = s2.next().trim().split(" ")[1];// inch
								visibility = s2.next();//miles
								realFeel = null;
							} else if (sourceID.equalsIgnoreCase("uswx")) {
								timeStamp = s2.next();
								location = s2.next();//location
								s2.next();// city and timestamp
								temp = s2.next().trim().split(" ")[0];// F C
								s2.next();// dewpt
								humidity = s2.next();//himidity
								pressure = s2.next().trim().split(" ")[0];// inch mb
								s2.next();//wind speed
								s2.next();//wind dir
								s2.next();//ceiling
								visibility = s2.next().trim().split(" ")[0];//miles km
								realFeel = null;
							} else if (sourceID.equalsIgnoreCase("washingtonpost")) {
								timeStamp = s2.next();
								location = s2.next();//location
								s2.next();// condition
								temp = s2.next().trim().split(" ")[0];// F C
								realFeel = s2.next().trim().split(" ")[0];// F C
								s2.next();// windspeed
								s2.next();// wind
								pressure = s2.next();// inch
								try {
									humidity = s2.next();
								} catch (NoSuchElementException e) {
									humidity = null;
								}
								visibility =null;
							} else if (sourceID.equalsIgnoreCase("weatherbug")) {
								timeStamp = s2.next();
								location = s2.next();//location
								temp = s2.next();// F 
								s2.next();// wind speed
								s2.next();// lo
								s2.next();// rain
								s2.next();// hi
								s2.next();// gust
								s2.next();// wind chill
								humidity = s2.next();
								realFeel = null;
								pressure = null;
								visibility =null;
							} else if (sourceID.equalsIgnoreCase("weatherforyou")) {
								timeStamp = s2.next();
								location = s2.next();//location
								s2.next(); // condition
								temp = s2.next().trim().split(" ")[0];// F C
								humidity = s2.next();
								s2.next();// wind 
								s2.next();// wind speed
								s2.next();// wind gust
								pressure = s2.next();//in
								s2.next();// dewpoint
								s2.next();// wind chill
								try {
									visibility = s2.next();// SM/miles (miles)
								} catch (NoSuchElementException e) {
									visibility = null;
								}
								realFeel = null;
							} else if (sourceID.equalsIgnoreCase("weather_gov")) {
								timeStamp = s2.next();
								location = s2.next();//location
								s2.next(); // condition
								temp = s2.next().trim().split(" ")[0];// F C
								humidity = s2.next();
								s2.next();// wind 
								s2.next();// wind speed
								pressure = s2.next().trim().split(" ")[0];//in/mb
								s2.next();// dewpoint
								s2.next();// wind chill
								try {
									visibility = s2.next();// SM/miles (miles)
								} catch (NoSuchElementException e) {
									visibility = null;
								}
								realFeel = null;
							} else if (sourceID.equalsIgnoreCase("wunderground")) {
								timeStamp = s2.next();
								location = s2.next();//location
								temp = s2.next();// F 
								s2.next(); // condition
								humidity = s2.next();
								s2.next();// wind speed
								s2.next();// wind variable
								visibility = s2.next();// miles
								s2.next();// dewpoint
								s2.next();// Precipitation
								s2.next();// air quality
								pressure = s2.next();//in
								realFeel = null;
							} else if (sourceID.equalsIgnoreCase("yahoo")) {
								timeStamp = s2.next();
								location = s2.next();//location
								temp = s2.next();// F 
								humidity = null;
								visibility = null;
								pressure = null;//in
								realFeel = null;
							} else if (sourceID.equalsIgnoreCase("aol")) {
//								(Timestamp)	(Location)	Current conditions	(temperature) ( °)	
//								feels like ( °)	High	Low	Precipitation (%)	Relative Humidity (%)	
//								Barometer (atm)	Dew Point (°)	Visibility (Miles)	(Wind Direction)	Wind Speed (mph)

								timeStamp = s2.next();
								location = s2.next();//location
								s2.next(); // condition
								temp = s2.next();// F 
								realFeel = s2.next();
								s2.next();// hi
								s2.next();// lo
								s2.next();//Precipitation
								humidity = s2.next();
								pressure = s2.next().split(" ")[0];//in
								s2.next();// dewpoint
								visibility = s2.next();// miles
							}
							s2.close();
							location = getLocation(location, sourceID);
							
							addClaims(sourceID, timeStamp, temp, realFeel, humidity, pressure, writer, location, visibility);
						} catch (NoSuchElementException e) {e.printStackTrace();}
					}
					scanner.close();

					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						writer.close();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
					numberOfReadFiles++;
				}
			}
		} catch (IOException e)  {
			e.printStackTrace();
		}
		Globals.log(numberOfReadFiles + " readed files.\n" + numberOfClaims + " extracted claims.");
		Globals.log("Last Claim ID = " + claimId);
		
		System.out.println("locations:" + allLocations.size() + ", times" + alltimes.size());
		ArrayList<String> ll = new ArrayList<String>(allLocations);
		Collections.sort(ll);
		for (String s : ll) {
			System.out.println(s);
		}
		ArrayList<String> tl = new ArrayList<String>(alltimes);
		Collections.sort(tl);
		for (String t:tl) {
			System.out.println(t);
		}
	}

	private String getLocation(String location, String sourceID) {
		location = location.toLowerCase();
		location = location.trim();
		String parts[];
		if (sourceID.equalsIgnoreCase("accuweather")) {
			parts = location.split(" ");
			int size = parts.length;
//			String code = parts[size-1];
//			int test = new Integer(code);
			location = "";
			for (int i = 0 ; i < size - 1; i++) {
				location = location + parts[i];
			}
		} else if (sourceID.equalsIgnoreCase("wunderground") || sourceID.equalsIgnoreCase("yahoo")
				|| sourceID.equalsIgnoreCase("washingtonpost") || sourceID.equalsIgnoreCase("weatherbug")
				||sourceID.equalsIgnoreCase("msn")  || sourceID.equalsIgnoreCase("unisys")
				||sourceID.equalsIgnoreCase("herald") || sourceID.equalsIgnoreCase("foxnews") 
				|| sourceID.equalsIgnoreCase("climaton") || sourceID.equalsIgnoreCase("cnn")) {
			parts = location.split(",");
			if (parts.length != 2) {
				if (sourceID.equals("yahoo")) 
					System.out.println("------> LOCATION FORMAT PTOBLEM : " + location + " in source: " + sourceID);
			}
			location = parts[0];
		} else if (sourceID.equalsIgnoreCase("findlocalweather")) {
			parts = location.split(" ");
			int size = parts.length;
			String code = parts[size-1];
			if (code.length() == 2) {
				location = "";
				for (int i = 0 ; i < size - 1; i++) {
					location = location + parts[i];
				}
			} else {
				System.out.println("------> LOCATION FORMAT PTOBLEM : " + location + " in source: " + sourceID);
			}
		} else if (sourceID.equalsIgnoreCase("nytimes")) {
			// nothing
		} else if (sourceID.equalsIgnoreCase("uswx")) {
			int index = location.indexOf("(");
			if (index != -1) {
				location = location.substring(0, index);
			} else {
				System.out.println("------> LOCATION FORMAT PTOBLEM : " + location + " in source: " + sourceID);
			}
		} else if (sourceID.equalsIgnoreCase("weatherforyou")) {
			parts = location.split(",");
			if (parts.length != 3) {
					System.out.println("------> LOCATION FORMAT PTOBLEM : " + location + " in source: " + sourceID);
			}
			location = parts[0];
		} else if (sourceID.equalsIgnoreCase("weather_gov")) {
			if (location.startsWith("nws")) {
				location = location.substring(3).trim();
			}
			if (location.contains(",")) {
				location = location.substring(0, location.indexOf(","));
			} else {
				parts = location.split(" ");
				int size = parts.length;
				if (parts[size-1].length() == 2) {
					location = "";
					for (int i = 0 ; i < size - 1; i++) {
						location = location + parts[i];
					}
				} else {
					System.out.println("------> LOCATION FORMAT PTOBLEM : " + location + " in source: " + sourceID);
				}
			}
		} else if (sourceID.equalsIgnoreCase("aol")) {
			parts = location.split(",");
			if (parts.length != 4) {
				System.out.println("------> LOCATION FORMAT PTOBLEM : " + location + " in source: " + sourceID);
			} else {
				location = parts[2];
			}
		}
		return location;
	}
	private void addClaims(String sourceID, String timeStamp, String temp,
			String realFeel, String humidity, String pressure,
			BufferedWriter writer, String location, String visibility) {
		location = location.replaceAll(" ", "");
		location = location.replaceAll(",", "");
		timeStamp = timeStamp.toLowerCase();
		String day = timeStamp.substring(0, timeStamp.indexOf(" "));
		timeStamp = timeStamp.substring(timeStamp.indexOf(" ")).trim();
		String month = timeStamp.substring(0, timeStamp.indexOf(" "));
		timeStamp = timeStamp.substring(timeStamp.indexOf(" ")).trim();
		String dayNum = timeStamp.substring(0, timeStamp.indexOf(" "));
		timeStamp = timeStamp.substring(timeStamp.indexOf(" ")).trim();
		String hour = timeStamp.substring(0, timeStamp.indexOf(":"));
		
		String time = day + month + dayNum + "-" + hour;
		alltimes.add(time);
		String objectId = location + time;
		boolean lineWriten;

		allLocations.add(location);
		if (temp != null && !temp.trim().equals("")) {
			temp = temp.trim();
			lineWriten = ClaimWriter.writeClaim(writer, claimId, objectId, Globals.weatherDataSet_Tempreture, 
					temp, timeStamp, sourceID, Globals.delimiterText);
			if (lineWriten) {claimId ++;numberOfClaims ++;}
		}

		if (realFeel != null && !realFeel.trim().equals("")) {
			realFeel = realFeel.trim();
			lineWriten = ClaimWriter.writeClaim(writer, claimId, objectId, Globals.weatherDataSet_ReafFeel, 
					realFeel, timeStamp, sourceID, Globals.delimiterText);
			if (lineWriten) {claimId ++;numberOfClaims ++;}
		}

		if (humidity != null && !humidity.trim().equals("")) {
			humidity = humidity.trim();
			lineWriten = ClaimWriter.writeClaim(writer, claimId, objectId, Globals.weatherDataSet_Humidity, 
					humidity, timeStamp, sourceID, Globals.delimiterText);
			if (lineWriten) {claimId ++;numberOfClaims ++;}
		}

		if (pressure != null && !pressure.trim().equals("")) {
			pressure = pressure.trim();
			lineWriten = ClaimWriter.writeClaim(writer, claimId, objectId, Globals.weatherDataSet_Pressure, 
					pressure, timeStamp, sourceID, Globals.delimiterText);
			if (lineWriten) {claimId ++;numberOfClaims ++;}
		}

		if (visibility != null && !visibility.trim().equals("")) {
			visibility = visibility.trim();
			lineWriten = ClaimWriter.writeClaim(writer, claimId, objectId, Globals.weatherDataSet_Visibility, 
					visibility, timeStamp, sourceID, Globals.delimiterText);
			if (lineWriten) {claimId ++;numberOfClaims ++;}
		}
	}
}