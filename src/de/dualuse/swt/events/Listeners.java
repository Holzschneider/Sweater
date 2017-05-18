package de.dualuse.swt.events;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class Listeners implements Listener {

	public Listener listener;
	public Listeners others;
	
//==[ Constructors ]================================================================================
	
	public Listeners(Listener listener) {
		this.listener = listener;
		this.others = null;
	}
	
	public Listeners(Listener listener, Listeners others) {
		this.listener = listener;
		this.others = others;
	}

	public Listeners(Listener... listeners) {
		this(listeners[0]);
		Listeners l = this;
		for (int i=1,I=listeners.length;i<I;i++)
			l = l.others = new Listeners(listeners[i]);
	}
	
//==[ Equals ]======================================================================================
	
	@Override public boolean equals(Object other) {
		if (other instanceof Listener) {
			return this.listener==other;
		} else if (other instanceof Listeners) {
			return this==other;
		} else return false;
	}
	
//==[ Join/exclude ]================================================================================
	
	public Listeners join(Listener l) {
		return new Listeners(l, this);
	}
	
	public Listeners exclude(Listener l) {
		if (this.listener.equals(l))
			return others;

		if (this.others==null)
			return this;
		
		return new Listeners(this.listener, this.others.exclude(l));
	}
	
//==[ Listeners static join/exclude ]===============================================================
	
	public static Listener join(Listener a, Listener b) {
		if (a==null) return b;
		if (b==null) return a;
		if (a instanceof Listeners) return new Listeners(b, (Listeners)a); // add the chain to the end/'others'
		else return new Listeners(b,a);
	}
	
	public static Listener exclude(Listener chain, Listener excludee) {
		
		// null argument, just return null
		if (chain == null) {
			
			return null;
			
		// chain is really a chain of listeners
		} else if (chain instanceof Listeners) {
			
			if (chain.equals(excludee)) { // Excluded element is the head of this chain? just return the rest
				return ((Listeners) chain).others;
			} else {
				Listeners l = ((Listeners)chain).exclude(excludee);
				return l.others!=null?l:l.listener;
			}
			
		// chain is just a singular listener
		} else {
			
			if (chain.equals(excludee))
				return null;
			else
				return chain;
			
		}
		
	}
	
	public static Listener join(Listener... listeners) {
		Listeners l = new Listeners(listeners[0]);
		for (int i=1,I=listeners.length;i<I;i++)
			l = l.join(l);
		
		return l;
	}
	
//==[ Event Handling ]==============================================================================

	@Override final public void handleEvent(Event event) {

		if (others!=null)
			others.handleEvent(event);
		
		if (listener!=null)
			listener.handleEvent(event);

	}
	
}
