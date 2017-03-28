package de.dualuse.swt;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.dualuse.swt.app.Window;

public class WindowTest extends Window {
	@AutoMenu(value="File",rank=10) Menu fileMenu;
	
//	@AutoMenuItem({"File","Recents", SEPARATOR}) MenuItem bla;
	
	@AutoMenuItem({"File", "Recents", SEPARATOR}) MenuItem bla;
	
	
	@AutoMenuItem(value={"File","Recents","..."},  accelerator=ALT|FUNCTION|'4')
	public void open() {
		
	}
	
	
	

	public static void main(String[] args) {
		
		
	}
}
