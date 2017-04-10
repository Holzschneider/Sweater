package de.dualuse.swt.experiments;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.layout.BorderLayout;
import de.dualuse.swt.util.SWTUtil;

public class Monitors {

	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell shell = new Shell(dsp, SWT.BORDER);
		shell = new Shell();
		
		SWTUtil.center(shell, 800, 600);
		SWTUtil.exitOnClose(shell);

		System.out.println("DPI:");
		System.out.println("\t" + dsp.getDPI());
		System.out.println();
		
		for (Monitor monitor : dsp.getMonitors()) {
			System.out.println(monitor);
			System.out.println("\t" + monitor.getBounds());
			System.out.println("\t" + monitor.getClientArea());
		}
		
		BorderLayout layout = new BorderLayout();		
		shell.setLayout(layout);
		
		shell.open();
		
		SWTUtil.eventLoop();
		
	}

}
