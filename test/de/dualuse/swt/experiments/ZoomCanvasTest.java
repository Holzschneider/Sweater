package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
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
		
		ZoomCanvas zc = new ZoomCanvas(shell, NONE|H_SCROLL|V_SCROLL);
		zc.addPaintListener((e) -> {
			e.gc.setAntialias(SWT.OFF);
			e.gc.setInterpolation(SWT.NONE);
			e.gc.setLineWidth(1);
			e.gc.drawImage(image, 0, 0);
			e.gc.drawLine(0, 0, 100, 100);
		});
		
		zc.setCanvasBounds(0, 0, 1129, 750); //1f/0f);
		zc.zoomX = true;
		zc.zoomY = false;
		zc.relative = zc.widthPinned = false;
//		zc.zoomY = false;
//		zc.scrollY = false;
		
//		new Browser(shell,  SWT.NONE).setUrl("http://news.ycombinator.com");
		
		shell.setBounds(100, 100, 1400, 800);
		shell.setVisible(true);
		app.loop(shell);
	}
	

}
