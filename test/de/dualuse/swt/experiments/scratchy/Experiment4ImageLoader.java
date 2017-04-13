package de.dualuse.swt.experiments.scratchy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

/*
 * Loading Images: ImageLoader vs Image Constructor
 * 
 * 2063 images
 * 
 * with image loader:
 * 		1 Thread
 * 			15.81s, 15.74s, 15.92s
 *		4 Threads
 *			5.81s, 5.58s, 5.27s
 * 
 * with image constructor:
 * 		1 Thread
 * 			16.11s, 15.47s, 15.39s
 *		4 Threads
 *			5.75s, 5.29s, 5.08s
 * 
 * Conclusion: makes no difference performance-wise
 */

public class Experiment4ImageLoader extends Experiment {

	static void withImageLoader() {
		
		Display dsp = Display.getDefault();
		
		List<Image> images = new ArrayList<Image>();
		ImageLoader loader = new ImageLoader();
		
		ExecutorService workers = Executors.newFixedThreadPool(4);
		CountDownLatch cdl = new CountDownLatch(files.length);
		
		start("Loading images (Image Loader)");
		for (File file : files) {
			workers.submit( () -> {
				images.add(new Image(dsp, loader.load(file.getAbsolutePath())[0]));
				cdl.countDown();
			});
		}
		
		try { cdl.await(); } catch (InterruptedException e) {}
		stop("Done");
		
		workers.shutdown();
	}
	
	static void withImageConstructor() {

		Display dsp = Display.getDefault();
		
		List<Image> images = new ArrayList<Image>();
		
		ExecutorService workers = Executors.newFixedThreadPool(4);
		CountDownLatch cdl = new CountDownLatch(files.length);
		
		start("Loading images (Image Constructor)");
		
		for (File file : files) {
			workers.submit( () -> {
				images.add(new Image(dsp, file.getAbsolutePath()));
				cdl.countDown();
			});
		}
		
		try { cdl.await(); } catch (InterruptedException e) {}
		stop("Done");
		
		workers.shutdown();
	}
	
	public static void main(String[] args) {

		// withImageLoader();
		withImageConstructor();
		
	}

}
