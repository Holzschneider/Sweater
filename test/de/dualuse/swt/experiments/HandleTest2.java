package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.widgets.Handle;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;

public class HandleTest2 {
	public static void main(String[] args) {
		Application app = new Application();
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		LayerCanvas lc = new LayerCanvas(sh, NONE);
		
		
		Layer z = new Layer(lc);
		
		Handle a = new Handle(z).translate(100, 100);
		Handle b = new Handle(z).translate(200, 200);
		
		
		z.addPaintListener((e) -> {
			e.gc.drawLine( (int)a.centerX, (int)a.centerY, (int)b.centerX, (int)b.centerY);
		});
		
		
		
		sh.setBounds(1200, 200, 800, 800);
		sh.setVisible(true);
		
		
		app.loop(sh);
		
	}
}
