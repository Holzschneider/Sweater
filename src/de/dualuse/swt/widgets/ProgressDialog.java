package de.dualuse.swt.widgets;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.util.SWTUtil;
import de.dualuse.swt.util.SimpleFuture;

public class ProgressDialog<E> extends Dialog {

	final static int MIN_WIDTH = 360;
	
	Shell parent;
	
	RuntimeException exception;
	E result;
	
//==[ Interfaces ]==================================================================================

	public interface Task<E> {
		void execute(TaskProgress<E> tp);
		void pause();
		void resume();
		void cancel();
	}

	public interface TaskProgress<E> {
		Progress createProgress();
		Progress createIndeterminateProgress();
		void abort();
		void abort(RuntimeException e);
		void done(E result);
	}
	
	public interface Progress {
		Progress setLabel(String label);
		Progress setValues(int min, int max, int value);
		Progress setMin(int min);
		Progress setMax(int max);
		Progress setValue(int value);
		Progress indeterminate();
		Progress absolute();
		void dispose();
	}
	
//==[ Constructor ]=================================================================================

	public ProgressDialog(Shell parent, String title) {
		this(parent, SWT.NONE); // default dialog style
		setText(title);
	}
	
	public ProgressDialog(Shell parent, int style) {
		super(parent, style);
		this.parent = parent;
	}
	
//==[ Create Dialog and Return Value ]==============================================================
	
	public E open(Task<E> task) {
		
		final Shell parent = getParent();
		
		// Setup dialog shell
		// Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL); // mac: BORDER no effect, TITLE includes border, Windows?
		shell.setText(getText());
		
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.marginWidth = 8;
		layout.marginHeight = 8;
		layout.spacing = 0;
		shell.setLayout(layout);
		
		Container progressPane = new Container(shell, SWT.NONE);
		progressPane.setLayout(new FillLayout(SWT.VERTICAL));
		
		Container buttonPane = new Container(shell, SWT.NONE);

		final Button cancelButton = new Button(buttonPane, SWT.NONE);
		cancelButton.setText("Cancel");
		
		final Button pauseButton = new Button(buttonPane, SWT.NONE);
		pauseButton.setText("Pause");
		
		buttonPane.setLayout(new Layout() {
			int margin = 4;
			@Override protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				cancelButton.pack();
				pauseButton.pack();
				Rectangle cancelBounds = cancelButton.getBounds();
				Rectangle pauseBounds = pauseButton.getBounds();
				int width = 3*margin + cancelBounds.width + pauseBounds.width;
				int height = 2 * margin + Math.max(cancelBounds.height, pauseBounds.height);
				return new Point(width, height);
			}

			@Override protected void layout(Composite composite, boolean flushCache) {
				cancelButton.pack();
				cancelButton.setLocation(composite.getBounds().width - margin - cancelButton.getBounds().width, 0);
				
				pauseButton.pack();
				pauseButton.setLocation(cancelButton.getLocation().x - margin - pauseButton.getBounds().width, 0);
			}
		});

		TaskProgressHandler handler = new TaskProgressHandler(shell, progressPane);
		Thread t = new Thread(new Runnable() {
			@Override public void run() {
				task.execute(handler);
			}
		});
		t.start();
		
		Display display = parent.getDisplay();
		while(!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		if (result==null && exception!=null)
			throw exception;
		
		return result;
	}
	
//==[ TaskProgress Handler ]========================================================================
	
	class TaskProgressHandler implements TaskProgress<E> {

		Shell shell;
		Composite parent;
		Display dsp;
		
		public TaskProgressHandler(Shell shell, Composite parent) {
			dsp = Display.getCurrent();
			this.shell = shell;
			this.parent = parent;
		}
		
		@Override public Progress createProgress() {
			return createProgressController(SWT.NONE);
		}
		
		@Override public Progress createIndeterminateProgress() {
			return createProgressController(SWT.INDETERMINATE);
		}
		
		@Override public void abort() {
			dsp.asyncExec(() -> {
				result = null;
				shell.dispose();
			});
		}
		
		@Override public void abort(RuntimeException e) {
			dsp.asyncExec(() -> {
				exception = e;
				result = null;
				shell.dispose();
			});
		}
		
		@Override public void done(E res) {
			dsp.asyncExec(() -> {
				result = res;
				shell.dispose();
			});
		}
		
