package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.ALT;
import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.BUTTON2;
import static org.eclipse.swt.SWT.BUTTON3;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.SHIFT;

import java.awt.geom.Point2D;
import java.util.ArrayDeque;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;

public class LayerCanvasFunctionTest2 {

	public static void main(String[] args) {
		
		Application app = new Application();
		
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		LayerCanvas lc = new LayerCanvas(sh, NONE);
		
		ArrayDeque<Point2D.Float> trail = new ArrayDeque<Point2D.Float>();
		Layer a = new Layer(lc) 
				.setExtents(0, 0, 200, 200)
				.preTranslate(300, 300)
//				.rotate(.3)
//				.setClipping(true);
				;
		
		
		Listener li =  (e) -> {
////			System.out.println(e.x+ ", "+e.y);
//			a.locate(e.x, e.y, (LayerLocation) (x,y) -> System.out.println(x+", "+y) );
			Point2D.Float p = a.intersect(e.x, e.y, (x,y) -> new Point2D.Float(x,y));
			trail.add(p);
			while (trail.size()>30)
				trail.removeFirst();
			a.redraw();
			
//			a.getRoot()
		};
		
		a.addListener(MouseMove,li);
		
//		public void onMouseMove(float x, float y, Event e) {
//			if (e.stateMask==BUTTON1) {
//				System.out.println(x+", "+y);
//				trail.add(new Point2D.Float(x,y));
//				while (trail.size()>30)
//					trail.removeFirst();
//					
//				redraw();
////				lc.redraw();
//			}
//		}
		
		
		a.onPaint( (e) -> {
			e.gc.setBackground(app.getSystemColor(SWT.COLOR_CYAN));
			e.gc.fillRectangle(a.getBounds());
			
			if (trail.size()>0) {
				Path q = new Path(app);
	 			for (Point2D.Float p: trail) {
//	 				q.moveTo(trail.get(0).x, trail.get(0).y);
	 				q.moveTo(p.x-1, p.y-1);
					q.lineTo(p.x+1, p.y+1);
					q.moveTo(p.x+1, p.y-1);
					q.lineTo(p.x-1, p.y+1);
	 			}
	 			
	 			e.gc.drawPath(q);
	 			q.dispose();	 			
			}
		});
		
		
		
		Layer b = new Layer(a)
//				.setClipping(true)
				.setExtents(-50, -50, 50, 50)
				.preTranslate(100, 100);
		
		
		
		b.onPaint( (e) -> {
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
				b.preRotate(2);
			
			if (e.stateMask==(BUTTON2|ALT))
				a.preRotate(1);
			
			if (e.stateMask==(BUTTON2|SHIFT)) 
				a.removeListener(MouseMove, li);
		});
		
		lc.addListener(MouseMove, (e) -> {
			if (e.stateMask==BUTTON1)
				b.preRotate( (e.x-last.x)*0.01 );
			
			if (e.stateMask==BUTTON3)
				a.preRotate( (e.x-last.x)*0.01 );

			if (e.stateMask==(BUTTON3|ALT)) {
				lc.getCanvasTransform();
			}
			
			last.x = e.x;
			last.y = e.y;
		});
		
		
		
		
		sh.setBounds(1200,200,800,800);
		sh.setVisible(true);
		
		
		app.loop(sh);
		
		
		
	}
}
