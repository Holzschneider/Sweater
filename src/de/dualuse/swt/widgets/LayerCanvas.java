package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.MouseDoubleClick;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.MouseWheel;
import static org.eclipse.swt.SWT.Paint;

import java.util.Arrays;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class LayerCanvas extends Canvas implements LayerContainer, Listener {

	protected Transform canvasTransform = new Transform(getDisplay());

	public LayerCanvas(Composite parent, int style) {
		super(parent, style);
		addListener(Paint, this);
		addListener(MouseUp, this);
		addListener(MouseDown, this);
		addListener(MouseMove, this);
		addListener(MouseWheel, this);
		addListener(MouseDoubleClick, this);
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
				r.setParentLayer(null);
				children[i] = children[children.length-1];
				children = Arrays.copyOf(children, children.length-1);
			}
		
		r.setRoot(null);
		
		return this;
	}
	
	final protected void point(Event e) {
		for (Layer r: children)
			if (e.doit)
				r.point(e);
		
		if (e.doit)
			handleMouseEvent(e);
	}
	
	final protected void render(Rectangle clip, Transform t, GC c) {
		renderBackground(clip, t, c);
		
		for (int I=children.length-1,i=0;I>=i;I--)
			children[I].render(clip,t,c);
	}
		
	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
		case Paint:
			canvasTransform.identity();
			render(event.gc.getClipping(), canvasTransform, event.gc);
			break;
		
		case MouseDown:
		case MouseUp:
		case MouseMove:
		case MouseWheel:
		case MouseDoubleClick:
			point(event);
		}
	}
	
//==[ To be implemented by subclasses ]=============================================================

	protected void handleMouseEvent(Event e) {}

	protected void renderBackground(Rectangle clip, Transform t, GC gc) { }

}
