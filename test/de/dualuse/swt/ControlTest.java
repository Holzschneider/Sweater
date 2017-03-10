package de.dualuse.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.internal.SWTEventObject;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import de.dualuse.swt.util.SWTUtil;

public class ControlTest {
	public static void main(String[] args) {
		Shell shell = new Shell();
		SWTUtil.exitOnClose(shell);
		
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.marginTop = layout.marginBottom = layout.marginRight = layout.marginLeft = 16;
		layout.spacing = 16;
		shell.setLayout(layout);
		
		Link link = new Link(shell, SWT.NONE);
		link.setText("Go there to get more information: <a href=\"http://www.golem.de/\">www.golem.de</a>");
		link.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				System.out.println("Selected: " + e.text);
			}
			@Override public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		SWTEventObject obj = null;
		
		Spinner spinner = new Spinner(shell, SWT.BORDER);
		
		DateTime time = new DateTime(shell, SWT.BORDER);
		
		Group group = new Group(shell, SWT.BORDER);
		group.setText("Group:");
		group.setLayout(new FillLayout());
		
		Button button = new Button(group, SWT.NONE);
		button.setText("Click Me");
		
		shell.pack();
		SWTUtil.center(shell);
		shell.open();
		
		SWTUtil.eventLoop();
	}
}
