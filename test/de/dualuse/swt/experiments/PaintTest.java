package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;

import java.awt.geom.RoundRectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.PathShape;

public class PaintTest {

	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		Canvas c = new Canvas(sh,NONE);
		
		
		Image im  =new Image(app, PaintTest.class.getResourceAsStream("generic-cat.jpeg"));
		c.addPaintListener(new PaintListener() {
			Transform t = new Transform(app); 
			
			LineAttributes attr = new LineAttributes(1f, SWT.CAP_FLAT, SWT.JOIN_MITER);
			
			float angle = 0;
			
			public void paintControl(PaintEvent e) {

				GC gc = e.gc;
				
				t.identity();
				t.translate(100,100);
				t.scale(.5f, .5f);
				
				t.translate(+50, +50);
				
				t.rotate(angle);
				
				t.translate(-50, -50);
				
				e.gc.setTransform(t);

				e.gc.drawImage(im, 0, 0);
				
//				e.gc.getGCData().state |= 1 << 14;
				e.gc.setLineAttributes(attr);
				
				PathShape p = new PathShape(app, new RoundRectangle2D.Double(0,0,100,100,30,30));
				e.gc.drawPath( p );
				p.dispose();
				
//				e.gc.drawRectangle(0, 0, 100, 100);
				angle += 0.128f;
				c.redraw();
			}
		});
		
		
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);
		
		app.loop(sh);
		
		
	}
}
