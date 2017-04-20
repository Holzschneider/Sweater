package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.H_SCROLL;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.MouseWheel;
import static org.eclipse.swt.SWT.Paint;
import static org.eclipse.swt.SWT.Selection;
import static org.eclipse.swt.SWT.V_SCROLL;

import java.util.ArrayList;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

public class ZoomCanvas extends Canvas implements PaintListener, Listener, ControlListener {
	private ArrayList<Listener> listeners = new ArrayList<Listener>();
	private ArrayList<PaintListener> paintListeners = new ArrayList<PaintListener>(); 
	
	private ScrollBar horizontal, vertical;
	
	public ZoomCanvas(Composite parent, int style) {
		super(parent, style);

		canvasTransform = new Transform(getDisplay());
		
		super.addListener(Paint, this);
		super.addListener(MouseWheel, this);
		super.addListener(MouseMove, this);
		super.addListener(MouseDown, this);
		super.addListener(MouseUp, this);
		super.addPaintListener(this);
		
		this.addControlListener(this);
		
		
		if ((style&H_SCROLL)==H_SCROLL) {
			horizontal = getHorizontalBar();
			horizontal.addListener(Selection, this);
			horizontal.setIncrement(16);
		}
		
		if ((style&V_SCROLL)==V_SCROLL) {
			vertical=getVerticalBar();
			vertical.addListener(Selection, this);
			vertical.setIncrement(16);
		}
	}
	
	
	private void scrollbarScrolled(Event event) {
		float canvasX = 0, canvasY = 0;
		int screenX = 0, screenY = 0;

		if (horizontal!=null) {
			int currentX = horizontal.getSelection();
			screenX = (scrollBarX-currentX);
			canvasX = screenX / elements[0];
			scrollBarX = currentX;
		}
		
		if (vertical!=null) {
			int currentY = vertical.getSelection();
			screenY = (scrollBarY-currentY);
			canvasY = screenY / elements[3];
			scrollBarY = currentY;
		}
		
		canvasTransform.translate(canvasX, canvasY);
		
		// USE .scroll instead -> so repaint will be clipped to the area that's new
		
		Point size = getSize();
		this.scroll(screenX, screenY, 0, 0, size.x, size.y, false);
//		redraw();
	}
	
	@Override
	final public void handleEvent(Event event) {
		switch (event.type) {
		case Paint:
			for (Listener l: listeners)
				if (event.doit)
					l.handleEvent(event);
			break;
			
		case MouseMove:
			mouseMove(event);
			break;

		case MouseDown:
			mouseDown(event);
			break;
			
		case MouseUp:
			mouseUp(event);
			break;
			
		case MouseWheel:
			mouseScrolled(event);
			event.doit = !zoomX && !zoomY; 
			//prevent scrolling from happening when zooming should happen instead 
			break;
			
		case Selection:
			scrollbarScrolled(event);
		}
	}

	@Override
	final public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		
		Transform at = new Transform(getDisplay());
		gc.getTransform(at);
		at.multiply(canvasTransform);
		
		if (flipY) {
			at.translate(0, getBounds().height);
			at.scale(1,  -1);
		}
		
		gc.setTransform(at);
		
		// XXX stroke size? only int, no subpixel precision...
		if (normalizeStrokeSize) {
			canvasTransform.getElements(elements);
			float scaleX = elements[0];
			gc.setLineWidth((int)(1f/scaleX));
		}
		
