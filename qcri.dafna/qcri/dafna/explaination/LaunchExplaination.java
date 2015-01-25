package qcri.dafna.explaination;


public class LaunchExplaination {
	public static void main(String args[])
	{
		TextGeneration t = new TextGeneration();
		//String[] metrics = {"0.5256384699966441","0.05162170698017994","0.05046583432262075","0.05200505818102974","75.0","25.0","24.0","3.0","85.77981651376147","100.0","69.6969696969697","87.5","TRUE"};
		String[] metrics = {"0.5103789981355668","0.05054878730020672","0.05026218084207576","0.0512463476752496","57.142857142857146","42.857142857142854","14.0","2.0","53.669724770642205","50.0","26.83982683982684","50.0","FALSE"};
		double[] scores = {0.10548401989643952,0.005206719276570372,0.06174001196074881,0.10080315039443996,0.2772473330874408,0.2772473330874408,0.10295689348058405,0.168638187140844,0.09440913844421205,0.49593058170899856,0.008459286566193196,-0.00638440146804976};
		String[] header = {"CLAIM-ID", "DI", "VALUE", "SOURCE ID", "RUN ID"};
		// metrics - entire row of file
		// scores - Relief Scores
		// header - claimid, di, value string, source-id, run-id
		String temp = t.generateText(metrics, scores, header);
		System.out.println(temp);
	}
}
