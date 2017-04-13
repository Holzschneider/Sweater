package de.dualuse.swt.widgets;

import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;

public class ScrollPane extends Composite {
	
	Layout scrollLayout = new Layout() {
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			return new Point(wHint, hHint);
		}

		protected void layout(Composite composite, boolean flushCache) {
			
		}
	};

	public ScrollPane(Composite parent, int style) {
		super(parent, style);
		super.setLayout(scrollLayout);
	}
	
	@Override
	public void setLayout(Layout layout) {
		throw new RuntimeException("Layout configuration is not allowed");
	}
	
	
	
	
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app);
		
		
		Image im = new Image(app, ScrollPane.class.getResourceAsStream("generic-cat.jpeg"));
		
		
		ScrollPane sp = new ScrollPane(sh, NONE);
		Canvas c = new Canvas(sp, NONE);
		
		c.addPaintListener((e) -> {
			e.gc.drawImage(im, 0, 0);
		});
		
		
		
		
		sh.setBounds(100, 100, 800, 600);
		sh.setVisible(true);
		
		app.loop();
		
	}

}