		paintCanvas(e);
		for (PaintListener pl: paintListeners)
			pl.paintControl(e);
	}
	
	protected void paintCanvas(PaintEvent e) {
		// do nothing
	}
	
	@Override
	public void addListener(int eventType, Listener listener) {
		if (eventType == Paint)
			listeners.add(listener);
		else
			super.addListener(eventType, listener);
	}
	
	@Override
	public void removeListener(int eventType, Listener listener) {
		if (eventType == Paint)
			listeners.remove(listener);
		else
			super.removeListener(eventType, listener);
	}
	
	@Override
	public void addPaintListener(PaintListener listener) {
		paintListeners.add(listener);
	}
	
	@Override
	public void removePaintListener(PaintListener listener) {
		paintListeners.remove(listener);
	}
	
	
	////////////////////
	
	
	private float top = -1.0f/0.0f;
	private float bottom = 1.0f/0.0f;
	private float left = -1.0f/0.0f;
	private float right = 1.0f/0.0f;
	
	public void setCanvasBounds(float left, float top, float right, float bottom) {
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
	}
	
	
	public boolean relative = true;
	public boolean widthPinned = true;

	public boolean normalizeStrokeSize = false;
	public boolean flipY = false;
	
	public boolean scrollX = true;
	public boolean scrollY = true;
	public boolean zoomX = true;
	public boolean zoomY = true;
	
	private Transform canvasTransform;
	
	private Rectangle lastSize = null;
	



//==[ Setter/Getter ]===============================================================================
	
	public void setRelative(boolean relative) {
		this.relative = relative;
	}
	public boolean isRelative() {
		return relative;
	}
	
	public void setWidthPinned(boolean widthPinned) {
		this.widthPinned = widthPinned;
	}
	
	public boolean isWidthPinned() {
		return widthPinned;
	}
	
//==[ Canvas Transform ]============================================================================

	// Internal Work Variables
	private float[] elements = new float[6];
	private Transform inverseTransform = new Transform(getDisplay());
	
	final public float[] getPointOnCanvas(float[] onComponent) {
		float[] onCanvas = new float[2];
		onCanvas[0] = onComponent[0];
		onCanvas[1] = onComponent[1];
		inverseTransform(onCanvas);
		return onCanvas;
	}
	
	private float[] inverseTransform(float[] p) {
		try {
			canvasTransform.getElements(elements);
			inverseTransform.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
			inverseTransform.invert();
			inverseTransform.transform(p);
		} catch (SWTException ex) {
			ex.printStackTrace();
		}
		return p;
	}

	
//==[ Resize-Listener ]=============================================================================
	@Override
	final public void controlMoved(ControlEvent e) { }

	@Override
	final public void controlResized(ControlEvent e) {
		Rectangle currentSize = ZoomCanvas.this.getBounds();
		
		if (lastSize!=null && isRelative()) {
			float dx = (currentSize.width - lastSize.width)/2f;
			float dy = (currentSize.height - lastSize.height)/2f;
			float dsw = currentSize.width*1f / lastSize.width;
			float dsh = currentSize.height*1f / lastSize.height;
			
			Transform at = new Transform(getDisplay());
			float cx = getBounds().width/2;
			float cy = getBounds().height/2;
			
			if (isWidthPinned()) {
				at.translate(0, cy + dy*dsw);
				at.scale(dsw, dsw);
				at.translate(0,  -cy);
			} else {
				at.translate(cx + dx + dsh, 0);
				at.scale(dsh,  dsh);
				at.translate(-cx, 0);
			}
			
			canvasTransform.multiply(at);
//			fireStateChanged();
		}
		
		
		respectCanvasBoundsAndUpdateScrollbars();

			
		lastSize = currentSize;
	}
	
	
