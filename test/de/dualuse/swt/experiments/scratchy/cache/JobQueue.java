package de.dualuse.swt.experiments.scratchy.cache;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class JobQueue<E> {
	
	static final int SIZE = 20;
	
	private LinkedList<E> jobs = new LinkedList<E>();
	private int maxSize;
	
	private ReentrantLock lock = new ReentrantLock();
	private Condition notEmpty = lock.newCondition();
	
//==[ Constructor ]=================================================================================
	
	public JobQueue() {
		this(SIZE);
	}
	
	public JobQueue(int maxSize) {
		this.maxSize = maxSize;
	}
	
//==[ Public Interface ]============================================================================
	
	public void add(E job) {
	
		try {
			lock.lock();
			
			jobs.add(job);
			
			if (jobs.size() > maxSize)
				jobs.removeFirst();
			
			notEmpty.signal();
			
		} finally {
			lock.unlock();
		}
	}
	
	public E get() throws InterruptedException {
		
		try {
			lock.lock();
		
			while(jobs.isEmpty())
				notEmpty.await();
			
			return jobs.removeLast();
			
		} finally {
			lock.unlock();
		}
		
	}
	
//==[ Test-Main ]===================================================================================
	
//	public static void main(String[] args) throws Exception {
//	}
	
}
