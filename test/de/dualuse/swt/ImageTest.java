package de.dualuse.swt;

import static org.eclipse.swt.SWT.*;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.*;
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
		
//		Image im = new Image(d, new FileInputStream(new File("/Users/holzschneider/Desktop/Sitzordnung.pdf")));
		Image im = new Image(d,ImageTest.class.getResource("generic-cat.jpeg").openStream() );
		ImageData id = im.getImageData();
		int[] pixels = new int[id.width*id.height];

		for (int y=0;y<id.height;y++)
			id.getPixels(0, y, id.width, pixels, y*id.width);
		
		for (int y=0;y<100;y++)
			for (int x=0;x<100;x++)
				pixels[x+y*id.width] = ((x&y)>0)?0:0xFFFFFF;

		for (int y=0;y<id.height;y++)
			id.setPixels(0, y, id.width, pixels, y*id.width);


		im.handle.setCacheMode(1);
		
		Image im2 = new Image(d, id);
		
		GC g = new GC(im2);
		g.drawLine(0, 0, id.width, id.height);
		g.dispose();
		
		
		Shell sh = new Shell(d);
		sh.setLayout(new FillLayout());
		
		Canvas c = new Canvas(sh,  SWT.NO_BACKGROUND );
		
		Point b = new Point(0, 0);
		Point t = new Point(0, 0);
//		Transform tx = new Transform(d);
		
		c.addPaintListener((e) -> {
//			e.gc.getTransform(tx);
//			tx.scale(.5f, .5f);
//			e.gc.setTransform(tx);
			e.gc.drawImage(im2, t.x-b.x, t.y-b.y);
			e.gc.dispose();
		});
		
		MouseMoveListener mml = (e) -> {
					t.x = e.x;
					t.y = e.y;
					c.redraw();
//					c.redraw(0, 0, 100, 100, false);
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
				d.sleep();
		
	}
}
