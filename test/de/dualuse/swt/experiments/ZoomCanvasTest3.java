package de.dualuse.swt.experiments;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.app.Window;
import de.dualuse.swt.widgets.ZoomCanvas;

public class ZoomCanvasTest3 {
	public static void main(String[] args) {
		Application app = new Application();
		Window w = new Window(app);
//		w.setLayout(new FillLayout());
		
		ZoomCanvas zc = new ZoomCanvas(w,0);
		zc.setBackground(app.getSystemColor(SWT.COLOR_RED));
		
		zc.addPaintListener(new PaintListener() {
			@Override public void paintControl(PaintEvent e) {
				
				e.gc.drawLine(0, 0, 100, 100);
				e.gc.drawRectangle(100,100,200,200);
				
			}
		});
				
		w.setBounds(1200, 150, 800, 800);
		w.setVisible(true);
				
		app.loop(w);
	}
}
