package qcri.dafna.dataModel.data;
import java.nio.charset.Charset;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qcri.dafna.dataModel.dataFormatter.DataComparator;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher;
import qcri.dafna.dataModel.dataFormatter.DataTypeMatcher.ValueType;
import qcri.dafna.dataModel.dataFormatter.PersonsNameComparator;
import qcri.dafna.dataModel.quality.dataQuality.DataQualityMeasurments;


/**
 * @author dalia
 *
 */
public class DataSet {
	
	private Charset ENCODING = Globals.FILE_ENCODING;
	// A regular expression for the used delimiter
	private String delimiterRegularExpression = Globals.delimiterRegularExpression;
	// The used delimiter
	private String delimiterText = Globals.delimiterText;
	private String listOfValuesdelimiter = Globals.cleanedListDelimiter;

	// The initial value used by the voter for the claims' confidence 
	private double startingConfidence;
	// The initial value used by the voter for the sources' trustworthiness
	private double startingTrustworthiness;
	
	// the id = (objectIdentifier + propertyName).hashCode
	// value = all claims with the given id
	private HashMap<String, List<SourceClaim>> dataItemClaims;
	//the id = (objectIdentifier).hashCode
	// value = all claims with the given id
	private HashMap<String, List<SourceClaim>> objectToClaimHash;
	// sourceIdentifier is the key
	private HashMap<String, Source> SourcesHash;
	// id = (objectIdentifier + propertyName).hashCode
	// value = all claims buckets with the given id
	private HashMap<String, List<ValueBucket>> dataItemsBuckets;

	private DataQualityMeasurments dataQualityMeasurments;

	public DataSet() {
		this.SourcesHash = new HashMap<String, Source>();
		this.dataItemsBuckets = new HashMap<String, List<ValueBucket>>();
		this.dataItemClaims = new HashMap<String, List<SourceClaim>>();
		this.objectToClaimHash = new HashMap<String, List<SourceClaim>>();
		this.startingConfidence = 0;
		this.startingTrustworthiness = 0;
	}

	public Charset getENCODING() {
		return ENCODING;
	}
	public void setENCODING(Charset encoding) {
		ENCODING = encoding;
	}
	public void setDelimiterRegularExpression(String delimiterRegularExpression) {
		this.delimiterRegularExpression = delimiterRegularExpression;
	}
	public String getDelimiterRegularExpression() {
		return delimiterRegularExpression;
	}
	public void setDelimiterText(String delimiterText) {
		this.delimiterText = delimiterText;
	}
	public String getDelimiterText() {
		return delimiterText;
	}
	public String getListOfValuesdelimiter() {
		return listOfValuesdelimiter;
	}
	public void setListOfValuesdelimiter(String listOfValuesdelimiter) {
		this.listOfValuesdelimiter = listOfValuesdelimiter;
	}
	public HashMap<String, List<SourceClaim>> getObjectToClaimHash() {
		return objectToClaimHash;
	}
	public HashMap<String, List<ValueBucket>> getDataItemsBuckets() {
		return dataItemsBuckets;
	}
	public double getStartingConfidence() {
		return startingConfidence;
	}
	public double getStartingTrustworthiness() {
		return startingTrustworthiness;
	}
	public HashMap<String, List<SourceClaim>> getDataItemClaims() {
		return dataItemClaims;
	}
	public HashMap<String, Source> getSourcesHash() {
		return SourcesHash;
	}
	public void setDataQualityMeasurments(
			DataQualityMeasurments dataQualityMeasurments) {
		this.dataQualityMeasurments = dataQualityMeasurments;
	}
	public DataQualityMeasurments getDataQualityMeasurments() {
		return dataQualityMeasurments;
	}

	/**
	 * If the source does not exist, it is created and returned.
	 * If it already exist it is returned.
	 * @param sourceId
	 * @return The source Object after persisted in the sources hash table.
	 */
	public Source addSource(String sourceId) {
		if (SourcesHash.containsKey(sourceId)) {
			return SourcesHash.get(sourceId);
		} else {
			Source source = new Source(sourceId);
			SourcesHash.put(sourceId, source);
			return source;
		}
	}

