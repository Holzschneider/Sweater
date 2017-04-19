package de.dualuse.swt.experiments.scratchy;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

public class ResourceManager {
	
	// private Map<Image,Integer> refCount = new HashMap<Image,Integer>();
	// XXX disposed images return hashcode 0? -> then the refCount adds up for all images that were erroneously disposed twice?
	
	private Map<Image,Integer> refCount = new IdentityHashMap<Image,Integer>();
	
	ExecutorService workers = Executors.newSingleThreadExecutor();
	
	int maxCount = 0;
	
	public synchronized void register(Image image) {
		// if (Display.getCurrent() == null) SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
		
		int oldSize = refCount.size();
		
		Integer oldCount = refCount.get(image);
		if (oldCount == null) oldCount = 0;
		
		int newCount = oldCount + 1;
		refCount.put(image, newCount);

//		if (newCount > 2) {
//			System.err.println("===== register(" + System.identityHashCode(image) + ")");
//			System.err.println("\t" + Thread.currentThread().getStackTrace()[2]);
//		}
		
		maxCount = Math.max(maxCount, newCount);
		
		log("count: " + newCount);
		
		log("register: " + System.identityHashCode(image) + " (" + oldCount + " -> " + newCount + ") (total: " + oldSize + " -> " + refCount.size() + ")  (" + Thread.currentThread().getStackTrace()[2] + ")");
		log("max: " + maxCount);
	}
	
	public synchronized void release(Image image) {
		// if (Display.getCurrent() == null) SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
		if (image==null) return;
		
//		System.err.println("===== release(" + System.identityHashCode(image) + ")");
//		System.err.println("\t" + Thread.currentThread().getStackTrace()[2]);
		
		Integer oldCount = refCount.get(image);
		if (oldCount == null) {
			log("double release: " + System.identityHashCode(image) + " (" + Thread.currentThread().getStackTrace()[2] + ")");
			SWT.error(SWT.ERROR_INVALID_ARGUMENT, null, "Image not managed by this ResourceManager."); // return; // not managed by this instance
		}
		
		int newCount = oldCount - 1;
		
		log("release: " + System.identityHashCode(image) + " (" + oldCount + " -> " + newCount + ") (total: " + refCount.size() + ") (" + Thread.currentThread().getStackTrace()[2] + ")");
		
		if (newCount == 0) {
		
			int oldSize = refCount.size();
			
			refCount.remove(image);
		
			workers.submit( () -> image.dispose() );
			log("Disposing image (" + oldCount + " -> " + newCount + ") (total: " + oldSize + " -> " + refCount.size() + ")");
			
		} else {
			
			refCount.put(image, newCount);
			
		}
		
	}

	public void dispose() {
		for (Entry<Image,Integer> entry : refCount.entrySet())
			entry.getKey().dispose();
		
		refCount.clear();
		
		workers.shutdown();
	}
	
	void log(String msg) {
		// System.out.println("ResourceManager: " + msg);
	}
}
