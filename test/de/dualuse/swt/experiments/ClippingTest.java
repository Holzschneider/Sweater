package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;

public class ClippingTest {
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell();
		sh.setLayout(new FillLayout());
		
		Canvas c = new Canvas(sh, NONE);
		
		c.addPaintListener((e) -> {
			
			
			Transform t = new Transform(app);
			
			t.rotate(20);
			
			e.gc.setTransform(t);
			e.gc.setClipping(100, 100, 300, 300);
			
			e.gc.setBackground(app.getSystemColor(COLOR_DARK_GRAY));
			e.gc.fillOval(80, 80, 400, 200);
			
			
		});
		
		
		
		
		
		
		
		
		sh.setBounds(1100, 200, 800, 800);
		sh.setVisible(true);
		
		app.loop(sh);
	}
}