	/**
	 * Add the sourceClaim in both dataItemClaims map and objectToClaimHash.
	 * @param claim the SourceClaim object to be added to the dataSet
	 */
	public void addClaim(int claimId, /*String entityId,*/ String objectId, String uncleanedObjectId,
			String propertyName, String propertyStringValue, double claimWeight, String timeStamp, String sourceId) {
		Source source = addSource(sourceId);
		
		SourceClaim claim = new SourceClaim(claimId, /*entityId,*/ objectId, uncleanedObjectId, propertyName, 
				propertyStringValue, claimWeight, timeStamp, source);
		source.addClaim(claim);

		List<SourceClaim> cSet = dataItemClaims.get(claim.dataItemKey());
		if (cSet == null) {
			cSet = new ArrayList<SourceClaim>();
			dataItemClaims.put(claim.dataItemKey(), cSet);
		}
		cSet.add(claim);
		
		List<SourceClaim> objectClaims = objectToClaimHash.get(claim.objectKey());
		if (objectClaims == null) {
			objectClaims = new ArrayList<SourceClaim>();
			objectToClaimHash.put(claim.objectKey(), objectClaims);
		}
		objectClaims.add(claim);
	}

	/**
	 * Add the claim to the given bucket. and add the bucket to the given claim.
	 * @param c
	 * @param b
	 */
	public void addClaimToBucket(SourceClaim c, ValueBucket b) {
		b.addClaim(c);
		c.setBucket(b);
	}
	/**
	 * 
	 */
	public void initializeTheDisagreeingSources() {
		Set<String> allSources;
		Set<String> disagreeingSources;
		for (List<ValueBucket> bList : dataItemsBuckets.values()) {
			allSources = new HashSet<String>();
			for (ValueBucket b : bList) {
				allSources.addAll(b.getSourcesKeys());
			}
			for (ValueBucket b : bList) {
				disagreeingSources = new HashSet<String>();
				disagreeingSources.addAll(allSources);
				disagreeingSources.removeAll(b.getSourcesKeys());
				b.setDisagreeingSourcesKeys(new ArrayList<String>(disagreeingSources));
			}

		}
	}
	/**
	 * Reset All the measures for the sources and claims into their initial value.
	 * this method is called before running a new truth discovery technique after 
	 * another truth discovery technique on the same dataSet.
	 */
	public void resetDataSet(double sourceTrustworthiness, double claimConfidence, double errorFactor) {
		this.startingTrustworthiness = sourceTrustworthiness;
		this.startingConfidence = claimConfidence;
		for (Source source : SourcesHash.values()) {
			source.resetSourceTrustworthiness(sourceTrustworthiness);
		}
		for (List<ValueBucket> bucketList: dataItemsBuckets.values()) {
			for (ValueBucket bucket : bucketList) {
				bucket.resetClaimsConfidence(claimConfidence, errorFactor);
			}
		}
	}

	/*------------------ Data Bucketing ------------------*/
	public void computeValueBuckets(boolean tolerance) {
		List<ValueBucket> buckets = null;
		for (String dataItemKey : dataItemClaims.keySet()) {
			buckets = computeValueBuckets(dataItemKey, dataItemClaims.get(dataItemKey), tolerance);
			dataItemsBuckets.put(dataItemKey, buckets);
			
		}
		initBucketIDs();
	}

	private List<ValueBucket> computeValueBuckets(String dataItemKey, List<SourceClaim> claims, boolean tolerance) {
		List<ValueBucket> buckets = new ArrayList<ValueBucket>();

		ValueType type = claims.get(0).getValueType();
		if (tolerance) {
			/**
			 * In case we need to compute buckets, dominant value must be comparable.
			 * If the dominant value is not cleaned then it is not comparable,
			 * then the behavior of bucketing will be as if without using tolerance (String comparison is always case insensitive) 
			 */
			if ( ! dataQualityMeasurments.getDataItemMeasures().get(dataItemKey).isDominantValueCleaned()) {
				tolerance = false;
			}
		}
		if (type.equals(ValueType.STRING)) {
			for (SourceClaim claim : claims) {
				insertclaimInBucketString(claim, buckets);
			}
		} else if (type.equals(ValueType.TIME)) {
			for (SourceClaim claim : claims) {
				insertclaimInBucketTime(dataItemKey, claim, buckets, tolerance);
			}
		} else if (type.equals(ValueType.DATE)) {
			for (SourceClaim claim : claims) {
				insertclaimInBucketDate(dataItemKey, claim, buckets, tolerance);
			}
		} else if (type.equals(ValueType.Name)) {
			for (SourceClaim claim : claims) {
				insertclaimInBucketName(claim, buckets);
			}
		} else if (type.equals(ValueType.ListNames)) {
			for (SourceClaim claim : claims) {
				insertclaimInBucketListOfNames(claim, buckets);
			}
		} else if (type.equals(ValueType.FLOAT) || type.equals(ValueType.LONG)) {
			for (SourceClaim claim : claims) {
				insertclaimInBucketNumerical(dataItemKey, claim, buckets, tolerance);
			}
		} else if (type.equals(ValueType.BOOLEAN)) {
			for (SourceClaim claim : claims) {
				insertclaimInBucketBoolean(dataItemKey, claim, buckets);
			}
		}
		return buckets;
	}
	private void insertclaimInBucketString(SourceClaim claim, List<ValueBucket> buckets) {
		for (ValueBucket b : buckets) {
			if (b.getCleanedString().equalsIgnoreCase((String)claim.getPropertyValue())) {
				addClaimToBucket(claim, b);
				return;
			}
		}
		newBucket(claim, buckets, true, true);
	}

