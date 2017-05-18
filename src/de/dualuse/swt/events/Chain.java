package de.dualuse.swt.events;

public class Chain<T> {
	private Chain<T> rest;
	private T element;
	
	public Chain(T element) {
		this.element = element;
		this.rest = null;
	}
	
	public Chain(T element, Chain<T> rest) {
		this.element = element;
		this.rest = rest;
	}
	
	public Chain<T> join(T element) {
		return new Chain<T>(element, this);
	}
	
	public Chain<T> exclude(T element) {
		if (this.element==element)
			return rest;

		return new Chain<T>(this.element, rest.exclude(element) );
	}
}


//public class Listeners implements Listener {
//
//	private Listeners others;
//	private Listener listener;
//	
//	public Listeners(Listener listener) {
//		this.listener = listener;
//		this.others = null;
//	}
//	
//	public Listeners(Listener... listeners) {
//		this(listeners[0]);
//		Listeners l = this;
//		for (int i=1,I=listeners.length;i<I;i++)
//			l = l.others = new Listeners(listeners[i]);
//	}
//	
//	public Listeners(Listener listener, Listeners others) {
//		this.listener = listener;
//		this.others = others;
//	}
//	
//	public Listeners join(Listener l) {
//		return new Listeners(l, this);
//	}
//	
//	public Listeners exclude(Listener l) {
//		if (this.listener==l)
//			return others;
//
//		this.others = this.others.exclude(l);
//		return this;
//	}
//	
//	@Override
//	final public void handleEvent(Event event) {
//		if (others!=null)
//			others.handleEvent(event); 
//			
//		if (listener!=null)
//			listener.handleEvent(event);
//	}
//	
//	
//	public static Listeners join(Listener... listeners) {
//		Listeners l = new Listeners(listeners[0]);
//		for (int i=1,I=listeners.length;i<I;i++)
//			l = l.join(l);
//		
//		return l;
//	}
//}
