package de.dualuse.swt.experiments;


import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;

public class TransformTest {
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		Canvas c = new Canvas(sh,NONE);

		c.addPaintListener(new PaintListener() {
			Transform t = new Transform(app);
			
			public void paintControl(PaintEvent e) {
				
				
				float M[] = {
					1,0,1,1, 100,100	
				};
				
				
				t.setElements(M[0], M[1], M[2], M[3], M[4], M[5]);
				e.gc.setTransform(t);
				e.gc.drawRectangle(0, 0, 300, 300);
				
				
			}
		});
		
		
		
		
		
		sh.setBounds(1100, 200, 800, 600);
		sh.setVisible(true);
		app.loop(sh);
		
	}
}
