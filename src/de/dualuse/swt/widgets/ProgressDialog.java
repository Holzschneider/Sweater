package de.dualuse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.dualuse.swt.util.SWTUtil;
import de.dualuse.swt.util.SimpleFuture;

public class ProgressDialog<E> extends Dialog {

	final static String CANCEL_LABEL = "Cancel";
	final static String PAUSE_LABEL = "Pause";
	final static String RESUME_LABEL = "Resume";
	
	final static int DIALOG_WIDTH = 500;
	final static int LOG_HEIGHT = 400;
	
	Shell parent;
	
	RuntimeException exception;
	E result;

	boolean cancellable = true;
	boolean pausable = true;

	Image image;
	String message;

	Button cancelButton;
	Button pauseButton;
	Button logButton;
	
	boolean logOpen = false;
	boolean done = false;
	
	int dialogWidth = DIALOG_WIDTH;
	
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
		
		void log(String message);
		void logWarn(String message);
		void logError(String message);
		
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
	
//==[ Dialog Configuration/Setup ]==================================================================
	
	public ProgressDialog<E> setCancellable(boolean cancellable) {
		this.cancellable = cancellable;
		return this;
	}
	
	public ProgressDialog<E> setPausable(boolean pausable) {
		this.pausable = pausable;
		return this;
	}
	
	public ProgressDialog<E> setDescription(Image image, String message) {
		this.image = image;
		this.message = message;
		return this;
	}
	
	public ProgressDialog<E> setWidth(int width) {
		this.dialogWidth = width;
		return this;
	}
	
//==[ Create Dialog and Return Value ]==============================================================

	public E open(Task<E> task) {
		
		final Shell parent = getParent();
		final Display dsp = parent.getDisplay();
		
		// Setup dialog shell
		Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL); // mac: BORDER no effect, TITLE includes border, Windows?
		shell.setText(getText());
		
		GridLayout shellLayout = new GridLayout();
		shellLayout.marginWidth = 8;
		shellLayout.marginHeight = 8;
		shellLayout.horizontalSpacing = 8;
		shellLayout.numColumns = 1;
		shell.setLayout(shellLayout);
		
		////////// Initial Image / Description
		
		if (image != null || message != null) {
			ImageLabel label = new ImageLabel(shell, SWT.WRAP);
			
			if (image != null) {
				label.setImage(image);
				label.setVerticalAlignment(SWT.CENTER);
			}
			
			label.setText(message);

			GridData labelData = new GridData(SWT.FILL, SWT.FILL, true, false);
			labelData.widthHint = 492;
			label.setLayoutData(labelData);
			
//			label.setBackground(dsp.getSystemColor(SWT.COLOR_DARK_GREEN));
//			label.getTextLabel().setBackground(dsp.getSystemColor(SWT.COLOR_DARK_RED));
//			label.getImageLabel().setBackground(dsp.getSystemColor(SWT.COLOR_DARK_YELLOW));
		}

		////////// ProgressPane
		
		Container progressPane = new Container(shell, SWT.NONE);
		
		GridData progressPaneData = new GridData();
		progressPaneData.horizontalAlignment = GridData.FILL;
		progressPaneData.grabExcessHorizontalSpace = true;
		progressPane.setLayoutData(progressPaneData);		
		
		GridLayout containerLayout = new GridLayout();
		containerLayout.marginWidth = 8;
		containerLayout.marginHeight = 8;
		containerLayout.numColumns = 2;
		progressPane.setLayout(containerLayout);
		
		////////// Button Pane
		
		Container buttonPane = new Container(shell, SWT.NONE); // | SWT.RIGHT_TO_LEFT); // right_to_left for RowLayout alignment
		
		GridData buttonPaneData = new GridData();
		buttonPaneData.horizontalAlignment = GridData.FILL;
		buttonPane.setLayoutData(buttonPaneData);

		GridLayout buttonPaneLayout = new GridLayout(3, false);
		buttonPane.setLayout(buttonPaneLayout);
		
		logButton = new Button(buttonPane, SWT.NONE);
		
		Image logButtonImageClosed = new Image(dsp, ProgressDialog.class.getResourceAsStream("right_sm.png"));
		logButton.setImage(logButtonImageClosed);
		shell.addListener(SWT.Dispose, (e) -> logButtonImageClosed.dispose());
		
		Image logButtonImageOpen = new Image(dsp, ProgressDialog.class.getResourceAsStream("down_sm.png"));
		shell.addListener(SWT.Dispose, (e) -> logButtonImageOpen.dispose());
		
		cancelButton = new Button(buttonPane, SWT.NONE);
		cancelButton.setText(CANCEL_LABEL);
		cancelButton.setEnabled(cancellable);
		
