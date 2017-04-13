package de.dualuse.swt.experiments.scratchy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ResourceManager {
	
	private Map<Image,Integer> refCount = new HashMap<Image,Integer>();
	ExecutorService workers = Executors.newSingleThreadExecutor();
	
	public synchronized void register(Image image) {
		if (Display.getCurrent() == null) SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
		
		Integer oldCount = refCount.get(image);
		if (oldCount == null) oldCount = 0;
		int newCount = oldCount + 1;
		refCount.put(image, newCount);
		
	}
	
	public synchronized void release(Image image) {
		if (Display.getCurrent() == null) SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
		
		Integer oldCount = refCount.get(image);
		if (oldCount == null) return; // not managed by this instance
		int newCount = oldCount - 1;
		if (newCount == 0) {
		
			int oldSize = refCount.size();
			refCount.remove(image);
		
			image.dispose();
			workers.submit( () -> image.dispose() );
			log("Disposing image (" + oldSize + " -> " + refCount.size() + ")");
			
		} else {
			refCount.put(image, newCount);
		}
		
	}

	void log(String msg) {
		// System.out.println("ResourceManager: " + msg);
	}
}
