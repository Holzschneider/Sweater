package de.dualuse.swt.experiments.scratchy;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * When being used as an image cache for SWT:
 * 
 * 		- image loading (-> ImageData) can happen asynchronously (but user defined)
 * 
 * 		- creating an Image from an ImageData object should happen on the SWT UI thread
 * 
 * 		- disposing resources should probably happen on the UI thread as well
 *
 */

public abstract class CacheExecutorService<K,V> implements Cache<K,V> {
	
	int nThreads = 4; // Runtime.getRuntime().availableProcessors()/2 + 1;
	
	ExecutorService workers = Executors.newFixedThreadPool(nThreads, new NamedThreadFactory("Cache Worker"));
	
	int MAX_ENTRIES = 2200;
	
	LinkedHashMap<K,V> cache = new LinkedHashMap<K,V>(256, 0.75f, true) {
		private static final long serialVersionUID = 1L;
		@Override public boolean removeEldestEntry(Entry<K,V> eldest) {
			if (size() > MAX_ENTRIES) {
				remove(eldest.getKey());
				removeEntry(eldest.getKey(), eldest.getValue());
			}
			return false;
		}
	};
	
	Set<K> loading = new HashSet<K>();
	
//==[ Constructor ]=================================================================================
	
	public CacheExecutorService() {
		log(nThreads + " workers.");
	}
		
//==[ Implementation ]==============================================================================
	
	public V request(K key) {
		return request(key, null);
	}
	
	public V request(K key, ResourceListener<K,V> lDone) {
		return request(key, lDone, null);
	}
	
	public synchronized V request(K key, ResourceListener<K,V> lDone, FailListener<K> lFail) {

		// Not yet available and loading hasn't been triggered yet? Start asynchronous loading...
		if (!cache.containsKey(key) && !loading.contains(key)) {
			loading.add(key);
			
			workers.execute( () -> {
				
				try {
					
					V value = loadEntry(key);
					if (value != null) { // might have been cancalled by the subclass implementation
						
						synchronized(CacheExecutorService.this) {
							cache.put(key, value);
							loading.remove(key);
							addEntry(key, value);
						}
						
						if (lDone!=null)
							lDone.loaded(key, value);
						
					}
					
				} catch (Exception e) {
					if (lFail!=null)
						lFail.failed(key, e);
				}
				
			});
		}
		
		// Can be null if asynchronous loading has already been triggered but resource has not been loaded/added yet
		return cache.get(key);
		
	}
	
//==[ Abstract Interface ]==========================================================================
	
	protected abstract V loadEntry(K key) throws IOException; 

	protected abstract void addEntry(K key, V value);
	protected abstract void removeEntry(K key, V value);
	
//==[ Dispose ]=====================================================================================
	
	public void dispose() {
		workers.shutdownNow();
		cache.clear();
	}	
	
	void log(String msg) {
		System.out.println("Cache: " + msg);
	}
}
