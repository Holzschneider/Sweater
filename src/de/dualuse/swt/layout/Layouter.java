package de.dualuse.swt.layout;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface Layouter {
	
	Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache);
	boolean flushCache(Control control);
	void layout(Composite composite, boolean flushCache);
	
}
