package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.widgets.Handle;
import de.dualuse.swt.widgets.LayerCanvas;

public class HandleTest {

	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		
		LayerCanvas lc = new LayerCanvas(sh, NONE);
		
		Handle a = new Handle(lc).translate(100, 100);
		Handle b = new Handle(lc).translate(200, 100);
		
		
		lc.addPaintListener((e) -> {
			e.gc.drawLine( (int)a.getCenterX(), (int)a.getCenterY(), (int)b.getCenterX(), (int)b.getCenterY());
		});
		
		
		
		sh.setBounds(1200, 200, 800, 800);
		sh.setVisible(true);
		
		
		app.loop(sh);
		
	}
}
