package de.dualuse.swt.experiments.scratchy;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public abstract class JobDispatcher {
	
	final static int nThreads = 4;
	
	static class Job {
		int frame;
		Runnable work;
	}
	
	static class Worker implements Runnable {
		
		LinkedBlockingQueue<Worker> queue;
		Runnable runner;
		volatile boolean shutDown = false;
		
		public Worker(LinkedBlockingQueue<Worker> queue) {
			this.queue = queue;
		}
		
		@Override public void run() {
			
			while(!shutDown) {
				waitForJob();
				try { runner.run(); } catch(Exception e) { e.printStackTrace(); }
				runner = null;
				queue.offer(this);
			}
			
		}
		
		private void waitForJob() {
			while(!shutDown && runner==null) {
				synchronized(this) {
					try {
						wait();
					} catch (InterruptedException e) {}
				}
			}
		}
		
		public void shutDown() {
			shutDown = true;
			notifyAll();
		}
		
		public void submit(Runnable runner) {
			this.runner = runner;
			notifyAll();
		}
	}
	
	LinkedBlockingQueue<Job> jobs = new LinkedBlockingQueue<Job>();
	LinkedBlockingQueue<Worker> workers = new LinkedBlockingQueue<Worker>();
	
	{
		for (int i=0; i<nThreads; i++)
			workers.offer(new Worker(workers));
	}
	
//==[ Job Dispatch Thread ]=========================================================================
	
	Thread jobDispatchThread = new Thread(() -> dispatchLoop());
	volatile boolean quit = false;
	
	private void dispatchLoop() {
		while(!quit) try {
			
			Worker worker = workers.take();
			Job job = jobs.take();

			synchronized(JobDispatcher.this) {
				List<Job> todo = jobs.stream()
					.filter((j) -> filter(j))
					.sorted((j1,j2) -> compare(j1,j2))
					.collect(Collectors.toList());

				jobs.clear();
				jobs.addAll(todo);
				
				Job selected = todo.get(0);
				
				worker.submit(selected.work);
			}
			
			
		} catch(InterruptedException e) {}
	}
	
//==[ Public Interface ]============================================================================
	
	public synchronized void addJob(Job job) {
		
	}
	
//==================================================================================================
	
	public abstract boolean filter(Job job);
	public abstract int compare(Job job1, Job job2);
	
}
