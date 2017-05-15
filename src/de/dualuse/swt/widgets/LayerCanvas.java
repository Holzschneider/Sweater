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
	
	public LayerCanvas(Composite parent) { this(parent,NONE); }
	
	public LayerCanvas(Composite parent, int style) {
		super(parent, style);
		
		super.addListener(Paint, this);
		super.addListener(MouseUp, this);
		super.addListener(MouseDown, this);
		super.addListener(MouseMove, this);
		super.addListener(MouseWheel, this);
		super.addListener(MouseDoubleClick, this);
		
		super.addListener(Dispose, this::disposer);
	}
	
	
	
	////////////////////////////////////////////////////////////

	private Layer children[] = {};
	
	public Layer[] getLayers() {
		return children;
	}
	
	@Override
	public int indexOf(Layer r) {
		for (int i=0,I=children.length;i<I;i++)
			if (children[i]==r)
				return i;
				
		return -1;
	}
	
	@Override
	public LayerCanvas addLayer(Layer r) {
		(children = Arrays.copyOf(children, children.length+1))[children.length-1]=r;
		r.setRoot(this);
		
		return this;
	}
	
	@Override
	public LayerCanvas removeLayer(Layer r) {
		
		for (int i=0,I=children.length;i<I;i++)
			if (children[i]==r) {
				r.setParent(null);
				children[i] = children[children.length-1];
				children = Arrays.copyOf(children, children.length-1);
			}
		
		r.setRoot(null);
		
		return this;
	}
	
	final protected void point(Event e) {
		for (Layer r: children)
			if (e.doit)
				if (r.captive()==captive) //either captive == null, or set to a specific layer
					r.point(e);
	}
	
	private Layer captive = null;
	
	@Override
	public void capture(Layer c) {
		captive = c;
	}
	
	final protected void paint(Rectangle clip, Transform t, Event c) {
		for (int I=children.length-1,i=0;I>=i;I--)
			children[I].paint(clip,t,c);
	}
	
	private void disposer(Event e) {
		layerTransform.dispose();
	}
	
	private float[] backup = new float[6];
	private Transform layerTransform = new Transform(getDisplay());
	public void setLayerTransform(Transform matrix) {
		globalCount ++;
		transformCount ++;
		layerTransform.identity();
		layerTransform.multiply(matrix);
	}
	
	public void getCanvasTransform(Transform matrix) {
		matrix.identity();
		matrix.multiply(layerTransform);
	}

	
	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
		case Paint:
			layerTransform.getElements(backup);
			event.gc.setLineAttributes(new LineAttributes(1));
			paint(event.gc.getClipping(), layerTransform, event);
			layerTransform.setElements(backup[0], backup[1], backup[2], backup[3], backup[4], backup[5]);
			break;
		
		case MouseDown:
		case MouseUp:
		case MouseMove:
		case MouseWheel:
		case MouseDoubleClick:
			point(event);
		}
	}

	
	/////////////////////////////////////////
	
	
	@Override
	public <T> T transform(double x, double y, TransformedCoordinate<T> i) {
		return i.define((float)x, (float)y);
	}

	@Override
	public <T> T transform(double x, double y, Layer b, TransformedCoordinate<T> i) {
		return b.invert(x, y, i);
	}

	
	@Override
	public void redraw(float x, float y, float width, float height, boolean all) {
		this.redraw((int)x, (int)y, (int)width, (int)height, all);
	}

}



