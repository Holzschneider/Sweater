package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CanvasTest {
	public static void main(String[] args) {

		Display dsp = new Display();
		Shell sh = new Shell(dsp, SHELL_TRIM|NO_BACKGROUND);
		sh.setLayout(new FillLayout());

		
		Canvas c = new Canvas(sh, NO_BACKGROUND);
		c.addPaintListener((e) -> {
			Point dim = c.getSize();
			e.gc.drawLine(0, 0, dim.x, dim.y);

			e.gc.setAlpha(100);
			Color col = new Color(e.gc.getDevice(), new RGB(0, 0, 0));
			e.gc.setLineWidth(30);
			e.gc.setLineDash(new int[] {100});
			e.gc.setForeground(col);
			e.gc.drawOval(100, 100, dim.x-200, dim.y-200);		
		});
		
			
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);

		///
		
		while(!sh.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();

		
	}
}