	private void insertclaimInBucketListOfNames(SourceClaim claim, List<ValueBucket> buckets) {
		for (ValueBucket b : buckets) {
			if (DataComparator.sameNames(b.getCleanedString(), (String)claim.getPropertyValue())) {
				addClaimToBucket(claim, b);
				return;
			}
		}
		newBucket(claim, buckets, true, true);
	}

	private void insertclaimInBucketName(SourceClaim claim, List<ValueBucket> buckets) {
		double d;
		for (ValueBucket b : buckets) {
			d = PersonsNameComparator.sameName(b.getCleanedString(), (String) claim.getPropertyValue());
			if (d > 0) {
				addClaimToBucket(claim, b);
				return;
			}
		}
		newBucket(claim, buckets, true, true);
	}

	private ValueBucket newBucket(SourceClaim claim, List<ValueBucket> buckets, boolean clean, boolean setCleanedString) {
		ValueBucket newBucket = new ValueBucket(startingConfidence, clean);
		if (setCleanedString) {
			newBucket.setCleanedString((String)claim.getPropertyValue());
		} else {
			newBucket.setMaxValue(claim.getPropertyValue());
			newBucket.setMinValue(claim.getPropertyValue());
		}
		addClaimToBucket(claim, newBucket);
		newBucket.setValueType(claim.getValueType());
		buckets.add(newBucket);
		return newBucket;
	}

	/**
	 * @param dataItemKey
	 * @param claim
	 * @param buckets
	 * @param tolerance
	 */
	private void insertclaimInBucketTime(String dataItemKey, SourceClaim claim, List<ValueBucket> buckets, boolean tolerance) {
		ValueBucket newBucket;
		// if the claim value is not cleaned, compare it with the not-cleaned values buckets only.
		if (! claim.isClean()) {
			insertUncleanedClaim(claim, buckets);
			return;
		}

		long max, min, val;
		for (ValueBucket b : buckets) {
			if (b.isClean()) {
				val = ((Time)claim.getPropertyValue()).getTime();
				max = ((Time)b.getMaxValue()).getTime();
				min = ((Time)b.getMinValue()).getTime();
				if ((val <= max && val > min) || (!tolerance && val == max)) {
					addClaimToBucket(claim, b);
					return;
				}
			}
		}
		// if no bucket for such claim value exist, then create a new bucket.
		newBucket = newBucket(claim, buckets, true, false);
		if (tolerance) {
			double value = ((Time)claim.getPropertyValue()).getTime();

			double dominantValue = ((Time) dataQualityMeasurments.getDataItemMeasures().get(dataItemKey).getDominantValue()).getTime();
			// date tolerance = 10 minuites = 600 second = 600000 millisecond
			setBucketLimits(value, dominantValue, 600000, newBucket);
			// the max and min are of data type double
			max = ((Double)newBucket.getMaxValue()).longValue() ;
			min = ((Double)newBucket.getMinValue()).longValue() ;
			newBucket.setMaxValue(new Time(max));
			newBucket.setMinValue(new Time(min));
		}
	}

	private void insertclaimInBucketDate(String dataItemKey, SourceClaim claim, List<ValueBucket> buckets, boolean tolerance) {
		ValueBucket newBucket;
		// if the claim value is not cleaned, compare it with the not-cleaned values buckets only.
		if (! claim.isClean()) {
			insertUncleanedClaim(claim, buckets);
			return;
		}

		long max, min, val;
		for (ValueBucket b : buckets) {
			if (b.isClean()) {
				val = ((Date)claim.getPropertyValue()).getTime();
				max = ((Date)b.getMaxValue()).getTime();
				min = ((Date)b.getMinValue()).getTime();
				if ((val <= max && val > min) || (!tolerance && val == max)) {
					addClaimToBucket(claim, b);
					return;
				}
			}
		}
		// if no bucket for such claim value exist, then create a new bucket.
		newBucket = newBucket(claim, buckets, true, false);
		if (tolerance) {
			double value = ((Date)claim.getPropertyValue()).getTime();

			double dominantValue = ((Date) dataQualityMeasurments.getDataItemMeasures().get(dataItemKey).getDominantValue()).getTime();
			// date tolerance = 10 minuites = 600 second = 600000 millisecond
			setBucketLimits(value, dominantValue, 600000, newBucket);
			// the max and min are of data type double
			max = ((Double)newBucket.getMaxValue()).longValue() ;
			min = ((Double)newBucket.getMinValue()).longValue() ;
			newBucket.setMaxValue(new Time(max));
			newBucket.setMinValue(new Time(min));
		}
	}

