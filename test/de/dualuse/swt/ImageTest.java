package de.dualuse.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ImageTest {
	public static void main(String[] args) throws Exception {
		
		
		Display d = new Display();
		
		Display.setAppName("Image Test");
		Display.setAppVersion("1.0");
		
		
//		BufferedImage bi = ImageIO.read(ImageTest.class.getResource("generic-cat.jpeg"));
		
//		Image im = new Image(d,ImageTest.class.getResource("generic-cat.jpeg").openStream() );		
		Image im = new Image(d,ImageTest.class.getResource("transparency-demonstration.png").openStream() );		

		
		Shell sh = new Shell(d);
		sh.setLayout(new FillLayout());
		
		Canvas c = new Canvas(sh, 0);
		
		
		Point b = new Point(0, 0);
		Point t = new Point(0, 0);
		Transform tx = new Transform(d);
		
		c.addPaintListener((e) -> {
//			e.gc.getTransform(tx);
//			tx.scale(.5f, .5f);
//			e.gc.setTransform(tx);
			e.gc.drawImage(im, t.x-b.x, t.y-b.y);
		});
		
		MouseMoveListener mml = (e) -> {
					t.x = e.x;
					t.y = e.y;
					c.redraw();
				};
				
		c.addListener(SWT.MouseUp, (e) -> c.removeMouseMoveListener(mml) );
		c.addListener(SWT.MouseDown, (e) -> {
			b.x = e.x;//-b.x-t.x;
			b.y = e.y;//-b.y-t.y;
			c.addMouseMoveListener(mml);
		});
		
		
		
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);
		

		
		while (!sh.isDisposed())
			if (!d.readAndDispatch())
				if (d.isDisposed())
					d.sleep();
		
		
	}
}
