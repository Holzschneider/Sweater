package de.dualuse.swt.experiments;

import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.COLOR_WHITE;
import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.widgets.Handle;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;

public class LayerCanvasFunctionTest3 {
	
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell();
		sh.setLayout(new FillLayout());
		
		LayerCanvas lc = new LayerCanvas(sh, NONE);
		
		lc.addListener(SWT.KeyDown, (e) -> {
			if (e.keyCode==SWT.ESC)
				sh.dispose();
		});
		
		// Grid background layer
		Layer a = new Layer(lc) {
			
			@Override public void onMouseWheel(float x, float y, Event e) {
				double s = pow(1.01337, e.count);
				scale(s, s, x, y);
			}

			float startx = 0, starty = 0;
			boolean pressed;
			@Override public void onMouseDown(float x, float y, Event e) {
				startx = x;
				starty = y;
				pressed = true;
			}
			
			@Override public void onMouseUp(float x, float y, Event e) {
				pressed = false;
			}
			
			@Override public void onMouseMove(float x, float y, Event e) {
				
				// danger: can be true before onMouseDown was triggered, then uses wrong starting coordinates
				// if ((e.stateMask&BUTTON1)!=0) {
				
				if (pressed) {
					translate(x-startx, y-starty);
				} else {
					// System.out.println("x: " + x + ", y: " + y);
				}
			}
			
			@Override protected void onPaint(Event e) {
//				getCanvasTransform((sx,a1,a2,a3,a4,a5) -> e.gc.setLineAttributes(new LineAttributes(1/sx)) ); 
				
				e.gc.setAlpha(64);
				
				for (int i=0;i<=50;i++) {
					e.gc.drawLine(0, i*20, 1000, i*20);
					e.gc.drawLine(i*20, 0, i*20, 1000);
				}
				
				e.gc.setAlpha(255);

			}
		};
		
		// Simple layer as a handle
		Layer b = new Layer( a ) {
			int S = 9, R = 6;
			{
				setExtents(-S, -S, S, S);
			}
			
			@Override protected void onPaint(Event e) {
				e.gc.setAlpha(255);
				LineAttributes la = e.gc.getLineAttributes();
				
				e.gc.setLineAttributes( new LineAttributes(R) );
				e.gc.drawOval(-S+R/2, -S+R/2, 2*S-R, 2*S-R);
				
				e.gc.setForeground(app.getSystemColor(SWT.COLOR_RED));
				e.gc.setLineAttributes( new LineAttributes(R-2) );
				e.gc.drawOval(-S+R/2, -S+R/2, 2*S-R, 2*S-R);
				
				e.gc.setLineAttributes(la);
//				e.gc.setBackground(app.getSystemColor(WHITE));
				
//				e.gc.drawLine(0, 0, 100, 100);
				
//				e.gc.setBackground(app.getSystemColor(COLOR_BLUE));
//				e.gc.fillRectangle(getBounds());
//				
//				getLayerTransform((sx,a1,a2,a3,px,py) -> System.out.println(px+", "+py));
			}
			
			@Override protected boolean validateTransform() {
				getCanvasTransform((sx,a1,a2,a3,a4,a5) -> scale(1/sx));
				return super.validateTransform();
			}
			
			float startx = 0, starty = 0;
			@Override public void onMouseDown(float x, float y, Event e) {
				startx = x;
				starty = y;
			}
			
			@Override public void onMouseMove(float x, float y, Event e) {
				if (e.stateMask==BUTTON1)
					translate(x-startx, y-starty);
			}
			
		}.translate(200, 200);
		
		
		// True Handle layer
		Layer c = new Handle(a)
				.translate(300, 200);
//				.locate(0,0).on(b);
		Layer d = new Handle(a)
				.translate(400, 200);
		
		Listener listener1 = new Listener() {
			@Override public void handleEvent(Event event) {
				System.out.println("Listener 1");
			}
		};
		Listener listener2 = new Listener() {
			@Override public void handleEvent(Event event) {
				System.out.println("Listener 2");
			}
		};
		Listener listener3 = new Listener() {
			@Override public void handleEvent(Event event) {
				System.out.println("Listener 3");
			}
		};
		
//		d.addListener(SWT.MouseEnter, l);
//		d.removeListener(SWT.MouseEnter, l);
//
//		d.addListener(SWT.MouseEnter, l);
//		d.removeListener(SWT.MouseEnter, l);
//		d.removeListener(SWT.MouseEnter, l);

		d.addListener(SWT.MouseEnter, listener1);
		d.addListener(SWT.MouseEnter, listener2);
		d.addListener(SWT.MouseEnter, listener3);
		
		d.removeListener(SWT.MouseEnter, listener1);
		
//		d.addPaintListener(new PaintListener() {
//			@Override public void paintControl(PaintEvent e) {
//				e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_RED));
//				e.gc.fillRectangle(-6, -6, 12, 12);
//			}
//		});
		
		sh.setBounds(1200, 200, 800, 800);
		sh.setVisible(true);
		app.loop(sh);
		
	}

}
