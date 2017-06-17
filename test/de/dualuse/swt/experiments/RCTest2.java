package de.dualuse.swt.experiments;


import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.RC;

public class RCTest2 {
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		long start = System.nanoTime();
		Canvas c = new Canvas(sh,0);
		c.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				long now = System.nanoTime();
				
				Point size = c.getSize();
				try (RC rc = new RC(e.gc)) {
					
					double a = size.y*1d/size.x, s = 1;
					rc.viewport(0, 0, size.x, size.y);
					rc.frustum(-1/s, 1/s, -a/s, a/s, 2/s, 10/s);
					rc.scale(s,s,s);
					
					rc.translate(0, 0,-5);

					rc.rotate( (now-start)/1e9, 0,0,1);
					rc.rotate( (now-start)/1e9, 1,0,0);
					
					rc.translate(-0.5,-0.5,-0.5);
					rc.begin(RC.QUADS);
						for (int i=0;i<8;i++)
							rc.vertex(-i>>1&1, +i>>1&1, +i>>2&1);
					rc.end();
					
				}
				
				c.redraw();
			}
		});
		
		
		sh.setBounds(-1200, 150, 800, 600);
		sh.setVisible(true);
		
		app.loop(sh);
		
		
	}
}
