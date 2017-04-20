package de.dualuse.swt.experiments.scratchy.experiments;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

/*
 * 2063 frames, 1080p, 155mb
 * 
 * 1 worker thread:
 * 		64.40s
 * 		62.81s (with threadlocal image loader)
 * 
 * 4 worker threads:
 * 		19.08s
 * 		18.31s (with threadlocal image loader)
 * 
 * 8 worker threads:
 * 		16.22s
 * 		16.20s (with threadlocal image loader)
 * 
 */

public class Experiment1ConcurrentImageLoader {
	public static void main(String[] args) {
		File root = new File("/home/sihlefeld/Documents/footage/trip1/frames1");
		File[] files = root.listFiles((name) -> name.getName().toLowerCase().endsWith(".jpg"));
		System.out.println("#length: " + files.length);
		
		int threads = 8;
		ExecutorService workers = Executors.newFixedThreadPool(threads);
		ImageLoader loader = new ImageLoader();
		
		ThreadLocal<ImageLoader> loaders = new ThreadLocal<ImageLoader>() {
			@Override protected ImageLoader initialValue() {
				return new ImageLoader();
			}
		};
		
		System.out.println("Threads: " + threads);
		
		long start = System.nanoTime();
		
		CountDownLatch cdl = new CountDownLatch(files.length);
		for (int i=0; i<files.length; i++) {
			final int index = i;
			workers.submit( ()-> {
				
				ImageLoader localLoader = loaders.get();
				ImageData data = localLoader.load(files[index].getAbsolutePath())[0];
				// ImageData data = loader.load(files[index].getAbsolutePath())[0];
				System.out.println("Finished (" + index + ")");
				cdl.countDown();
				
			});
		}
		
		try { cdl.await(); } catch (InterruptedException e) { e.printStackTrace(); }
		long end = System.nanoTime();
		
		double duration = (end-start)/1e9;
		System.out.println("Fininshed in: " + duration + "s");
		
		workers.shutdown();
	}
}
