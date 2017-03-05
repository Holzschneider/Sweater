package de.dualuse.swt;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.internal.cocoa.NSBitmapImageRep;
import org.eclipse.swt.internal.cocoa.NSImage;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;



public class ScrollCanvasTest {
	static boolean scroll = false; 
	
	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell sh = new Shell(dsp);
		sh.setLayout(new FillLayout());

		Image im = new Image(dsp, ScrollCanvasTest.class.getResourceAsStream("generic-cat.jpeg")) ;
//		NSBitmapImageRep rep = new NSBitmapImageRep(im.handle.representations().objectAtIndex(0).id);
//		System.out.println( rep.id+": "+rep.description().getString() ) ;
//		im.handle.removeRepresentation(rep);
//		
//		CIImage ciim = new CIImage().initWithBitmapImageRep(rep);
//		
//		NSCIImageRep cirep = new NSCIImageRep().initWithCIImage(ciim);
//		
//		im.handle.addRepresentation(cirep);
//		
//		System.out.println(im.handle.representations().objectAtIndex(0));
//		System.out.println( rep.id+": "+new NSBitmapImageRep(im.handle.representations().objectAtIndex(0).id).description().getString() ) ;
		
		
		
		Point q = new Point(0,0);
		Canvas c = new Canvas(sh,NO_BACKGROUND);		
		c.addPaintListener(new PaintListener() {
			Transform t = new Transform(c.getDisplay());

			public void paintControl(PaintEvent e) {
				paintComponent(e.gc);
			}
			
			private void paintComponent(GC g) {
				g.getTransform(t);
				t.translate(q.x, q.y);
				t.scale(2, 2);
				g.setTransform(t);
				g.drawImage(im, 0, 0);
				t.scale(.5f, .5f);
				g.setTransform(t);				
			}
		});

		
		Point p = new Point(0,0);
		Listener scroller = (e) -> {
			Point s = c.getSize();
			
			if (scroll)
				c.scroll(e.x-p.x, e.y-p.y, 0, 0, s.x, s.y, false);
			else
				c.redraw();
			
			q.x += e.x-p.x;
			q.y += e.y-p.y;
			
			p.x = e.x; 
			p.y = e.y; 
		};
		
		Listener switcher = (e) -> sh.setText("scoll: "+(scroll=!scroll)) ;
		switcher.handleEvent(null);
		
		c.addListener(MouseDown, (e) -> { c.addListener(MouseMove, scroller); p.x = e.x; p.y = e.y; } );
		c.addListener(MouseUp, (e) -> c.removeListener(MouseMove, scroller) );

		c.addListener(MouseDoubleClick, switcher );
		
		
		sh.setBounds(100, 100, im.getImageData().width*2, im.getImageData().height );
		sh.setVisible(true);
		
		
		while(!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
	}
}
