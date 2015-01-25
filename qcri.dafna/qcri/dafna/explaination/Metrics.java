package qcri.dafna.explaination;

public class Metrics {
	
	private String claimID;
	private double cv;
	private double trust;
	private double minTrust;
	private double maxTrust;
	private double nbSS;
	private double nbC;
	private double totalSources;
	private double nbDI;
	private double cvGlobal;
	private double cvLocal;
	private double trustGlobal;
	private double trustLocal;
	private String truthLabel;
	
	public Metrics(String claimID, double cv, double trust, double minTrust, double maxTrust, double nbSS, double nbC, double totalSources, double nbDI, double cvGlobal, double cvLocal, double trustGlobal, double trustLocal, String truthLabel){
		this.claimID = claimID;
		this.cv = cv;
		this.trust = trust;
		this.minTrust = minTrust;
		this.maxTrust = maxTrust;
		this.nbSS = nbSS;
		this.nbC = nbC;
		this.totalSources = totalSources;
		this.nbDI = nbDI;
		this.cvGlobal = cvGlobal;
		this.cvLocal = cvLocal;
		this.trustGlobal = trustGlobal;
		this.trustLocal = trustLocal;
		this.truthLabel = truthLabel;
	}
	
	public void setClaimID(String claimID){
		this.claimID = claimID;
	}
	
	public void setCv(double cv){
		this.cv = cv;
	}
	
	public void setTrust(double trust){
		this.trust = trust;
	}
	
	public void setMinTrust(double minTrust){
		this.minTrust = minTrust;
	}
	
	public void setMaxTrust(double maxTrust){
		this.maxTrust = maxTrust;
	}
	
	public void setNbSS(double nbSS){
		this.nbSS = nbSS;
	}
	
	public void setNbC(double nbC){
		this.nbC = nbC;
	}
	
	public double setTotalSources(){
		return totalSources;
	}
	
	public void setNbDI(double nbDI){
		this.nbDI = nbDI;
	}
	
	public void setCvGlobal(double cvGlobal){
		this.cvGlobal = cvGlobal;
	}
	
	public void setCvLocal(double cvLocal){
		this.cvLocal = cvLocal;
	}
	
	public void setTrustGlobal(double trustGlobal){
		this.trustGlobal = trustGlobal;
	}
	
	public void setTrustLocal(double trustLocal){
		this.trustLocal = trustLocal;
	}
	
	public void setTruthLabel(String truthLabel){
		this.truthLabel = truthLabel;
	}
	
	public String getClaimID(){
		return claimID;
	}
	
	public double getCv(){
		return cv;
	}
	
	public double getTrust(){
		return trust;
	}
	
	public double getMinTrust(){
		return minTrust;
	}
	
	public double getMaxTrust(){
		return maxTrust;
	}
	
	public double getNbSS(){
		return nbSS;
	}
	
	public double getNbC(){
		return nbC;
	}
	
	public double getTotalSources(){
		return totalSources;
	}
	
	public double getNbDI(){
		return nbDI;
	}
	
	public double getCvGlobal(){
		return cvGlobal;
	}
	
	public double getCvLocal(){
		return cvLocal;
	}
	
	public double getTrustGlobal(){
		return trustGlobal;
	}
	
	public double getTrustLocal(){
		return trustLocal;
	}
	
	public String getTruthLabel(){
		return truthLabel;
	}
	
}
