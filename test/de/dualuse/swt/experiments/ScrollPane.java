package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.DOUBLE_BUFFERED;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.NO_REDRAW_RESIZE;
import static org.eclipse.swt.SWT.SHELL_TRIM;

import java.util.Random;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;

public class ScrollPane extends Canvas {
	
	Layout scrollLayout = new Layout() {
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			return new Point(wHint, hHint);
		}

		protected void layout(Composite composite, boolean flushCache) {
			scrolledLayout();
		}
	};
	
	
	boolean pressed = false;
	Point translation = new Point(0,0);
	Point delta = new Point(0,0);
	
	private void scrolledLayout() {
		Composite composite = this;
		
		Point size = composite.getSize();
		for (Control c: composite.getChildren()) {
			c.setBounds(translation.x, translation.y, 400, 400);
			c.redraw(0, 0, 200, 200, true);
		}
	}

	
	Listener mouseControl = new Listener() {
		Point p = new Point(0,0); 
		
		
		@Override
		public void handleEvent(Event e) {
			Point size = getSize();
			
			switch(e.type) {
			case MouseMove:
				if (!pressed) 
					break;
				
				translation.x += delta.x = e.x-p.x;
				translation.y += delta.y = e.y-p.y;
				
//				scrolledLayout();
				
				scroll(delta.x, delta.y, 0, 0, size.x, size.y, true);
				
				p.x = e.x;
				p.y = e.y;
			
			break;
				
			case MouseDown:
				p.x = e.x;
				p.y = e.y;
				pressed = true;
				
				setRedraw(false);
//				System.out.println("DONT REDRAW");
//				for (Control c: getChildren()) 
//					c.setRedraw(false);
//				
				break;
				
			case MouseUp:
				pressed = false;
				
				
//				setRedraw(true);
//				System.out.println("DO REDRAW");
//				for (Control c: getChildren()) { 
//					c.setRedraw(true);
//					c.redraw(0,0,200,200,true);
//				}
//				scrolledLayout();
				
				break;
			}
		}
		
	};
	

	public ScrollPane(Composite parent, int style) {
		super(parent, style);
		super.setLayout(scrollLayout);
		
		super.addListener(MouseMove, mouseControl);
		super.addListener(MouseDown, mouseControl);
		super.addListener(MouseUp, mouseControl);
	}
	
	@Override
	public void setLayout(Layout layout) {
		throw new RuntimeException("Layout configuration is not allowed");
	}
	
	
	
	
	
	public static void main(String[] args) {
		
		Application app = new Application();
		Shell sh = new Shell(app,SHELL_TRIM|DOUBLE_BUFFERED|NO_REDRAW_RESIZE);
		sh.setLayout(new FillLayout());
		
		ScrollPane sp = new ScrollPane(sh, NO_REDRAW_RESIZE);
		Canvas c = new Canvas(sp, NO_REDRAW_RESIZE);
		
		Image im = new Image(app, ScrollPane.class.getResourceAsStream("generic-cat.jpeg"));
		Pattern pat = new Pattern(app, im);
		Random rng = new Random(1337);
		
		c.addPaintListener((e) -> {
			Point size = c.getSize();
			e.gc.setBackgroundPattern(pat);
			e.gc.fillRectangle(e.x, e.y, e.width, e.height);
			e.gc.drawRectangle(0, 0, size.x-1, size.y-1);
			
			Color random = new Color(app, new RGB(rng.nextInt(360), 0.8f, 0.7f));
			e.gc.setBackground(random);
			e.gc.setAlpha(127);
			e.gc.fillRectangle(e.x, e.y, e.width, e.height);
			
			random.dispose();
		});
		
		
		sh.setBounds(100, 100, 800, 600);
		sh.setVisible(true);
		
		app.loop();
		
	}

}
