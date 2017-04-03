package de.dualuse.swt.experiments.mac;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ScrollPaneTest {
	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell sh = new Shell(dsp);
		sh.setLayout(new FillLayout());

		
		ScrolledComposite sc = new ScrolledComposite(sh, H_SCROLL|V_SCROLL|NO_BACKGROUND);
		Image im = new Image(dsp, ScrollPaneTest.class.getResourceAsStream("generic-cat.jpeg"));
		
		im.handle.setCacheMode(1);
		
		Canvas c = new Canvas(sc,NO_BACKGROUND);
		c.setSize( im.getImageData().width*2, im.getImageData().height*2 );
		
		c.addPaintListener(new PaintListener() {
			Point s = c.getSize();
			Image offscreen = new Image(dsp,s.x,s.y); 
			GC offscreenGc = new GC(offscreen);
			int count = 0;
			Transform t = new Transform(c.getDisplay());
			public void paintControl(PaintEvent e) {
				
				paintComponent(e.gc);
//				if (count++==0)
//					paintComponent(offscreenGc);
//				
//				e.gc.drawImage(offscreen, 0, 0);
//				e.gc.dispose();
							}
			
			private void paintComponent(GC g) {
				g.getTransform(t);
				t.scale(2, 2);
				g.setTransform(t);
				g.drawImage(im, 0, 0);
				t.scale(.5f, .5f);
				g.setTransform(t);

			}
			
		});
		sc.setContent(c);
		
//		Label l = new Label(c, NONE|NO_REDRAW_RESIZE);
//		l.setImage( im );
//		l.setSize( im.getImageData().width*2, im.getImageData().height*2 );
//		sc.setContent(l);
		
		sh.setBounds(100, 100, im.getImageData().width*2, im.getImageData().height );
		sh.setVisible(true);
		
		
		
		while(!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
	}
}
