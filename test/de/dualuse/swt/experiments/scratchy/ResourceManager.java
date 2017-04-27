package de.dualuse.swt.experiments.scratchy;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;

public class ResourceManager<T extends Resource> {
	
	private Display dsp;
	
	private Map<T,Integer> refCount = new IdentityHashMap<T,Integer>();
	
//	int maxCount = 0;
	
//==[ Constructor ]=================================================================================
	
	public ResourceManager(Display display) {
		this.dsp = display;
	}
	
//==[ Increase/Decrease RefCount ]==================================================================
	
	public synchronized T register(T image) {
		if (image==null) return null;
		
//		int oldSize = refCount.size();
		
		Integer oldCount = refCount.get(image);
		if (oldCount == null) oldCount = 0;
		
		int newCount = oldCount + 1;
		refCount.put(image, newCount);

//		Debug me
//		maxCount = Math.max(maxCount, newCount);
//		log("count: " + newCount);
//		log("register: " + System.identityHashCode(image) + " (" + oldCount + " -> " + newCount + ") (total: " + oldSize + " -> " + refCount.size() + ")  (" + Thread.currentThread().getStackTrace()[2] + ")");
//		log("max: " + maxCount);
		
		return image;
	}
	
	public synchronized void release(T image) {
		if (image==null) return;
		
		Integer oldCount = refCount.get(image);
		if (oldCount == null) {
			log("double release: " + System.identityHashCode(image) + " (" + Thread.currentThread().getStackTrace()[2] + ")");
			SWT.error(SWT.ERROR_INVALID_ARGUMENT, null, "Image not managed by this ResourceManager."); // return; // not managed by this instance
		}
		
		int newCount = oldCount - 1;
		
//		log("release: " + System.identityHashCode(image) + " (" + oldCount + " -> " + newCount + ") (total: " + refCount.size() + ") (" + Thread.currentThread().getStackTrace()[2] + ")");
		
		if (newCount == 0) {
		
			refCount.remove(image);

			dsp.asyncExec(() -> image.dispose());
			
		} else {
			
			refCount.put(image, newCount);
			
		}
		
	}

//==[ Dispose All ]=================================================================================
	
	public void dispose() {
		
		for (Entry<T,Integer> entry : refCount.entrySet())
			entry.getKey().dispose();
		
		refCount.clear();
	}
	
//==[ Debug & Log ]=================================================================================
	
	public synchronized int size() {
		return refCount.keySet().size();
	}
	
	void log(String msg) {
		System.out.println("ResourceManager: " + msg);
	}
	
}
