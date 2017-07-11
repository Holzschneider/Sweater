package de.dualuse.swt.experiments;
import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;




public class DrawPerformanceTest {
	static double scale = 1;
	
	static long setWantsLayer = OS.sel_registerName("setWantsLayer:");
//	static long layer = OS.sel_registerName("layer");
//	static long setContents = OS.sel_registerName("setContents:");
//	static long setBackgroundColor = OS.sel_registerName("setBackgroundColor");

	public static void main(String[] args) {
		Display d = new Display();
		
		Shell sh = new Shell(d);
		sh.setLayout(new FillLayout());
		
		Canvas c = new Canvas(sh,NO_BACKGROUND);
//		c.setRedraw(false);
//		sh.setRedraw(false);
		
		
		//////////////////////
		
		OS.objc_msgSend(c.view.id, OS.sel_registerName("setWantsLayer:"), true);
//		long layer_id = OS.objc_msgSend(c.view.id, layer);
		
		
		//////////////////////
		
		
//		OS.objc_msgSend(this.id, OS.sel_addSubview_positioned_relativeTo_, aView != null ? aView.id : 0, place, otherView != null ? otherView.id : 0);
//		OS.objc_msgSend(c.view.id, )
		
		Image im = new Image(d, new ImageData("/Library/Desktop Pictures/El Capitan.jpg"));
		
//		Image im = new Image(d,  1000,1000);
//		im.handle.setCacheMode(0);
//
//		NSArray a = im.handle.representations();
//		id obj = a.objectAtIndex(a.count()-1);
//		NSBitmapImageRep bir = new NSBitmapImageRep(obj);
//		
//		NSString s = bir.description();
//		
////		NSString s = im.handle.representations().description();
//		byte[] bytes = new byte[(int)s.length()];
//		C.memmove( bytes, s.UTF8String(), s.length() );
//		System.out.println( new String(bytes) );
		
		
		AffineTransform at = new AffineTransform();
		
		ArrayList<Point2D> lasso = new ArrayList<Point2D>();
		
		c.addMouseMoveListener(new MouseMoveListener() {
			int lx, ly;
			boolean moved = true;
			public void mouseMove(MouseEvent e) {
				if (e.stateMask==BUTTON3) {
					at.preConcatenate( AffineTransform.getTranslateInstance(e.x-lx, e.y-ly) );
				} else if (e.stateMask==BUTTON1) try {
					lasso.add( at.inverseTransform(new Point2D.Double(e.x, e.y), new Point2D.Double()) );
					if (moved)
						lasso.clear();
					
					moved = false;
					
				} catch (Exception ex) {} else {
					moved = true;
				}
					
				
				lx = e.x;
				ly = e.y;
				
				c.redraw();
			}
		});

		
		
		c.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent e) {
				scale = pow(1.01337, e.count);

				AffineTransform bt = new AffineTransform();
				bt.translate(+e.x, +e.y);
				bt.scale(scale, scale);
				bt.translate(-e.x, -e.y);
				
				at.preConcatenate(bt);
				c.redraw();
			}
		});
		
		
		c.addPaintListener(new PaintListener() {
			Transform transform = new Transform(d);
			Pattern pat = new Pattern(d, im);
			
			@Override
			public void paintControl(PaintEvent e) {
//				e.gc.setBackgroundPattern();
				
				e.gc.getTransform(transform);
				e.gc.setAntialias(OFF);
				e.gc.setInterpolation(NONE);

				transform.setElements((float)at.getScaleX(), (float)at.getShearY(), (float)at.getShearX(), (float)at.getScaleY(), (float)at.getTranslateX(), (float)at.getTranslateY());
				
				e.gc.setTransform(transform);
				e.gc.setBackgroundPattern(pat);
				e.gc.drawImage(im, 0, 0);
				
				Path p = new Path(d);
				for (Point2D q: lasso)
					if (q==lasso.get(0))
						p.moveTo((float)q.getX(), (float)q.getY());
					else
						p.lineTo((float)q.getX(), (float)q.getY());

				///
				
				e.gc.setAntialias(ON);
				e.gc.setInterpolation(HIGH);
				e.gc.setLineAttributes(new LineAttributes(10));

				e.gc.setForeground(d.getSystemColor(SWT.COLOR_YELLOW));
				e.gc.drawPath(p);
				
				p.dispose();
			}
		});
		
		sh.setBounds(100, 100, 1000, 1000);
		sh.setVisible(true);
		
//		OS.objc_msgSend(layer_id, setContents, im.handle.id);
		
		while (!sh.isDisposed())
			if (!d.readAndDispatch())
				d.sleep();
		
				
	}
}
