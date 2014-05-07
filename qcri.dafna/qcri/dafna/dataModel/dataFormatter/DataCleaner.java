package qcri.dafna.dataModel.dataFormatter;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;

import com.att.research.solomon.format.ISBNCleaner;
import com.att.research.solomon.format.PersonListCleaner;
import com.att.research.solomon.format.TextCleaner;

public class DataCleaner {

	/**
	 * If the value cannot be cleaned into the given data type. it is cleaned as a String.
	 * @param value
	 * @param dataType
	 * @return
	 */
	public static Object clean(String value, ValueType dataType) {
		if (dataType.equals(ValueType.DATE)) {
			return cleanDate(value);
		}
		if (dataType.equals(ValueType.TIME)) {
			return cleanTime(value);
		}
		if (dataType.equals(ValueType.ListNames)) {
			return cleanListOfNames(value);
		}
		if (dataType.equals(ValueType.Name)) {
			try {
				return PersonListCleaner.INSTANCE.clean(value.replaceAll(",", " "));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (dataType.equals(ValueType.STRING)) {
			try {
				return ((String)TextCleaner.INSTANCE.clean(value)).trim();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (dataType.equals(ValueType.ISBN)) {
			try {
				return ((String)ISBNCleaner.INSTANCE.clean(value)).trim();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		if (dataType.equals(ValueType.BOOLEAN)) {
			if (value.equalsIgnoreCase("true")) {
				return new Boolean(true);
			} else {
				return new Boolean(false);
			}
		}
		if (dataType.equals(ValueType.LONG)) {
			Long l = Long.valueOf(value);
			return l;
		}
		if (dataType.equals(ValueType.FLOAT)) {
			try {
			Float f = Float.valueOf(value);
			return f;
			} catch (NumberFormatException e) {
				return (Object)value;
			}
		}
		

		return (Object)value;
	}

	private static Object cleanListOfNames(String value) {
		String cleanedNames;
		try {
			cleanedNames = PersonListCleaner.INSTANCE.clean(value);
			cleanedNames = cleanedNames.trim();
			cleanedNames = cleanedNames.replaceAll("_", "-");
			cleanedNames = cleanedNames.replaceAll("editor-", "");
			if (cleanedNames.endsWith(";")) {
				cleanedNames = cleanedNames.substring(0,cleanedNames.length()-1);
			}
			String[] names = cleanedNames.split(Globals.cleanedListDelimiter);
			cleanedNames = "";
			for (String s : names) {
				cleanedNames = cleanedNames + Globals.cleanedListDelimiter + s.trim();
			}
			cleanedNames = cleanedNames.substring(Globals.cleanedListDelimiter.length());
			return cleanedNames;
		} catch (Exception e) {}
		return value;
	}
	/**
	 * The time is extracted from the string value. if cannot extract it. the value is cleaned as a string and returned.
	 * @param stringValue
	 * @return
	 */
	private static Object cleanTime(String stringValue) {
		String hoursMinutes = "[0-9]?[0-9]:[0-9]?[0-9](?!\\))";
		String optionalSpaces = "\\s*";
		//		String ampm = "[aA][mM]|[pP][mM]|[aA]|[pP]|[aA]\\.[mM]\\.|[pP]\\.[mM]\\.";
		String ampm = "[aA]|[pP]";
		String optionalAmPm = "[" + ampm + "]?";
		String regex = hoursMinutes + optionalSpaces + optionalAmPm;

		String finalTime = "";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(stringValue);
		if (matcher.find()) {

			int start = matcher.start(); 
			int end = matcher.end();
			String time = stringValue.substring(start, end);
			String hours = time.substring(0, time.indexOf(":"));
			time = time.substring(time.indexOf(":")+1);
			String min, sec = "00";
			Integer h;
			time = time.toLowerCase();
			if (time.contains("p")) {
				min = time.substring(0,time.indexOf("p"));
				h = new Integer(hours);
				h = h + 12;
				hours = h.toString();
			} else if (time.contains("a")) {
				min = time.substring(0,time.indexOf("a"));
			} else {
				min = time.trim();
			}
			finalTime = hours.trim() + ":" + min.trim() + ":" + sec;
			try {
				return Time.valueOf(finalTime);
			} catch (Exception e) {
				try {
					return clean(stringValue, ValueType.STRING);
				} catch (Exception e2) {}
			}
		}
		try {
			return clean(stringValue, ValueType.STRING);
		} catch (Exception e) {}

		return clean(stringValue, ValueType.STRING);
	}

	public static Object cleanDate(String stringValue) {
		SimpleDateFormat f = new SimpleDateFormat("MM/dd/yyyy");
		try {
			Date date = f.parse(stringValue);
			return date;
		} catch (ParseException e) {
			f = new SimpleDateFormat("dd MMMM yyyy");
			try {
				Date date = f.parse(stringValue);
				return date;
			} catch (ParseException e2) {
				f = new SimpleDateFormat("yyyy");
				try {
					Date date = f.parse(stringValue);
					return date;
				} catch (ParseException e3) {
					f = new SimpleDateFormat("MMMM yyyy");
					try {
						Date date = f.parse(stringValue);
						return date;
					} catch (ParseException e4) {
						return stringValue;
					}
				}
			}
		}
	}
	//	private static Object cleanDate(String stringValue) {
	////		try {
	////			DateTime dt = new DateTime(stringValue);
	////			return dt.toDate();
	////		} catch (Exception e) {
//				List<SimpleDateFormat> formatList = new ArrayList<SimpleDateFormat>();
	//
	//										//
	//			//
//				formatList.add(new SimpleDateFormat("MM/dd/yyyy"));
//				formatList.add(new SimpleDateFormat("kk:mm aa  EEE dd-MMM-yyyy"));//02:29 PM  Sun 18-Dec-2011
//				formatList.add(new SimpleDateFormat("kk:mm aaMMM yy"));//9:31 AMJan 04
//				formatList.add(new SimpleDateFormat("EEE, MMM dd kk:mm aa"));//Tue, Jan 3 8:47 AM
//				formatList.add(new SimpleDateFormat("kk:mmaMMM dd"));
//				formatList.add(new SimpleDateFormat("MMM dd - kk:mmaa"));//Dec 18 - 6:30pm
//				formatList.add(new SimpleDateFormat("EEE, MMM dd"));//Tue, Jan 3
//				formatList.add(new SimpleDateFormat("kk:mma MM-dd-yy"));//5:39P 12-18-11
//				formatList.add(new SimpleDateFormat("kk:mm aa, MMM dd"));
//				formatList.add(new SimpleDateFormat("MM/dd/yy kk:mm aa"));// 1/3/12 1:29 PM 
//				formatList.add(new SimpleDateFormat("MM/dd/yy kk:mmaa z"));// 1/3/2012 7:39PM EST
//				formatList.add(new SimpleDateFormat("EEE., MMM.dd, yyyy kk:mm a.a."));// Tue., Jan.03, 2012 3:55 p.m.
//				formatList.add(new SimpleDateFormat("yyyy-MM-dd kk:mmaa z"));//2012-01-03 06:30AM EST
//				formatList.add(new SimpleDateFormat("yyyy-MM-dd kk:mm aa"));//2012-01-03 06:42 PM
//				formatList.add(new SimpleDateFormat("MM-dd-yyyy kk:mmaa z"));//12/18/2011 4:08PM MST
//				formatList.add(new SimpleDateFormat("yyyy-MM-dd HH:mm"));//2011-12-18 22:21 
//				formatList.add(new SimpleDateFormat("kk:mm aa"));//
	////			formatList.add(new SimpleDateFormat(""));//
	////			formatList.add(new SimpleDateFormat(""));//
	////			formatList.add(new SimpleDateFormat(""));//
	////			formatList.add(new SimpleDateFormat(""));//
	//			
	//			
	//			formatList.add(new SimpleDateFormat("MMM dd")); //Dec 18
	//
	////			formatList.add(new SimpleDateFormat());
	//
	//			if (stringValue.contains("- Not Available")) {
	//				stringValue = stringValue.substring(0, stringValue.indexOf("- Not Available"));
	//			} else if (stringValue.contains("- Contact Airline")) {
	//				stringValue = stringValue.substring(0, stringValue.indexOf("- Contact Airline"));
	//			} else if (stringValue.contains("Cancelled")) {
	//				stringValue = stringValue.substring(0, stringValue.indexOf("Cancelled"));
	//			} else if (stringValue.contains("(")) {
	//				stringValue = stringValue.substring(0, stringValue.indexOf("("));
	//			} 
	//			Date date;
	//			for (int i = 0 ; i < formatList.size(); i++ ) {
	//				try {
	//					date = formatList.get(i).parse(stringValue);
	//					return date;
	//				} catch (Exception e2) {}
	//			}
	//			String temp = stringValue;
	////			8:35pJan 3
	//			stringValue = stringValue.toUpperCase();
	//			int p = stringValue.indexOf('P');
	//			int a = stringValue.indexOf('A');
	//			
	//			if (p > -1 && ((a==-1) || (a>p))  ) {
	//				stringValue = stringValue.substring(0, p) + " pm " + stringValue.substring(p+1);
	//			} else if (a > -1){
	//				stringValue = stringValue.substring(0, a) + " am " + stringValue.substring(a+1);
	//			}
	//			try {
	//				// 8:35 pm Jan 3
	//				SimpleDateFormat f = new SimpleDateFormat("kk:mm aa MMM dd");
	//				date = f.parse(stringValue);
	//				return date;
	//				
	//				
	//			} catch (Exception e3) {
	//				try {
	//					SimpleDateFormat f = new SimpleDateFormat("kk:mm aa  MM-dd-yy");//5:39P 12-18-11;
	//				
	//				date = f.parse(stringValue);
	//				return date;
	//				} catch (Exception e4) {
	//					System.out.println(temp); 
	//					e4.printStackTrace();
	//				}
	//			}
	////		}
	//		return stringValue;
//		}
}
