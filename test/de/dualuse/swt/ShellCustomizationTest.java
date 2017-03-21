package de.dualuse.swt;


import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

public class ShellCustomizationTest {

	public static void main(String[] args) {
		
		Display dsp = new Display();
		Shell sh = new Shell(dsp, NO_TRIM) {
			Point down = null;
			
			boolean dragged = false;
			
			private void up (Event e) {
				dragged = false;
			}
			
			private void drag( MouseEvent e ) {
				dragged = true;
				
				down = dsp.map(this, null, new Point(e.x, e.y));
			}
			
			
			private void move(MouseEvent e) {
				if (!dragged) return;

				Point next = dsp.map(this, null, new Point(e.x, e.y));
				
				Point location = this.getLocation();
				location.x += next.x-down.x;
				location.y += next.y-down.y;
				this.setLocation(location);
				
				down = next;
				
			}
			
			
			{
				setBackgroundMode(INHERIT_FORCE);
//				setBackground( new Color(dsp, new RGBA(0, 0, 255, 10)) );
				setBackground( dsp.getSystemColor( COLOR_TRANSPARENT ) );
				
				System.out.println(dsp.getSystemColor( COLOR_TRANSPARENT ));
//				setAlpha(140);
				addDragDetectListener( this::drag );
				addMouseMoveListener( this::move );
				addListener(MouseUp, this::up );
			}
			
			protected void checkSubclass() { } //fu!
		};
		
		
		sh.setMinimumSize(200, 200);
		sh.view.window().setHasShadow(true);
		
		Region windowRegion = new Region(dsp);
		windowRegion.add(0, 0, 800, 800);
		sh.setRegion(windowRegion);
		
		
//		Color titleBar = new Color(dsp, 23,60,111);
		Color titleBar = new Color(dsp, 5,56,99);
		Color close = new Color(dsp, 200,0,0);
		Color min = new Color(dsp, 0,200,0);
		Color max = new Color(dsp, 0,0,200);
		Color title = new Color(dsp, 255,255,255);
		Color background = new Color(dsp, 240,240,240);
		
		FontData defaultFont = dsp.getFontList(null, true)[0];
		defaultFont.setStyle(BOLD);
		Font titleFont = new Font(dsp, defaultFont); 
		
		
		sh.addPaintListener((e) -> {
			
			e.gc.setBackground(background);
			e.gc.fillRoundRectangle(0, 0, sh.getSize().x-1, sh.getSize().y-1, 8, 8);

			//e.gc.drawLine(0, -10, 1000, 100);
			
			e.gc.setBackground(titleBar );
			e.gc.fillRectangle(0, 0, sh.getSize().x, 32);
			
			e.gc.setForeground( title );
			e.gc.setFont(titleFont);
			e.gc.drawText("Tracer", 8, 8);
			
			e.gc.setForeground(titleBar);
//			e.gc.drawLine(0, 0, 1000, 1000);
//			e.gc.drawRoundRectangle(0, 0, sh.getSize().x-1, sh.getSize().y-1, 5, 5);
		});
		
		
		sh.setBounds(200, 200, 500, 400);
		sh.setVisible(true);
		
		
		
		
		
		
		
		
		while (!sh.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
		
	}
}
