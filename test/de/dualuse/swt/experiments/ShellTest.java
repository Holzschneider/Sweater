package de.dualuse.swt.experiments;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ShellTest {
	public static void main(String[] args) {
		
		Display dsp = new Display();
		Shell sh = new Shell(dsp);
		
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);

		///
		
//		while(!dsp.isDisposed())
//			if (dsp.readAndDispatch())
//				if (!dsp.isDisposed())
//					dsp.sleep();
		while(!sh.isDisposed())
			if (!dsp.readAndDispatch())
				if (!dsp.isDisposed())
					dsp.sleep();

		
	}
}
