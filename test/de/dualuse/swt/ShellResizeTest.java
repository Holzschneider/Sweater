package de.dualuse.swt;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class ShellResizeTest {
	public static void main(String[] args) {

		Display dsp = new Display();
		Shell sh = new Shell(dsp);
		
		sh.setLayout(new FillLayout());
		
		Image im = new Image(dsp, ShellResizeTest.class.getResourceAsStream("generic-cat.jpeg"));
		Label l = new Label(sh,0);
		l.setImage( im );
		
		
		l.addMouseListener(new MouseListener() {
			Event down = null;
			Listener dragListener = this::mouseDragged;
			
			
			@Override
			public void mouseUp(MouseEvent e) {
				l.removeListener(MouseMove, dragListener );
				down = null;
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				l.addListener(MouseMove, dragListener );
			}
			
			Point prev = null;
			public void mouseDragged(Event e)  {
				Point p = new Point(e.x, e.y);
				dsp.map(l, null, p);
				if (down!=null) {
					Point q = prev; //new Point(down.x, down.y);
					dsp.map(l, null, q);
//					System.out.println((e.x-down.x)+", "+(e.y-down.y));
					Point l = sh.getLocation();
//					sh.setLocation(l.x+(p.x-q.x),l.y+(p.y-q.y));
					
					Point s = sh.getSize();
					sh.setSize(s.x+(e.x-down.x), s.y+(e.y-down.y));
					
				}
				
				
				down=e;

				prev = p;
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				
			}
		});
		
		
		
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);
		
		
		while (!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
	}
}
