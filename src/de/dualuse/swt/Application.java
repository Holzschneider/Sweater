package de.dualuse.swt;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Application extends Display implements AutoMenuBar {
	
	
	{
		build(this, this.getMenuBar(), MenuScope.INHERIT, MenuScope.APPLICATION);
		build(this, this.getSystemMenu(), MenuScope.SYSTEM);
	}
	
	//support for splash-windows / start-wizards
	
	//////////////
	
	
	public void loop() {
		while (!isDisposed())
			if (!readAndDispatch())
				sleep();
		
		onDisposed();
	}
	

	/**
	 * Loops as long as the Application Window has not been disposed 
	 * @param applicationWindow
	 */
	public void loop(Shell applicationWindow) {
		while (!applicationWindow.isDisposed())
			if (!readAndDispatch())
				sleep();
	
		dispose();
		onDisposed();
	}
	
	
//	public void addDisposedListener(DisposedListener dl); //need?
	
	protected void onDisposed() { }
		
	
	
	
//	public void loop(Shell... applicationWindow) //need?
	
	
}
