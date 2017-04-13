package de.dualuse.swt.experiments.scratchy;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class CacheFirstTest {

	Display dsp;
	
	File root;
	File frames[];
	
	NavigableMap<Integer,Image> cache = new TreeMap<Integer,Image>();
	Set<Integer> loading = new HashSet<Integer>();
	
	int lastIndex = -1; 

	ThreadLocal<ImageLoader> loaders = new ThreadLocal<ImageLoader>() {
		@Override protected ImageLoader initialValue() {
			return new ImageLoader();
		}
	};
	
	// ImageLoader loader = new ImageLoader();
	// ExecutorService worker = Executors.newSingleThreadExecutor();
	ExecutorService worker = Executors.newFixedThreadPool(4);
	
	int redrawCounter = 0;
	boolean specialRedraw = true;
	
	int loadCounter = 0;
	int cacheCounter = 0;

//	Map<Integer,Image> cache = /*Collections.synchronizedMap*/(new LinkedHashMap<Integer,Image>() {
//		private static final long serialVersionUID = 1L;
//		
//		@Override
//		protected boolean removeEldestEntry(java.util.Map.Entry<Integer, Image> eldest) {
//			if (size()<100) 
//				return false;
//			
//			eldest.getValue().dispose();
//			return true;
//		}				
//	});
	
	public CacheFirstTest(Display dsp, File root) {
		this.dsp = dsp;
		
		frames = root.listFiles((f) -> f.getName().endsWith(".jpg"));
		System.out.println("#frames: " + frames.length);
		
		Arrays.sort(frames, new Comparator<File>() {
			@Override public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
	
	int frames() {
		return frames.length;
	}
	
	Entry<Integer,Image> frame(int currentIndex) {
		
		synchronized(cache) {
			
			boolean cacheContains = cache.containsKey(currentIndex);
			boolean loadingContains = loading.contains(currentIndex);
			System.out.println("(cache: " + cacheContains + ", loading: " + loadingContains + ", size: " + cache.size() + ")");
			
			if (!cacheContains && !loadingContains) {
				
				loading.add(currentIndex);
				
				worker.execute( () -> { 

					System.out.println("Loading (" + currentIndex + ")");
					
					ImageLoader loader = loaders.get();
					ImageData id = loader.load(frames[currentIndex].getPath())[0];
					
					Image image = new Image(dsp, id); // not necessarily on ui thread, but has internal locking, so can lead to deadlocks if in another synchronnized block
					
					synchronized(cache) {
						// cache.put(currentIndex, id);
						cache.put(currentIndex, image);
						loading.remove(currentIndex);
						
						loadCounter++;
						cacheCounter++;
					}
					/*
					dsp.asyncExec(() -> {
						
						Image image = new Image(dsp, id); // image must be constructed on swt thread
						
						synchronized(cache) {
							cache.put(currentIndex, image);
							loading.remove(currentIndex);
							
							loadCounter++;
							cacheCounter++;
						}
						
//						 redraw();
						
					});
					*/
				} );
			}

			if (cache.containsKey(currentIndex)) {
				
				lastIndex = currentIndex;
				// return cache.get(currentIndex);
				return cache.floorEntry(currentIndex);
				
			} else {
				
				Entry<Integer,Image> entry = lastIndex<=currentIndex ? cache.floorEntry(currentIndex) : cache.ceilingEntry(currentIndex);
				if (entry != null) {
					
					int nearest = entry.getKey();
					
					lastIndex = nearest;
					// return entry.getValue();
					return entry;
					
				} else {
					return null;
				}
				
			}
				
		}
	}
	
	public void dispose() {
		System.out.println("Disposing cache");
		
//		for (Image image : cache.values())
//			image.dispose();
		
		cache.clear();
		
		worker.shutdown();
	}
}
