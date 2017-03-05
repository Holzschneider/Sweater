package de.dualuse.swt;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TaskBar;
import org.eclipse.swt.widgets.TaskItem;

public class TaskBarTest {
	
	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell shell = new Shell(dsp);
		shell.setSize(800, 600);
		shell.setVisible(true);

		shell.setText("TaskBar Test Shell");
		
		shell.addShellListener(new ShellListener() {
			@Override public void shellIconified(ShellEvent arg0) {}
			@Override public void shellDeiconified(ShellEvent arg0) {}
			@Override public void shellDeactivated(ShellEvent arg0) {}
			@Override public void shellActivated(ShellEvent arg0) {}

			@Override public void shellClosed(ShellEvent arg0) {
				dsp.dispose();
			}
		});

		
		TaskBar bar = dsp.getSystemTaskBar();
		System.out.println(bar);
		
		TaskItem[] items = bar.getItems();

		System.out.println(bar.getItemCount());
		System.out.println(items.length);
		for (TaskItem item : items) {
			System.out.println("1> " + item.getText());
			System.out.println("2> " + item.getOverlayText());
			item.setProgress(40);
		}
		
		while(!dsp.isDisposed()) {
			
			if (!dsp.readAndDispatch())
				dsp.sleep();
		}
		
		System.out.println("Shutting down");
	}
}
