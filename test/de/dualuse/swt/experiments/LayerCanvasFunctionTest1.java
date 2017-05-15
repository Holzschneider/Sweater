package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.BUTTON3;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;

public class LayerCanvasFunctionTest1 {

	public static void main(String[] args) {
		
		Application app = new Application();
		
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		LayerCanvas lc = new LayerCanvas(sh, NONE);
		
		Layer a = new Layer(lc)
				.setLimits(0, 0, 200, 200)
				.translate(300, 300)
				.rotate(.3)
//				.setClipping(true);
				;
		
		a.debug = 1;
		
		a.addPaintListener( (e) -> {
			Color c = new Color(app, new RGB((float) (Math.random()*360), .4f, 1f));
			
//			e.gc.setBackground(app.getSystemColor(SWT.COLOR_CYAN));
			e.gc.setBackground(c);
			e.gc.fillRectangle(a.getBounds());
			
			c.dispose();
		});
		
		Layer b = new Layer(a)
//				.setClipping(true)
				.setLimits(-50, -50, 50, 50)
				.translate(100, 100);
		
		b.debug = 2;
		
		b.addPaintListener( (e) -> {
			e.gc.setBackground(app.getSystemColor(SWT.COLOR_RED));
			e.gc.fillRectangle(b.getBounds());
		} );
		
		
		Point last = new Point(0,0);
		lc.addListener(MouseDown, (e) -> {
			last.x = e.x;
			last.y = e.y;
		});
		
		lc.addListener(MouseUp, (e) -> {
			if (e.stateMask==SWT.BUTTON2)
				b.rotate(2);
			
			if (e.stateMask==(SWT.BUTTON2|SWT.ALT))
				a.rotate(1);
		});
		
		lc.addListener(MouseMove, (e) -> {
			if (e.stateMask==BUTTON1)
				b.rotate( (e.x-last.x)*0.01 );
			
			if (e.stateMask==SWT.BUTTON3)
//				a.translate((e.x-last.x),(e.y-last.y));
//				System.out.println("drei");
				a.rotate( (e.x-last.x)*0.01 );
			
			last.x = e.x;
			last.y = e.y;
		});
		
		
		
		
		
		
		
		sh.setBounds(1200,200,800,800);
		sh.setVisible(true);
		
		
		app.loop(sh);
		
		
		
	}
}
