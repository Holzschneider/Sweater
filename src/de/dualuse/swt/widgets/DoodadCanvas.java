package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.NONE;

import java.awt.geom.AffineTransform;
import java.util.Arrays;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.PathShape;

public class DoodadCanvas extends Canvas implements Renderable, PaintListener {
	
	public DoodadCanvas(Composite parent, int style) {
		super(parent, style);
		addPaintListener(this);
	}

	////////////////////////////////////////////////////////////

	private Renderable parent = null;
	private Renderable children[] = {};

	@Override
	public DoodadCanvas add(Renderable r) {
		(children = Arrays.copyOf(children, children.length+1))[children.length-1]=r;
		return this;
	}

	@Override
	public DoodadCanvas remove(Renderable r) {
		for (int i=0,I=children.length;i<I;i++)
			if (children[i]==r) {
				r.setParentRenderable(null);
				children[i] = children[children.length-1];
				children = Arrays.copyOf(children, children.length-1);
				return this;
			}
		
		return this;
	}
	
	@Override
	public DoodadCanvas transform(float[] m) { return this; }

	@Override
	public void setParentRenderable(Renderable r) {
		parent = r;
	}

	@Override
	public Renderable getParentRenderable() {
		return parent;
	}

	@Override
	final public void onMouse(float x, float y, Event e) {
		
	}

	@Override
	final public void render(Rectangle clip, Transform t, GC c) {
		for (Renderable r: children)
			r.render(clip, t, c);
	}
	
	Transform canvasTransform = new Transform(getDisplay());
	
	@Override
	final public void paintControl(PaintEvent e) {
		canvasTransform.identity();
		render(e.gc.getClipping(), canvasTransform, e.gc);		
	}
	
	
	////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app);
		
		sh.setLayout(new FillLayout());
		
		DoodadCanvas dc = new DoodadCanvas(sh, NONE);
		Doodad d = new Doodad(dc) {
			@Override
			protected void render(GC c) {
				
//				c.drawRectangle(0, 0, (int)getWidth(), (int)getHeight());
				
				PathShape p = new PathShape(app, new java.awt.Rectangle(0, 0, 100, 100));
				c.drawPath(p);
				p.dispose();
			}
		}
		.setSize(100, 100);
//		.translate(100, 100)
//		.scale(.5, .5);

		long then = System.nanoTime();
		dc.addPaintListener( (e) -> {
			long now = System.nanoTime();

			Doodad p = d; 
			
			p.identity();
			
			AffineTransform at = new AffineTransform();
			at.translate(100, 100);
			at.scale(.5, .5);
			at.translate(50, 50);
//			at.rotate( 45*Math.PI /180 );
//			at.rotate( (now-then)/1e9 );
			at.translate(-50, -50);

			d.set(at);
			
			
//			d.identity();
//			d.translate(100, 100);
//			d.scale(.5, .5);
//			
//			d.translate(+50, +50);
//			d.rotate( (now-then)/1e9 );
//			d.translate(-50, -50);
			
//			d.rotate( (now-then)/1e9, 50, 50);
			
			dc.redraw();
		});
		
		
		
		
		
		
		
		sh.setBounds(1500, 150, 800, 600);
		sh.setVisible(true);
		app.loop(sh);
		
		
		
	}


}
