package de.dualuse.swt;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

public class Adapter implements Listener {
	private Listener l = this;
	private int eventType = NONE;

	public Adapter() { }
			
	public Adapter(int eventType) {
		this.eventType = eventType;
	}
	
	public Adapter(Widget parent, int eventType) {
		parent.addListener(eventType, this);
		this.eventType = eventType;
	}

	public Adapter(Widget parent, int eventType,  Listener listener) {
		parent.addListener(eventType, this);
		this.eventType = eventType;
		l = listener;
	}

	@Override
	public void handleEvent(Event event) {
		if (l!=this)
			if (event.type==eventType)
				l.handleEvent(event);
	}
	
	
//	public static void main(String[] args) {
////		Layout l = Adapter.with(Layout.class).on("computeSize", this:: computeSize);
//	}
}
