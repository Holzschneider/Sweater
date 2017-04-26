package de.dualuse.swt.widgets;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Event;


public interface Renderable {
	public Renderable add( Renderable r );
	public Renderable remove( Renderable r );

	public Renderable transform(float[] m); 
	
	public void setParentRenderable( Renderable r );
	public Renderable getParentRenderable();

	public void render( Rectangle clip, Transform t, GC c );
	public void point( Event e );
}
