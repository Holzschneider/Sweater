package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.NO_BACKGROUND;
import static org.eclipse.swt.SWT.NO_REDRAW_RESIZE;
import static org.eclipse.swt.SWT.SHELL_TRIM;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.layout.LayoutDelegate;

public class RedrawTest {

	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app, SHELL_TRIM|NO_REDRAW_RESIZE);
		sh.setLayout(new FillLayout());
		
		Composite c = new Composite(sh, NONE|NO_REDRAW_RESIZE);
		
		Canvas a = new Canvas(c, NO_BACKGROUND|NO_REDRAW_RESIZE);
		Canvas b = new Canvas(c, NO_BACKGROUND|NO_REDRAW_RESIZE);
		
		a.addPaintListener((e) -> e.gc.drawRectangle(0, 0, 399, 599) );
		b.addPaintListener( (e) -> {
			e.gc.drawRectangle(0, 0, 399, 399);
			
			System.out.println(e.x+", "+e.y+", "+e.width+", "+e.height);
		});
		
		
		c.setLayout(new LayoutDelegate().layout( (cmpst, cache) -> {
			
			a.setBounds(100, 100, 400, 600);
			b.setBounds(200,200,400,400);
			
			
		}));
		
		
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);
		
		
		app.loop();
		
	}
}