		private Progress createProgressController(int style) {
			SimpleFuture<ProgressController> resultFuture = new SimpleFuture<ProgressController>();
			
			dsp.asyncExec(() -> {
				
				resultFuture.put(new ProgressController(parent, style));
				
				shell.pack();
				Rectangle bounds = shell.getBounds();
				if (bounds.width < MIN_WIDTH)
					shell.setSize(MIN_WIDTH, bounds.height);
				
				SWTUtil.center(shell.getParent(), shell, 0.5, 0.32);
				
				if (!shell.isVisible())
					shell.open();
				
			});
			
			try {
				ProgressController result = resultFuture.get();
				return result;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
//==[ Progress Controller ]=========================================================================

	class ProgressController implements Progress {
		
		Label label;
		ProgressBar progressBar;
		
		Composite parent;
		Display dsp;
		
		int min = 0, max = 100, current = 0; // default values
		
		boolean disposed;
		
		public ProgressController(Composite parent) {
			this(parent, SWT.NONE);
		}
		
		public ProgressController(Composite parent, int style) {
			this.parent = parent;
			dsp = Display.getCurrent();
			label = new Label(parent, SWT.NONE);
			progressBar = new ProgressBarLabeled(parent, style);
		}
		
		@Override public Progress setLabel(String text) {
			async(() -> label.setText(text));
			return this;
		}
		
		@Override public Progress setValues(int min, int max, int current) {
			async(() -> setValues());
			return this;
		}
		
		@Override public Progress setMin(int min) {
			async(() -> progressBar.setMinimum(this.min = min));
			return this;
		}
		
		@Override public Progress setMax(int max) {
			async(() -> progressBar.setMaximum(this.max = max));
			return this;
		}
		
		@Override public Progress setValue(int current) {
			async(() -> progressBar.setSelection(this.current = current));
			return this;
		}
		
		@Override public Progress indeterminate() {
			async(() -> {
				if ((progressBar.getStyle() & SWT.INDETERMINATE) == SWT.INDETERMINATE) return;
				
				updateStyle(progressBar.getStyle() | SWT.INDETERMINATE);
				
				progressBar.pack();
				setValues();
			});
			return this;
		}
		
		@Override public Progress absolute() {
			async(() -> {
				if ((progressBar.getStyle() & SWT.INDETERMINATE) == 0) return;

				updateStyle(progressBar.getStyle() ^ SWT.INDETERMINATE);
				
				progressBar.pack();
				setValues();
			});
			return this;
		}
		
		@Override public void dispose() {
			disposed = true;
			async(() -> {
				label.dispose();
				progressBar.dispose();
			});
		}
		
		private void async(Runnable callback) {
			if (disposed) throw new SWTException(SWT.ERROR_WIDGET_DISPOSED);
			dsp.asyncExec(() -> {
				if (label.isDisposed() || progressBar.isDisposed())
					return;
				callback.run();
			});
		}
		
		private void setValues() {
			progressBar.setMinimum(min);
			progressBar.setMaximum(max);
			progressBar.setSelection(current);
		}
		
		private void updateStyle(int newStyle) {
			// Find old index
			Composite parent = progressBar.getParent();
			Control[] children = parent.getChildren();
			int index = -1;
			for (int i=0; i<children.length; i++) {
				if (children[i] == progressBar) {
					index = i;
					break;
				}
			}
			if (index == -1) return;
			
			// Remove current ProgresBar
			progressBar.dispose(); // has child already been removed after this call?
			
			// Create and position new replacment ProgressBar
			progressBar = new ProgressBar(parent, SWT.NONE);
			progressBar.moveAbove(parent.getChildren()[index]);
		}
	}

//==[ Simple Task ]=================================================================================
	
	public static abstract class SimpleTask<E> implements Task<E> {
		
		private AtomicBoolean shouldSleep  = new AtomicBoolean();
		private AtomicBoolean shouldCancel = new AtomicBoolean();
		
		protected void yield() {
			while(shouldSleep.get())
				try { shouldSleep.wait(); } catch (InterruptedException ex) {}
		}
		
		protected boolean shouldCancel() {
			return shouldCancel.get();
		}
		
		@Override public void pause() {
			shouldSleep.set(true);
			shouldSleep.notifyAll();
		}
		
		@Override public void resume() {
			shouldSleep.set(false);
			shouldSleep.notifyAll();
		}
		
		@Override public void cancel() {
			shouldCancel.set(true);
		}
	}
	
}
