package de.dualuse.swt.experiments.scratchy;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class Experiment3ConcurrentImageConstruction extends Experiment {

	/*
	 * Conversion:
	 * 
	 * single threaded:
	 * 		8.58s
	 * 
	 * 4 threads:
	 * 		3.46s
	 * 
	 * 8 threads:
	 * 		3.32s
	 * 
	 * Mit UI die nebenher laeuft:
	 * 
	 * 4 threads:
	 * 		3.98s (nicht signifikant mehr)
	 */
	
	public static void run(Display dsp) {
		ImageLoader loader = new ImageLoader();
		
		start("Loading... File -> ImageData");
		ImageData[] datas = new ImageData[files.length];
		for (int i=0, I=files.length; i<I; i++)
			datas[i] = loader.load(files[i].getAbsolutePath())[0];
		stop("Done Loading");
		
		int nThreads = 4;
		ExecutorService workers = Executors.newFixedThreadPool(nThreads);
		
		CountDownLatch cdl = new CountDownLatch(files.length);
		
		start("Converting to Images");
		Image[] images = new Image[files.length];
		for (int i=0, I=files.length; i<I; i++) {
			final int index=i;
			workers.submit(() -> {
				images[index] = new Image(dsp, datas[index]);
				cdl.countDown();
			});
		}
		try { cdl.await(); } catch (InterruptedException e) { e.printStackTrace(); }
		stop("Done Converting");
		
		workers.shutdown();
	}
	
	public static void main(String[] args) {
		
		Display dsp = Display.getDefault();
		run(dsp);
		
	}
	
}