		pauseButton = new Button(buttonPane, SWT.NONE);
		pauseButton.setText(PAUSE_LABEL);
		pauseButton.setEnabled(pausable);
		
		GridData logButtonData = new GridData();
		logButtonData.horizontalAlignment = GridData.BEGINNING;
		logButtonData.grabExcessHorizontalSpace = true;
		logButton.setLayoutData(logButtonData);

		if (cancellable) {
			cancelButton.addListener(SWT.Selection, (e) -> {
				if (done) return;
				task.cancel();
			});
			shell.addListener(SWT.Dispose, (e) -> task.cancel()); // XXX make sure cancel() being called after the progress is actually completed doesn't cause problems
		}

		if (pausable) {
			pauseButton.addListener(SWT.Selection, new Listener() {
				boolean paused = false;
				@Override public void handleEvent(Event event) {
					if (done) return;
					
					if (paused) {
						paused = false;
						pauseButton.setText(PAUSE_LABEL);
						buttonPane.layout();
						task.resume();
					} else {
						paused = true;
						pauseButton.setText(RESUME_LABEL);
						buttonPane.layout();
						task.pause();
					}
				}		
			});
		}
		
		logButton.addListener(SWT.Selection, new Listener() {
			
			@Override public void handleEvent(Event event) {
				if (logOpen) {
					logButton.setImage(logButtonImageClosed);
					
					logArea.dispose();
					logArea = null;
					
					shell.layout();
					SWTUtil.packHeight(shell);
					
					logOpen = false;
				} else {
					
					logArea = new Text(buttonPane, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL) {
						@Override protected void checkSubclass() {}
						@Override public Point computeSize(int wHint, int hHint, boolean changed) {
							Point result = super.computeSize(wHint,  hHint, changed);
							result.y = LOG_HEIGHT;
							// result.y = Math.min(result.y, LOG_HEIGHT); // grows until max height, requires re-layout when message is appended
							return result;
						}
					};
					
					GridData textData = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1);
					logArea.setLayoutData(textData);
					
					ScrollBar bar = logArea.getVerticalBar();
					
					bar.setSelection(bar.getMaximum());
					System.out.println("initial value: " + logArea.getVerticalBar().getSelection() + " / " + logArea.getVerticalBar().getMaximum());
					System.out.println("thumb: " + bar.getThumb());
					
					logButton.setImage(logButtonImageOpen);
					
					shell.layout();
					SWTUtil.packHeight(shell);
					
					logOpen = true;
				}
			}
			
		});
		
		//////////
		
		TaskProgressHandler handler = new TaskProgressHandler(shell, progressPane);
		Thread t = new Thread(new Runnable() {
			@Override public void run() {
				task.execute(handler);
			}
		});
		t.start();
		
		while(!shell.isDisposed()) {
			if (!dsp.readAndDispatch())
				dsp.sleep();
		}
		
		if (result==null && exception!=null)
			throw exception;
		
		return result;
	}
	
