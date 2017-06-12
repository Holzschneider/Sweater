package de.dualuse.swt.app;

import java.io.File;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class DocumentMenu extends Menu {
	
	@Override public void checkSubclass() {}
	
	// TODO modify in DocumentWindow subclasses
	final static String NEW_DOCUMENT = "New";
	final static String OPEN_DOCUMENT = "Open";
	
	final static String CLOSE_DOCUMENT = "Close";
	final static String CLOSE_ALL_DOCUMENTS = "Close All";
	
	final static String OPEN_DOCUMENTS = "Current";
	final static String RECENT_DOCUMENTS = "Recent";
	
	MenuItem newDocumentMenuItem;
	MenuItem openDocumentMenuItem;
	
	MenuItem closeDocumentMenuItem;
	MenuItem closeAllDocumentsMenuItem;
	
	MenuItem currentDocumentsMenuItem;
	MenuItem recentDocumentsMenuItem;
	
	Menu currentDocumentsMenu;
	Menu recentDocumentsMenu;
	
	boolean appMenu = false;
	
//==[ Constructor ]=================================================================================
	
	// Application Menu (OS X only, requires an application menu independent of a window/shell)
	public DocumentMenu(MultiDocumentApplication application) {
		super(application.getMenuBar());
		// super(window.getMenu());

		appMenu = true;
		
		// MultiDocumentApplication application = window.getApplication();
		init(application);
		setupMenu(application, application.getMenuBar());
	}
	
	// Window Menu
	public DocumentMenu(MultiDocumentApplication application, DocumentWindow window, Menu menuBar) {
		super(menuBar);
		
		init(application);
		setupMenu(application, menuBar);
		
		closeDocumentMenuItem.setEnabled(true);
		closeDocumentMenuItem.addListener(SWT.Selection, (e) -> window.close());
	}
	
	/////
	
	private void init(MultiDocumentApplication application) {
		application.addApplicationListener(appListener);
		this.addListener(SWT.Dispose, (e) -> application.removeApplicationListener(appListener));
	}
	
	private void setupMenu(MultiDocumentApplication application, Menu menuBar) {
		// File MenuItem & Menu
		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		// cascadeFileMenu.setText("&File");
		if (appMenu)
			cascadeFileMenu.setText("&App");
		else
			cascadeFileMenu.setText("&File");
		
		Menu fileMenu = new Menu(cascadeFileMenu);
		cascadeFileMenu.setMenu(fileMenu);
		
		// New Document
		newDocumentMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		newDocumentMenuItem.setText(NEW_DOCUMENT);
		newDocumentMenuItem.setAccelerator(SWT.MOD1 | 'N');
		newDocumentMenuItem.addListener(SWT.Selection, (e) -> application.createNewDocument());
		newDocumentMenuItem.setEnabled(application.canCreate());
		
		// Open Document
		openDocumentMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		openDocumentMenuItem.setText(OPEN_DOCUMENT);
		openDocumentMenuItem.setAccelerator(SWT.MOD1 | 'O');
		openDocumentMenuItem.addListener(SWT.Selection, (e) -> application.openDocument());
		openDocumentMenuItem.setEnabled(application.canOpen());
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		// Close Document
		closeDocumentMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		closeDocumentMenuItem.setText(CLOSE_DOCUMENT);
		closeDocumentMenuItem.setAccelerator(SWT.MOD1 | 'W');
		closeDocumentMenuItem.setEnabled(false);
		
		// Close All Documents
		closeAllDocumentsMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		closeAllDocumentsMenuItem.setText(CLOSE_ALL_DOCUMENTS);
		closeAllDocumentsMenuItem.setAccelerator(SWT.SHIFT | SWT.MOD1 | 'W');
		closeAllDocumentsMenuItem.setEnabled(false);
		closeAllDocumentsMenuItem.addListener(SWT.Selection, (e) -> application.closeAllDocuments());
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		// Current Documents
		currentDocumentsMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		currentDocumentsMenuItem.setText(OPEN_DOCUMENTS);
		currentDocumentsMenuItem.setEnabled(false);
	
		currentDocumentsMenu = new Menu(currentDocumentsMenuItem);
		currentDocumentsMenuItem.setMenu(currentDocumentsMenu);
		
		List<DocumentWindow> windows = application.getOpenWindows();
		System.out.println("Menu Creation: Currently existing windows");
		for (DocumentWindow docWindow : windows) {
			System.out.println("\tAdding open window: " + docWindow.getText());
			addDocumentWindow(docWindow);
		}
		
		// Recent Documents
		recentDocumentsMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		recentDocumentsMenuItem.setText(RECENT_DOCUMENTS);
		recentDocumentsMenuItem.setEnabled(false);
		
		recentDocumentsMenu = new Menu(recentDocumentsMenuItem);
		recentDocumentsMenuItem.setMenu(recentDocumentsMenu);
		
		// XXX get recent document windows from application and populate menu
	}
	
//==[ App Listener ]================================================================================
	
	MultiDocumentApplication.ApplicationListener appListener = new MultiDocumentApplication.ApplicationListener() {
		@Override public void documentWindowCreated(DocumentWindow window) {
			addDocumentWindow(window);
		}

		@Override public void documentWindowClosed(DocumentWindow window) {
			removeDocumentWindow(window);
		}
	};
	
//==[ Update Open Document Menu ]===================================================================
	
	// New window has been opened (add corresponding menu entry)
	private void addDocumentWindow(DocumentWindow window) {

		// Add MenuItem for new Window in CurrentDocumentsMenu
		MenuItem menuItem = new MenuItem(currentDocumentsMenu, SWT.PUSH);
		menuItem.setText(window.getText());
		menuItem.setData(window);
		menuItem.addListener(SWT.Selection, (e) -> {
			if (window.getMinimized())
				window.setMinimized(false);
			window.forceFocus();
		});

		// Add Window Dispose Listener (remove entry from CurrnetDocumentsMenu if window closed)
		Listener disposeListener = (e) -> removeDocumentWindow(window);
		window.addListener(SWT.Dispose, disposeListener);
		
		// Remove window dispose listener if menu itself is disposed
		addListener(SWT.Dispose, (e) -> {
			if (!window.isDisposed()) // XXX own window already disposed at this point (either check or don't add if own window above)
				window.removeListener(SWT.Dispose, disposeListener);
		});
		
		// Update menu state
		currentDocumentsMenuItem.setEnabled(true);
		closeAllDocumentsMenuItem.setEnabled(true);

	}
	
	// Window has been closed (remove corresponding menu entry)
	private void removeDocumentWindow(DocumentWindow window) {

		// Remove MenuItem from the CurrentDocuments Menu for the window that has been closed
		for (int i=0; i<currentDocumentsMenu.getItemCount(); i++) {
			MenuItem menuItem = currentDocumentsMenu.getItem(i);
			if (menuItem.getData() == window) {
				menuItem.dispose();
				break;
			}
		}
		
		// Update menu state
		if (currentDocumentsMenu.getItemCount()==0) {
			currentDocumentsMenuItem.setEnabled(false);
			closeAllDocumentsMenuItem.setEnabled(false);
		}
	}
	
//==[ Update Recent Document Menu ]=================================================================
	
	public void addRecentDocument(File document) {
		// TODO
	}
	
	public void removeRecentDocument(File document) {
		// TODO
	}
	
}
