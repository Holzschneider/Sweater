package de.dualuse.swt;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class MouseEventTest {
	public static void main(String[] args) {

		Display dsp = new Display();
		
		Shell sh = new Shell(dsp);
		sh.setLayout(new FillLayout());
		
		
		Label c = new Label(sh, NONE);
		
		c.addDragDetectListener( (e)-> c.setData("drag", true) );
		c.addListener(MouseUp, (e)->c.setData("drag",false) );
		
		c.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (Boolean.TRUE.equals(c.getData("drag")))
					System.out.println(e.x+", "+e.y);
			}
		});
		
		
		c.addMouseMoveListener( (e) -> { if (c.getDragDetect()) System.out.println(e.x+", "+e.y); } );
		
		
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);
		
		
		
		
		
		
		while(!sh.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
		
	}
}
