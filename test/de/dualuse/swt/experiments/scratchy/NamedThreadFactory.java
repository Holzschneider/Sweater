package de.dualuse.swt.experiments.scratchy;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {

	private ThreadFactory defaultFactory = Executors.defaultThreadFactory();
	private String name;
	
	public NamedThreadFactory(String name) {
		this.name = name;
	}
	
	@Override public Thread newThread(Runnable r) {
		Thread thread = defaultFactory.newThread(r);
		thread.setName(name);
		return thread;
	}
	
}
