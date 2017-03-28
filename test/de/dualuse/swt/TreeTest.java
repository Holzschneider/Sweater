package de.dualuse.swt;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class TreeTest {
	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell shell = new Shell(dsp);
		shell.setSize(800, 600);
		

		shell.setText("Tree");
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		
		shell.addShellListener(new ShellListener() {
			@Override public void shellIconified(ShellEvent arg0) {}
			@Override public void shellDeiconified(ShellEvent arg0) {}
			@Override public void shellDeactivated(ShellEvent arg0) {}
			@Override public void shellActivated(ShellEvent arg0) {}

			@Override public void shellClosed(ShellEvent arg0) {
				dsp.dispose();
			}
		});

		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		
		SashForm sashForm = new SashForm(tabFolder, SWT.HORIZONTAL | SWT.SMOOTH);
		
		TabItem item = new TabItem(tabFolder, SWT.NONE);
		item.setControl(sashForm);
		item.setText("Main");
		
		item = new TabItem(tabFolder, SWT.NONE);
		item.setText("Other");
		
		new TreeTestTracerSigns(sashForm, SWT.NONE);
		new TreeTestTracerPreview(sashForm, SWT.NONE);

		sashForm.setWeights(new int[] { 1, 4 } );
		sashForm.setSashWidth(4);
		
		shell.pack();
		shell.setVisible(true);
		
		while(!dsp.isDisposed()) {
			
			if (!dsp.readAndDispatch())
				dsp.sleep();
		}
		
		System.out.println("Shutting down");
	}
}
