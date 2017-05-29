package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import java.awt.BasicStroke;
import java.awt.geom.RoundRectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
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
		
		// sh.setBounds(-1200,200,800,600);
		sh.setBounds(0,200,800,600);
		
		Canvas c = new Canvas(sh, NONE);
		
		BasicStroke as = new BasicStroke(0.1f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, new float[] { 0.2f}, 0);
		BasicStroke bs = new BasicStroke(20f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, new float[] { 50f}, 0);
		BasicStroke cs = new BasicStroke(10f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, new float[] { 20f}, 0);
		
		Image im = new Image(app, RCTest.class.getResourceAsStream("vorfahrt.png"));
//		Image im = new Image(app, RCTest.class.getResourceAsStream("generic-cat.jpeg"));
		ImageData id = im.getImageData();
		
		c.addListener(SWT.KeyDown, (e) -> {
			if (e.keyCode == SWT.ESC)
				sh.dispose();
		});
		
		long start = System.nanoTime();
		c.addPaintListener((e) -> {
			
			
			e.gc.setAntialias(ON);
			e.gc.setInterpolation(ON);
//			e.gc.setTextAntialias(ON);
			
			RC rc = new RC(e.gc);
			
			Point size = c.getSize();
			rc.viewport(0, 0, size.x, size.y);
			
			double s = 2;
			
			double r = size.y*1.0/size.x;
			rc.frustum(-1/s, 1/s, r/s, -r/s, 2/s, 10/s);
			rc.scale(1*s, 1*s, 1*s);
			rc.translate(0, 0, -5);
			
			
			double t = (System.nanoTime()-start)/1e9;
			rc.rotate( 1.0, 1,0,0 );
			rc.rotate(10.2, 0,1,0 );
			rc.rotate( t, 0,0,1);
			rc.rotate( t, 0,1,0);
			rc.rotate( t, 1,0,0);
			

			rc.setStroke(cs);
			
			rc.begin(RC.QUADS);
				rc.vertex(-1, -1, -1);
				rc.vertex(-1, +1, -1);
				rc.vertex(-1, +1, +1);
				rc.vertex(-1, -1, +1);
				
				rc.vertex(+1, -1, -1);
				rc.vertex(+1, +1, -1);
				rc.vertex(+1, +1, +1);
				rc.vertex(+1, -1, +1);
			rc.end();

			
			rc.setStroke(bs);
//			Color col = new Color(e.display, 100, 0, 200);
			rc.setForeground(new RGB(100, 0, 200));
			rc.setBackground(new RGB(100, 190, 0));

			rc.translate(0, 0, -1);
			rc.setFillRule(FILL_WINDING);
			
//			rc.draw(new RoundRectangle2D.Double(-1,-1,2,2, .3,.3));
//			rc.gc.setBackground( new Color(e.display, new RGB(100,100,0)));
					
			rc.fill(as.createStrokedShape(new RoundRectangle2D.Double(-1,-1,2,2, .3,.3)));
			
			rc.setStroke(null);
			rc.translate(0, 0, 2);
			rc.draw(new RoundRectangle2D.Double(-1,-1,2,2, .3,.3));

			rc.setForeground(new RGB(0, 0, 0));

			
			rc.translate(0,0,-1);
			rc.scale(0.05,0.05);
			rc.pushTransform();
			rc.scale(0.05,0.05);
			
//			rc.setBackground(app.getSystemColor(COLOR_GREEN));
			rc.translate(-id.width/2, -id.height/2);
//			rc.fill(new Rectangle2D.Double(0,0,id.width,id.height));


			rc.drawImage(im, 0, 0);
			rc.popTransform();
			

			rc.setAlpha(50);			
			
			rc.drawString("hallo", -10, -5, true);
			

//			col.dispose();
			rc.dispose();

			
			
			c.redraw();
		});
		
		
		sh.setVisible(true);
		app.loop(sh);
				
	}
}



