package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;

import java.util.concurrent.atomic.AtomicInteger;

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

		wnd.setLayout(new FillLayout());
		
		Canvas c = new Canvas(wnd, NONE);
		
		ResourcePool rp = new ResourcePool(app);
//		System.out.println( rp.color(100,100,255) );
		
		AtomicInteger ai = new AtomicInteger(5);
		
		c.addPaintListener( (e) -> {
			try (ResourcePool r = rp) {
				
				Color c1 = r.color(100,100,255);
				e.gc.setBackground(c1);
				
				e.gc.fillRectangle(100, 100, 400, 300);
				
				Color c2 = r.color(100,100,255);
				
				System.out.println( c1==c2 );
			}
			
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
