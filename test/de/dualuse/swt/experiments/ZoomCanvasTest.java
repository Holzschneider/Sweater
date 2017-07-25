package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.H_SCROLL;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.V_SCROLL;

import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.widgets.ZoomCanvas;

public class ZoomCanvasTest {

	public static void main(String[] args) {
		Application app = new Application();
		Shell shell = new Shell(app);
		shell.setLayout(new FillLayout());
		
		Image image = new Image(Display.getCurrent(), Microscope.class.getResourceAsStream("generic-cat.jpeg"));
		
		System.out.println( image.getImageData().width );
		System.out.println( image.getImageData().height );
		
		Random r = new Random(1337);
		
		ZoomCanvas zc = new ZoomCanvas(shell, NONE|H_SCROLL|V_SCROLL);
		zc.addPaintListener((e) -> {
			
			// System.out.println("Painting");
			
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
		
		});
		
		zc.setCanvasBounds(0, 0, 1129, 750); //1f/0f);
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
