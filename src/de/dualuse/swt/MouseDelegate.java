package de.dualuse.swt;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class MouseDelegate extends MouseAdapter {
	
	@Override
	public void mouseDoubleClick(MouseEvent e) { }
	public MouseDelegate mouseDoubleClick(MouseDoubleClickFunction mu) { return this; }
	public static interface MouseDoubleClickFunction { public void mouseDoubleClick(MouseEvent e); }


	@Override
	public void mouseDown(MouseEvent e) { }
	public MouseDelegate mouseDown(MouseDownFunction mu) { return this; }
	public static interface MouseDownFunction { public void mouseDown(MouseEvent e); }


	@Override
	public void mouseUp(MouseEvent e) { }
	public MouseDelegate mouseUp(MouseUpFunction mu) { return this; }
	public static interface MouseUpFunction { public void mouseUp(MouseEvent e); }
	
}
