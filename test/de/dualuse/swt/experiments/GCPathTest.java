package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.ON;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.graphics.PathShape;

public class GCPathTest {
	final static int S = 1;
	
	public static void main(String[] args) {
		
		Display dsp = new Display();
		Shell sh = new Shell(dsp);
		
		///////
		sh.setLayout(new FillLayout());
		
		Canvas c = new Canvas(sh,NONE);
		
		Transform scaleUp = new Transform(dsp);
		scaleUp.scale(1f/S, 1f/S);
		
		
		Color white = new Color(dsp, 0,0,255);
		Point to = new Point(200,200);
		c.addMouseMoveListener( (e) -> { to.x = e.x; to.y = e.y; c.redraw(); } );
		
		long start = System.nanoTime();
		c.addPaintListener( (e) -> {
			int left = 100, top = 100;
			e.gc.setBackground(white);
			
			e.gc.setAntialias(ON);

			e.gc.setBackgroundPattern(new Pattern(dsp, 100, 100, 200, 300, new Color(dsp, 100, 100, 30),new Color(dsp,20,200,100)));
			
			
			PathShape p = new PathShape(dsp);

			float R = to.x/5f;
			
			////////////////// AWT.GEOM.SHAPE
			Area a = new Area();
			a.add(new Area(new RoundRectangle2D.Double(100, 100, 500, 320, R, R)));
			a.subtract(new Area(new Ellipse2D.Double(left*S, top*S*4, (to.x-left)*S/10f, (to.y/S-top)*S/10f)));
			
			Rectangle2D r = new Rectangle2D.Double(300,100,200,200);
			Shape ts = AffineTransform.getRotateInstance((System.nanoTime()-start)/1e9, 400, 200).createTransformedShape(r);
			a.subtract( new Area( ts ) );
			//////////////////
			
			p.addShape(a);
			
			e.gc.fillPath(p);
			
			p.dispose();
			
			
			c.redraw();
		});
		
		
		///////
		sh.setBounds(100, 100, 800, 600);
		sh.setVisible(true);
		while(!sh.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
		
	}
}
