package de.dualuse.swt.experiments.scratchy.experiments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class Experiment2ImageMemoryConsumption extends Experiment {
	
	/*
	 * 2063 files, 360p
	 * 27.6 MB jpg
	 * (306.4 MB as png)
	 */
	
	
	/*
	 * Used Heap: ~ 50mb
	 * 		ImageData objects garbage collected,
	 * 		Image objects have separate memory management (-> therefore .dispose())
	 * 
	 * 2.6G/16G (pre) (swap ~ 2.2)
	 * 4.6G/16G (post) (swap ~ 2.2)
	 * 
	 * time: 15.82s
	 */
	
	static void keepImageOnly() {
		
		Display dsp = Display.getDefault();
		ImageLoader loader = new ImageLoader();
		
		System.out.println("Loading Images");
		
		List<Image> images = new ArrayList<Image>();
		int counter = 0;
		start();
		for (File file : files) {
			ImageData[] datas = loader.load(file.getAbsolutePath());
			ImageData data = datas[0];
			Image image = new Image(dsp, data);
			
			images.add(image);
			
			System.out.println("Finished (" + (++counter) + "/" + files.length + ")");
		}
		System.out.println("Done Loading");
		stop();
		
		synchronized(images) {
			try { images.wait(); } catch (InterruptedException e) {}
		}
		
	}
	
	/*
	 * Used Heap: ~1.5 gb
	 *		ImageData objects not garbage collected,
	 *		risks outofmemoryecxeption if configured max heap size exceeded
	 *
	 * 		double the total memory consumption (since images make an internal copy?)
	 * 
	 * 		(getImageData() also must return a copy, since modifications have no
	 * 		effect on the images themselves)
	 * 
	 * 2.6G/16G (pre)
	 * 6.6G/16G (post)
	 * 
	 * time: 17.53s
	 * 
	 */
	
	/*
	 * Disposing has no immediate effect on memory consumption.
	 */
	
	static void keepImageAndImageData() {
		
		Display dsp = Display.getDefault();
		ImageLoader loader = new ImageLoader();
		
		System.out.println("Loading Images");
		
		List<Image> images = new ArrayList<Image>();
		List<ImageData> dataList = new ArrayList<ImageData>();
		
		int counter = 0;
		start();
		for (File file : files) {
			ImageData[] datas = loader.load(file.getAbsolutePath());
			ImageData data = datas[0];
			Image image = new Image(dsp, data);
			
			images.add(image);
			dataList.add(data);
			
			System.out.println("Finished (" + (++counter) + "/" + files.length + ")");
		}
		System.out.println("Done Loading");
		stop();
		
//		System.out.println("Disposing");
//		for (Image image : images)
//			image.dispose();
//		System.out.println("Done");
//		images.clear();
		
		synchronized(images) {
			try { images.wait(); } catch (InterruptedException e) {}
		}		
	}
	
	public static void main(String[] args) {
		keepImageOnly();
//		keepImageAndImageData();
	}
	
}
