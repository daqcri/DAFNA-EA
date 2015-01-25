package qcri.dafna.explaination;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextGeneration {
	
	public String generateText(String[] metrics, double[] scores, String[] header){
		
		String headerText = String.format("The claim \"%s\" belongs to the data item \"%s\". Among %s distinct values provided by %s sources for this data item, the confidence of the value \"%s\" provided by \"%s\" is %s and it has been chosen as %s according to the algorithm %s because :\n", header[0], header[1], Math.round(Double.valueOf(metrics[7])), Math.round(Double.valueOf(metrics[6])) ,header[2], header[3], round(metrics[0], 4), metrics[12], header[4]);
		List<String> explanationText = new ArrayList<String>();
		String finalExplanation = headerText;
		
		if(Double.valueOf(metrics[6]) == 1.0)
		{
			explanationText.add("There is no conflicting value for this Data Item");
			finalExplanation = finalExplanation+explanationText.get(0);
			return finalExplanation;
		}
		
		if(metrics[12] == "TRUE")
		{
			// Add NbSS vs NbC
			if(Double.valueOf(metrics[4]) > 50.0)
				explanationText.add(String.format("More than half of the sources (%s %%) are supporting this value whereas %s %% of the sources disagree on this value.\n", Math.round(Double.valueOf(metrics[4])), Math.round(Double.valueOf(metrics[5]))));
			if(Double.valueOf(metrics[4]) < 50.0)
				explanationText.add(String.format("%s %% of the sources are supporting this value whereas %s %% of the sources disagree on this value.\n", Math.round(Double.valueOf(metrics[4])), Math.round(Double.valueOf(metrics[5]))));
			if(Double.valueOf(metrics[4]) == 50.0)
				explanationText.add("Half of the sources are supporting this value and half of the sources disagree on this value.\n");
			
			//Add LocalTrust and globalTrust
			explanationText.add(String.format("The trustworthiness of the source (%s) claiming this value is higher than %s %% of all the sources in this dataset and higher than %s %% of all sources providing a value for this data item.\n", round(metrics[1],4), Math.round(Double.valueOf(metrics[10])), Math.round(Double.valueOf(metrics[11]))));
			
			//Add Ts vs maxTrust
			if(Double.valueOf(metrics[1]) < Double.valueOf(metrics[3]))
				explanationText.add(String.format("Although the trustworthiness of the source (%s) claiming this value is not the highest (%s), but this value is also supported by sources with higher trustworthiness.\n", round(metrics[1],4), round(metrics[3],4)));
			else
				explanationText.add(String.format("The trustworthiness of the source (%s) claiming this value is the highest among all sources for this value.\n", round(metrics[1],4)));
			
			//Add cvGlobal and cvLocal
			explanationText.add(String.format("The confidence value of this value is %s, which is higher than %s %% of all confidence values in this dataset and higher than %s %% of all values for this data item.\n", round(metrics[0],4), Math.round(Double.valueOf(metrics[8])), Math.round(Double.valueOf(metrics[9]))));
			
			//Add minTrust
			explanationText.add(String.format("Among the sources providing this value, even the least trustworthy source has %s as trustworthiness score.\n", round(metrics[2],4)));
		}
		
		else if(metrics[12] == "FALSE")
		{
			// Add NbSS vs NbC
			if(Double.valueOf(metrics[4]) < 50.0)
				explanationText.add(String.format("More than half of the sources (%s %%) disagree on this value whereas %s %% of the sources are supporting this value.\n", Math.round(Double.valueOf(metrics[5])), Math.round(Double.valueOf(metrics[4]))));
			if(Double.valueOf(metrics[4]) > 50.0)
				explanationText.add(String.format("%s %% of the sources disagree on this value whereas %s %% of the sources are supporting this value.\n", Math.round(Double.valueOf(metrics[5])), Math.round(Double.valueOf(metrics[4]))));
			if(Double.valueOf(metrics[4]) == 50.0)
				explanationText.add("Half of the sources disagree on this value and half of the sources are supportingg this value.\n");
						
			//Add LocalTrust and global Trust
			explanationText.add(String.format("The trustworthiness of the source (%s) claiming this value is lower than %s %% of all the sources in this dataset and lower than %s %% of all sources providing a value for this data item.\n", round(metrics[1], 4), Math.round(Double.valueOf(100.0 - Double.valueOf(metrics[10]))) ,Math.round(Double.valueOf(100.0-Double.valueOf(metrics[11])))));
					
			//Add Ts vs minTrust
			if(Double.valueOf(metrics[1]) > Double.valueOf(metrics[2]))
				explanationText.add(String.format("Alhough the trustworthiness of the source (%s) claiming this value is not the minimum (%s), but this value is also supported by sources with lower trustworthiness.\n", round(metrics[1], 4), round(metrics[2],4)));
			else
				explanationText.add(String.format("The trustworthiness of the source (%s) claiming this value is the lowest among all sources for this value.\n", round(metrics[1], 4)));
						
			//Add cvGlobal na cvLocal
			explanationText.add(String.format("The confidence value of this value is %s, which is lower than %s %% of all confidence values in this dataset and lower than %s %% of all values for this data item.\n", round(metrics[0], 4), Math.round(Double.valueOf(100.0 - Double.valueOf(metrics[8]))), Math.round(Double.valueOf(100.0 - Double.valueOf(metrics[9])))));
						
			//Add maxTrust
			explanationText.add(String.format("Among the sources providing this value, the most trustworthy source has only %s as trustworthiness score.\n", round(metrics[3],4)));
		}
		
		List<Double> scoresList = new ArrayList<Double>();
		for(double place : scores)
			scoresList.add(place);
		
		double maxNow;
		int indexOfMax;
		int i = 0;
		int explanationAdded = 1;
		finalExplanation = finalExplanation+"Top Explanation "+explanationAdded+": "+explanationText.get(3);
		explanationAdded++;
		
		boolean mark = false;
		while(i < scores.length){
			maxNow = Collections.max(scoresList);
			indexOfMax = scoresList.indexOf(maxNow);
			scoresList.set(indexOfMax, -1*Double.MAX_VALUE);
			i++;
			switch(indexOfMax){
			case 5:
			case 4:
				if(mark == false)
				{
					finalExplanation = finalExplanation+"Top Explanation "+explanationAdded+": "+explanationText.get(0);
					explanationAdded++;
					mark = true;
				}
				break;
			case 11:
				finalExplanation = finalExplanation+"Top Explanation "+explanationAdded+": "+explanationText.get(1);
				explanationAdded++;
				break;
			case 3:
				finalExplanation = finalExplanation+"Top Explanation "+explanationAdded+": "+explanationText.get(2);
				explanationAdded++;
				break;
			//case 8:
			//	finalExplanation = finalExplanation+"Top Explanation "+explanationAdded+": "+explanationText.get(3);
			//	explanationAdded++;
			//	break;
			case 2:
				finalExplanation = finalExplanation+"Top Explanation "+explanationAdded+": "+explanationText.get(4);
				explanationAdded++;
				break;
			default:
				continue;
			}
		}
		return finalExplanation;
	}
	
	private static double round(String value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

}
