package de.dualuse.swt.experiments;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TouchEvent;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Synchronizer;

import de.dualuse.swt.util.SWTTimer;
import de.dualuse.swt.util.SWTUtil;

// FillLayout Test

public class Layout1 {
	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell shell = new Shell(dsp);
		shell.setSize(800, 600);
		SWTUtil.center(shell);
		SWTUtil.exitOnClose(shell);
		
		FillLayout layout = new FillLayout();
		layout.type = SWT.VERTICAL;
		layout.marginWidth = 16;
		layout.marginHeight = 16;
		layout.spacing = 55;
		
		shell.setLayout(layout);
		
		Button button1 = new Button(shell, SWT.NONE);
		button1.setText("Button 1");
		
		Button button2 = new Button(shell, SWT.NONE);
		button2.setText("Button 2");
		
		Button button3 = new Button(shell, SWT.NONE);
		button3.setText("Start");

		Runnable run = new Runnable() {
			int i = 0;
			@Override public void run() {
				i = i + 1;
				System.out.println(i%2==0 ? "Tick" : "Tock");
			}
		};
		
		final SWTTimer timer = new SWTTimer(100, run);
		
		button3.addSelectionListener(new SelectionListener() {
			
			@Override public void widgetDefaultSelected(SelectionEvent e) {}
			
			@Override public void widgetSelected(SelectionEvent e) {
				System.out.println("Widget selected");
				
				if (!timer.isRunning()) {
					timer.start();
					button3.setText("Stop");
				} else {
					timer.stop();
					button3.setText("Start");
				}
				
			}
			
		});
		
		Synchronizer sync = null;
		
		dsp.disposeExec(() -> System.out.println("Disposing"));
		
		shell.open();
				
		while(!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
	}
}
