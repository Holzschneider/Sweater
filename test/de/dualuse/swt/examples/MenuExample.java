package de.dualuse.swt.examples;


import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.app.Window;

public class MenuExample extends Window {
	
	@AutoMenu(value="File")
	Widget fileMenu;
	
	@AutoMenuItem(value={"File","New ..."})
	MenuItem newFileItem;

	@AutoMenuItem(value={"File","New ..."})
	public void newFile() {
		System.out.println("new");
	}
	
	@AutoMenuItem(value={"File","Open ..."})
	public void openFile() {
		System.out.println("open");
	}


	
	@AutoMenu(value={"Window"})
	Menu closeAllMenu;

	
	@AutoMenuItem(value={"Window","Close All"})
	MenuItem closeAllItem;

	
	@AutoMenuItem(value={"Window","Close All"})
	public void closeWindows() {
		System.out.println("closeWindows");
	}

	@AutoMenuItem(value={"Window","Hide All"})
	public void hideWindows() {
		System.out.println("hideWindows");
	}

	@AutoMenuItem(value={"Window","Hide All"})
	public void hideOtherWindows() {
		System.out.println("hideOtherWindows");
	}

	@AutoMenuItem(value={"Window","Hide All"})
	Listener hideListener = (e) -> System.out.println("Hide & Seek: "+closeAllItem.getSelection());
	
	public static void main(String[] args) {

		Application app = new Application();
		
		MenuExample me = new MenuExample();
		me.setLayout(new FillLayout());
		
		Button but = new Button(me, PUSH | NO_FOCUS);
		but.setText("Hallo");
		
		but.addListener(Selection, (e)-> System.out.println("haha"));
		
		me.setBounds(600, 150, 800, 800);
		me.setVisible(true);
		me.forceFocus();
		
		
		app.loop(me);
	}
}






