package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.graphics.ResourcePool;

public class ResourcePoolTest {
	public static void main(String[] args) {
		Display app = new Display();
		Shell wnd = new Shell(app);

		wnd.setLayout(new FillLayout(HORIZONTAL));
		
		Canvas c = new Canvas(wnd, NONE);
		
		ResourcePool rp = new ResourcePool(app);
		System.out.println( rp.color(100,100,255) );
		
		AtomicInteger ai = new AtomicInteger(5);
		
		c.addPaintListener( (e) -> {
//			ResourcePool r = rp.push();
//			try (ResourcePool r = rp)
			try (ResourcePool r = rp.push())
//			try (ResourcePool r = new ResourcePool())
			{
				
				Color c1 = r.color(100,100,255);
				e.gc.setBackground(c1);
				e.gc.fillRectangle(100, 100, 400, 300);
				
				Color c2 = r.color(100,100,255);
				
				System.out.println( c1==c2 );
			}
//			r.pop();
		});
		
		
		Canvas d = new Canvas(wnd, NONE);
		d.addPaintListener(new PaintListener() {
			ResourcePool rpd = new ResourcePool(d);
			Color cc = rpd.color(0x336699);
			
			public void paintControl(PaintEvent e) {
				e.gc.setBackground(cc);
				e.gc.fillRectangle(0,0,1000,1000);
				cc.dispose();
			}
		});
		
		d.addListener(MouseDoubleClick, (e) -> {
			d.dispose();
		});
		
//		c.addPaintListener((e) -> {
//				try {
//					if (ai.decrementAndGet()==0) 
//						throw new RuntimeException("hals");
//				} catch(Exception ex) {
////					ex.printStackTrace();
//					e.gc.dispose();
////					throw ex;
//				}
//			} 
//		 );
		
		wnd.setBounds(200, 200, 800, 600);
		wnd.setVisible(true);
		
		while (!app.isDisposed())
			if(!app.readAndDispatch())
				app.sleep();
	}
}
