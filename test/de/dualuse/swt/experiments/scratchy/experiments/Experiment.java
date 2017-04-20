package de.dualuse.swt.experiments.scratchy.experiments;

import java.io.File;

public class Experiment {

	static File root = new File("/home/sihlefeld/Documents/footage/trip1/frames3");
	static File[] files = root.listFiles((f) -> f.getName().toLowerCase().endsWith(".jpg"));
	
	static long start;
	static long end;
	
	static void start() { start(null); }
	static void start(String msg) {
		if (msg!=null) System.out.println(msg);
		start = System.nanoTime();
	}
	
	static void stop() { stop(null); }
	static void stop(String msg) {
		end = System.nanoTime();
		if (msg != null) System.out.print(msg + " ");
		System.out.println("(" + (end-start)/1e9 + "s)");
	}
}
