package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NO_REDRAW_RESIZE;
import static org.eclipse.swt.SWT.SHELL_TRIM;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;

public class NoRedrawResizeTest {

	public static void main(String[] args) {

		Application app = new Application();
		Shell sh = new Shell(app, SHELL_TRIM|NO_REDRAW_RESIZE);

		sh.setLayout(new FillLayout());
		final Canvas canvas = new Canvas(sh, SWT.NO_REDRAW_RESIZE);

		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = canvas.getClientArea();
				e.gc.setBackground(app.getSystemColor(SWT.COLOR_CYAN));
				e.gc.fillOval(0, 0, clientArea.width, clientArea.height);
			}
		});

		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);

		app.loop(sh);

	}
}
