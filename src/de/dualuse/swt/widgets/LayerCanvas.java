package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.Dispose;
import static org.eclipse.swt.SWT.MouseDoubleClick;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.MouseWheel;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.Paint;

import java.util.Arrays;

import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class LayerCanvas extends Canvas implements LayerContainer, Listener {
	
	protected Transform canvasTransform = new Transform(getDisplay());
	
	protected int transformCount = 0, globalCount = 0;

	private Layer children[] = {};
	
//==[ Constructors ]================================================================================
	
	public LayerCanvas(Composite parent) { this(parent,NONE); }
	
	public LayerCanvas(Composite parent, int style) {
		super(parent, style);
		
		super.addListener(Paint, this);
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
	
	@Override public void handleEvent(Event event) {
		switch (event.type) {
			case Paint:
				canvasTransform.getElements(backup);
				event.gc.setLineAttributes(new LineAttributes(1));
				paint(event.gc.getClipping(), canvasTransform, event);
				canvasTransform.setElements(backup[0], backup[1], backup[2], backup[3], backup[4], backup[5]);
				break;
			
			case MouseDown:
			case MouseUp:
			case MouseMove:
			case MouseWheel:
			case MouseDoubleClick:
				point(event);
		}
	}

	final protected void point(Event e) {
		for (int i=children.length-1, I=0; i>=I; i--) {
			Layer r = children[i];
			if (e.doit)
				if (r.captive()==captive) //either captive == null, or set to a specific layer
					r.point(e);
		}
	}
	
	@Override public void capture(Layer c) {
		captive = c;
	}
	
//==[ Rendering ]===================================================================================
	
	final protected void paint(Rectangle clip, Transform t, Event c) {
		for (int I=0,i=children.length-1; I<=i; I++)
			children[I].paint(clip,t,c);
		paintOverlay(clip, t, c);
	}

	protected void paintOverlay(Rectangle clip, Transform t, Event c) {
		// Override if additional overlay unaffected by canvasTransform should be painted
	}
	
	@Override public void redraw(float x, float y, float width, float height, boolean all) {
		this.redraw((int)x, (int)y, (int)width, (int)height, all);
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
	
}



