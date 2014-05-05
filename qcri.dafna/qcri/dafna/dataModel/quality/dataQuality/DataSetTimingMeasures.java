package qcri.dafna.dataModel.quality.dataQuality;

public class DataSetTimingMeasures {
	private long dataSetReadingTime;
	private long truthReadingTime;
	private long bucketsBuildingTime;
	private long dataQualityMeasuresComputationTime;

	public void printTimingMeasures() {
		System.out.println("dataSet Reading Time: " + dataSetReadingTime + " m.s. (" + dataSetReadingTime/60000.0 + "mins. )");
		System.out.println("truth vakues Reading Time: " + truthReadingTime + " m.s. (" + truthReadingTime/1000.0 + "sec. )");
		System.out.println("buckets Building Time: " + bucketsBuildingTime + " m.s. (" + bucketsBuildingTime/1000.0 + "sec. )");
		System.out.println("data-Quality Measures Computation Time: " + 
		dataQualityMeasuresComputationTime + "m.s.(" + dataQualityMeasuresComputationTime/60000.0 + "mins. )");
	}

	/* ------------- dataSetReadingTime ------------- */
	public void startDataSetReadingTime() {
		this.dataSetReadingTime = System.currentTimeMillis();
	}
	public void endDataSetReadingTime() {
		this.dataSetReadingTime = System.currentTimeMillis() - this.dataSetReadingTime;
	}
	public long getDataSetReadingTime() {
		return dataSetReadingTime;
	}
	/* ------------- dataSetReadingTime ------------- */

	/* ------------- truthReadingTime ------------- */
	public void startTruthReadingTime() {
		this.truthReadingTime = System.currentTimeMillis();
	}
	public void endTruthReadingTime() {
		this.truthReadingTime = System.currentTimeMillis() - this.truthReadingTime;
	}
	public long getTruthReadingTime() {
		return truthReadingTime;
	}
	/* ------------- truthReadingTime ------------- */

	/* ------------- bucketsBuildingTime ------------- */
	public void startBucketsBuildingTime() {
		this.bucketsBuildingTime = System.currentTimeMillis();
	}
	public void endBucketsBuildingTime() {
		this.bucketsBuildingTime = System.currentTimeMillis() - this.bucketsBuildingTime;
	}
	public long getBucketsBuildingTime() {
		return bucketsBuildingTime;
	}
	/* ------------- bucketsBuildingTime ------------- */

	/* ------------- dataQualityMeasuresComputationTime ------------- */
	public void startDataQualityMeasuresComputationTime() {
		this.dataQualityMeasuresComputationTime = System.currentTimeMillis();
	}
	public void endDataQualityMeasuresComputationTime() {
		this.dataQualityMeasuresComputationTime = System.currentTimeMillis() - this.dataQualityMeasuresComputationTime;
	}
	public long getDataQualityMeasuresComputationTime() {
		return dataQualityMeasuresComputationTime;
	}
	/* ------------- dataQualityMeasuresComputationTime ------------- */

}