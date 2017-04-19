package de.dualuse.swt.experiments.scratchy.video;

import java.io.IOException;
import java.io.InputStream;

public class VideoZip implements Video {

	@Override public int numFrames() {
		throw new RuntimeException("not yet implemented");
	}

	@Override public double fps() {
		throw new RuntimeException("not yet implemented");
	}

	@Override public InputStream getFrame(int no) throws IOException {
		throw new RuntimeException("not yet implemented");
	}

}
