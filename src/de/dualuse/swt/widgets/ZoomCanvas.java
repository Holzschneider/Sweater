package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.H_SCROLL;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.MouseWheel;
import static org.eclipse.swt.SWT.Paint;
import static org.eclipse.swt.SWT.Selection;
import static org.eclipse.swt.SWT.V_SCROLL;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
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

import de.dualuse.swt.graphics.PathShape;


public class ZoomCanvas extends LayerCanvas implements PaintListener, Listener, ControlListener {
	
	private ArrayList<Listener> listeners = new ArrayList<Listener>();
	private ArrayList<PaintListener> paintListeners = new ArrayList<PaintListener>(); 
	
	private ScrollBar horizontal, vertical;

	private float top = -1.0f/0.0f;
	private float bottom = 1.0f/0.0f;
	private float left = -1.0f/0.0f;
	private float right = 1.0f/0.0f;
	
	
	public boolean relative = true;
	public boolean widthPinned = true;
	public boolean constrained = false;
	
	public boolean normalizeStrokeSize = false;
	public boolean flipY = false;
	
	public boolean scrollX = true;
	public boolean scrollY = true;
	public boolean zoomX = true;
	public boolean zoomY = true;
	
	private Transform contentTransform;		// maps content to window size
	private Transform zoomTransform;		// positions viewport on top of content
	private Transform combinedTransform;
	// canvasTransform = contentTransform * zoom/viewportTransform
	
	private Rectangle lastSize = null;
	
//==[ Constructor ]=================================================================================
	
	public ZoomCanvas(Composite parent, int style) {
		super(parent, style);

		contentTransform = new Transform(getDisplay());
		zoomTransform = new Transform(getDisplay());
		combinedTransform = new Transform(getDisplay());
		
//		super.addListener(Paint, this);
//		super.addListener(MouseWheel, this);
//		super.addListener(MouseMove, this);
//		super.addListener(MouseDown, this);
//		super.addListener(MouseUp, this);
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
	
	public ZoomCanvas(Composite parent, int style, boolean constrained) {
		this(parent, style);
		setConstrained(constrained);
		setWidthPinned(true);
	}
	
	@Override protected void onDispose(Event e) {
		super.onDispose(e);
		at.dispose();
		bt.dispose();
		zoomTransform.dispose();
		inverseTransform.dispose();
	}
	
//==[ Setter/Getter ]===============================================================================

	float contentWidth, contentHeight;
	
	public void setContentSize(float width, float height) {
		this.contentWidth = width;
		this.contentHeight = height;
		setCanvasBounds(0, 0, width, height); // XXX depends on the contentTransform (aspectRatio & empty bars for centered content)
	}
	
	public void setCanvasBounds(float left, float top, float right, float bottom) {
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
	}
	
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
	
	public void setConstrained(boolean constrained) {
		this.constrained = constrained;
	}
	
	public boolean isConstrained() {
		return constrained;
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
		// setCanvasTransform(zoomTransform); // XXX
		updateCanvasTransform();
		
		// USE .scroll instead -> so repaint will be clipped to the area that's new
		
		Point size = getSize();
		this.scroll(screenX, screenY, 0, 0, size.x, size.y, false);
//		redraw();
	}

//==[ Events: Control Resized ]=====================================================================
	
	@Override final public void controlMoved(ControlEvent e) { }

	@Override final public void controlResized(ControlEvent e) {
		
		computeCanvasTransform();
		if (true) return;
		
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
			}  else {
				at.translate(cx + dx*dsh, 0);
				at.scale(dsh,  dsh);
				at.translate(-cx, 0);
			}
			
			zoomTransform.multiply(at);
			
			at.dispose();
			// fireStateChanged();
		}
		
		respectCanvasBoundsAndUpdateScrollbars();
		// setCanvasTransform(zoomTransform); // XXX
		updateCanvasTransform();
			
