package de.dualuse.swt;

import static org.eclipse.swt.SWT.*;

import java.awt.Point;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


public class ComponentTest {

	
	public static void main(String[] args) {
		
		Shell sh = new Shell();
		
		
		Component c = new Component(sh, NONE) {
			
			@AutoEvent(Dispose)
			ResourcePool rp = new ResourcePool();
			
			
			Color white = rp.createColor(255,255,255);
			
			
			//////
			
			
			public final Listener onMoveListener = new Adapter(this, MouseMove, this::onMove);
			public final Listener otherMoveListener = new Adapter(this, MouseMove) {
				public void handleEvent(Event event) {
					
				}
			};
			
			
			@AutoEvent(not={MouseDown,MouseUp})
			boolean down = false;

			@AutoEvent(point={MouseDown},nullify={MouseUp})
			Point last = new Point(0,0);

			
//			@AutoEventOutlet(assign=@AutoAssignment(string="hallo",))
//			String message;
			
			@AutoEvent(MouseDown) public void onDown(Event e) { down = true; }
			@AutoEvent(MouseUp) public void onUp(Event e) { down = false; }
			
			@AutoEvent(MouseMove)			
			public void onMove(Event e) {
				if (down)
					;
			}

			
		};

		
		
		System.out.println( c.getDragDetect());
		
		
		
		
	}
}
