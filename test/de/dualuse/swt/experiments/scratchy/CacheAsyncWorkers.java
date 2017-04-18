package de.dualuse.swt.experiments.scratchy;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;

public abstract class CacheAsyncWorkers<K,V> implements Cache<K,V> {
	
	///// Cache
	
	int numWorkers = Runtime.getRuntime().availableProcessors()/2 + 1;
	int MAX_ENTRIES = 200;
	
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
	
	///// JobQueue & Asynchronous Worker Threads

	static class ResourceLoadJob<K,V> {
		K key;
		ResourceListener<K, V> lDone;
		FailListener<K> lFailed;
		public ResourceLoadJob(K key, ResourceListener<K,V> lDone, FailListener<K> lFailed) {
			this.key = key;
			this.lDone = lDone;
			this.lFailed = lFailed;
		}
	}
	
	JobQueue<ResourceLoadJob<K,V>> jobs = new JobQueue<ResourceLoadJob<K,V>>();
	WorkerService<ResourceLoadJob<K,V>> workers = new WorkerService<ResourceLoadJob<K,V>>(jobs, numWorkers) {
		@Override protected void handle(ResourceLoadJob<K,V> job) {
			loadJob(job);
		}
	};
	
//==[ Constructor ]=================================================================================
	
	public CacheAsyncWorkers() {
		workers.start();
	}
	
//==[ Asynchronous Load Job ]=======================================================================
	
	private void loadJob(ResourceLoadJob<K,V> job) {
		try {
			
			K key = job.key;
			V value = loadEntry(job.key);
			
			if (value != null) { // might have been cancalled by the subclass implementation
				
				synchronized(CacheAsyncWorkers.this) {
					cache.put(key, value);
					addEntry(key, value);
					loading.remove(key);
				}
				
				if (job.lDone!=null)
					job.lDone.loaded(key, value);
				
			}
			
		} catch (Exception e) {
			if (job.lFailed!=null)
				job.lFailed.failed(job.key, e);
		}
	}
	
//==[ Request Resources ]===========================================================================

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
			jobs.add(new ResourceLoadJob<K,V>(key, lDone, lFail));
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
		workers.shutdown();
		cache.clear();
	}
	
}
