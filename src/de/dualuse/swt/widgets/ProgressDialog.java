package de.dualuse.swt.widgets;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.util.SWTUtil;

public class ProgressDialog<E> extends Dialog {

	Shell parent;
	
//==[ Interfaces ]==================================================================================
	
	public interface TaskProgress<E> {
		int createProgress(String label, int start, int max);
		void setProgress(int id, String label, int start, int max);
		
		void update(int id, int value);
		
		void update(int value);
		
		void done(E result);
		void indeterminate();
	}
	
	public interface Task<E> {
		void execute(TaskProgress<E> tp);
		void pause();
		void resume();
		void cancel();
	}
	
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
	
	E result;
	ProgressBar progress;
	
	public E open(Task<E> task) {
		
		final Shell parent = getParent();
		final Display dsp = parent.getDisplay();
		
		// Setup dialog shell
		Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
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

		
//		Label label = new Label(shell, SWT.NONE);
//		label.setText("Loading File...");
//		
//		progress = new ProgressBar(shell, SWT.NONE);
//		progress.setToolTipText("Progress Test");
		
		ArrayList<Label> labels = new ArrayList<Label>();
		ArrayList<ProgressBar> progresss = new ArrayList<ProgressBar>();
		
		Thread t = new Thread(new Runnable() {
			@Override public void run() {
				task.execute(new TaskProgress<E>() {
					
//					@Override public int addProgress(int total) {
//						parent.getDisplay().syncExec(() -> {
//							
//						});
//					}
					
					@Override public int createProgress(String text, int start, int max) {
						AtomicInteger result = new AtomicInteger();
						dsp.syncExec(() -> {
							int index = labels.size();
							
							Label label = new Label(progressPane, SWT.NONE);
							label.setText(text);
							label.pack();
							labels.add(label);
	
							ProgressBar progress = new ProgressBar(progressPane, SWT.NONE);
							progress.setMaximum(max);
							progress.setSelection(start);
							progress.pack();
							progresss.add(progress);
							
							shell.pack();
							
							result.set(index);
							
							shell.pack();
							SWTUtil.center(parent, shell);
							
							if (!shell.isVisible())
								shell.open();
						});
						return result.get();
					}
					
					@Override public void setProgress(int id, String text, int start, int max) {
						dsp.asyncExec(() -> {
							Label label = labels.get(id);
							label.setText(text);
							
							ProgressBar progress = progresss.get(id);
							progress.setMaximum(max);
							progress.setSelection(start);
						});
					}
					
					@Override public void update(int id, int value) {
						dsp.asyncExec(() -> {
							createDefaultProgress();
							ProgressBar progress = progresss.get(id);
							progress.setSelection(value);
						});
					}
					
					// @Override public void update(int id, int value) {
					@Override public void update(int value) {
						dsp.asyncExec(() -> {
							// progress.setState(SWT.SMOOTH);
							createDefaultProgress();
							ProgressBar progress = progresss.get(0);
							progress.setSelection(value);
						});
					}

					@Override public void done(E res) {
						dsp.asyncExec(() -> {
							result = res;
							shell.dispose();
						});
					}
					
					@Override public void indeterminate() {
						// XXX work with progressbar list (problem: order)
						
//						parent.getDisplay().asyncExec(() -> {
//							progress.dispose();
//							progress = new ProgressBar(shell, SWT.INDETERMINATE);
//							progress.pack();
//						});
					}
					
					private void createDefaultProgress() {
						if (progresss.isEmpty()) {
							
							System.out.println("Creating default progress");
							
							Label label = new Label(progressPane, SWT.NONE);
							label.setText("Loading file...");
							labels.add(label);
							
							ProgressBar progress = new ProgressBarLabeled(progressPane, SWT.NONE);
							
							progress.setMaximum(100);
							progress.pack();
							shell.pack();
							
							// center();
							SWTUtil.center(parent, shell);
							
							shell.open();
							progresss.add(progress);
						}
					}
					
//					private void center() {
//						
//						shell.pack();
//						
//						Display display = shell.getDisplay();
//						
//						Rectangle pbounds = parent.getBounds();
//						Rectangle sbounds = shell.getBounds();
//						
//						if (!parent.isVisible()) {
//							Monitor[] monitors = display.getMonitors();
//							// monitors[0].getBounds().contains
//							System.out.println("Display not visible");
//							
//							Point cursor = display.getCursorLocation();
//							for (Monitor monitor : monitors) {
//								Rectangle mbounds = monitor.getBounds();
//								if (mbounds.contains(cursor)) {
//									pbounds = mbounds;
//									break;
//								}
//							}
//						}
//						
//						int x = pbounds.x + (pbounds.width - sbounds.width)/2;
//						int y = pbounds.y + (pbounds.height - sbounds.height)/2;
//						
//						shell.setLocation(x, y);
//					}
				});
			}
		});
		t.start();
		
//		shell.pack();
//		shell.open();
		
		Display display = parent.getDisplay();
		while(!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return result;
	}
}
