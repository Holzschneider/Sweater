package de.dualuse.swt.experiments;


import static java.lang.Math.*;

import java.awt.geom.Line2D;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.RC;

public class RCTest3 {
	public static void main(String[] args) {
		
		ImageData data = new ImageData(RCTest3.class.getResourceAsStream("generic-cat.jpeg"));
		
		Application app = new Application();
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		Canvas c = new Canvas(sh,0);
		c.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Point size = c.getSize();
				try (RC rc = new RC(e.gc)) {
					
//					double s = min(size.x*1.0/data.width,size.y*1.0/data.height);
//					rc.viewport(0, 0, size.x, size.y);
//					rc.ortho(0, size.x, size.y, 0, -size.y, +size.y);
////					rc.scale(s, s, s);
//					
//					rc.draw(new Line2D.Double(0, 0, 100, 100));
					
					rc.viewport(100, 100, size.x-200, size.y-200);
//					rc.ortho(0, 1, 1, 0, -1, 1);
//					rc.ortho(0,size.x, size.y, 0, -1,1);
//					rc.draw(new Line2D.Double(-1, -1, 1, 1));
//					Image im = new Image(app,data);
//					rc.drawImage(im, 0, 0);
					
//					rc.ortho(0, data.width, 0, data.width, -1, 1);
					rc.ortho(0, 1, 0, 1, -1, 1);
					
					rc.begin(RC.LINES);
//					rc.vertex(-1,-1);
//					rc.vertex(+1,+1);
//					rc.vertex(+1,-1);
//					rc.vertex(-1,+1);
					
					rc.vertex(0,0);
					rc.vertex(+1,+1);
//					rc.vertex(data.width, data.height);
					rc.end();
					
//					rc.ortho(0,size.x, size.y, 0, -1,1);
//					Line2D l1 = new Line2D.Double(0, 0, size.x, size.y);
//					rc.draw(l1);
					
					
				}
			}
		});
		
		
		sh.setBounds(-1200, 150, 800, 600);
		sh.setVisible(true);
		
		app.loop(sh);
		
		
	}
}
