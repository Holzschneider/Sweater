package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.H_SCROLL;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.ON;
import static org.eclipse.swt.SWT.Paint;
import static org.eclipse.swt.SWT.V_SCROLL;

import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.ZoomCanvas;

public class ZoomCanvasTest2 {

	public static void main(String[] args) {
		Application app = new Application();
		Shell shell = new Shell(app);
		shell.setLayout(new FillLayout());
		
		Image image = new Image(Display.getCurrent(), Microscope.class.getResourceAsStream("generic-cat.jpeg"));
		
		System.out.println( image.getImageData().width );
		System.out.println( image.getImageData().height );
		
		Random r = new Random(1337);
		ZoomCanvas zc = new ZoomCanvas(shell, NONE|H_SCROLL|V_SCROLL);
		zc.addListener(Paint, (e)-> {
			System.out.println("eins: "+e.time);
		});
		
		zc.addPaintListener((e) -> {
			e.gc.setAntialias(SWT.OFF);
			e.gc.setInterpolation(SWT.NONE);
			e.gc.setLineWidth(1);
			e.gc.drawImage(image, 0, 0);
			e.gc.drawLine(0, 0, 100, 100);

			Color c = new Color(app, new RGB(r.nextFloat()*360, 0.8f, 0.7f));
			e.gc.setAlpha(64);
			e.gc.setBackground(c);
			e.gc.fillRectangle(0, 0, image.getImageData().width, image.getImageData().height);
			c.dispose();
			
			e.gc.setAlpha(255);
			
			System.out.println("zwei: "+e.time);
		});
		
		zc.addListener(Paint, (e)-> {
			System.out.println("drei: "+e.time);
		});
		
		zc.setCanvasBounds(0, 0, 1129, 750); //1f/0f);
		
		Layer f = new LayerCanvasTest2.Frame(zc) {
			protected void paint(GC c) {
				c.setAntialias(ON);
				c.setLineAttributes(new LineAttributes(1));
				
//				super.paint(c); // XXX commented out to get test class running again as not compatible with code changes
				
				System.out.println("holymoly");
				System.out.println();
				
			};
		}.translate(100,200).scale(.5);
		
		
		
//		zc.zoomX = true;
//		zc.zoomX = false;
//		zc.zoomY = false;
		zc.relative = zc.widthPinned = false;
//		zc.zoomY = false;
//		zc.scrollY = false;
		
//		new Browser(shell,  SWT.NONE).setUrl("http://news.ycombinator.com");
		
		shell.setBounds(100, 100, 1400, 500);
		shell.setVisible(true);
		app.loop(shell);
	}
	

}
