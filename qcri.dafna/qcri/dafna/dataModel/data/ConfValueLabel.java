package qcri.dafna.dataModel.data;

public class ConfValueLabel {

	private double confidenceValue;
	private boolean label;
	private int bucketId;
	
	public ConfValueLabel(){
		confidenceValue =  0.0;
		label = false;
		bucketId = 0;
	}
	
	public void setConfidenceValue(double confValue){
		this.confidenceValue = confValue;
	}
	
	public void setBucketId(int bucketId){
		this.bucketId = bucketId;
	}
	
	public void setLabel(boolean label){
		this.label = label;
	}
	
	public double getConfidenceValue()
	{
		return this.confidenceValue;
	}
	
	public boolean getLabel()
	{
		return this.label;
	}
	
	public int getBucketId()
	{
		return this.bucketId;
	}
	
}
