package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.graphics.BufferedImageData;

public class GCBufferedImageDataTest {
	public static void main(String[] args) {

		Display dsp = new Display();
		Shell sh = new Shell(dsp);
		
		///////
		sh.setLayout(new FillLayout());
		
		Canvas c = new Canvas(sh,NONE);
		
		
		BufferedImageData bid = new BufferedImageData(1000, 1000);
		Graphics2D g2 =  bid.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2.setStroke(new BasicStroke(10,BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, new float[] {20},0));
		g2.setColor(new Color(1f, 0.5f, 1f, 0.8f));
		g2.drawLine(0, 0, 400, 200);
		Image im = new Image(dsp, bid.data);
		
		
		
		Point to = new Point(200,200);
		c.addMouseMoveListener( (e) -> { to.x = e.x; to.y = e.y; c.redraw(); } );
		
		c.addPaintListener( (e) -> {
			Point s = c.getSize();
			e.gc.drawLine(0, 0, s.x, s.y);
			
			e.gc.drawImage(im, 0, 0);
			
			
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