//==[ TaskProgress Handler ]========================================================================
	
	Text logArea;
	StringBuilder logText = new StringBuilder();
	
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
		
		@Override public void log(String message) {
			dsp.asyncExec(() -> {
				
				logText.append(message);
				
				if (logArea!=null) {

					ScrollBar scrollBar = logArea.getVerticalBar();
					boolean atBottom = scrollBar.getSelection() + scrollBar.getThumb() >= scrollBar.getMaximum();
					
					System.out.println("atBottom: " + atBottom);
					System.out.println(scrollBar.getSelection() + " vs " + scrollBar.getMaximum());
					System.out.println("thumb: " + scrollBar.getThumb());
					
					int oldSelection = scrollBar.getSelection();
					
					if (!atBottom) {

						logArea.setRedraw(false);
						int scrollP = logArea.getTopIndex();
						Point selectionP = logArea.getSelection();
						
						logArea.append(message);
						
						logArea.setSelection(selectionP);
						logArea.setTopIndex(scrollP);
						logArea.setRedraw(true);
						
					} else {
						
						logArea.append(message); // apparently autoscrolls
						
					}
					
					// logArea.setText(logText.toString());
					
					// if (atBottom) scrollBar.setSelection(scrollBar.getMaximum());
					
					// if (!atBottom) scrollBar.setSelection(oldSelection);

					System.out.println(scrollBar.getSelection() + " vs " + scrollBar.getMaximum());
					System.out.println("thumb: " + scrollBar.getThumb());
					System.out.println();
					
					// 
					
//					ScrollBar bar = logArea.getVerticalBar();
//					bar.setSelection(bar.getMaximum());
				}
			});
		}
		
		@Override public void logWarn(String message) {
			log(message); // XXX use styledtext color to differentiate warning messages
		}
		
		@Override public void logError(String message) {
			log(message); // XXX use styledtext color to differentiate error messages
		}
		
		@Override public void abort() {
			dsp.asyncExec(() -> {
				result = null;
				workDone();
			});
		}
		
		@Override public void abort(RuntimeException e) {
			dsp.asyncExec(() -> {
				exception = e;
				result = null;
				workDone();
			});
		}
		
		@Override public void done(E res) {
			dsp.asyncExec(() -> {
				result = res;
				workDone();
			});
		}
		
		private void workDone() {
			if (logOpen) {
				cancelButton.setEnabled(false);
				pauseButton.setEnabled(true);
				pauseButton.setText("Close");
				pauseButton.requestLayout();
				pauseButton.addListener(SWT.Selection, (e) -> shell.dispose());
			} else {
				shell.dispose(); // XXX workDone (-> if log open, don't automatically close)
			}
		}
		
		private Progress createProgressController(int style) {
			SimpleFuture<ProgressController> resultFuture = new SimpleFuture<ProgressController>();
			
			dsp.asyncExec(() -> {
				
				resultFuture.put(new ProgressController(parent, style));
				
				// shell.pack(); // implementation of pack: setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT, true))
				Point prefSize = shell.computeSize(dialogWidth, SWT.DEFAULT, true); // DIALOG_WIDTH, SWT.DEFAULT, true);
				shell.setSize(prefSize);
				
//				Rectangle bounds = shell.getBounds();
//				if (bounds.width < DIALOG_WIDTH)
//					shell.setSize(DIALOG_WIDTH, bounds.height);
				// XXX different width causes different height due to label being higher if its width is shorter?
				
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
			async(() -> setIndeterminate(true));
			return this;
		}
		
		@Override public Progress absolute() {
			async(() -> setIndeterminate(false));
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
			setIndeterminate(false);
			updateLabel();
		}
		
		private void updateValues(int min, int max, int current) {
			progressBar.setMinimum(this.min = min);
			progressBar.setMaximum(this.max = max);
			progressBar.setSelection(this.current = current);
			setIndeterminate(false);
			updateLabel();
		}
		
		private void setIndeterminate(boolean indeterminate) {
			if (!indeterminate) {
				
				if ((progressBar.getStyle() & SWT.INDETERMINATE) == 0) return;
				updateStyle(progressBar.getStyle() ^ SWT.INDETERMINATE);
				// parent.layout();
				updateValues();
				
			} else {
				
				if ((progressBar.getStyle() & SWT.INDETERMINATE) == SWT.INDETERMINATE) return;
				updateStyle(progressBar.getStyle() | SWT.INDETERMINATE);
				// parent.layout();
				
			}

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
			progressBar = new ProgressBar(parent, newStyle);
			GridData progressData = new GridData();
			progressData.horizontalAlignment = GridData.FILL;
			progressData.horizontalSpan = 2;
			progressData.grabExcessHorizontalSpace = true;
			progressBar.setLayoutData(progressData);
			
			// Move to previous position
			progressBar.moveAbove(parent.getChildren()[index]);
			
			// Request layout
			progressBar.requestLayout();
		}
		
		private void updateLabel() {
			double percentage = current*100.0/max;
			progressValue.setText((int)percentage + " %");
			parent.layout();
		}
	}

//==[ Simple Task ]=================================================================================
	
	public static abstract class SimpleTask<E> implements Task<E> {

		public static class CancelledException extends Exception {
			private static final long serialVersionUID = 1L;
		}
		
		boolean shouldSleep;
		boolean shouldCancel;
		
		Thread thread;
		
		@Override public void execute(TaskProgress<E> tp) {
			thread = Thread.currentThread();
			run(tp);
		}
		
		public abstract void run(TaskProgress<E> tp);
		
		///// Called from Subclass/Worker Thread
		
		synchronized protected void yield() {
			while(shouldSleep && !shouldCancel) {
				try { wait(); } catch (InterruptedException ex) {}
			}
		}
		
		synchronized protected void yieldOrCancel() throws CancelledException {
			while(shouldSleep || shouldCancel) {
				if (shouldCancel) throw new CancelledException();
				try { wait(); } catch (InterruptedException ex) {}
			}
		}
		
		synchronized protected boolean shouldCancel() {
			return shouldCancel;
		}
		
		///// Called from UI Thread
		
		synchronized @Override public void pause() {
			shouldSleep = true;
			notifyAll();
		}
		
		synchronized @Override public void resume() {
			shouldSleep = false;
			notifyAll();
		}
		
		synchronized @Override public void cancel() {
			shouldCancel = true;
			notifyAll();
			thread.interrupt();
		}
	}
	
}
