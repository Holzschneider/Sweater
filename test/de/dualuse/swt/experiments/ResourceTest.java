package de.dualuse.swt.experiments;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 *	widget.dispose()
 *		not called recursively
 *
 * 	child components can't override dispose() to react towards being disposed,
 *  as method will not be called during their disposal. Dispose() is only called
 *  to initially trigger the disposal process.
 *  
 *  If children want to get notified about their disposal, they must add a DisposeListener.
 *
 */

public class ResourceTest {
	public static void main(String[] args) {
		
		Shell shell = new Shell() {
			@Override protected void checkSubclass() {}
			@Override public void dispose() {
				System.out.println("Shell disposed");
				super.dispose();
			}
		};
		
		Button button = new Button(shell, SWT.NONE) {
			@Override protected void checkSubclass() {}
			@Override public void dispose() {
				System.out.println("Button disposed");
				super.dispose();
			}
			@Override public String toString() {
				return "Custom Button Control";
			}
		};
		
		Text text = new Text(shell, SWT.NONE) {
			@Override protected void checkSubclass() {}
			@Override public void dispose() {
				System.out.println("Text disposed");
				super.dispose();
			}
			@Override public String toString() {
				return "Custom Text Control";
			}
		};
		
		shell.addDisposeListener((e) -> {
			System.out.println("Listener: Shell Disposed");
		});
		
		button.addDisposeListener((e) -> {
			System.out.println("Listener: Button Disposed");
		});
		
		text.addDisposeListener((e) -> {
			System.out.println("Listener: Text Disposed");
		});
		
		System.out.println("Children:");
		for (Control control : shell.getChildren())
			System.out.println("\t" + control);
		
		shell.setBounds(100, 100, 600, 600);
		shell.open();
		
		Display dsp = Display.getCurrent();
		while (!shell.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
		
	}
}
