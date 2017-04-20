package de.dualuse.swt.experiments.scratchy.cache;

public abstract class WorkerThread<E> extends Thread {

	boolean shutdown;
	JobQueue<E> queue;
	
//==[ Constructor ]=================================================================================
	
	public WorkerThread(JobQueue<E> queue) {
		this.queue = queue;
	}
	
//==[ Work Loop ]===================================================================================	

	@Override public void run() {
		
		while (!isShutdown()) try {
			
			E job = queue.get();
			handle(job);
			
		} catch(InterruptedException e) {}
		
	}
	
//==[ Abstract Interface ]==========================================================================
	
	public abstract void handle(E job);
	
//==[ Shutdown Worker ]=============================================================================
	
	public synchronized boolean isShutdown() {
		return shutdown;
	}
	
	public synchronized void shutdown() {
		shutdown = true;
		interrupt();
	}
	
}
