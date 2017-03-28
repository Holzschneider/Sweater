package de.dualuse.swt.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class SWTUtil {

//==[ Event Loop ]==================================================================================
	
	// Main Event Loop for an application that terminates once the associated Display is disposed
	public static void eventLoop() {
		Display dsp = getDisplay();
		while (!dsp.isDisposed()) {
			if (!dsp.readAndDispatch())
				dsp.sleep();
		}
	}
	
	// Event Loop that terminates once the given shell is disposed (and optionally also disposes the display)
	public static void eventLoop(Shell shell, boolean dispose) {
		Display dsp = getDisplay();
		while(!shell.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		if (dispose)
			dsp.dispose();
	}
	
	// Event Loop that terminates the application if the given shell is disposed
	public static void eventLoop(Shell shell) {
		eventLoop(shell, true);
	}

	// Temporary Event Loop for Dialogs... doesn't dispose the entire application once shell closes
	public static void dialogLoop(Shell shell) {
		eventLoop(shell, false);
	}
	
	///// Private helper
	
	// Get current display ( throw an exception if the current thread is not the UI thread )
	private static Display getDisplay() {
		Display dsp = Display.getCurrent();
		if (dsp == null)
			new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS);
		return dsp;
	}
	
//==[ Center Shell ]================================================================================
	
	// Set shell dimensions and center the shell on the current monitor
	public static void center(Shell shell, int width, int height) {
		shell.setSize(width, height);
		center(shell);
	}
	
	// Set shell dimensions and center the shell over its parent 
	public static void center(Shell parent, Shell child, int width, int height) {
		child.setSize(width, height);
		center(parent, child);
	}
	
	// Center the shell on the current monitor
	public static void center(Shell shell) {
		Display dsp = shell.getDisplay();
		Rectangle monitorBounds = getCurrentMonitorBounds(dsp);
		center(monitorBounds, shell);
	}
	
	// Center the shell over its parent
	public static void center(Shell parent, Shell child) {
		Display dsp = child.getDisplay();
		Rectangle region = parent.isVisible() ? parent.getBounds() : getCurrentMonitorBounds(dsp);
		center(region, child);
	}
	
	///// Private Helper
	
	// Center the shell over the given region
	private static void center(Rectangle region, Shell shell) {
		Rectangle s_bounds = shell.getBounds();
		
		System.out.println("Center: " + s_bounds);
		System.out.println("Over: " + region);
		
		int x = region.x + (region.width - s_bounds.width) / 2;
		int y = region.y + (region.height - s_bounds.height) / 2;
		
		shell.setLocation(x, y);
	}
	
	// Retrieve the current monitor (via the mouse cursor location)
	private static Rectangle getCurrentMonitorBounds(Display dsp) {
		Monitor monitor = dsp.getPrimaryMonitor();

		// Choose current Monitor
		Point cursor = dsp.getCursorLocation();
		Monitor[] monitors = dsp.getMonitors();
		for (Monitor mon : monitors) {
			Rectangle mbounds = mon.getBounds();
			if (mbounds.contains(cursor)) {
				monitor = mon;
				break;
			}
		}
		
		return monitor.getBounds();
	}
	
//==[ Automatically Dispose App on Shell Exit ]=====================================================
	
	// Dispose the associated Display when the given shell is closed (adds a ShellListener) 
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