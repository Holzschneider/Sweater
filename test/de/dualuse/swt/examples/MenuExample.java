package de.dualuse.swt.examples;


import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.app.Window;

public class MenuExample extends Window {

	
	
	
	@AutoMenu("File")
	Widget fileMenu;
	
	@AutoMenuItem({"File","New ..."})
	MenuItem newFileItem;

	@AutoMenuItem({"File","New ..."})
	public void newFile() {
		System.out.println("new");
	}
	
	@AutoMenuItem(path={"File","Open ..."},accelerator=COMMAND|'O',checkbox=true,checked=true)
	public void openFile() {
		System.out.println("open");
	}


	
	@AutoMenu("Window")
	Menu closeAllMenu;

	
	@AutoMenuItem(path={"Window","Close All"})
	MenuItem closeAllItem;

	
	@AutoMenuItem(path={"Window","Close All"})
	public void closeWindows() {
		System.out.println("closeWindows");
	}

	@AutoMenuItem(path={"Window","Hide All"})
	public void hideWindows() {
		System.out.println("hideWindows");
	}

	@AutoMenuItem(path={"Window","Hide All"})
	public void hideOtherWindows() {
		System.out.println("hideOtherWindows");
	}

	@AutoMenuItem(path={"Window","Hide All"})
	Listener hideListener = (e) -> System.out.println("Hide & Seek: "+closeAllItem.getSelection());
	
	public static void main(String[] args) {
		
		System.out.println( MenuExample.class.getResource("undo.png") );
		System.out.println( MenuExample.class.getResource("/de/dualuse/swt/experiments/stop_sm.png") );
		
		
		Application app = new Application() {
			
			@AutoMenuItem(
					path={"File","Open All Recents"},
					scope={MenuScope.WINDOW,MenuScope.APPLICATION}, 
					rank=9999, 
					checkbox=true, 
					checked=true, 
					splitBefore=true, 
					icon="undo.png"
			)
			public void openRecents(MenuItem mi) {
				System.out.println("nö: "+newDocumentItem +" @ "+System.identityHashCode(newDocumentItem));
				newDocumentItem.setEnabled(mi.getSelection());
			}

			@AutoMenu(path={"bla"},index=0, scope=MenuScope.SYSTEM)
			public Menu blaMenu;
			
			@AutoMenuItem(path={"bla","blub"}, scope=MenuScope.SYSTEM)
			public void extraSystemPaneEntry() {
				System.out.println("blabub!!");
			}

			@AutoMenuItem(path="About.*", scope=MenuScope.SYSTEM)
			public void aboutItem() {
				System.out.println("about!!");
			}
			
			
			@AutoMenuItem(path="Preferences.*", scope=MenuScope.SYSTEM)
			public void preferencePane() {
				System.out.println("PREFERENCES!!");
			}
			
			@AutoMenuItem(path={"File","New Document"}, scope={MenuScope.WINDOW, MenuScope.APPLICATION})
			MenuItem newDocumentItem;
			
			@AutoMenuItem(path={"File","New Document"}, scope={MenuScope.WINDOW,MenuScope.APPLICATION}, enabled=false, splitAfter=true, systemIcon=SWT.ICON_WARNING)
			public void newDocument() {
				System.out.println("Application New Document");
				
				MenuExample me = new MenuExample();
				me.setLayout(new FillLayout());
				
				Button but = new Button(me, PUSH | NO_FOCUS);
				but.setText("Hallo");
				
				but.addListener(Selection, (e)-> System.out.println("haha"));
				
				me.setBounds(600, 150, 800, 800);
				me.setVisible(true);
				me.forceFocus();
			}
			
		};
		
		
		
		
		app.loop();
	}
}






