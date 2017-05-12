package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.RC;

public class RCTest {

	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		sh.setBounds(1200,200,800,600);
		
		Canvas c = new Canvas(sh, NONE);
		
		long start = System.nanoTime();
		c.addPaintListener((e) -> {
			RC rc = new RC(e.gc);

			
			Point size = c.getSize();
			rc.viewport(0, 0, size.x, size.y);

			double r = size.y*1.0/size.x;
			rc.frustum(-1, 1, r, -r, 2, 10);
			rc.translate(0, 0, 4);
			
			
			
			
			double t = (System.nanoTime()-start)/1e9;
			rc.rotate( 1,0,0, t);
			rc.rotate( 0,1,0, t);
			
			rc.translate(0, 0, -1);
			rc.draw(new RoundRectangle2D.Double(-1,-1,2,2, .3,.3));
			rc.translate(0, 0, 2);
			rc.draw(new RoundRectangle2D.Double(-1,-1,2,2, .3,.3));
			
			

			rc.dispose();
			
			
			c.redraw();
		});
		
		
		sh.setVisible(true);
		app.loop(sh);
				
	}
}



