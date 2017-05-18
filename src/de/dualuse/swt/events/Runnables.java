package de.dualuse.swt.events;

public class Runnables implements Runnable {

	private Runnables rest;
	private Runnable runnable;

	public Runnables(Runnable element) {
		this.runnable = element;
		this.rest = null;
	}

	public Runnables(Runnable... runnables) {
		this(runnables[0]);
		Runnables l = this;
		for (int i=1,I=runnables.length;i<I;i++)
			l = l.rest = new Runnables(runnables[i]);
	}

	
	public Runnables(Runnable element, Runnables rest) {
		this.runnable = element;
		this.rest = rest;
	}

	public Runnables join(Runnable element) {
		return new Runnables(element, this);
	}

	public Runnables exclude(Runnable element) {
		if (this.runnable == element)
			return rest;

		return new Runnables(this.runnable, rest.exclude(element));
	}

	@Override
	public void run() {
		if (rest!=null)
			rest.run();
		
		if(runnable!=null)
			runnable.run();
	}
	
	

}
