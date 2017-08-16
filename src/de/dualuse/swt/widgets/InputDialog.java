package de.dualuse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InputDialog extends Dialog {

//==[ Constructor ]=================================================================================
	
	public InputDialog(Shell parent, int style) {
		super(parent, style);
		
	}

//==[ Create Dialog and Return Value ]==============================================================

	String result;
	
	public String open(String message) {
		return open(message, "");
	}
	
	public String open(String message, String oldValue) {
		return open(message, message, oldValue);
	}
	
	public String open(String title, String message, String oldValue) {
		
		final Shell parent = getParent();
		final Display dsp = parent.getDisplay();
		
		// Setup dialog shell
		Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL); // mac: BORDER no effect, TITLE includes border, Windows?
		shell.setText(message);
		
		FormLayout layout = new FormLayout();
		layout.spacing = 5;
		layout.marginLeft = layout.marginRight = layout.marginBottom = layout.marginTop = 5;
		shell.setLayout(layout);
		
		Label label = new Label(shell, SWT.NONE);
		label.setText(message);
		
		Text text = new Text(shell, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
		text.setText(oldValue);

		Button okButton = new Button(shell, SWT.NONE);
		okButton.setText("Done");
		
		Button cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText("Cancel");
		
		FormData data1 = new FormData();
		data1.left = new FormAttachment(0,0);
		data1.top = new FormAttachment(0,0);
		label.setLayoutData(data1);
		
		FormData data2 = new FormData();
		data2.left = new FormAttachment(0,0);
		data2.top = new FormAttachment(label);
		data2.right = new FormAttachment(100, -5);
		text.setLayoutData(data2);
		
		FormData data3 = new FormData();
		data3.top = new FormAttachment(text);
		data3.right = new FormAttachment(text, 0, SWT.RIGHT);
		cancelButton.setLayoutData(data3);
		
		FormData data4 = new FormData();
		data4.top = new FormAttachment(text);
		data4.right = new FormAttachment(cancelButton, -5, SWT.LEFT);
		okButton.setLayoutData(data4);
		
		text.addKeyListener(new KeyListener() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.keyCode==SWT.CR) {
					result = text.getText();
					shell.dispose();
				}
			}
			@Override public void keyReleased(KeyEvent e) {}
		});
		text.selectAll();
		
		okButton.addListener(SWT.Selection, (e) -> {
			result = text.getText();
			shell.dispose();
		});
		
		cancelButton.addListener(SWT.Selection, (e) -> {
			result = null;
			shell.dispose();
		});

		shell.pack();
		shell.setSize(256, shell.getSize().y);
		
		int px = parent.getLocation().x;
		int py = parent.getLocation().y;
		int pw = parent.getSize().x;
		int ph = parent.getSize().y;
		
		int w = shell.getSize().x;
		int h = shell.getSize().y;
		
		shell.setLocation(px + (pw-w)/2, py + (ph-h)/2);
		
		shell.open();
		
		//////////
		
		while(!shell.isDisposed()) {
			if (!dsp.readAndDispatch())
				dsp.sleep();
		}
		
		return result;
	}
	
}
