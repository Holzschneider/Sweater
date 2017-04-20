package de.dualuse.swt.experiments.scratchy.cache;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

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

public interface Cache<K,V> {
	
//==[ Callback Interface ]==========================================================================
	
	public interface ResourceListener<K,V> {
		void loaded(K key, V value);
	}
	
	public interface FailListener<K> {
		void failed(K key, Exception e);
	}
	
	public static class ResourceListenerUI<K,V> implements ResourceListener<K,V> {
		Display dsp;
		ResourceListener<K,V> listener;
		
		public ResourceListenerUI(ResourceListener<K,V> l) {
			this.dsp = Display.getCurrent();
			if (dsp == null) SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
			this.listener = l;
		}
		
		@Override public void loaded(K key, V value) {
			dsp.asyncExec(() -> listener.loaded(key, value));
		}
	}
	
	public static class FailListenerUI<K> implements FailListener<K> {
		Display dsp;
		FailListener<K> listener;
		
		public FailListenerUI(FailListener<K> l) {
			this.dsp = Display.getCurrent();
			if (dsp == null) SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
			this.listener = l;
		}

		@Override public void failed(K key, Exception exception) {
			dsp.asyncExec(() -> listener.failed(key, exception));
		}
	}
			
//==[ Implementation ]==============================================================================
	
	public V request(K key);
	
	public V request(K key, ResourceListener<K,V> lDone);
	
	public abstract V request(K key, ResourceListener<K,V> lDone, FailListener<K> lFail);
	
//==[ Dispose ]=====================================================================================
	
	public void dispose();
	
}
