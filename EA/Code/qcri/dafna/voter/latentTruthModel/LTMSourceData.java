package qcri.dafna.voter.latentTruthModel;

public class LTMSourceData {

	/**
	 * The counter of claims with voter  truth opinion = false and dataset observation = false 
	 */
	int n00;
	/**
	 * The counter of claims with voter  truth opinion = false and dataset observation = true 
	 */
	int n01;
	/**
	 * The counter of claims with voter  truth opinion = true and dataset observation = false 
	 */
	int n10;
	/**
	 * The counter of claims with voter  truth opinion = true and dataset observation = true 
	 */
	int n11;
	double phay0;
	double phay1;
	public LTMSourceData() {
		n00 = 0;
		n01 = 0;
		n10 = 0;
		n11 = 0;
	}
	/**
	 *  Increment the counter of claims with voter  truth opinion = false and dataset observation = false 
	 */
	public void incrementN00() {
		n00 ++;
	}
	/**
	 *  Increment the counter of claims with voter  truth opinion = false and dataset observation = true 
	 */
	public void incrementN01() {
		n01 ++;
	}
	/**
	 *  Increment the counter of claims with voter  truth opinion = true and dataset observation = false 
	 */
	public void incrementN10() {
		n10 ++;
	}
	/**
	 *  Increment the counter of claims with voter  truth opinion = true and dataset observation = true 
	 */
	public void incrementN11() {
		n11 ++;
	}
	/**
	 *  Decrement the counter of claims with voter  truth opinion = false and dataset observation = false 
	 */
	public void decrementN00() {
		n00 --;
	}
	/**
	 *  Decrement the counter of claims with voter  truth opinion = false and dataset observation = true 
	 */
	public void decrementN01() {
		n01 --;
	}
	/**
	 *  Decrement the counter of claims with voter  truth opinion = true and dataset observation = false 
	 */
	public void decrementN10() {
		n10 --;
	}
	/**
	 *  Decrement the counter of claims with voter  truth opinion = true and dataset observation = true 
	 */
	public void decrementN11() {
		n11 --;
	}
	/**
	 *  get the counter of claims with voter  truth opinion = false and dataset observation = false 
	 */
	public int getN00() {
		return n00;
	}
	/**
	 *  Decrement the counter of claims with voter  truth opinion = false and dataset observation = true 
	 */
	public int getN01() {
		return n01;
	}
	/**
	 *  Decrement the counter of claims with voter  truth opinion = true and dataset observation = false 
	 */
	public int getN10() {
		return n10;
	}
	/**
	 *  Decrement the counter of claims with voter  truth opinion = true and dataset observation = true 
	 */
	public int getN11() {
		return n11;
	}
	public void setPhay0(double phay0) {
		this.phay0 = phay0;
	}
	public double getPhay0() {
		return phay0;
	}
	public void setPhay1(double phay1) {
		this.phay1 = phay1;
	}
	public double getPhay1() {
		return phay1;
	}
}
