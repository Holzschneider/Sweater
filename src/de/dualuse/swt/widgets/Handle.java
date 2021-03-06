package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.COLOR_BLACK;
import static org.eclipse.swt.SWT.COLOR_WHITE;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public class Handle extends Gizmo<Handle> {

	private static final int S = 9, R = 6;
	
	/////////////////////
		
	final private LayerContainer[] consumers;
	protected Color background = null;
	protected Color foreground = null;

	private float centerX, centerY;
	
//==[ Constructor ]=================================================================================

	public Handle(LayerContainer parent) {
		this(parent,new LayerContainer[0]);
	}
	
	public Handle(LayerContainer parent, LayerContainer... consumers) {
		super(parent);
		setExtents(-S, -S, S, S);
		this.consumers = consumers;
	}
	
//==[ Setter/Getter ]===============================================================================
	
	public void setBackground(Color c) { this.background = c; }
	public void setForeground(Color c) { this.foreground = c; }
	
	public Color getBackground() { return background; }
	public Color getForeground() { return foreground; }
	
	public boolean isDragged() { return drag; }
	
//==[ Rendering ]===================================================================================
	
	// if additional PaintListeners should get the chance to be called, requires call to super.onPaint(e)
	@Override protected void onPaint(Event e) {
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
	}

//==[ Event Handling ]==============================================================================

	protected float downx = 0, downy = 0;
	protected boolean drag;	// is being dragged? mousemove after mousedown&hit
	protected boolean hit;	// was hit during mousedown?

	@Override protected boolean isMouseHandler() {
		return true;
	}
	
	@Override public void onMouseDown(float x, float y, Event e) {
		hit = hit(x,y);
		
		if (!hit) { // only capture&start drag if button 1 and hit
			e.doit = true;
			return;
		}

		if (e.button==1) {
			moveTop();
			downx = x;
			downy = y;
		}
		
		fireOnMouseDown(x, y, e);
	}
	
	@Override public void onMouseUp(float x, float y, Event e) {
		if (!drag) { // only consume if we were dragging
			e.doit = true;
			return;
		}
		
		drag = false;
	}
	
	@Override public void onMouseMove(float x, float y, Event e) {

		boolean button1Pressed = (e.stateMask & SWT.BUTTON1)!=0;
		
		if (!drag && hit && button1Pressed) {
			drag = true;
		}
		
		if (!drag) {
			if (!hit(x,y))
				e.doit = true;
			return;
		}

		float fromX = centerX, fromY = centerY;
		float deltaX = x-downx, deltaY = y-downy;

		if (deltaX!=0 || deltaY!=0) {
			
			translate(deltaX, deltaY);
			
			onHandleDragged(fromX, fromY, centerX, centerY, e);
			
			getParentContainer().redraw();
			for (LayerContainer lc: consumers)
				lc.redraw();
		}
		
	}

	// Hit detection (round handle with radius S)
	@Override protected boolean hit(float x, float y) {
		return (x*x + y*y) <= S*S;
	}
	
//==[ Start Drag programmatically ]=================================================================
	
	// Allow external class to trigger a drag operation programmatically
	public void startDrag() {
		startDrag(0,0);
	}
	
	public void startDrag(float x, float y) {
		if (drag) return; // handle is already being dragged
		
		// Automatically trigger dragging the handle
		Display.getCurrent().asyncExec(() -> {
			// init drag with the specified coordinates
			moveTop();
			drag = true;
			downx = x;
			downy = y;

			// Start capturing events
			capture(this);
		});
	}

//==[ Handler ]=====================================================================================
	
	
//	static class HandleListeners extends Chain<HandleListener> implements HandleListener {
//		public HandleListeners(HandleListener element) {
//			super(element);
//		}
//
//		public void onHandleChanged(float fromX, float fromY, float toX, float toY) {
//			element.onHandleChanged(fromX, fromY, toX, toY);
//		}
//	}
//	
//	public static interface HandleListener {
//		public void onHandleMoved( float fromX, float fromY, float toX, float toY ); 
//	}
//
//	private HandleListener onHandleDragged = null, onHandleMoved = null;
//	public Handle onHandleDragged( HandleListener hl ) { onHandleDragged = hl; return this; }
//	public Handle onHandleMoved( HandleListener hl ) { onHandleMoved = hl; return this; }
	
	protected void onHandleDragged(float fromX, float fromY, float toX, float toY, Event e) { }
	protected void onHandleMoved(float fromX, float fromY, float toX, float toY) { }
	
//==[ Transform ]===================================================================================
	
	@Override protected boolean validateTransform() {
		getCanvasTransform( this::normalizeCanvasTransform );
		return super.validateTransform();
	}

	double lastScx;
	private Handle normalizeCanvasTransform(double scx, double shy, double shx, double scy, double tx, double ty) {
		if (lastScx!=scx)
			return scale(1/(lastScx=scx));
		else return this;
	}

//==[ Keep Track of Center ]========================================================================

	public Handle setCenter( double x, double y ) {
		if (Math.hypot(x-centerX, y-centerY)<1e-4) 
			return this;
		else {
			return identity().translate(x, y);
		}
	}
	
	public double getCenterX() { return centerX; }
	public double getCenterY() { return centerY; }
	
	public<T> T getCenter( LayerTranslationFunction<T> l ) { return getLayerTranslation(l); }
	public Handle readCenter( LayerTranslationConsumer l ) { return readLayerTranslation(l); }
	
	@Override public Handle concatenate(double scX, double shY, double shX, double scY, double tx, double ty) {
		super.concatenate(scX, shY, shX, scY, tx, ty);
		getLayerTranslation(this::onLayerPositionChanges);
		return this;
	}
	
	protected Handle onLayerPositionChanges(double x, double y) {
		float fromX = centerX, fromY = centerY;
		centerX = (float) x;
		centerY = (float) y;
		
		if (fromX!=centerX || fromY !=centerY)
			onHandleMoved(fromX, fromY, centerX, centerY);
		
//		if (centerX==x && centerY==y) return this;
//		
//		centerX = x;
//		centerY = y;
//		
//		float deltax = (float)(centerX-x);
//		float deltay = (float)(centerY-y);
//		onMove(deltax, deltay);
		
		return this;
	}
	
}


