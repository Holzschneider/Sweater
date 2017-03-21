package de.dualuse.swt.util;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class SWTUtil {

	public static void eventLoop() {
		Display dsp = Display.getCurrent();
		
		if (dsp == null)
			throw new RuntimeException("Current thread is not a user-interface thread of an active Display.");
		
		while (!dsp.isDisposed()) {
			if (!dsp.readAndDispatch())
				dsp.sleep();
		}
	}
	
	public static void center(Shell shell, int width, int height) {
		shell.setSize(width, height);
		center(shell);
	}
	
	public static void center(Shell shell) {
		Display dsp = shell.getDisplay();
		Monitor primary = dsp.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
	}
	
	public static void exitOnClose(Shell shell) {
		shell.addShellListener(new ShellListener() {
			@Override public void shellActivated(ShellEvent event) {}
			@Override public void shellClosed(ShellEvent event) {
				Display dsp = shell.getDisplay();
				if (dsp != null) dsp.dispose();
			}
			@Override public void shellDeactivated(ShellEvent arg0) {}
			@Override public void shellDeiconified(ShellEvent arg0) {}
			@Override public void shellIconified(ShellEvent arg0) {}
			
		});
	}
	
}