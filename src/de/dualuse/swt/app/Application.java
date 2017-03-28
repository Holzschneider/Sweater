package de.dualuse.swt.app;

import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.AutoMenuBar.MenuScope;

public class Application extends Display implements AutoMenuBar {

	//support for splash-windows / start-wizards
	
//==[ Constructor ]=================================================================================
	
	public Application(String title) {
		super(setAppNameVersion(title));
	}
	
	public Application(String title, String version) {
		super(setAppNameVersion(title, version));
	}
	
	// setAppName() and setAppVersion must be called before Display() Object is intialized
	private static DeviceData setAppNameVersion(String title) {
		Display.setAppName(title);
		return new DeviceData();
	}
	
	private static DeviceData setAppNameVersion(String title, String version) {
		Display.setAppName(title);
		Display.setAppVersion(version);
		return new DeviceData();
	}
	
	// Overriden to silence SWTException that is otherwise thrown because of subclassing
	@Override protected void checkSubclass() {}
	
//==[ Build Application Menu ]======================================================================
	
	{
		build(this, this.getMenuBar(), MenuScope.INHERIT, MenuScope.APPLICATION);
		build(this, this.getSystemMenu(), MenuScope.SYSTEM);
	}

//==[ Event Loop ]==================================================================================
	
	public void loop() {
		while (!isDisposed())
			if (shouldSleep()) 
				sleep();
		
		onDisposed();
	}

	/**
	 * Loops as long as the Application Window has not been disposed 
	 * @param applicationWindow
	 */
	
	public void loop(Shell applicationWindow) {
		while (!applicationWindow.isDisposed())
			if (shouldSleep())
				sleep();
	
		dispose();
		onDisposed();
	}
	
	// XXX readAndDispatch() throws NullPointerException or SWTException("Device is disposed") when 
	//     application is terminated via the system menu, presents onDisposed() from being called
	private boolean shouldSleep() {
		try {
			return !readAndDispatch();
		} catch (Exception e) {
			return false;
		}
	}

//==[ Resource Management ]=========================================================================
	
	protected void onDisposed() { }

//	public void addDisposedListener(DisposedListener dl); //need?
//	public void loop(Shell... applicationWindow) //need?
	
//==[ Test-Main ]===================================================================================
	
	public static void main(String[] args) {
		
	}
}
