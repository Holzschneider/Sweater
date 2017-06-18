package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.*;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;


public class ZoomCanvas extends LayerCanvas implements PaintListener, Listener, ControlListener {
	
	private ArrayList<Listener> listeners = new ArrayList<Listener>();
	private ArrayList<PaintListener> paintListeners = new ArrayList<PaintListener>(); 
	
	private ScrollBar horizontal, vertical;

	// Canvas Bounds
	private float top = -1.0f/0.0f;
	private float bottom = 1.0f/0.0f;
	private float left = -1.0f/0.0f;
	private float right = 1.0f/0.0f;
	
	public boolean relative = true;
	public boolean widthPinned = true;

	public boolean normalizeStrokeSize = false;
	public boolean flipY = false;
	
	public boolean scrollX = true;
	public boolean scrollY = true;
	public boolean zoomX = true;
	public boolean zoomY = true;
	
	private Transform zoomTransform = new Transform(getDisplay());
	
	private Rectangle lastSize = null;

//==[ Constructor ]=================================================================================
	
	public ZoomCanvas(Composite parent) {
		this(parent,NONE);
	}
			
	public ZoomCanvas(Composite parent, int style) {
		super(parent, style);

		zoomTransform = new Transform(getDisplay());
		
//		super.addListener(Paint, this);
//		super.addListener(MouseWheel, this);
//		super.addListener(MouseMove, this);
//		super.addListener(MouseDown, this);
//		super.addListener(MouseUp, this);
		
		addPaintListener(this);
		addControlListener(this);
		
		
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

	@Override protected void onDispose(Event event) {
		super.onDispose(event);
		zoomTransform.dispose();
		at.dispose();
		bt.dispose();
	}
	
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

	public void setCanvasBounds(float left, float top, float right, float bottom) {
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
	}
	
//==[ Manage PaintListeners ]=======================================================================
	
	@Override public void addListener(int eventType, Listener listener) {
		if (eventType == Paint)
			listeners.add(listener);
		else
			super.addListener(eventType, listener);
	}
	
	@Override public void removeListener(int eventType, Listener listener) {
		if (eventType == Paint)
			listeners.remove(listener);
		else
			super.removeListener(eventType, listener);
	}
	
	@Override public void addPaintListener(PaintListener listener) {
		paintListeners.add(listener);
	}
	
	@Override public void removePaintListener(PaintListener listener) {
		paintListeners.remove(listener);
	}

//==[ Event Handling ]==============================================================================

	@Override final public void handleEvent(Event event) {
		
		if (event.type==Paint)
			for (Listener l: listeners)
				if (event.doit)
					l.handleEvent(event);
		
		super.handleEvent(event);
		
		switch (event.type) {
			
			case MouseMove:
				if (event.doit)
					mouseMove(event);
				break;

			case MouseDown:
				if (event.doit)
					mouseDown(event);
				break;
				
			case MouseUp:
				if (event.doit)
					mouseUp(event);
				break;
				
			case MouseWheel:
				if (event.doit)
					mouseScrolled(event);
				event.doit = !zoomX && !zoomY; 
				//prevent scrolling from happening when zooming should happen instead 
				break;
				
			case Selection:
				scrollbarScrolled(event);
		}
	}

//==[ Events: Scrollbars Manipulated ]==============================================================
		
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
		
		zoomTransform.translate(canvasX, canvasY);
		setCanvasTransform(zoomTransform);
		
		// USE .scroll instead -> so repaint will be clipped to the area that's new
		
		Point size = getSize();
		this.scroll(screenX, screenY, 0, 0, size.x, size.y, false);
//		redraw();
	}

//==[ Events: Control Resized ]=====================================================================
	
	@Override final public void controlMoved(ControlEvent e) { }

	@Override public void controlResized(ControlEvent e) {
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
			
			zoomTransform.multiply(at);
//			fireStateChanged();
		}
		
		
		respectCanvasBoundsAndUpdateScrollbars();
		setCanvasTransform(zoomTransform);

			
		lastSize = currentSize;
	}

//==[ Events: Input Events to Control the Viewport ]================================================
	
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
		
		if (e.button==2 && e.count>=2) {
			reset();
			return;
		}
		
		dragActive = true;
		setLocation(p, e);
	}
		
	private void mouseUp(Event e) {
		dragActive = false;
	}
		
	private void mouseDragged(Event e) {
		setLocation(q, e);
		
		zoomTransform.getElements(elements);
		float scx = elements[0];
		float scy = elements[3];
		float shy = elements[1];
		float shx = elements[2];
		float tx = elements[4];
		float ty = elements[5];

		
		float deltaX = (q[0]-p[0]) * (scrollX?1:0);
		float deltaY = (q[1]-p[1]) * (scrollY?1:0); 
		float zx = (float)Math.hypot(scx, shy);
		float zy = (float)Math.hypot(scy, shx);
		
		zoomTransform.translate( deltaX / zx, deltaY / zy );
		respectCanvasBoundsAndUpdateScrollbars();
		setCanvasTransform(zoomTransform);

		zoomTransform.getElements(elements);
		float tx_ = elements[4], ty_ = elements[5];
		
		int dx = (int)(tx_-tx);
		int dy = (int)(ty_-ty);
		
		setLocation(p, q);
		
		Point size = getSize();
		this.scroll(dx, dy, 0, 0, size.x, size.y,false);
	}

	private void mouseScrolled(Event e) {
		setLocation(q, e);
		
		if (!zoomX && !zoomY)
			return;
		
		inverseTransform(q);
		zoomTransform.translate(q[0], q[1]);
		
		double scaleIncrementPerTick = 1.0337;
		float sx = (float)Math.pow(scaleIncrementPerTick, e.count * (zoomX?1:0));
		float sy = (float)Math.pow(scaleIncrementPerTick, e.count * (zoomY?1:0));
		
		zoomTransform.scale(sx, sy);
		zoomTransform.translate(-q[0],  -q[1]);
		respectCanvasBoundsAndUpdateScrollbars();
		setCanvasTransform(zoomTransform);
		
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

//==[ Rendering ]===================================================================================
	
	Transform bt = new Transform(getDisplay());
	Transform at = new Transform(getDisplay());

	// PaintListener
	@Override final public void paintControl(PaintEvent e) {
		
		GC gc = e.gc;
	
		gc.getTransform(bt);
		
		gc.getTransform(at);
		at.multiply(zoomTransform);
		
		if (flipY) {
			at.translate(0, getBounds().height);
			at.scale(1,  -1);
		}
		
		gc.setTransform(at);
		
		// XXX stroke size? only int, no subpixel precision...
		if (normalizeStrokeSize) {
			zoomTransform.getElements(elements);
			float scaleX = elements[0];
			gc.setLineWidth((int)(1f/scaleX));
		}
		
		paintCanvas(e);
		for (PaintListener pl: paintListeners)
			pl.paintControl(e);
		
		gc.setTransform(bt);
	}
	
	protected void paintCanvas(PaintEvent e) {
		// do nothing
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
			zoomTransform.getElements(elements);
			inverseTransform.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
			inverseTransform.invert();
			inverseTransform.transform(p);
		} catch (SWTException ex) {
			ex.printStackTrace();
		}
		return p;
	}
	
	public void reset() {
		zoomTransform.identity();
		setCanvasTransform(zoomTransform);
		respectCanvasBoundsAndUpdateScrollbars();
		redraw();
	}
	
//==[ Viewport ]====================================================================================

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
			
			zoomTransform.translate(-constrainX, -constrainY);
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
