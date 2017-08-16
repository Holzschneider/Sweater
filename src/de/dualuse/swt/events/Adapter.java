package de.dualuse.swt.events;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

public class Adapter implements Listener {
	private Listener delegate = this;
	private int eventType = NONE;

	public Adapter() { }
			
	public Adapter(int eventType) {
		this.eventType = eventType;
	}
	
	public Adapter(Widget parent, int eventType) {
		parent.addListener(eventType, this);
		this.eventType = eventType;
	}

	public Adapter(Widget parent, int eventType, Runnable handler) {
		this(parent, eventType, e->handler.run() );
	}

	public Adapter(Widget parent, int eventType, Listener listener) {
		parent.addListener(eventType, this);
		this.eventType = eventType;
		delegate = listener;
	}
	
	public Adapter append(Runnable handler) {
		return this.append( e->handler.run() );
	}

	public Adapter append(Listener handler) {
		final Listener delegate = this.delegate;
		this.delegate = e-> { delegate.handleEvent(e); handler.handleEvent(e); };
		return this;
	}
	
	@Override
	public void handleEvent(Event event) {
		if (delegate!=this)
			if (event.type==eventType)
				delegate.handleEvent(event);
	}
	
}
