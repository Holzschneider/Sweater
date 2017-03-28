package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class MenuTest {
	public static void main(String[] args) {
//		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MenuMenu");
		Display.setAppName("Menu");
		Display.setAppVersion("0.0.1");
		
		Display dsp = new Display();
		Image im = dsp.getSystemImage(SWT.ICON_INFORMATION);
		Image im2 = dsp.getSystemImage(SWT.ICON_WARNING);
		

		{
			System.out.println( dsp.getSystemMenu() );
			dsp.getSystemMenu().getItems()[0].setText("About Menues");
			
			Menu bar = dsp.getMenuBar();
		
			{
				MenuItem mi = new MenuItem(bar, CASCADE);
				mi.setText("Application");
				
				Menu applicationMenu = new Menu(mi);
				mi.setMenu(applicationMenu);
				
				MenuItem openItem = new MenuItem(applicationMenu, PUSH);
				openItem.setText("hallo");
				openItem.setImage(im2);
				openItem.setAccelerator(COMMAND|'H');
				
				MenuItem closeItem = new MenuItem(applicationMenu, CHECK);
				closeItem.setText("welt");
				closeItem.setSelection(true);
			}
		

			{
				/// MENU
				MenuItem mi = new MenuItem(bar, CASCADE);
				mi.setText("Application2");
				
				Menu applicationMenu = new Menu(mi);
//				Menu fileMenu = new Menu(sh1, DROP_DOWN);
				mi.setMenu(applicationMenu);
				MenuItem openItem = new MenuItem(applicationMenu, PUSH);
				
				openItem.setText("open");
				MenuItem closeItem = new MenuItem(applicationMenu, PUSH);
				closeItem.setText("close");
		
				openItem.setAccelerator(SHIFT|'O');
			}		
			
//			/// MENU
//			Menu m = new Menu(dsp, BAR);
//			MenuItem mi = new MenuItem(m, CASCADE);
//			mi.setText("File");
//			
//			Menu fileMenu = new Menu(dsp, DROP_DOWN);
//			mi.setMenu(fileMenu);
//			MenuItem openItem = new MenuItem(fileMenu, PUSH);
//			
//			openItem.setText("open");
//			MenuItem closeItem = new MenuItem(fileMenu, PUSH);
//			closeItem.setText("close");
//	
//			openItem.setAccelerator(SHIFT|'O');
//			
//			
//			sh1.setMenuBar(m);
		}
		
		//////////////////////////////
		

		

		/// WINDOW
		Shell sh1 = new Shell(dsp);
		sh1.setText(" ONE ");
		sh1.setImage( im );
		sh1.setBounds(100, 100, 800, 800);
		sh1.setVisible(true);

		{
			/// MENU
			Menu m = new Menu(sh1, BAR);
//			Menu m = sh1.getMenuBar();
			MenuItem mi = new MenuItem(m, CASCADE);
			mi.setText("File");
			
			Menu fileMenu = new Menu(m);
//			Menu fileMenu = new Menu(sh1, DROP_DOWN);
			mi.setMenu(fileMenu);
			MenuItem openItem = new MenuItem(fileMenu, PUSH);
			
			openItem.setText("open");
			MenuItem closeItem = new MenuItem(fileMenu, PUSH);
			closeItem.setText("close");
	
			openItem.setAccelerator(SHIFT|'O');
			sh1.setMenuBar(m);
		}		
		
		
		
		//////////////////
		
		Shell sh2 = new Shell(dsp);
		sh2.setText(" TWO");
		sh2.setImage( im );
		sh2.setBounds(1000, 100, 800, 800);
		sh2.setVisible(true);
		
//		{
//
//			/// MENU
//			Menu m = new Menu(sh2, BAR);
//			MenuItem mi = new MenuItem(m, CASCADE);
//			mi.setText("Document");
//			
//			Menu fileMenu = new Menu(sh2, DROP_DOWN);
//			mi.setMenu(fileMenu);
//			MenuItem openItem = new MenuItem(fileMenu, PUSH);
//			
//			openItem.setText("open");
//			MenuItem closeItem = new MenuItem(fileMenu, PUSH);
//			closeItem.setText("close");
//	
//			openItem.setAccelerator(SHIFT|'O');
//			
//			
//			sh2.setMenuBar(m);
//		}

//		Region r = new Region(dsp);
//		r.add(new int[] {0,0,  100,100, 200,0});
//		sh2.setRegion(r);
		
		while (!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
	}
}
