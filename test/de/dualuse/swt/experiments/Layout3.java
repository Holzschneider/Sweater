package de.dualuse.swt.experiments;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.dualuse.swt.layout.BorderLayout;
import de.dualuse.swt.util.SWTUtil;

public class Layout3 {
	
	static void sendMessage(Text source, Text target) {
		String text = source.getText();
		source.setText("");
		target.append(text + "\n");
		System.out.println("> " + text);		
	}
	
	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell shell = new Shell(dsp, SWT.BORDER);
		shell = new Shell();
		
		SWTUtil.center(shell, 800, 600);
		SWTUtil.exitOnClose(shell);
		
		Text input = new Text(shell, SWT.MULTI);
		input.setLayoutData(BorderLayout.SOUTH);
		
		Color foregroundColor = dsp.getSystemColor(SWT.COLOR_WHITE);
		System.out.println(foregroundColor);
		System.out.println(foregroundColor.isDisposed());
		
		Color whiteColor = new Color(dsp, 254, 255, 255);
		foregroundColor = whiteColor;
		
		System.out.println(foregroundColor);
		
		Text output = new Text(shell, SWT.MULTI | SWT.V_SCROLL);
		output.setBackground(dsp.getSystemColor(SWT.COLOR_DARK_BLUE));
		output.setForeground(foregroundColor);
		output.setLayoutData(BorderLayout.CENTER);
		output.setEditable(false);
		
		// Update layout if textfield should grow
		input.addListener(SWT.Modify, (e) -> {
			input.requestLayout();
		});
		
		// Only fired for single line text fields
		input.addListener(SWT.DefaultSelection, (e) -> {
			sendMessage(input, output);
		});

		// For multiline text fields, use keylistener
		input.addListener(SWT.KeyDown, (e) -> {
			if (e.keyCode != 13 || (e.stateMask & SWT.SHIFT) != 0) return;
			sendMessage(input, output);
			e.doit = false;
		});
		
		BorderLayout layout = new BorderLayout();		
		shell.setLayout(layout);
		
		shell.open();
		
		SWTUtil.eventLoop();
		
		whiteColor.dispose();
	}

}
