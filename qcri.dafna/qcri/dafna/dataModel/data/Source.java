package qcri.dafna.dataModel.data;
import java.util.ArrayList;
import java.util.List;

/**
 * Source object
 * with set of claims: claim
 * @author dalia
 *
 */
public class Source {

	private String sourceIdentifier; 

	private double trustworthiness = 0.9;
	private double oldTrustworthiness = 0.9;
	private List<SourceClaim> claims;

	public Source(String sourceIdentifier) {
		this.sourceIdentifier = sourceIdentifier;
		claims = new ArrayList<SourceClaim>();
		oldTrustworthiness = trustworthiness;
	}

	public void setTrustworthiness(double trustworthiness) {
		this.trustworthiness = trustworthiness;
	}
	public double getTrustworthiness() {
		return trustworthiness;
	}
	public void setOldTrustworthiness(double oldTrustworthiness) {
		this.oldTrustworthiness = oldTrustworthiness;
	}
	public double getOldTrustworthiness() {
		return oldTrustworthiness;
	}
	public List<SourceClaim> getClaims() {
		return claims;
	}
	public void addClaim(SourceClaim claim) {
		claims.add(claim);
	}
	public String getSourceIdentifier() {
		return sourceIdentifier;
	}

	void resetSourceTrustworthiness(double value) {
		trustworthiness = value;
		oldTrustworthiness = value;
	}

}
