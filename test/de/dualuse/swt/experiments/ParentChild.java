package de.dualuse.swt.experiments;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.util.SWTUtil;

public class ParentChild {
	
	public static void main(String[] args) {
		
		Shell parent = new Shell();

		parent.setText("Parent");
		parent.open();
		
		Shell sub = new Shell(parent, SWT.PRIMARY_MODAL);
		sub.setSize(480, 320);
		SWTUtil.center(parent, sub);
		sub.open();
		
		Display dsp = parent.getDisplay();
		while(!parent.isDisposed()) {
			if (!dsp.readAndDispatch())
				dsp.sleep();
		}
		
		dsp.dispose();
		
		System.out.println("Disposed");
	}
	
}
