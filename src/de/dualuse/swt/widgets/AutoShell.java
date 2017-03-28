package de.dualuse.swt.widgets;

import org.eclipse.swt.widgets.Shell;

/**
 * 
 * Shell that is automatically closed/disposed when used in a try-block.
 * For example used for creating hidden shells that serve as parents for dialogs that have no parents on MacOS.
 * 
 * @author ihlefeld
 *
 */

public class AutoShell extends Shell implements AutoCloseable {
	
	@Override protected void checkSubclass() {}
	
	@Override public void close() {
		super.close();
		// System.out.println("Hidden shell was closed.");
	}
	
	@Override public void dispose() {
		super.dispose();
		// System.out.println("Hidden shell was disposed.");
	}
	
}
