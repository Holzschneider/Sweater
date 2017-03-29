package de.dualuse.swt.app;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

public class Window extends Shell implements AutoMenuBar {
	
	//hier per Annotations gleich auch das per-Window-Menu definieren
	
	public Window() {
		
		setMenuBar(
				new AutoMenuBuilder()
				.add(this.getDisplay(), MenuScope.WINDOW)
				.add(this, MenuScope.UNSPECIFIED, MenuScope.WINDOW)
				.build(new Menu(this,BAR))
		);

//		setMenuBar(
//				new AutoMenuBuilder()
//				.add(this, MenuScope.INHERIT, MenuScope.WINDOW)
//				.add(this.getDisplay(), MenuScope.WINDOW)
//				.build(new Menu(this,BAR))
//			);
		
	}
	
	@Override
	protected void checkSubclass() { }
	
	
    public static final int DO_NOTHING_ON_CLOSE = 0;
    public static final int HIDE_ON_CLOSE = 1;
    public static final int DISPOSE_ON_CLOSE = 2;
    public static final int EXIT_ON_CLOSE = 3;

	public void setDefaultCloseOperation(int operation) {
		//...
	}
	
	
	
	
}
