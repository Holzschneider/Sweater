package de.dualuse.swt.experiments.scratchy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SimpleReader {
	
	public static int loadInt(File src) {
		return loadInt(src, 0);
	}
	
	public static int loadInt(File src, int defaultValue) {
		try (BufferedReader in = new BufferedReader(new FileReader(src))) {
			
			String line = in.readLine();
			return Integer.parseInt(line);
			
		} catch (IOException io) {
			return defaultValue;
		}
	}
	
	public static double loadDouble(File src) {
		return loadDouble(src, 0.0);
	}
	
	public static double loadDouble(File src, double defaultValue) {
		try (BufferedReader in = new BufferedReader(new FileReader(src))) {
			
			String line = in.readLine();
			return Double.parseDouble(line);
			
		} catch (IOException io) {
			return defaultValue;
		}
	}
	
}
