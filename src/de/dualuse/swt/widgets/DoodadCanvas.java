package de.dualuse.swt.widgets;

import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.ALT;
import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.BUTTON3;
import static org.eclipse.swt.SWT.MouseDoubleClick;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.MouseWheel;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.Paint;

import java.util.Arrays;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.PathShape;

public class DoodadCanvas extends Canvas implements Renderable, Listener {
	
	public DoodadCanvas(Composite parent, int style) {
		super(parent, style);
		addListener(Paint, this);
		addListener(MouseUp, this);
		addListener(MouseDown, this);
		addListener(MouseMove, this);
		addListener(MouseWheel, this);
		addListener(MouseDoubleClick, this);
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
	final public void onMouse(Event e) {
		for (Renderable r: children)
			if (e.doit)
				r.onMouse(e);
	}

	@Override
	final public void render(Rectangle clip, Transform t, GC c) {
		for (Renderable r: children)
			r.render(clip, t, c);
	}
	
	Transform canvasTransform = new Transform(getDisplay());
	
	
	@Override
	final public void handleEvent(Event event) {
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
			onMouse(event);
		}
	}
	
	////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app);
		
		sh.setLayout(new FillLayout());
		
		DoodadCanvas dc = new DoodadCanvas(sh, NONE);
		Doodad d = new Doodad(dc) {
			protected boolean onMouseDown(float x, float y, int button, int modifierKeys) {
				System.out.println("clocked");
				return true;
			};
		}
//		.setSize(100, 100);
//		.rotate(0.5)
		.translate(100, 100)
		.scale(.5, .5);
		
		
		Doodad e = new Doodad(d) {
			boolean in = false;
			protected void onMouseEnter() { 
				in = true;
				System.out.println("IN");
				dc.redraw();
			};
			
			protected boolean onMouseClick(float x, float y, int button, int modifierKeys) {
				System.out.println("REAL CLICK!");
				return true;
			};
			protected boolean onDoubleClick(float x, float y, int button, int modifierKeys) {
				System.out.println("huhu");
				return true;
			};
			
			protected void onMouseExit() { 
				in = false; 
				System.out.println("OUT");
				dc.redraw();
			};
			
			float xl, yl;
			@Override
			protected boolean onMouseDown(float x, float y, int button, int modifierKeys) {
				System.out.println("clicked!");
				xl = x;
				yl = y;
				return true;
			}
			
			protected boolean onMouseWheel(float x, float y, int tickCount, int modifierKeys) {
//				System.out.println(tickCount);
				
				translate(x,y);
				
				if (modifierKeys==ALT)
					rotate( tickCount*0.01337 );
				else
					scale( pow(1.0337, tickCount));
				
				translate(-x,-y);

				dc.redraw();
				return true;
			};
			
			protected boolean onMouseMove(float x, float y, int modifierKeysAndButtons) {
				if (modifierKeysAndButtons==BUTTON1)
					translate(x-xl, y-yl);
				
				dc.redraw();
				return true;
			};
			
			@Override
			protected void render(GC c) {
				c.setLineAttributes(new LineAttributes(in?5:1));
				PathShape p = new PathShape(app, new java.awt.Rectangle(0, 0, 100, 100));
				c.drawPath(p);
				p.dispose();
			}
		}.setSize(100, 100);
		
		
		dc.addListener(MouseDown, (ev) -> {
			for (Renderable rnd: dc.children)
				rnd.onMouse(ev);
				
//			System.out.println((ev.stateMask&ALT)!=0);
//			System.out.println((ev.stateMask&MOD1)!=0);
		});
		
		dc.addListener(MouseMove, (ev) -> {
			sh.setText( ((ev.stateMask&BUTTON1)!=0?"ONE ":"") + ((ev.stateMask&BUTTON3)!=0?"TWO ":""));
		});
		
		dc.addMouseMoveListener( (ev) -> sh.setText( ((ev.stateMask&BUTTON1)!=0)?"dragged":"moved" ) );
		
		long then = System.nanoTime();
		dc.addPaintListener( (ev) -> {
			long now = System.nanoTime();

//			Doodad p = d; 
//			
//			p.identity();
//			
//			AffineTransform at = new AffineTransform();
//			at.translate(100, 100);
//			at.scale(.5, .5);
//			at.translate(50, 50);
////			at.rotate( 45*Math.PI /180 );
//			at.rotate( (now-then)/1e9 );
//			at.translate(-50, -50);
//
//			d.set(at);
			
			
//			d.identity();
//			d.translate(100, 100);
//			d.scale(.5, .5);
//			
////			d.translate(+50, +50);
////			d.rotate( (now-then)/1e9 );
////			d.translate(-50, -50);
//			
//			e.identity();
//			e.rotate( (now-then)/1e9, 50, 50);
			
//			dc.redraw();
		});
		
		
		
		
		
		sh.setBounds(1500, 150, 800, 600);
		sh.setVisible(true);
		app.loop(sh);
		
		
//		System.out.println(getVersion());
	}



}
