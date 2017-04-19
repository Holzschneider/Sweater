package de.dualuse.swt.experiments.scratchy.cache;

import java.util.LinkedList;
import java.util.List;

public abstract class WorkerService<E> {
	
	JobQueue<E> jobs;
	List<WorkerThread<E>> workers;
	
	int numThreads;
	
	boolean started;
	
//==[ Constructor ]=================================================================================
	
	public WorkerService(JobQueue<E> jobs, int numThreads) {
		this.workers = new LinkedList<WorkerThread<E>>();
		this.jobs = jobs;
		this.numThreads = numThreads;
	}
	
//==[ Abstract Interface: Asynchronous Job Handling ]===============================================
	
	protected abstract void handle(E job);

//==[ Start / Stop ]================================================================================
	
	public void start() {
		if (started) return;
		started = true;
		
		for (int i=0; i< numThreads; i++) {
			WorkerThread<E> worker = new WorkerThread<E>(jobs) {
				@Override public void handle(E job) {
					WorkerService.this.handle(job);
				}
			};
			worker.start();
			workers.add(worker);
			System.out.println("Launched worker thread: " + worker);
		}
	}
	
	public void shutdown() {
		for (WorkerThread<E> worker : workers)
			worker.shutdown();
	}
	
}
