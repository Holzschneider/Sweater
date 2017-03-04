package de.dualuse.swt;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class CustomControlTest {

	public static void main(String[] args) {
		
		Display dsp = new Display();
		Shell sh = new Shell(dsp);
		
		sh.setLayout(new FillLayout());
		
		
		Image i = new Image(dsp, CustomControlTest.class.getResourceAsStream("generic-cat.jpeg"));
		
//		Label l = new Label(sh, NONE);
//		l.setText("hallo");
////		l.setBackground(new Color(dsp, new RGB(0x70, 0xF0, 0x70)));
//		sh.setBackgroundImage(i);
		
		
//		Control c = new Control(sh, NONE) {
//			protected void checkSubclass() { }
//		};
//		
//		c.setRedraw(true);
//		c.addPaintListener( (e) -> {
//			System.out.println("bla");
//			e.gc.drawImage(i, 0, 0);
//		});
//		
//		System.out.println(c.view);
		
		Sash s = new Sash(sh, HORIZONTAL);
		System.out.println(s.view);
				
		
		sh.setBounds(1000, 120, 800, 800);
		sh.setVisible(true);
		
		
		while (!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		
		
		
	}
}
