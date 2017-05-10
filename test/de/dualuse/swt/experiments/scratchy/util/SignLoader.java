package de.dualuse.swt.experiments.scratchy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Sign {
	
	public int frame;
	
	public int x;
	public int y;
	public int w;
	public int h;
	
	public String type;
	
	public Sign(int x, int y, int w, int h, String type) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.type = type;
	}
}

public class SignLoader {
	
	public static List<Sign> loadSigns(File source) throws IOException {
		
		List<Sign> signs = new ArrayList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(source))) {
			String line = br.readLine();
			if (!line.equals("x,y,width,height,class"))
				throw new IOException("Wrong format");
			
			while ((line = br.readLine()) != null) {
				String[] elements = line.split(",");
				int x = Integer.parseInt(elements[0]);
				int y = Integer.parseInt(elements[1]);
				int w = Integer.parseInt(elements[2]);
				int h = Integer.parseInt(elements[3]);
				String type = elements[4];
				signs.add(new Sign(x,y,w,h,type));
			}
		}
		
		return signs;
	}
	
	public static void main(String[] args) {
		
		File data = new File("/home/sihlefeld/Documents/footage/arosa_test/objects");
		
		File[] files = data.listFiles();
		Arrays.sort(files);
		
		for (File file : files) {
			
			try {
				String name = file.getName().replaceAll("\\D", "");
				int frame = Integer.parseInt(name);
				
				List<Sign> signs = loadSigns(file);
				System.out.println(frame + ": " + signs.size());
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