//==[ Viewport Controller ]=========================================================================
	private float[] p = new float[2];
	private float[] q = new float[2];
	
	private boolean dragActive = false;
	
	private void mouseMove(Event e) {
		if (dragActive)
			mouseDragged(e);
		else
			setLocation(p, e);
		
	}
	
	private void mouseDown(Event e) {
		dragActive = true;
		setLocation(p, e);
	}
		
	private void mouseUp(Event e) {
		dragActive = false;
	}
		
	private void mouseDragged(Event e) {
		setLocation(q, e);
		
		canvasTransform.getElements(elements);
		float scx = elements[0];
		float scy = elements[3];
		float shy = elements[1];
		float shx = elements[2];
		
		float deltaX = (q[0]-p[0]) * (scrollX?1:0);
		float deltaY = (q[1]-p[1]) * (scrollY?1:0); 
		
		canvasTransform.translate(
			(deltaX / (float)Math.hypot(scx, shy)),
			(deltaY / (float)Math.hypot(scy, shx))
		);
		respectCanvasBoundsAndUpdateScrollbars();
		
		setLocation(p, q);
		redraw();
	}

	private void mouseScrolled(Event e) {
		setLocation(q, e);
		
		if (!zoomX && !zoomY)
			return;
		
		inverseTransform(q);
		
		canvasTransform.translate(q[0], q[1]);
			
		double scaleIncrementPerTick = 1.0337;
		float sx = (float)Math.pow(scaleIncrementPerTick, e.count * (zoomX?1:0));
		float sy = (float)Math.pow(scaleIncrementPerTick, e.count * (zoomY?1:0));
		
		canvasTransform.scale(sx, sy);
		canvasTransform.translate(-q[0],  -q[1]);
		respectCanvasBoundsAndUpdateScrollbars();
		
		redraw();
	}
	
	private void setLocation(float[] arr, Event src) {
		arr[0] = src.x;
		arr[1] = src.y;
	}
	
	private void setLocation(float[] to, float[] from) {
		to[0] = from[0];
		to[1] = from[1];
	}

	
	private float transformedClipping[] = {0,0, 0,0};
	private Rectangle clipping = getClientArea();
	private int scrollBarX = 0, scrollBarY = 0; 
	
	private void respectCanvasBoundsAndUpdateScrollbars() {
		
		clipping = getClientArea();
		for (int i=0;i<2;i++) {
			
			transformedClipping[0] = clipping.x;
			transformedClipping[1] = clipping.y;
	
			transformedClipping[2] = clipping.x+clipping.width;
			transformedClipping[3] = clipping.y+clipping.height;
			
			inverseTransform(transformedClipping);
	
			float constrainX = 0, constrainY = 0;
	
			if (transformedClipping[0]<=left && right<=transformedClipping[2])
				constrainX = (left-transformedClipping[0])/2f + (right-transformedClipping[2])/2f;
			else
			if (transformedClipping[0]<left)
				constrainX = (left-transformedClipping[0]);
			else
			if (right<transformedClipping[2])
				constrainX = (right-transformedClipping[2]);
			
			if (transformedClipping[1]<=top && bottom<=transformedClipping[3])
				constrainY = (top-transformedClipping[1])/2f + (bottom-transformedClipping[3])/2f;
			else
			if (transformedClipping[1]<top)
				constrainY = (top-transformedClipping[1]);
			else
			if (bottom<transformedClipping[3])
				constrainY = (bottom-transformedClipping[3]);
			
			canvasTransform.translate(-constrainX, -constrainY);
		}		
		/////////////
		
		
		
		if ((getStyle()&H_SCROLL)==H_SCROLL) {
			ScrollBar sb = horizontal;
			if (Float.isFinite(left) && Float.isFinite(right)) {
				float scaleX = clipping.width/(transformedClipping[2]-transformedClipping[0]);
				
				float min = scaleX*left;
				float max = scaleX*right;
				
				float lower = scaleX*transformedClipping[0];
				float higher= scaleX*transformedClipping[2];
				
				sb.setMinimum(0);
				sb.setMaximum((int)(max-min));
				sb.setThumb((int)(higher-lower));
				sb.setSelection((int)(lower-min));
				
				scrollBarX = sb.getSelection();
			} else
				sb.setEnabled(false);
		}

		if ((getStyle()&V_SCROLL)==V_SCROLL) {
			ScrollBar sb = vertical;
			if (Float.isFinite(top) && Float.isFinite(bottom)) {
				float scaleY = clipping.height/(transformedClipping[3]-transformedClipping[1]);
				
				float min = scaleY*top;
				float max = scaleY*bottom;
				
				float lower = scaleY*transformedClipping[1];
				float higher= scaleY*transformedClipping[3];
				
				sb.setMinimum(0);
				sb.setMaximum((int)(max-min));
				sb.setThumb((int)(higher-lower));
				sb.setSelection((int)(lower-min));
				
				scrollBarY = sb.getSelection();
			} else
				sb.setEnabled(false);
		}
	}
	
	

}