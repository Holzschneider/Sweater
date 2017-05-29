package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.*;

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
		//XXX suggestion: add Layers as paint listeners upon addLayer and remove them upon removelayer, for paint order
		
		
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
		
//		for (int i=0,I=children.length;i<I;i++)
//			if (children[i]==r) {
//				r.setParent(null);
//				children[i] = children[children.length-1]; // XXX z Order
//				children = Arrays.copyOf(children, children.length-1);
//			}
//		
//		r.setRoot(null);
		
		return this;
	}
	
	final protected void point(Event e) {
		// for (Layer r: children) {
		for (int i=children.length-1, I=0; i>=I; i--) {
			Layer r = children[i];
			if (e.doit)
				if (r.captive()==captive) //either captive == null, or set to a specific layer
					r.point(e);
		}
	}
	
	private Layer captive = null;
	
	@Override
	public void capture(Layer c) {
		captive = c;
	}
	
	final protected void paint(Rectangle clip, Transform t, Event c) {
		// for (int I=children.length-1,i=0;I>=i;I--)
		for (int I=0,i=children.length-1; I<=i; I++)
			children[I].paint(clip,t,c);
	}
	
	private void disposer(Event e) {
		layerTransform.dispose();
	}
	
	private float[] backup = new float[6];
	private Transform layerTransform = new Transform(getDisplay());
	public void setLayerTransform(Transform matrix) {
		globalCount++;
		transformCount++;
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
				dirty.clear();
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

	Bounds dirty = new Bounds();
	Rectangle dirtyRect = new Rectangle(0, 0, 0, 0); 
	
	@Override
	public void redraw() {
		if (dirty.isEmpty() || dirty.isFinite())
			super.redraw();
		
		dirty.setExtents(-1f/0, -1f/0, +1f/0f, +1f/0f);
	}

	@Override
	public void redraw(int x, int y, int width, int height, boolean all) {
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

}



