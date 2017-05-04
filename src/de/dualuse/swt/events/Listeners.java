package de.dualuse.swt.events;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class Listeners implements Listener {

	private Listeners others;
	private Listener listener;
	
	public Listeners(Listener listener) {
		this.listener = listener;
		this.others = null;
	}
	
	public Listeners(Listener... listeners) {
		this(listeners[0]);
		Listeners l = this;
		for (int i=1,I=listeners.length;i<I;i++)
			l = l.others = new Listeners(listeners[i]);
	}
	
	public Listeners(Listener listener, Listeners others) {
		this.listener = listener;
		this.others = others;
	}
	
	public Listeners join(Listener l) {
		return new Listeners(l, this);
	}
	
	public Listeners exclude(Listener l) {
		if (this.listener==l)
			return others;

		return new Listeners(this.listener, this.others.exclude(l));
	}
	
	@Override
	final public void handleEvent(Event event) {
		if (others!=null)
			others.handleEvent(event); 
			
		if (listener!=null)
			listener.handleEvent(event);
	}
	
	
	public static Listeners join(Listener... listeners) {
		Listeners l = new Listeners(listeners[0]);
		for (int i=1,I=listeners.length;i<I;i++)
			l = l.join(l);
		
		return l;
	}
}
