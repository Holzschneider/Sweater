package de.dualuse.swt.experiments.scratchy.video;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.eclipse.swt.graphics.ImageData;

import de.dualuse.swt.experiments.scratchy.util.SimpleReader;

public class VideoDir implements Video {

	static final String[] extensions = { ".jpg", ".jpeg", ".png" };
	static final String META_RES = "res.txt";
	static final String META_FPS = "fps.txt";
	static final double DEFAULT_FPS = 29.97;
	
	File root;
	File[] frames;
	
	int numFrames;
	
	int[] resolution;
	double fps = DEFAULT_FPS;
	
//==[ Constructor ]=================================================================================
	
	public VideoDir(File root) {
		this.root = root;
		frames = sortedListing(root);
		numFrames = frames.length;
		
		if (numFrames==0)
			return;

		File fpsMetaFile = new File(root.getParentFile(), META_FPS);
		fps = SimpleReader.loadDouble(fpsMetaFile, DEFAULT_FPS);
		
		File resMetaFile = new File(root.getParentFile(), META_RES);
		if (resMetaFile.exists())
			resolution = SimpleReader.loadInts(new File(root.getParentFile(), META_FPS));
		else {
			ImageData id = new ImageData(frames[0].getAbsolutePath());
			resolution = new int[] { id.width, id.height };
		}
		
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
		return fps;
	}

	@Override public int[] resolution() {
		return resolution;
	}
	
	@Override public InputStream getFrame(int no) throws IOException {
		return new FileInputStream(frames[no]);
	}

}
