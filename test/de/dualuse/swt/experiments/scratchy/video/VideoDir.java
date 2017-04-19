package de.dualuse.swt.experiments.scratchy.video;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import de.dualuse.swt.experiments.scratchy.util.SimpleReader;

public class VideoDir implements Video {

	static final String[] extensions = { ".jpg", ".jpeg", ".png" };
	static final String META_FPS = "fps.txt";
	static final double DEFAULT_FPS = 29.97;
	
	File root;
	File[] frames;
	
	int numFrames;
	
//==[ Constructor ]=================================================================================
	
	public VideoDir(File root) {
		this.root = root;
		frames = sortedListing(root);
		numFrames = frames.length;
	}

	/////
	
	private static File[] sortedListing(File dir) {
		File[] files = dir.listFiles((file) -> matches(file));
		Arrays.sort(files, (f1, f2) -> { return f1.getName().compareTo(f2.getName()); } );
		return files;
	}
	
	private static boolean matches(File file) {
		String name = file.getName().toLowerCase();
		for (String extension : extensions)
			if (name.endsWith(extension))
				return true;
		return false;
	}
	
//==[ Implementation ]==============================================================================
	
	@Override public int numFrames() {
		return numFrames;
	}

	@Override public double fps() {
		return SimpleReader.loadDouble(new File(root.getParentFile(), META_FPS), DEFAULT_FPS);
	}

	@Override public InputStream getFrame(int no) throws IOException {
		return new FileInputStream(frames[no]);
	}

}
