package de.dualuse.swt.experiments;

import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.PathShape;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;

public class LayerCanvasTest {
	
		public static void main(String[] args) {
			
			Application app = new Application();
			Shell sh = new Shell(app);
			
			sh.setLayout(new FillLayout());
			
			LayerCanvas dc = new LayerCanvas(sh, NONE);
			Layer d = new Layer(dc) {
				@Override public void onMouseDown(float x, float y, Event event) {
					System.out.println("clocked");
				}
			}
	//		.setSize(100, 100);
//			.rotate(0.5)
			.translate(100, 100)
			.scale(.5, .5);
			
			dc.addListener(SWT.KeyDown, (e) -> {
				if (e.keyCode == SWT.ESC)
					sh.dispose();
			});
			
			Layer f = new Layer(d) {
				@Override public void onMouseMove(float x, float y, Event event) {
					if ((event.stateMask&BUTTON1) != 0)
						translate(x-xl, y-yl);
					dc.redraw();
				}
			
				float xl, yl;
				@Override public void onMouseDown(float x, float y, Event event) {
					moveTop();
					System.out.println("clicked!");
					xl = x;
					yl = y;
				}
				
				@Override public void onPaint(Event e) {
					e.gc.setLineAttributes(new LineAttributes(1));
					PathShape p = new PathShape(app, new java.awt.Rectangle(0, 0, 100, 100));
					e.gc.drawPath(p);
					p.dispose();
				}
			}.setExtents(0, 0, 100, 100).translate(300, 100);
			
			Layer e = new Layer(d) {
				boolean in = false;
				
				@Override public void onMouseEnter(float x, float y, Event event) { 
					in = true;
					System.out.println("IN");
					dc.redraw();
				}
				
				@Override public void onMouseClick(float x, float y, Event event) {
					System.out.println("REAL CLICK!");
				}
				
				@Override public void onMouseDoubleClick(float x, float y, Event event) {
					System.out.println("huhu");
				}
				
				@Override public void onMouseExit(float x, float y, Event event) { 
					in = false; 
					System.out.println("OUT");
					dc.redraw();
				}
				
				float xl, yl;
				@Override public void onMouseDown(float x, float y, Event event) {
					moveAbove(null);
					System.out.println("clicked!");
					xl = x;
					yl = y;
				}
				
				@Override public void onMouseWheel(float x, float y, Event event) {
	//				System.out.println(tickCount);
					
					int tickCount = event.count;
					
					translate(x,y);
					
					if ((event.stateMask&ALT) != 0)
						rotate( tickCount*0.01337 );
					else
						scale( pow(1.0337, tickCount));
					
					translate(-x,-y);
	
					dc.redraw();
				}
				
				@Override public void onMouseMove(float x, float y, Event event) {
					if ((event.stateMask&BUTTON1)!=0)
						translate(x-xl, y-yl);
					
					dc.redraw();
				}
				
				@Override public void onPaint(Event e) {
					e.gc.setLineAttributes(new LineAttributes(in?5:1));
					PathShape p = new PathShape(app, new java.awt.Rectangle(0, 0, 100, 100));
					e.gc.drawPath(p);
					p.dispose();
				}
			}.setExtents(0,0,100, 100);
			
			
			dc.addListener(MouseDown, (ev) -> {
				for (Layer rnd: dc.getLayers())
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

