package de.dualuse.swt.app;

import org.eclipse.swt.widgets.Shell;

public class Window extends Shell implements AutoMenuBar {
	
	//hier per Annotations gleich auch das per-Window-Menu definieren
	
	public Window() {
		build(this.getDisplay(), this.getMenuBar(), MenuScope.WINDOW); //!
		build(this, this.getMenuBar(), MenuScope.INHERIT, MenuScope.WINDOW); //!
	}
	
	
    public static final int DO_NOTHING_ON_CLOSE = 0;
    public static final int HIDE_ON_CLOSE = 1;
    public static final int DISPOSE_ON_CLOSE = 2;
    public static final int EXIT_ON_CLOSE = 3;

	public void setDefaultCloseOperation(int operation) {
		//...
	}
	
	
	
	
}
