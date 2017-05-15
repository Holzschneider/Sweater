package de.dualuse.swt.experiments;

import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.COLOR_WHITE;
import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
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
		
		Layer a = new Layer(lc) {
			@Override
			public void onMouseWheel(float x, float y, Event e) {
				double s = pow(1.01337, e.count);
				scale(s, s, x, y);
			}

			float dx = 0, dy = 0;
			@Override
			public void onMouseDown(float x, float y, Event e) {
				dx = x;
				dy = y;
			}
			
			@Override
			public void onMouseMove(float x, float y, Event e) {
				if (e.stateMask==BUTTON1)
					translate(x-dx, y-dy);
			}
			
			@Override
			protected void onPaint(Event e) {
//				getCanvasTransform((sx,a1,a2,a3,a4,a5) -> e.gc.setLineAttributes(new LineAttributes(1/sx)) ); 
				
				e.gc.setAlpha(64);
				
				for (int i=0;i<50;i++) {
					e.gc.drawLine(0, i*30, 1000, i*30);
					e.gc.drawLine(i*30, 0, i*30, 1000);
				}
				e.gc.setAlpha(255);

			}
		};
		
		
		Layer b = new Layer( a ) {
			int S = 9, R = 6;
			{
				setExtents(-S, -S, S, S);
			}
			
			protected void onPaint(Event e) {
				e.gc.setAlpha(255);
				LineAttributes la = e.gc.getLineAttributes();
				
				e.gc.setLineAttributes( new LineAttributes(R) );
				e.gc.drawOval(-S+R/2, -S+R/2, 2*S-R, 2*S-R);
				
				e.gc.setForeground(app.getSystemColor(COLOR_WHITE));
				e.gc.setLineAttributes( new LineAttributes(R-2) );
				e.gc.drawOval(-S+R/2, -S+R/2, 2*S-R, 2*S-R);
				
				
				e.gc.setLineAttributes(la);
//				e.gc.setBackground(app.getSystemColor(WHITE));
				
//				e.gc.drawLine(0, 0, 100, 100);
				
//				e.gc.setBackground(app.getSystemColor(COLOR_BLUE));
//				e.gc.fillRectangle(getBounds());
//				
//				getLayerTransform((sx,a1,a2,a3,px,py) -> System.out.println(px+", "+py));
			};
			
			protected boolean validateTransform() {
				getCanvasTransform((sx,a1,a2,a3,a4,a5) -> scale(1/sx));
				
				return super.validateTransform();
			};
			
			float dx = 0, dy = 0;
			@Override
			public void onMouseDown(float x, float y, Event e) {
				dx = x;
				dy = y;
			}
			
			@Override
			public void onMouseMove(float x, float y, Event e) {
				if (e.stateMask==BUTTON1)
					translate(x-dx, y-dy);
			}
			
		}.translate(200, 200);
		
		
		Layer c = new Handle(a)
				.translate(300, 200);
//				.locate(0,0).on(b);
		
		
		sh.setBounds(1200, 200, 800, 800);
		sh.setVisible(true);
		
		app.loop(sh);
		
	}

}
