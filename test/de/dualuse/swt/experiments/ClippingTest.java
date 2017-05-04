package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.events.Listeners;

public class ClippingTest {
	public static void main(String[] args) {
		
		
		Listener myListener = null;
		
		
		myListener = (e)-> System.out.print("bla");
		
		
//		new Listeners(myListener).join( (e) -> System.out.println("bla") ).exclude(l);
		
		
		
		
		Application app = new Application();
		Shell sh = new Shell();
		sh.setLayout(new FillLayout());
		
		Canvas c = new Canvas(sh, NONE);
		
		c.addPaintListener((e) -> {
			Transform t = new Transform(app);
			
			t.rotate(45);
			
			e.gc.setTransform(t);
			e.gc.setClipping(100, 100, 300, 300);
			
			e.gc.setBackground(app.getSystemColor(COLOR_DARK_GRAY));
			e.gc.fillOval(80, 80, 400, 200);			
			
//			BasicStroke bs = new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, new float[] { 10 }, 0);
//			new PathShape(app,bs.createStrokedShape(new Rectangle2D.Double(100,100,200,200)));
		});
		
		
		
		
		
		
		
		
		sh.setBounds(1100, 200, 800, 800);
		sh.setVisible(true);
		
		app.loop(sh);
	}
}
