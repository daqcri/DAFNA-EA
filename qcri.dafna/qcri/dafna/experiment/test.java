package qcri.dafna.experiment;

import java.util.ArrayList;
import java.util.List;

public class test {

//	public static void main(String[] args) {
//		List<String> s = new ArrayList<String>();
//		for (int i = 0 ; i < 1000000; i++) {
//			s.add("2 : fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
//			if (i%1000==0) {
//				computeMemoryConsumption();
//			}
//		}
//	}
	private static void computeMemoryConsumption() {
		// Get the Java runtime
	    Runtime runtime = Runtime.getRuntime();
	    // Run the garbage collector
	    runtime.gc();
	    // Calculate the used memory
	    long memory = runtime.totalMemory() - runtime.freeMemory();
//	    System.out.println("Used memory is bytes: " + memory);
	    System.out.println("2: "
	        + bytesToMegabytes(memory));
	}
	private static final long MEGABYTE = 1024L * 1024L;

	  public static long bytesToMegabytes(long bytes) {
	    return bytes / MEGABYTE;
	  }
}
