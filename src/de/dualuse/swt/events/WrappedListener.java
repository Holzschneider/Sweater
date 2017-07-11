package de.dualuse.swt.events;

import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.widgets.TypedListener;

public class WrappedListener extends TypedListener {

	public WrappedListener(SWTEventListener listener) {
		super(listener);
	}

	public boolean equals(Object obj) {
		if (obj==this)
			return true;
		else
			return ((WrappedListener) obj).eventListener==this.eventListener;
	};
	
}
