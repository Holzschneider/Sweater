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

public class LayerCanvas extends Canvas implements LayerContainer, Listener {
	
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
	
	protected void handleMouseEvent(Event e) {}

	protected void renderBackground(Rectangle clip, Transform t, GC gc) { }
	
	final protected void render(Rectangle clip, Transform t, GC c) {
		renderBackground(clip, t, c);
		
		for (Layer r: children)
			r.render(clip, t, c);
	}
	
	protected Transform canvasTransform = new Transform(getDisplay());
	
	
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
	
	////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app);
		
		sh.setLayout(new FillLayout());
		
		LayerCanvas dc = new LayerCanvas(sh, NONE);
		Layer d = new Layer(dc) {
			@Override protected boolean onMouseDown(float x, float y, int button, int modifierKeys) {
				System.out.println("clocked");
				return true;
			};
		}
//		.setSize(100, 100);
//		.rotate(0.5)
		.translate(100, 100)
		.scale(.5, .5);
		
		
		Layer f = new Layer(d) {
			protected boolean onMouseMove(float x, float y, int modifierKeysAndButtons) {
				if (modifierKeysAndButtons==BUTTON1)
					translate(x-xl, y-yl);
				
				dc.redraw();
				return true;
			};
		
			
		
			float xl, yl;
			@Override
			protected boolean onMouseDown(float x, float y, int button, int modifierKeys) {
				moveAbove(null);
				System.out.println("clicked!");
				xl = x;
				yl = y;
				return true;
			}
			
			@Override
			protected void render(GC c) {
				c.setLineAttributes(new LineAttributes(1));
				PathShape p = new PathShape(app, new java.awt.Rectangle(0, 0, 100, 100));
				c.drawPath(p);
				p.dispose();
			}
		}.setBounds(0, 0, 100, 100).translate(300, 100);
		
		Layer e = new Layer(d) {
			boolean in = false;
			protected boolean onMouseEnter() { 
				in = true;
				System.out.println("IN");
				dc.redraw();
				
				return false;
			};
			
			protected boolean onMouseClick(float x, float y, int button, int modifierKeys) {
				System.out.println("REAL CLICK!");
				return true;
			};
			protected boolean onDoubleClick(float x, float y, int button, int modifierKeys) {
				System.out.println("huhu");
				return true;
			};
			
			protected boolean onMouseExit() { 
				in = false; 
				System.out.println("OUT");
				dc.redraw();
				return true;
			};
			
			float xl, yl;
			@Override
			protected boolean onMouseDown(float x, float y, int button, int modifierKeys) {
				moveAbove(null);
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
		}.setBounds(0,0,100, 100);
		
		
		dc.addListener(MouseDown, (ev) -> {
			for (Layer rnd: dc.children)
				rnd.point(ev);
				
//			System.out.println((ev.stateMask&ALT)!=0);
//			System.out.println((ev.stateMask&MOD1)!=0);
		});
		
		dc.addListener(MouseMove, (ev) -> {
			sh.setText( ((ev.stateMask&BUTTON1)!=0?"ONE ":"") + ((ev.stateMask&BUTTON3)!=0?"TWO ":""));
		});
		
		dc.addMouseMoveListener( (ev) -> sh.setText( ((ev.stateMask&BUTTON1)!=0)?"dragged":"moved" ) );
		
		
		dc.addListener(MouseDown, (ev) -> {
			
			System.out.println("RIGHT DOWN: "+((ev.stateMask & BUTTON3)!=0));
			
			
		});
		
		
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
