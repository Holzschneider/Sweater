package de.dualuse.swt.experiments;

import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;

import java.awt.geom.Point2D;

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
		
		LayerCanvas lc = new LayerCanvas(sh);
		Layer z = new Layer(lc);

		Point2D.Float l = new Point2D.Float();
		z.addListener(MouseMove, (e) -> l.setLocation(e.x,e.y));
		z.addListener(MouseMove, (e) -> z.postTranslate( (e.x-l.x)*e.stateMask/BUTTON1, (e.y-l.y)*e.stateMask/BUTTON1));
		z.addMouseWheelListener( (e) -> z.postScale(pow(1.01337,e.count), e.x, e.y) );
		
		Handle a = new Handle(z).translate(100, 100);
		Handle b = new Handle(z).translate(200, 200);
		z.addPaintListener( (e) -> a.center( (ax,ay) -> b.center( (bx,by ) -> e.gc.drawLine( (int)ax, (int)ay, (int)bx, (int)by) )) );


//		z.addListener(MouseDown, (e) -> l.setLocation(lc.transform(e.x, e.y, z, (x,y) -> new Point2D.Float(x, y))));
//		z.addListener(MouseMove, (e) -> lc.transform(e.x, e.y, z, (x,y) -> z.translate( (x-l.x)*e.stateMask/BUTTON1, (y-l.y)*e.stateMask/BUTTON1)));
//		z.addMouseWheelListener( (e) -> lc.transform(e.x,e.y, z, (x,y) -> z.scale(pow(1.01337,e.count), x, y)) );
		
		sh.setBounds(1200, 200, 800, 800);
		sh.setVisible(true);
		
		
		app.loop(sh);
		
	}
}
