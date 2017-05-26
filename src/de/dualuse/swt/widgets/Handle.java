package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.COLOR_BLACK;
import static org.eclipse.swt.SWT.COLOR_WHITE;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.widgets.Event;

public class Handle extends Gizmo<Handle> {

	private static final int S = 9, R = 6;
	
	/////////////////////
		
	final private LayerContainer[] consumers;
	private Color background = null;
	private Color foreground = null;

	private float centerX, centerY;
	
	
//==[ Constructor ]=================================================================================

	public Handle(LayerContainer parent) { this(parent,new LayerContainer[0]); }
	
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

	private float dx = 0, dy = 0;
	private boolean drag;

	@Override protected boolean isMouseHandler() {
		return true;
	}
	
	@Override public void onMouseDown(float x, float y, Event e) {
		if (e.button!=1) return;
		moveTop();
		drag = true;
		dx = x;
		dy = y;
	}
	
	@Override public void onMouseUp(float x, float y, Event e) {
		drag = false;
	}
	
	@Override public void onMouseMove(float x, float y, Event e) {
		if (drag) {
			float fromX = centerX, fromY = centerY;
			float deltaX = x-dx, deltaY = y-dy;
			translate(deltaX, deltaY);
			
			if (deltaX!=0 || deltaY!=0)
				onHandleDragged(fromX, fromY, fromX+deltaX, fromY+deltaY, e);
			
			getParent().redraw();
			for (LayerContainer lc: consumers)
				lc.redraw();
		}
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

	@Override public Handle concatenate(double scX, double shY, double shX, double scY, double tx, double ty) {
		super.concatenate(scX, shY, shX, scY, tx, ty);
		getLayerTranslation(this::onLayerPositionChanges);
		return this;
	}
	
	public Handle setCenter( double x, double y ) {
		if (Math.hypot(x-centerX, y-centerY)<1e-4) 
			return this;
		else {
			System.out.println(centerX+", "+centerY+" -> "+x+", "+y);
			return identity().translate(x, y);
		}
	}
	
	public double getCenterX() { return centerX; }
	public double getCenterY() { return centerY; }
	
	public<T> T getCenter( LayerTranslationFunction<T> l ) { return getLayerTranslation(l); }
	public Handle readCenter( LayerTranslationConsumer l ) { return readLayerTranslation(l); }
	
	protected Handle onLayerPositionChanges(double x, double y) {
		float fromX = centerX, fromY = centerY;
		centerX = (float) x;
		centerY = (float) y;
		
		if (fromX!=centerX || fromY !=centerY)
			onHandleMoved(fromX, fromY, centerX, centerY);
		
		return this;
	}
	
	private Handle normalizeCanvasTransform(double scx, double shy, double shx, double scy, double tx, double ty) {
		return scale(1/scx);
	}

}


