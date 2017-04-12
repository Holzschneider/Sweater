package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GCFixedPointTest {
	final static int S = 10;
	
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
		
		c.addPaintListener( (e) -> {
			e.gc.setTransform(scaleUp);
			
			int left = 100, top = 100;
			e.gc.setBackground(white);
			e.gc.fillRoundRectangle(left*S, top*S, (to.x-left)*S/10, (to.y-top)*S/10, 100*S, 100*S);
			
			e.gc.dispose();
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
