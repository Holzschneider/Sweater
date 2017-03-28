package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;



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
//				paintComponent(e.gc);
				
				
				System.out.println(e.x+", "+e.y+", "+e.width+", "+e.height);
				
//				Color c = new Color((int)(Math.random()*0xFFFFFF));
				Color c = new Color(dsp, (int)(Math.random()*100)+100,(int)(Math.random()*100)+100,(int)(Math.random()*100)+100);
//						
				e.gc.setBackground(c);
				e.gc.fillRectangle(e.x,e.y,e.width,e.height);
////				e.gc.drawRectangle(e.x+1, e.y+1, e.width-2, e.height-2);
//				
				c.dispose();

//				e.gc.drawLine(0, 0, 100, 100);
				
			}
			
			private void paintComponent(GC g) {
				g.setInterpolation(NONE);
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
