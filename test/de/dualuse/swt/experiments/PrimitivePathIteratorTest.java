package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.RC;

public class PrimitivePathIteratorTest {
	public static void main(String[] args) {
		Application app = new Application();
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		
		Canvas ca = new Canvas(sh, NONE);

		ca.addPaintListener( (e) -> {
			try (RC r = new RC(e.gc)) {
				r.begin(RC.LINES);
					r.vertex(10, 10);
					r.vertex(100, 100);
					
					r.vertex(110, 200);
					r.vertex(220, 120);
				r.end();
				
				r.begin(RC.TRIANGLES);
					r.vertex(400, 10);
					r.vertex(510, 200);
					r.vertex(320, 220);
					
					r.vertex(600, 80);
					r.vertex(710, 200);
					r.vertex(520, 320);
				r.end();
				
				r.begin(RC.QUADS);
					r.vertex(500, 310);
					r.vertex(610, 500);
					r.vertex(400, 520);
					r.vertex(420, 280);
					
					r.vertex(700, 380);
					r.vertex(810, 500);
					r.vertex(820, 720);
					r.vertex(620, 680);
				r.end();
				
				r.begin(RC.LINE_STRIP);
					r.vertex(100, 310);
					r.vertex(210, 500);
					r.vertex(100, 520);
					r.vertex(120, 280);
					
					r.vertex(300, 380);
					r.vertex(310, 500);
					r.vertex(380, 620);
					r.vertex(220, 680);
				r.end();
					
				r.begin(5);
					r.vertex(100, 610);
					
					r.vertex(210, 800);
					r.vertex(100, 820);
					r.vertex(60, 680);
					r.vertex(80, 500);
					r.vertex(220, 510);
					r.vertex(210, 800);
				r.end();				
			}
		});
		
		sh.setBounds(1200, 150, 900, 900);
		sh.setVisible(true);
		
		app.loop(sh);
		
	}
}
