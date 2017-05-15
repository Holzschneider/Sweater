package de.dualuse.swt.experiments;

import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.COLOR_BLUE;
import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;

public class LayerCanvasFunctionalTest3 {
	
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell();
		sh.setLayout(new FillLayout());
		
		LayerCanvas lc = new LayerCanvas(sh, NONE);
		
		Layer a = new Layer(lc) {
			@Override
			public void onMouseWheel(float x, float y, Event e) {
				double s = pow(1.01337, e.count);
				preScale(s, s, x, y);
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
					preTranslate(x-dx, y-dy);
			}
			
			@Override
			protected void onPaint(Event e) {
//				getCanvasTransform((sx,a1,a2,a3,a4,a5) -> e.gc.setLineAttributes(new LineAttributes(1/sx)) ); 
				
				e.gc.setAlpha(100);
				
				for (int i=0;i<50;i++) {
					e.gc.drawLine(0, i*30, 1000, i*30);
					e.gc.drawLine(i*30, 0, i*30, 1000);
				}
			}
		};
		
		
		Layer b = new Layer( a ) {
			int S = 10, R = 5;
			{
				setExtents(-S, -S, S, S);
			}
			
			protected void onPaint(Event e) {
				e.gc.setAlpha(255);
				e.gc.setLineAttributes( new LineAttributes(R) );
				e.gc.drawOval(-S+R/2, -S+R/2, 2*S-R, 2*S-R);
				
//				e.gc.drawLine(0, 0, 100, 100);
				
//				e.gc.setBackground(app.getSystemColor(COLOR_BLUE));
//				e.gc.fillRectangle(getBounds());
//				
//				getLayerTransform((sx,a1,a2,a3,px,py) -> System.out.println(px+", "+py));
			};
			
			protected boolean validateTransform() {
				getCanvasTransform((sx,a1,a2,a3,a4,a5) -> preScale(1/sx));
				
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
					preTranslate(x-dx, y-dy);
			}
			
		}.postTranslate(200, 200);
		
		
		
		sh.setBounds(1200, 200, 800, 800);
		sh.setVisible(true);
		
		app.loop(sh);
		
	}

}
