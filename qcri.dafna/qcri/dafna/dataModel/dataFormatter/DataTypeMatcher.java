package qcri.dafna.dataModel.dataFormatter;

import qcri.dafna.dataModel.data.Globals;

public class DataTypeMatcher {
	
	/**
	 * If you add a data type you must add its cleaning function.
	 * and its bucketing function, and the data comparator function.
	 * @author dalia
	 *
	 */
	public static enum ValueType {
		LONG, STRING, FLOAT, TIME, DATE, ListNames, ISBN, Name,
		BOOLEAN;
	};
	public static enum Measure {
		METER, CM, INCH, KG, GRAM, POUND;
	};

	public static ValueType getPropertyDataType(String propertyName) {
		if (propertyName.equals(Globals.flightDataSet_ActualArrivalTime)) return ValueType.TIME;
		if (propertyName.equals(Globals.flightDataSet_ActualDepartureTime)) return ValueType.TIME;
		if (propertyName.equals(Globals.flightDataSet_ExpectedArrivalTime)) return ValueType.TIME;
		if (propertyName.equals(Globals.flightDataSet_ExpectedDepartureTime)) return ValueType.TIME;
		if (propertyName.equals(Globals.flightDataSet_ArrivalGate)) return ValueType.STRING;
		if (propertyName.equals(Globals.flightDataSet_DepartureGate)) return ValueType.STRING;

		if (propertyName.equals(Globals.weatherDataSet_Humidity)) return ValueType.FLOAT;
		if (propertyName.equals(Globals.weatherDataSet_Tempreture)) return ValueType.FLOAT;
		if (propertyName.equals(Globals.weatherDataSet_Pressure)) return ValueType.FLOAT;
		if (propertyName.equals(Globals.weatherDataSet_ReafFeel)) return ValueType.FLOAT;
		if (propertyName.equals(Globals.weatherDataSet_Visibility)) return ValueType.FLOAT;

		if (propertyName.equals(Globals.bookDataSet_AuthorsNamesList)) return ValueType.ListNames;
		else if (propertyName.startsWith(Globals.bookDataSet_AuthorsNamesList)) return ValueType.BOOLEAN; // For MLE
		if (propertyName.equals(Globals.bookDataSet_BookName)) return ValueType.STRING;

		if (propertyName.equals(Globals.biographiesDataSet_Born)) return ValueType.DATE;
		if (propertyName.equals(Globals.biographiesDataSet_Died)) return ValueType.DATE;
		if (propertyName.equals(Globals.biographiesDataSet_Spouse)) return ValueType.Name;
		if (propertyName.equals(Globals.biographiesDataSet_Spouse_s)) return ValueType.ListNames;
		if (propertyName.equals(Globals.biographiesDataSet_Father)) return ValueType.Name;
		if (propertyName.equals(Globals.biographiesDataSet_Mother)) return ValueType.Name;
		if (propertyName.equals(Globals.biographiesDataSet_Children)) return ValueType.ListNames;

		if (propertyName.startsWith(Globals.populationDataSet_Population)) return ValueType.LONG;

		// For synthetic dataset
		if (propertyName.startsWith(Globals.syntheticDataSet_Property)) return ValueType.STRING;
		if (propertyName.startsWith(Globals.syntheticDataSet_BooleanProperty)) return ValueType.BOOLEAN;

		// Conflicts dataset
		if (propertyName.startsWith(Globals.conflicts_StartDate)) return ValueType.DATE;
		if (propertyName.startsWith(Globals.conflicts_Continent)) return ValueType.STRING;
		if (propertyName.startsWith(Globals.conflicts_Locations)) return ValueType.ListNames;
		if (propertyName.startsWith(Globals.conflicts_cumulativeFatalities)) return ValueType.LONG;
		if (propertyName.startsWith(Globals.conflicts_Fatalities2013)) return ValueType.LONG;
		if (propertyName.startsWith(Globals.conflicts_Fatalities2014)) return ValueType.LONG;

		return ValueType.STRING;
	}
	
	public static boolean savedAsString(ValueType v) {
		if (v.equals(ValueType.STRING) || v.equals(ValueType.ListNames) || v.equals(ValueType.Name)) {
			return true;
		}

		return false;
	}
}
