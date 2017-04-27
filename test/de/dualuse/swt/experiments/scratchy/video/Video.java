package de.dualuse.swt.experiments.scratchy.video;

import java.io.IOException;
import java.io.InputStream;

public interface Video {
	
	int numFrames();
	
	int[] resolution();
	double fps();
	
	InputStream getFrame(int no) throws IOException;
	
}
