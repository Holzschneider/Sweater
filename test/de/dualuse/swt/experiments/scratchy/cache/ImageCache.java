package de.dualuse.swt.experiments.scratchy.cache;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import de.dualuse.swt.experiments.scratchy.ResourceManager;
import de.dualuse.swt.experiments.scratchy.video.Video;

public class ImageCache extends AsyncCache<Integer, Image> {

	Display dsp;
	
	///// Frame Source
	
	Video video;
	
	///// Image Loader
	
	ThreadLocal<ImageLoader> loaders = new ThreadLocal<ImageLoader>() {
		@Override public ImageLoader initialValue() {
			return new ImageLoader();
		}
	};

	///// Resource Manager
	
	/*
	 * If some part of the application intends to keep the image reference for later use,
	 * it should register its reference with the resourcemanager and release it once its done with it.
	 * 
	 * Images that are automatically removed from the size-restricted cache are only disposed,
	 * if no additional external references are present.
	 */
	public ResourceManager<Image> manager;
	
	///// Additional order-sensitive image cache
	
	TreeMap<Integer,Image> orderedImages = new TreeMap<Integer,Image>();

	// Keep track of the current and last frame (as specified in the requests)
	int last;
	int current;
	
//==[ Constructor ]=================================================================================
	
	public ImageCache(Display dsp, Video video) {
		this.dsp = dsp;
		this.video = video;
		this.manager = new ResourceManager<Image>(dsp);
	}
	
	public ImageCache(Display dsp, Video video, ResourceManager<Image> manager) {
		this.dsp = dsp;
		this.video = video;
		this.manager = manager;
	}
	
	public ImageCache(Display dsp, Video video, ResourceManager<Image> manager, int maxEntries) {
		this(dsp, video, manager, maxEntries, Runtime.getRuntime().availableProcessors()/2 + 1);
	}
	
	public ImageCache(Display dsp, Video video, ResourceManager<Image> manager, int maxEntries, int numWorkers) {
		super(maxEntries, numWorkers);
		this.dsp = dsp;
		this.video = video;
		this.manager = manager;
	}
	
//==[ Getter ]======================================================================================
	
	public ResourceManager<Image> getManager() {
		return manager;
	}
	
//==[ Request Images ]==============================================================================
	
	@Override public Image request(Integer key, ResourceListener<Integer,Image> lDone, FailListener<Integer> lFailed) {

		ResourceListener<Integer,Image> lDoneSWT = lDone==null ? null :
			(k, v) -> dsp.asyncExec(() -> lDone.loaded(k, v));
		
		FailListener<Integer> lFailedSWT = lFailed==null ? null : 
			(k, e) -> dsp.asyncExec(() -> lFailed.failed(k, e));
		
		return manager.register(super.request(key, lDoneSWT, lFailedSWT));
	}

	/////
	
	public Entry<Integer,Image> requestNearest(Integer last, Integer current) {
		return requestNearest(last, current, null, null);
	}
	
	public Entry<Integer,Image> requestNearest(Integer last, Integer current, ResourceListener<Integer,Image> lDone) {
		return requestNearest(last, current, lDone, null);
	}
	
	public synchronized Entry<Integer,Image> requestNearest(Integer last, Integer current, ResourceListener<Integer,Image> lDone, FailListener<Integer> lFailed) {

		this.last = last;
		this.current = current;

		// Already present?
		Image image = request(current, lDone, lFailed);
		Integer frame = current;

		if (image != null)
			return new SimpleEntry<Integer,Image>(frame, image);

		// Otherwise return nearest available in the given direction
		Entry<Integer,Image> result = (current >= last) ? orderedImages.floorEntry(current) : orderedImages.ceilingEntry(current); 
		if (result!=null) manager.register(result.getValue());
		
		return result;
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
		
		ImageData imgData = loader.load(video.getFrame(key))[0];
		
		Image image = new Image(dsp, imgData);
		
		return image;
		
	}
	
	@Override protected void addEntry(Integer key, Image image) {
		
		log("addEntry: " + System.identityHashCode(image) + " (#" + cache.size() + ")");
		
		orderedImages.put(key, image);
		manager.register(image);
				
	}
	
	@Override protected void removeEntry(Integer key, Image image) {
		
		log("removeEntry: " + System.identityHashCode(image) + " (#" + cache.size() + ")");

		orderedImages.remove(key, image);
		manager.release(image);
		
	}
	
//==[ Dispose Image Cache ]=========================================================================
	
	@Override public void dispose() {
		for (Image image : cache.values()) // XXX macOS concurrent modification exception (fired twice? two threads that triggered dispose concurrently?))
			image.dispose();
		
		manager.dispose();
		
		super.dispose();
	}

//==[ Debug Code ]==================================================================================

	void log(String msg) {
//		System.out.println("CacheImages: " + msg);
	}
	
	public synchronized int size() {
		return cache.size();
	}
	
	public synchronized void countDisposed() {
		int counter = 0;
		for (Entry<Integer,Image> entry : cache.entrySet()) {
			Image image = entry.getValue();
			if (image.isDisposed())
				counter++;
		}
		log("#disposed images in the cache: " + counter);
	}
	
}