		lastSize = currentSize;
	}
	
	void computeCanvasTransform() {
		
		Rectangle rect = getClientArea();
		
		float width  = rect.width;
		float height =  rect.height;
		
		float sx = 1, sy = 1, tx = 0, ty = 0;
		
		if (width*contentHeight > height*contentWidth) {
		
			sy = height / contentHeight;
			sx = sy;
			
			ty = 0;
			tx = (width - contentWidth*sx)/2;
			
		} else {
			
			sx = width / contentWidth;
			sy = sx;
			
			tx = 0;
			ty = (height - contentHeight*sy)/2;
			
		}

		contentTransform.identity();
		contentTransform.translate(tx, ty);
		contentTransform.scale(sx, sy);
		// setCanvasTransform(zoomTransform);
		updateCanvasTransform();
		
		lastSize = rect;
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
		dragActive = true;
		setLocation(p, e);
		
		if (e.button==3 && e.count==2) {
			zoomTransform.identity();
			// setCanvasTransform(zoomTransform); // XXX
			updateCanvasTransform();
			redraw();
		}
	}
		
	private void mouseUp(Event e) {
		dragActive = false;
	}
		
	private void mouseDragged(Event e) {
		// q = current point
		// p = last point
		setLocation(q, e);
		
		// zoomTransform.getElements(elements); // XXX
		combinedTransform.getElements(elements);
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
		
		updateTransform();
		
		respectCanvasBoundsAndUpdateScrollbars();
		// setCanvasTransform(zoomTransform); // XXX
		updateCanvasTransform();

		// zoomTransform.getElements(elements); // XXX
		combinedTransform.getElements(elements);
		float tx_ = elements[4], ty_ = elements[5];
		
		int dx = (int)Math.round(tx_-tx);
		int dy = (int)Math.round(ty_-ty);
		
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
		// setCanvasTransform(zoomTransform); // XXX
		updateCanvasTransform();
		
		redraw();
	}

	///// Helper
	
	private static void setLocation(float[] arr, Event src) {
		arr[0] = src.x;
		arr[1] = src.y;
	}
	
	private static void setLocation(float[] to, float[] from) {
		to[0] = from[0];
		to[1] = from[1];
	}
	
//==[ Rendering ]===================================================================================
	
	Transform bt = new Transform(getDisplay());
	Transform at = new Transform(getDisplay());
	
	@Override final public void paintControl(PaintEvent e) {
		GC gc = e.gc;
	
		// Save original transform
		gc.getTransform(bt);
		
		// Apply zoom transform
		gc.getTransform(at);
		at.multiply(combinedTransform);
		
		if (flipY) {
			at.translate(0, getBounds().height);
			at.scale(1,  -1);
		}
		
		gc.setTransform(at);
		
		// Adapt stroke size to zoom level
		// XXX stroke size? only int, no subpixel precision...
		if (normalizeStrokeSize) {
			combinedTransform.getElements(elements);
			float scaleX = elements[0];
			gc.setLineWidth((int)(1f/scaleX));
		}
		
		// Paint canvas itself
		paintCanvas(e);
		
		// Call added paintlisteners
		for (PaintListener pl: paintListeners)
			pl.paintControl(e);
		
		// Reset original transform
		gc.setTransform(bt);
		
	}
	
	protected void paintCanvas(PaintEvent e) {
		// do nothing
	}
	
	@Override protected void paintOverlay(Rectangle clip, Transform t, Event c) {
		GC gc = c.gc;
		
		// ...
		Rectangle bounds = getBounds();
		Transform ct = new Transform(gc.getDevice());
		float s = 200f/contentWidth;
		ct.translate(bounds.width, bounds.height);
		ct.scale(s,s);
		ct.translate(-contentWidth, -contentHeight);
		gc.setTransform(ct);
		
		gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_GRAY));
		Rectangle2D contentRect = new Rectangle2D.Float(0, 0, contentWidth, contentHeight);
		PathShape contentPath = new PathShape(gc.getDevice(), contentRect);
		gc.fillPath(contentPath);
		contentPath.dispose();
		ct.dispose();

		gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_YELLOW));
		fillRect(getClientArea(), combinedTransform, gc);
		
		gc.setTransform(bt);
		
	}
	
	private void fillRect(Rectangle rect, Transform transform, GC gc) {
		Transform inv = new Transform(gc.getDevice());
		inv.multiply(transform);
		inv.invert();
		float[] coords = new float[] { rect.x, rect.y, rect.x + rect.width, rect.y + rect.height };
		inv.transform(coords);
		Rectangle2D shape = new Rectangle2D.Float(coords[0], coords[1], coords[2]-coords[0], coords[3]-coords[1]);
		PathShape path = new PathShape(gc.getDevice(), shape);
		gc.fillPath(path);
		path.dispose();
		inv.dispose();
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
		/*
		try {
			// zoomTransform.getElements(elements); // XXX
			combinedTransform.getElements(elements);
			inverseTransform.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
			inverseTransform.invert();
			inverseTransform.transform(p);
		} catch (SWTException ex) {
			ex.printStackTrace();
		}*/
		
		inverseTransform.transform(p);
		return p;
	}
	
	
	private void updateTransform() {
		combinedTransform.identity();
		combinedTransform.multiply(contentTransform);
		combinedTransform.multiply(zoomTransform);
		
		try {
			// zoomTransform.getElements(elements); // XXX
			combinedTransform.getElements(elements);
			inverseTransform.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
			inverseTransform.invert();
		} catch (SWTException ex) {
			ex.printStackTrace();
		}
	}
	
	private void updateCanvasTransform() {
		updateTransform();
		setCanvasTransform(combinedTransform);
	}
	
