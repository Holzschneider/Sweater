package de.dualuse.swt.util;

public class SimpleFuture<I> {

	I value;
	boolean aborted;
	
	public synchronized void abort() {
		aborted = true;
		notifyAll();
	}
	
	public synchronized void put(I value) {
		this.value = value;
		notifyAll();
	}
	
	public synchronized I get() throws InterruptedException {
		while(value==null && !aborted)
			wait();
		return value;
	}
}