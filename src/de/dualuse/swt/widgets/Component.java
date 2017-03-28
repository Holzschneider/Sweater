package de.dualuse.swt.widgets;

import java.util.Arrays;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.dualuse.swt.events.LayoutDelegate;
import de.dualuse.swt.events.MouseDelegate;
import de.dualuse.swt.events.MouseDragListener;

public class Component extends Canvas {
	
	public Component(Composite parent, int style) {
		super(parent, style);
		addPaintListener(onPaint = this::onPaintControl);
		setLayout(new LayoutDelegate().layout(this::onLayout).computeSize(this::onComputeSize));
	}

	//// PaintListener Handler ////////////////////////////////
	final protected PaintListener onPaint;
	protected void onPaintControl(PaintEvent e) { 
		
	}

	//// Layout Handler ///////////////////////////////////////
	protected void onLayout(Composite composite, boolean flushCache) {
		
	}
	
	protected Point onComputeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		return new Point(0,0);
	}
	
	//// Mouse Handler ////////////////////////////////////////
	//XXX test this!!
	Object[][] listener = {};
	
	public void removeMouseDragListener(MouseDragListener mdl) {
		for (int i=0;i<listener.length;i++)
			if (listener[i][0]==mdl) {
				listener[i] = listener[listener.length-1];
				listener = Arrays.copyOf(listener, listener.length-1);
			}
	}
	
	public void addMouseDragListener(MouseDragListener mdl) {
		MouseListener ml = new MouseDelegate()
				.mouseDown( (e) -> addMouseMoveListener(mdl) )
				.mouseUp( (e) -> removeMouseMoveListener(mdl) );
		
		Object[] listenerPair = { mdl, ml };
		listener=Arrays.copyOf(listener, listener.length+1);
		listener[listener.length-1]=listenerPair;
		
		addMouseListener(ml);
	}
	
	
	public void dispose () {
		if (isDisposed ()) 
			return;
		
		super.dispose();
		onDisposed();
	}
		
	protected void onDisposed() {	}
	
	
	
}