//==[ Viewport ]====================================================================================

	private float transformedClipping[] = {0,0, 0,0};
	private Rectangle clipping = getClientArea();
	private int scrollBarX = 0, scrollBarY = 0; 
	
	private void respectCanvasBoundsAndUpdateScrollbars() {
		
		clipping = getClientArea();
		
		if (constrained)
			constrainTransform();
		
		if (true) return;
		
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

	// Optionally:
	// 1. constrain size (width/height) (width >= right-left, height >= bottom-top) (sx >= 1, sy >= 1)
	// 2. constrain x1, y1, x2, y2 (translation) (allow corner points to be moved outside the visible area, but not the reverse, e.g. the visible space is always fully covered by the content)
	private void constrainTransform() {
		// if (true) return;
		/*
		float[] p1 = new float[] { 0, 0 };
		float[] p2 = new float[] { contentWidth, contentHeight };
		
		contentTransform.transform(p1);
		contentTransform.transform(p2);
		
		float minx = p1[0];
		float maxx = p2[0] - clipping.width;
		float miny = p1[1];
		float maxy = p2[1] - clipping.height;
		*/
		
		float[] coords = new float[] {
			clipping.x,
			clipping.y,
			clipping.x + clipping.width,
			clipping.y + clipping.height
		};
		
		inverseTransform(coords);

		contentTransform.getElements(elements);
		float sx = elements[0];
		float sy = elements[3];
		
//		zoomTransform.getElements(elements);
		
		float tx = 0;
		float ty = 0;
		
		if (coords[0] < 0) tx = coords[0];
		if (coords[1] < 0) ty = coords[1];
		if (coords[2] > contentWidth) tx = -(contentWidth-coords[2]);
		if (coords[3] > contentHeight) ty = -(contentHeight-coords[3]);
		
//		elements[4] = Math.min(0, elements[4]);
//		elements[5] = Math.min(0, elements[5]);
//		
//		elements[4] = Math.max(-(contentWidth-coords[2])/sx, elements[4]);
//		elements[5] = Math.max(-(contentHeight-coords[3])/sy, elements[5]);
		
		System.out.println("combinedInverse:");
		System.out.println("\tupper left: " + coords[0] + "," + coords[1]);
		System.out.println("\tlower right: " + coords[2] + "," + coords[3]);
		
//		zoomTransform.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
		zoomTransform.translate(sx*tx, sy*ty);
		
//		updateCanvasTransform();
		
		if (true) return;
		
//		inverseTransform.transform
		
		zoomTransform.getElements(elements);
		
		// cap zoom (only allow to zoom in, not to zoom out)
		float s = elements[0];
		s = elements[0] = elements[3] = Math.max(1, s);
/*
		// extra space due to zoom in
//		float extraWidth  = s*clipping.width - clipping.width; // XXX
//		float extraHeight = s*clipping.height - clipping.height; // XXX
		
//		float extraWidth = contentWidth - s*clipping.width;
//		float extraHeight = contentHeight - s*clipping.height;
		
		float extraWidth = clipping.width - clipping.width/s;
		float extraHeight = clipping.height - clipping.height/s;
		
		// cap translation to negative values (don't allow to move out of the client area to the left/top)
//		elements[4] = Math.min(0, elements[4]);
//		elements[5] = Math.min(0, elements[5]);
		elements[4] = Math.min(minx, elements[4]);
		elements[5] = Math.min(miny, elements[5]);
		
		// cap translation to at most the extra space generated by the zoom (prevents moving out to the right/bottom)
//		elements[4] = Math.max(-extraWidth, elements[4]);
//		elements[5] = Math.max(-extraHeight, elements[5]);
		elements[4] = Math.max(-extraWidth, elements[4]);
		elements[5] = Math.max(-extraHeight, elements[5]);
		*/
		
		zoomTransform.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);

	}

}
