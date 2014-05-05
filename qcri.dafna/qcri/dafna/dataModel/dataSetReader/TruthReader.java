package qcri.dafna.dataModel.dataSetReader;

import java.util.HashMap;

import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.dataFormatter.DataCleaner;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;

public abstract class TruthReader {
	HashMap<String, DataItemMeasures> dataItemMeasures;
	int trueValueCount;

	public TruthReader(HashMap<String, DataItemMeasures> dataItemMeasures) {
		this.dataItemMeasures = dataItemMeasures;
	}
	void addTrueValue(String entityId, String onjectId, String propertyName, String propertyValue) {

		String dataItemKey = SourceClaim.dataItemKey(/*entityId,*/ onjectId, propertyName);
		ValueType valueType = DataTypeMatcher.getPropertyDataType(propertyName);
		DataItemMeasures dim = dataItemMeasures.get(dataItemKey);
		if (dim == null) {
//			System.out.println("Cannot find the claim for the dataItem: " + dataItemKey);
			return;
		}
		Object trueValue = DataCleaner.clean(propertyValue, valueType);
		if (trueValue instanceof String && 
				!(DataTypeMatcher.savedAsString(valueType))) {
			dim.setTrueValueCleaned(false);
		} else {
			dim.setTrueValueCleaned(true);
		}
		if (dim.getTrueValue() != null) {// TODO 
			//			System.out.println(dim.getTrueValue() + " => " +entityId + " || " + onjectId + " || " + propertyName + " || " + propertyValue );
			trueValueCount --; 
		}
		dim.setTrueValue(trueValue);
		trueValueCount ++;
	}
}
