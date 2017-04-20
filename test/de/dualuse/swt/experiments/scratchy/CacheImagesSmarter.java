package de.dualuse.swt.experiments.scratchy;
/*
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import de.dualuse.swt.experiments.scratchy.Cache.FailListener;
import de.dualuse.swt.experiments.scratchy.Cache.ResourceListener;

public class CacheImagesSmarter extends CacheAsyncWorkers<Integer, Image> {

	static class Result {
		public Integer frame;
		public Image image;
		public Result(Integer frame, Image image) {
			this.frame = frame;
			this.image = image;
		}
	}
	
	File root;
	File[] frames;
	byte[][] frame_data;
	
	Display dsp;

	// If some part of the application intends to keep the image reference for later use,
	// it should register its reference with the resourcemanager and release it once its done with it.

	// Images that are automatically removed from the size-restricted cache are only disposed,
	// if no additional external references are present.
	ResourceManager manager = new ResourceManager();
	
	ThreadLocal<ImageLoader> loaders = new ThreadLocal<ImageLoader>() {
		@Override public ImageLoader initialValue() {
			return new ImageLoader();
		}
	};
	
	TreeMap<Integer,Image> orderedImages = new TreeMap<Integer,Image>();
	
//==[ Constructor ]=================================================================================
	
	public CacheImagesSmarter(Display dsp, File root) {
		
		this.root = root;
		this.frames = root.listFiles( (f) -> f.getName().toLowerCase().endsWith(".jpg") );
		Arrays.sort(frames, (f1, f2) -> { return f1.getName().compareTo(f2.getName()); } );
		
		// XXX debug: only load every second frame
//		File[] reducedFrames = new File[frames.length/2];
//		for (int i=0; i<reducedFrames.length; i++)
//			reducedFrames[i] = frames[2*i];
//		frames = reducedFrames;
		
		frame_data = new byte[frames.length][];
		for (int i=0, I=frames.length; i<I; i++) {
			try {
				frame_data[i] = Files.readAllBytes(frames[i].toPath());
			} catch (IOException io) {
				System.err.println("Wasn't able to load frame " + i);
			}
		}
		
		this.dsp = dsp;
		
	}
	
//==[ Getter ]======================================================================================
	
	public int frames() {
		return frames.length;
	}

	public ResourceManager getManager() {
		return manager;
	}
	
//==[ Request Images ]==============================================================================
	
	@Override public Image request(Integer key, ResourceListener<Integer,Image> lDone, FailListener<Integer> lFailed) {

		ResourceListener<Integer,Image> lDoneSWT = lDone==null ? null :
			(k, v) -> dsp.asyncExec(() -> lDone.loaded(k, v));
		
		FailListener<Integer> lFailedSWT = lFailed==null ? null : 
			(k, e) -> dsp.asyncExec(() -> lFailed.failed(k, e));
		
		return super.request(key, lDoneSWT, lFailedSWT);
	}

	/////
	
	public Entry<Integer,Image> requestNearest(Integer last, Integer current) {
		return requestNearest(last, current, null, null);
	}
	
	public Entry<Integer,Image> requestNearest(Integer last, Integer current, ResourceListener<Integer,Image> lDone) {
		return requestNearest(last, current, lDone, null);
	}
	
	int last;
	int current;
	
	public synchronized Entry<Integer,Image> requestNearest(Integer last, Integer current, ResourceListener<Integer,Image> lDone, FailListener<Integer> lFailed) {

		this.last = last;
		this.current = current;
		
		// Already present?
		Image image = request(current, lDone, lFailed);
		Integer frame = current;
		
		if (image != null)
			return new SimpleEntry<Integer,Image>(frame, image);
	
		// Otherwise return nearest available in the given direction
		return (current >= last) ? orderedImages.floorEntry(current) : orderedImages.ceilingEntry(current);
	}
	
//==[ Abstract Interface: Load & Free Resources ]===================================================
	
	@Override protected Image loadEntry(Integer key) throws IOException {
		
		log("loading frame: " + key);
		
		synchronized (this) {
			if (key < last && last < current) {
				log("cancelled frame " + key);
				return null;
			}
			if (key > last && last > current) {
				log("cancelled frame " + key);
				return null;
			}
		}
		
		ImageLoader loader = loaders.get();
		
		// ImageData imgData = loader.load(frames[key].getAbsolutePath())[0];
		ImageData imgData = loader.load(new ByteArrayInputStream(frame_data[key]))[0];
		
		Image image = new Image(dsp, imgData);
		
		return image;
		
	}
	
	@Override protected void addEntry(Integer key, Image image) {
		
		log("Cache size: " + cache.size());
		
		orderedImages.put(key, image);
		dsp.asyncExec( () -> manager.register(image) );
		
	}
	
	@Override protected void removeEntry(Integer key, Image image) {
		
		log("Cache size: " + cache.size());
		
		orderedImages.remove(key, image);
		dsp.asyncExec( () -> manager.release(image) );
		
	}
	
//==[ Dispose Image Cache ]=========================================================================
	
	@Override public void dispose() {
		for (Image image : cache.values())
			image.dispose();
		
		super.dispose();
	}

	void log(String msg) {
		System.out.println("CacheImages: " + msg);
	}
}
*/