	private void insertUncleanedClaim(SourceClaim claim, List<ValueBucket> buckets) {
		for (ValueBucket b : buckets) {
			if (!b.isClean()) {
				if (b.getCleanedString().equalsIgnoreCase((String)claim.getPropertyValue())) {
					addClaimToBucket(claim, b);
					return;
				}
			}
		}
		// un-cleaned values are of string type
		newBucket(claim, buckets, false, true);
	}

	private void insertclaimInBucketNumerical(String dataItemKey, SourceClaim claim, List<ValueBucket> buckets, boolean tolerance) {
		if (!claim.isClean()) {
			insertUncleanedClaim(claim, buckets);
			return;
		}
		double max, min, val;
		for (ValueBucket b : buckets) {
			if (b.isClean()) {
				try{
				val = Double.valueOf(claim.getPropertyValue().toString()) ;
				max = Double.valueOf(b.getMaxValue().toString());
				min = Double.valueOf(b.getMinValue().toString());
				if ((val <= max && val > min) || (!tolerance && val == max)) {
					addClaimToBucket(claim, b);
					return;
				}
				} catch(Exception e) {
					e.printStackTrace();
				}
				
			}
		}

		// if no bucket for such claim value exist, then create a new bucket.
		ValueBucket newBucket = newBucket(claim, buckets, true, false);
		if (tolerance) {
			double value = Double.valueOf(claim.getPropertyValue().toString());
//			System.out.println(claim.getSource().getSourceIdentifier() + "\t" + claim.getObjectIdentifier() + "\t" + claim.getPropertyValueString());
			setBucketLimits(value,
					Double.valueOf(dataQualityMeasurments.getDataItemMeasures().get(dataItemKey).getDominantValue().toString()), 
					dataQualityMeasurments.getDataItemMeasures().get(dataItemKey).getTolerance(), 
					newBucket);
		}
		
	}

	private void insertclaimInBucketBoolean(String dataItemKey, SourceClaim claim, List<ValueBucket> buckets) {
		if (!claim.isClean()) {
			insertUncleanedClaim(claim, buckets);
			return; 
		}
		for (ValueBucket b : buckets) {
			if (claim.getPropertyValue().equals(b.getMaxValue())) {
				addClaimToBucket(claim, b);
				return;
			}
		}
		
		// if no bucket for such claim value exist, then create a new bucket.
		ValueBucket newBucket = newBucket(claim, buckets, true, false);
		newBucket.setMaxValue(claim.getPropertyValue());
		newBucket.setMinValue(claim.getPropertyValue());
		newBucket.setValueType(DataTypeMatcher.ValueType.BOOLEAN);
	}

	private void setBucketLimits(double value, double dominantValue,double tolerance, ValueBucket bucket) {
		double max, min;
		max = dominantValue + Math.abs(tolerance/2);
		min = dominantValue - Math.abs(tolerance/2);
		// TODO adjust where to remove the = 
		if (min == 0 && max == 0 && tolerance == 0) {
			bucket.setMaxValue(Globals.tolerance_Factor);
			bucket.setMinValue(- 1.0 * Globals.tolerance_Factor);
			bucket.setMinValue(0);
			return;
		}
		if (tolerance == 0) {
			bucket.setMaxValue(value);
			bucket.setMinValue(value);
			return;
		}
		if (value >= dominantValue) {
			while (value >= max) {
				min = max;
				max = max + Math.abs(tolerance);
			}
		} else {
			while (value <= min) {
				max = min;
				min = min - Math.abs(tolerance);
			}
		}
		bucket.setMaxValue(max);
		bucket.setMinValue(min);
	}
	
	private void initBucketIDs() {
		int id = 1;
		for (List<ValueBucket> bList : dataItemsBuckets.values()) {
			for (ValueBucket bucket : bList) {
				bucket.setId(id);
				id ++;
			}
		}
	}
	/*------------------ END - Data Bucketing ------------------*/
	public Integer[] getSourcesCoverageDistribution() {
		Integer[] cov = new Integer[SourcesHash.size()];
		
		int i = 0;
		for (Source s : SourcesHash.values()) {
			cov[i] = s.getClaims().size();
			i ++;
		}
		Arrays.sort(cov);
		return cov;
	}
	public Integer[] getDIDistinctValueDistribution() {
		Integer[] di = new Integer[dataItemsBuckets.size()];
		
		int i = 0;
		for (List<ValueBucket> l : dataItemsBuckets.values()) {
			di[i] = l.size();
			i ++;
		}
		Arrays.sort(di);
		return di;
	}
}
