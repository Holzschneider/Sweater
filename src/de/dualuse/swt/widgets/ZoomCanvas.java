package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.H_SCROLL;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.MouseWheel;
import static org.eclipse.swt.SWT.Paint;
import static org.eclipse.swt.SWT.V_SCROLL;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.experiments.Microscope;

public class ZoomCanvas extends Canvas implements PaintListener, Listener, ControlListener {
	private ArrayList<Listener> listeners = new ArrayList<Listener>();
	private ArrayList<PaintListener> paintListeners = new ArrayList<PaintListener>(); 
			
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
			break;
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
	final public void controlMoved(ControlEvent e) {
		System.out.println("MOVE");
	}

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
		
		
		respectCanvasBounds();

			
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
		respectCanvasBounds();
		
//		fireStateChanged();
		
		setLocation(p, q);
		redraw();
	}

	private void mouseScrolled(Event e) {
		setLocation(q, e);
		
		if (!zoomX && !zoomY)
			return;
		
		inverseTransform(q);
		
		canvasTransform.translate(q[0], q[1]);
					
		float sx = (float)Math.pow(1.0337, e.count * (zoomX?1:0));
		float sy = (float)Math.pow(1.0337, e.count * (zoomY?1:0));
		
		canvasTransform.scale(sx, sy);
		canvasTransform.translate(-q[0],  -q[1]);
		respectCanvasBounds();
		
//		fireStateChanged();
		
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

	
	float bounds[] = {0,0, 0,0};
	
	private void respectCanvasBounds() {
		
		Rectangle r = getClientArea();
		for (int i=0;i<2;i++) {
			
			bounds[0] = r.x;
			bounds[1] = r.y;
	
			bounds[2] = r.x+r.width;
			bounds[3] = r.y+r.height;
			
			inverseTransform(bounds);
	
			float constrainX = 0, constrainY = 0;
	
			if (bounds[0]<=left && right<=bounds[2])
				constrainX = (left-bounds[0])/2f + (right-bounds[2])/2f;
			else
			if (bounds[0]<left)
				constrainX = (left-bounds[0]);
			else
			if (right<bounds[2])
				constrainX = (right-bounds[2]);
			
			if (bounds[1]<=top && bottom<=bounds[3])
				constrainY = (top-bounds[1])/2f + (bottom-bounds[3])/2f;
			else
			if (bounds[1]<top)
				constrainY = (top-bounds[1]);
			else
			if (bottom<bounds[3])
				constrainY = (bottom-bounds[3]);
			
			canvasTransform.translate(-constrainX, -constrainY);
		}		
		/////////////
		
		
		
		if ((getStyle()&H_SCROLL)==H_SCROLL) {
			ScrollBar sb = getHorizontalBar();
			if (Float.isFinite(left) && Float.isFinite(right)) {
				float scaleX = r.width/(bounds[2]-bounds[0]);
				
				float min = scaleX*left;
				float max = scaleX*right;
				
				float lower = scaleX*bounds[0];
				float higher= scaleX*bounds[2];
				
				sb.setMinimum(0);
				sb.setMaximum((int)(max-min));
				sb.setThumb((int)(higher-lower));
				sb.setSelection((int)(lower-min));
			} else
				sb.setEnabled(false);
		}

		if ((getStyle()&V_SCROLL)==V_SCROLL) {
			ScrollBar sb = getVerticalBar();
			if (Float.isFinite(top) && Float.isFinite(bottom)) {
				float scaleY = r.height/(bounds[3]-bounds[1]);
				
				float min = scaleY*top;
				float max = scaleY*bottom;
				
				float lower = scaleY*bounds[1];
				float higher= scaleY*bounds[3];
				
				sb.setMinimum(0);
				sb.setMaximum((int)(max-min));
				sb.setThumb((int)(higher-lower));
				sb.setSelection((int)(lower-min));
			} else
				sb.setEnabled(false);
		}
	}
	
//==[ Test-Main ]===================================================================================

	
	public static void main(String[] args) {
		Application app = new Application();
		Shell shell = new Shell(app);
		shell.setText("SWTMicroscope");
		shell.setLayout(new FillLayout());
		
		Image image = new Image(Display.getCurrent(), Microscope.class.getResourceAsStream("generic-cat.jpeg"));
		
		System.out.println( image.getImageData().width );
		System.out.println( image.getImageData().height );
		
		ZoomCanvas zc = new ZoomCanvas(shell, SWT.NONE|H_SCROLL|V_SCROLL);
		zc.addPaintListener((e) -> {
			e.gc.setAntialias(SWT.OFF);
			e.gc.setInterpolation(SWT.NONE);
			e.gc.setLineWidth(1);
			e.gc.drawImage(image, 0, 0);
			e.gc.drawLine(0, 0, 100, 100);
		});
		
		zc.setCanvasBounds(0, 0, 1129, 750); //1f/0f);
//		zc.zoomX = zc.zoomY = false;
		zc.relative = zc.widthPinned = false;
//		zc.zoomY = false;
//		zc.scrollY = false;
		
//		new Browser(shell,  SWT.NONE).setUrl("http://news.ycombinator.com");
		
		shell.setBounds(100, 100, 1400, 800);
		shell.setVisible(true);
		app.loop(shell);
	}
	

}
