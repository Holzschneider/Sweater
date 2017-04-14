package de.dualuse.swt.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

public class SWTTimer {
	
	public interface TimerJob {
		public void run(SWTTimer source);
	}
	
	private TimerJob code;
	private boolean isRunning;
	private Display display;
	private int milliseconds;
	
	private Runnable timerCode = new Runnable() {
		@Override public void run() {
			synchronized (SWTTimer.this) {
				if (!isRunning) return;
				code.run(SWTTimer.this);
				display.timerExec(milliseconds, timerCode); // XXX timer granularity? smallest value feels 'slow' on os x
			}
		}
	};
	
	public SWTTimer(int milliseconds, Runnable run) {
		Display display = Display.getCurrent();
		if (display == null)
			throw new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS);
		TimerJob job = new TimerJob() {
			@Override public void run(SWTTimer source) {
				run.run();
			}
		};
		init(display, milliseconds, job);
	}
	
	public SWTTimer(int milliseconds, TimerJob run) {
		Display display = Display.getCurrent();
		if (display == null)
			throw new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS);
		init(display, milliseconds, run);
	}
	
	public SWTTimer(Display display, int milliseconds, TimerJob run) {
		init(display, milliseconds, run);
	}
	
	private void init(Display display, int milliseconds, TimerJob run) {
		this.code = run;
		this.milliseconds = milliseconds;
		this.display = display;
	}
	
	public synchronized void start() {
		if (isRunning) return;
		isRunning = true;
		display.timerExec(milliseconds, timerCode);		
	}
	
	public synchronized void stop() {
		if (!isRunning) return;
		isRunning = false;
	}
	
	public synchronized boolean isRunning() {
		return isRunning;
	}
	
}
