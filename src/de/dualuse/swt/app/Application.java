package de.dualuse.swt.app;

import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

public class Application extends Display implements AutoMenuBar {

	//support for splash-windows / start-wizards
	
//==[ Constructor ]=================================================================================
	public Application() {
		this(deriveAppName());
	}
	
	public Application(String title) {
		super(setAppNameVersion(title));
	}
	
	public Application(String title, String version) {
		super(setAppNameVersion(title, version));
	}
	
	private static String deriveAppName() {
		StackTraceElement ste[] = new Throwable().getStackTrace();
		String className = ste[ste.length-1].getClassName();
		String simpleName = className.replaceAll("^.+\\.", "");
		String appName = simpleName.replaceAll("((\\p{javaLowerCase})(\\p{javaUpperCase}))", "$2 $3");
		
		return appName;
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
	
	final public Menu applicationMenu =	new AutoMenuBuilder()
									.add(this, MenuScope.UNSPECIFIED, MenuScope.APPLICATION)
									.build(this.getMenuBar());
	
	final public Menu systemMenu = new AutoMenuBuilder()
									.add(this, MenuScope.SYSTEM)
									.build(this.getSystemMenu());
			
	

//==[ Event Loop ]==================================================================================
	
	public void loop() {
		checkDevice();
		
		try {
			
			while (!isDisposed())
				if (!readAndDispatch()) 
					sleep();
			
		} finally {
			onDisposed();
		}
	}

	/**
	 * Loops as long as the Application Window has not been disposed 
	 * @param applicationWindow
	 */
	
	public void loop(Shell applicationWindow) {
		checkDevice();
		
		try {
		
			while (!applicationWindow.isDisposed())
				if (!readAndDispatch())
					sleep();
		
			dispose();
		
		} finally {
			onDisposed();
		}
	}
	

//==[ Resource Management ]=========================================================================
	
	protected void onDisposed() { }

//	public void addDisposedListener(DisposedListener dl); //need?
//	public void loop(Shell... applicationWindow) //need?
	
//==[ Test-Main ]===================================================================================
	
	public static void main_(String[] args) {
		
	}
}
