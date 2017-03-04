package de.dualuse.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SashFormTest {

	public static void main (String [] args) {
		Display dsp = new Display();
		Shell sh = new Shell(dsp);
		sh.setText("SWT SashForm Example");
		
		sh.setLayout(new FillLayout());
		
	    SashForm sashForm = new SashForm(sh, SWT.HORIZONTAL);
	    new Button(sashForm, SWT.PUSH).setText("Left");
	    new Button(sashForm, SWT.PUSH).setText("Right");
		
	    SashForm sashForm2 = new SashForm(sh, SWT.VERTICAL);
	    new Button(sashForm2, SWT.PUSH).setText("Up");
	    new Button(sashForm2, SWT.PUSH).setText("Down");
	    new Button(sashForm2, SWT.PUSH).setText("Downer");
		
	    sh.setBounds(1000, 100, 1200, 800);
	    sh.setVisible(true);
	    
		while (!sh.isDisposed ()) 
			if (!dsp.readAndDispatch ()) 
				dsp.sleep ();
	}
}
