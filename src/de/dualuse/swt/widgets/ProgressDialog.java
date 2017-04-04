package de.dualuse.swt.widgets;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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
		
//		FillLayout shellLayout = new FillLayout(SWT.VERTICAL);
//		shellLayout.marginWidth = 8;
//		shellLayout.marginHeight = 8;
//		shellLayout.spacing = 0;
//		shell.setLayout(shellLayout);
		
		GridLayout shellLayout = new GridLayout();
		shellLayout.marginWidth = 8;
		shellLayout.marginHeight = 8;
		shellLayout.horizontalSpacing = 8;
		shellLayout.numColumns = 1;
		shell.setLayout(shellLayout);
		
		Container progressPane = new Container(shell, SWT.NONE);
		// progressPane.setLayout(new FillLayout(SWT.VERTICAL));
		// progressPane.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY)); // visual layout debug marker 
		
		GridData progressPaneData = new GridData();
		progressPaneData.horizontalAlignment = GridData.FILL;
		progressPaneData.grabExcessHorizontalSpace = true;
		progressPane.setLayoutData(progressPaneData);
		
		
		GridLayout containerLayout = new GridLayout();
		containerLayout.marginWidth = 8;
		containerLayout.marginHeight = 8;
		containerLayout.numColumns = 2;
		progressPane.setLayout(containerLayout);
		
		Container buttonPane = new Container(shell, SWT.NONE | SWT.RIGHT_TO_LEFT); // right_to_left for RowLayout alignment
		// Container buttonPane = new Container(progressPane, SWT.NONE);
		// buttonPane.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED)); // visual layout debug marker 
		
		GridData buttonPaneData = new GridData();
		buttonPaneData.horizontalAlignment = GridData.END;
		buttonPane.setLayoutData(buttonPaneData);
		
		final Button logButton = new Button(buttonPane, SWT.NONE);
		logButton.setText("Log");
		
		Image image = new Image(Display.getCurrent(), ProgressDialog.class.getResourceAsStream("right_sm.png"));
		logButton.setImage(image);
		shell.addListener(SWT.Dispose, (e) -> image.dispose());
		
		final Button cancelButton = new Button(buttonPane, SWT.NONE);
		cancelButton.setText("Cancel");
		
		final Button pauseButton = new Button(buttonPane, SWT.NONE);
		pauseButton.setText("Pause");
		
		RowLayout buttonPaneLayout = new RowLayout(); // if expandable log messages: this should probably be a grid layout as well, first column left aligned, second column right aligned
		buttonPane.setLayout(buttonPaneLayout);
		
		/*
		buttonPane.setLayout(new Layout() {
			int margin = 4;
			@Override protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				cancelButton.pack();
				pauseButton.pack();
				Rectangle cancelBounds = cancelButton.getBounds();
				Rectangle pauseBounds = pauseButton.getBounds();
				int width = 3 * margin + cancelBounds.width + pauseBounds.width;
				int height = 2 * margin + Math.max(cancelBounds.height, pauseBounds.height);
				return new Point(width, height);
			}

			@Override protected void layout(Composite composite, boolean flushCache) {
				cancelButton.pack();
				cancelButton.setLocation(
					composite.getBounds().width - margin - cancelButton.getBounds().width,
					0
				);
				
				pauseButton.pack();
				pauseButton.setLocation(
					cancelButton.getLocation().x - margin - pauseButton.getBounds().width,
					0
				);
			}
		});
		*/

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
		
		Label progressTitle;
		Label progressValue;
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
			
			progressTitle = new Label(parent, SWT.NONE);
			progressTitle.setText("Progress");
			GridData titleData = new GridData();
			titleData.horizontalAlignment = GridData.BEGINNING;
			titleData.grabExcessHorizontalSpace = true;
			progressTitle.setLayoutData(titleData);
			
			progressValue = new Label(parent, SWT.NONE);
			progressValue.setText("0%");
			GridData valueData = new GridData();
			valueData.horizontalAlignment = GridData.END;
			progressValue.setLayoutData(valueData);
			
			progressBar = new ProgressBar(parent, style);
			GridData progressData = new GridData();
			progressData.horizontalAlignment = GridData.FILL;
			progressData.horizontalSpan = 2;
			progressData.grabExcessHorizontalSpace = true;
			progressBar.setLayoutData(progressData);
			
		}
		
		@Override public Progress setLabel(String text) {
			async(() -> {
				progressTitle.setText(text);
				progressTitle.pack();
			});
			return this;
		}
		
		@Override public Progress setValues(int min, int max, int current) {
			async(() -> updateValues(min, max, current));
			return this;
		}
		
		@Override public Progress setMin(int min) {
			async(() -> updateMin(min));
			return this;
		}
		
		@Override public Progress setMax(int max) {
			async(() -> updateMax(max));
			return this;
		}
		
		@Override public Progress setValue(int current) {
			async(() -> updateCurrent(current));
			return this;
		}
		
		@Override public Progress indeterminate() {
			async(() -> {
				if ((progressBar.getStyle() & SWT.INDETERMINATE) == SWT.INDETERMINATE) return;
				
				updateStyle(progressBar.getStyle() | SWT.INDETERMINATE);
				
				progressBar.pack(); // XXX parent.layout() necessary? -> test
				updateValues();
			});
			return this;
		}
		
		@Override public Progress absolute() {
			async(() -> {
				if ((progressBar.getStyle() & SWT.INDETERMINATE) == 0) return;

				updateStyle(progressBar.getStyle() ^ SWT.INDETERMINATE);
				
				progressBar.pack();
				updateValues();
			});
			return this;
		}
		
		@Override public void dispose() {
			disposed = true;
			async(() -> {
				progressTitle.dispose();
				progressBar.dispose();
			});
		}
		
		private void async(Runnable callback) {
			if (disposed) throw new SWTException(SWT.ERROR_WIDGET_DISPOSED);
			dsp.asyncExec(() -> {
				if (progressTitle.isDisposed() || progressBar.isDisposed())
					return;
				callback.run();
			});
		}
		
		private void updateMin(int min) {
			progressBar.setMinimum(this.min = min);
			updateLabel();
		}
		
		private void updateMax(int max) {
			progressBar.setMaximum(this.max = max);
			updateLabel();
		}
		
		private void updateCurrent(int current) {
			progressBar.setSelection(this.current = current);
			updateLabel();
		}
		
		private void updateValues(int min, int max, int current) {
			progressBar.setMinimum(this.min = min);
			progressBar.setMaximum(this.max = max);
			progressBar.setSelection(this.current = current);
			updateLabel();
		}
		
		private void updateValues() {
			updateValues(this.min, this.max, this.current);
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
		
		private void updateLabel() {
			double percentage = current*100.0/max;
			progressValue.setText((int)percentage + " %");
			parent.layout();
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
