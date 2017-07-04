package de.dualuse.swt.app;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;



///XXX TODO Ableitbar machen und eine generic mainMethod bauen, sodass man kein new Application().loop() braucht

public class Application extends Display implements AutoMenuBar {

	//support for splash-windows / start-wizards
	
//==[ Constructor ]=================================================================================
	
	public Application() {
		this(deriveAppName());
	}
	
	public Application(DeviceData data) {
		this(data, deriveAppName());
	}
	
	public Application(String title) {
		super(setAppNameVersion(title));
	}
	
	public Application(DeviceData data, String title) {
		super(setAppNameVersion(data,title));
	}
	
	public Application(String title, String version) {
		super(setAppNameVersion(title, version));
	}
	
	public Application(DeviceData data, String title, String version) {
		super(setAppNameVersion(data, title, version));
	}
	
	/////
	
	private static String deriveAppName() {
		StackTraceElement ste[] = new Throwable().getStackTrace();
		String className = ste[ste.length-1].getClassName();
		String simpleName = className.replaceAll("^.+\\.", "");
		String appName = simpleName.replaceAll("((\\p{javaLowerCase})(\\p{javaUpperCase}))", "$2 $3");
		return appName;
	}

	// setAppName() and setAppVersion must be called before Display() Object is intialized
	private static DeviceData setAppNameVersion(DeviceData data, String title) {
		if (data==null) data = new DeviceData();
		Display.setAppName(title);
		return data;
	}
	
	private static DeviceData setAppNameVersion(String title) {
		Display.setAppName(title);
		return new DeviceData();
	}
	
	private static DeviceData setAppNameVersion(DeviceData data, String title, String version) {
		if (data==null) data = new DeviceData();
		Display.setAppName(title);
		Display.setAppVersion(version);
		return data;
	}
	
	private static DeviceData setAppNameVersion(String title, String version) {
		DeviceData data = new DeviceData();
		Display.setAppName(title);
		Display.setAppVersion(version);
		return data;
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
	//TODO add the the option to supply an Exception Handler that catches & handles RuntimeExceptions, that have been caused by events 
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
	
//	XXX Device.Dispose 
//	@Override
//	public boolean readAndDispatch() {
//		if (!this.isDisposed())
//			return super.readAndDispatch();
//		else
//			return false;
//	}
	
//==[ Event Loop that allows execution immediately after the processing for the current event finishes ]==
	
	List<Runnable> runAfter = new ArrayList<>();
	
	public void taskLoop() {
		checkDevice();
		
		try {
			
			while (!isDisposed()) {
				boolean moreWork = false;
				
				do {
					
					// Process delayed jobs that are supposed to run after the event handling of the last event was done
					// (could in turn add more events that need to be processed)
					while (!runAfter.isEmpty()) {
						Runnable task = runAfter.remove(runAfter.size()-1);
						task.run();
					}
					
					// Process event
					// (event handling code could add more runnables that need to be processed)
					moreWork = !isDisposed() ? readAndDispatch() : false;
					
				} while (!runAfter.isEmpty() || moreWork);
				
				if (!isDisposed())
					sleep();
				
			}
			
		} finally {
			onDisposed();
		}
	}

	public void runAfterEvent(Runnable run) {
		checkDevice();
		runAfter.add(run);
	}
	
//==[ Resource Management ]=========================================================================
	
	protected void onDisposed() { }

//	public void addDisposedListener(DisposedListener dl); //need?
//	public void loop(Shell... applicationWindow) //need?
	
//==[ Test-Main ]===================================================================================
	
	public static void main_(String[] args) {
		
	}
}
