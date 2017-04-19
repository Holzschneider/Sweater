package de.dualuse.swt.experiments.scratchy.video;

import java.io.IOException;
import java.io.InputStream;

public interface Video {
	
	int numFrames();
	double fps();
	
	InputStream getFrame(int no) throws IOException;
	
}
