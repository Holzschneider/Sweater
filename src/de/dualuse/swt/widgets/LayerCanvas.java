package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class LayerCanvas extends Canvas implements LayerContainer, Listener {
	
	protected Transform canvasTransform = new Transform(getDisplay());
	
	protected int transformCount = 0, globalCount = 0;

	private Layer children[] = {};
	private Layer snapshot[] = {};
	
//==[ Constructors ]================================================================================
	
	public LayerCanvas(Composite parent) { this(parent,NONE); }
	
	public LayerCanvas(Composite parent, int style) {
		super(parent, style);
		enablePlatformTweaks();
		
		super.addListener(Paint, this); 
		//XXX suggestion: add Layers as paint listeners upon addLayer and remove them upon removelayer, for paint order
		
		
		super.addListener(MouseUp, this);
		super.addListener(MouseDown, this);
		super.addListener(MouseMove, this);
		super.addListener(MouseWheel, this);
		super.addListener(MouseDoubleClick, this);
		
		super.addListener(Dispose, this::onDispose);
	}
	
	protected void onDispose(Event event) {
		for (Layer child : children)
			child.dispose();
		
		canvasTransform.dispose();
		originalTransform.dispose();
	}
	
//==[ Child Layers ]================================================================================

	@Override public Layer[] getLayers() {
		return children;
	}
	
	@Override public int indexOf(Layer r) {
		for (int i=0,I=children.length;i<I;i++)
			if (children[i]==r)
				return i;
				
		return -1;
	}
	
	@Override public LayerCanvas addLayer(Layer r) {
		(children = Arrays.copyOf(children, children.length+1))[children.length-1]=r;
		r.setRoot(this);
		
		return this;
	}
	
	@Override public LayerCanvas removeLayer(Layer r) {
		
		int i = indexOf(r);
		if (i>=0) {
			
			// Remove child without changing the z-order of the remaining children 
			Layer[] newchildren = new Layer[children.length-1];
			for (int j=0, J=children.length, k=0; j<J; j++) {
				if (j==i) continue;
				newchildren[k++] = children[j];
			}
			children = newchildren;
			
			r.setRoot(null);

			redraw(); // XXX only layer bounds?
			
		}
		
		return this;
	}

//==[ Event Handling ]==============================================================================
	
	private Layer captive = null;
	private float[] backup = new float[6];

	Transform originalTransform = new Transform(Display.getCurrent());
	
	@Override public void handleEvent(Event event) {
		updateSnapshot();
		switch (event.type) {
			case Paint:
				
				event.gc.getTransform(originalTransform);
				
				canvasTransform.getElements(backup);
				event.gc.setLineAttributes(new LineAttributes(1));
				paint(event.gc.getClipping(), canvasTransform, event);
				canvasTransform.setElements(backup[0], backup[1], backup[2], backup[3], backup[4], backup[5]);
				
				event.gc.setTransform(originalTransform);
				paintOverlay(event.gc.getClipping(), null, event);
				
				dirty.clear();
				break;
				
			case MouseDown:
			case MouseUp:
			case MouseMove:
			case MouseWheel:
			case MouseDoubleClick:
				point(event);
		}
		
		clearSnapshot();
	}

	final protected void point(Event e) {
		for (int i=snapshot.length-1, I=0; i>=I; i--) {
			Layer r = snapshot[i];
			if (e.doit && r.captive()==captive && r.isVisible() && r.isEnabled()) //either captive == null, or set to a specific layer
				r.point(e);
		}
	}

	// Used by Handle.startDrag() which programmatically starts a dragging operation to reset the last captive
	@Override public void resetCaptive() {
		Layer cap = captive;
		if (cap != null)
			while (cap.captive() != null && cap.captive()!=cap)
				cap = cap.captive();
		if (cap!=null)
			cap.capture(null);
	}
	
	@Override public void capture(Layer c) {
		captive = c;
	}
	
	@Override public Layer captive() {
		return captive;
	}
	
	private void updateSnapshot() {
		if (snapshot.length!=children.length)
			snapshot = new Layer[children.length];
		for (int i=0, I=children.length; i<I; i++)
			snapshot[i] = children[i];
	}
	
	private void clearSnapshot() {
		Arrays.fill(snapshot, null);
	}
	
//==[ Rendering ]===================================================================================
	
	Bounds dirty = new Bounds();
	Rectangle dirtyRect = new Rectangle(0, 0, 0, 0); 
	
	final protected void paint(Rectangle clip, Transform t, Event c) {
		paintBackground(clip, t, c);
		
		for (int I=0,i=snapshot.length-1; I<=i; I++)
			snapshot[I].paint(clip,t,c);
		// paintOverlay(clip, t, c);
	}

	protected void paintBackground(Rectangle clip, Transform t, Event c) {
		// Override if background unaffected by canvasTrasnform should be painted
	}
	
	protected void paintOverlay(Rectangle clip, Transform t, Event c) {
		// Override if additional overlay unaffected by canvasTransform should be painted
	}
	
	@Override public void redraw(float x, float y, float width, float height, boolean all) {
		this.redraw((int)x, (int)y, (int)width, (int)height, all);
	}

	@Override
	public void redraw() {
		if (dirty.isEmpty() || dirty.isFinite())
			super.redraw();
		
		dirty.setExtents(-1f/0, -1f/0, +1f/0f, +1f/0f);
	}

	@Override public void redraw(int x, int y, int width, int height, boolean all) {
		if (dirty.isEmpty())
			super.redraw(x, y, width, height, all);
		else {
			dirty.getBounds(dirtyRect);
			if (!dirtyRect.contains(x, y) ||
				!dirtyRect.contains(x+width-1,y) ||
				!dirtyRect.contains(x+width-1,y+height-1) ||
				!dirtyRect.contains(x,y+height-1))
				super.redraw(x, y, width, height, all);
		}
	}
	
//==[ CanvasTransform ]=============================================================================
	
	public void setCanvasTransform(Transform matrix) {
		globalCount++;
		transformCount++;
		canvasTransform.identity();
		canvasTransform.multiply(matrix);
	}
	
	public void getCanvasTransform(Transform matrix) {
		matrix.identity();
		matrix.multiply(canvasTransform);
	}
	
	///// Coordinate Transformations
	
	@Override public <T> T transform(double x, double y, TransformedCoordinate<T> i) {
		return i.define((float)x, (float)y);
	}

	@Override public <T> T transform(double x, double y, Layer b, TransformedCoordinate<T> i) {
		return b.invert(x, y, i);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	private void enablePlatformTweaks() {
		try {
			
			Class<?> osClass = Class.forName("org.eclipse.swt.internal.cocoa.OS");
			Method sel_registerNameMethod = osClass.getMethod("sel_registerName", String.class);
			Method objc_msgSendMethod = osClass.getMethod("objc_msgSend", long.class, long.class, boolean.class);
			Object setWantsLayerSelector = sel_registerNameMethod.invoke(osClass, "setWantsLayer:");
			
			Class<?> canvasClass = Canvas.class;
			Field viewField = canvasClass.getField("view");
			Object viewValue = viewField.get(this);
			Class<?> viewClass = viewValue.getClass();
			Field idField = viewClass.getField("id");
			Object idValue = idField.get(viewValue);
			
			objc_msgSendMethod.invoke(osClass, idValue, setWantsLayerSelector, true);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
}



