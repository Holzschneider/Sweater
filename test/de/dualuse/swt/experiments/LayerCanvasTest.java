package de.dualuse.swt.experiments;

import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.layout.FillLayout;
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

