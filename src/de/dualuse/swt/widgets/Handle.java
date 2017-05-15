package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.COLOR_BLACK;
import static org.eclipse.swt.SWT.COLOR_WHITE;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.widgets.Event;

public class Handle extends Gizmo implements LayerContainer.LayerTransform {
	private static final int S = 9, R = 6;
	public Handle(LayerContainer parent) {
		super(parent);
		setExtents(-S, -S, S, S);
		
	}
	
	private Color background = null, foreground = null;
	public void setBackground(Color c) { this.background = c; }
	public void setForeground(Color c) { this.foreground = c; }
	
	public Color getBackground() { return background; }
	public Color getForeground() { return foreground; }
	
	protected void onPaint(Event e) {
		LineAttributes la = e.gc.getLineAttributes();
		Color fg = e.gc.getForeground();
		
		if (background!=null)
			e.gc.setForeground(background);
		else
			e.gc.setForeground(e.display.getSystemColor(COLOR_BLACK));

		e.gc.setLineAttributes( new LineAttributes(R) );
		e.gc.drawOval(-S+R/2, -S+R/2, 2*S-R, 2*S-R);
		
		if (foreground!=null)
			e.gc.setForeground(foreground);
		else
			e.gc.setForeground(e.display.getSystemColor(COLOR_WHITE));
			
		e.gc.setLineAttributes( new LineAttributes(R-2) );
		e.gc.drawOval(-S+R/2, -S+R/2, 2*S-R, 2*S-R);
		
		
		e.gc.setLineAttributes(la);
		e.gc.setForeground(fg);
	};
	
	protected boolean validateTransform() {
		getCanvasTransform(this);
		return super.validateTransform();
	};
	
	private float dx = 0, dy = 0;
	
	@Override
	public void onMouseDown(float x, float y, Event e) {
		dx = x;
		dy = y;
	}
	
	@Override
	public void onMouseMove(float x, float y, Event e) {
		if (e.stateMask==BUTTON1)
			translate(x-dx, y-dy);
	}

	@Override
	final public void concatenate(float scx, float shy, float shx, float scy, float tx, float ty) {
		scale(1/scx);
	}
	


}
