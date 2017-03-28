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
	
	MenuItem newDocument;
	MenuItem openDocument;
	
	MenuItem closeDocument;
	MenuItem closeAllDocuments;
	
	MenuItem openDocumentWindows;
	MenuItem recentDocumentWindows;
	
	Menu openDocumentsMenu;
	Menu recentDocumentsMenu;
	
//==[ Constructor ]=================================================================================
	
	public DocumentMenu(MultiDocumentApplication application) {
		super(application.getMenuBar());
		// super(window.getMenu());

		// MultiDocumentApplication application = window.getApplication();
		init(application);
		setupMenu(application, application.getMenuBar());
	}
	
	public DocumentMenu(DocumentWindow window, Menu menuBar) {
		super(menuBar);
		
		MultiDocumentApplication application = window.getApplication();
		System.out.println("Application: " + application);
		init(application);
		
		closeDocument.setEnabled(true);
		closeDocument.addListener(SWT.Selection, (e) -> window.close());
	}
	
	private void init(MultiDocumentApplication application) {
		application.addApplicationListener(appListener);
		this.addListener(SWT.Dispose, (e) -> application.removeApplicationListener(appListener));
	}
	
	private void setupMenu(MultiDocumentApplication application, Menu menuBar) {
		// File MenuItem & Menu
		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText("&File");
		
		Menu fileMenu = new Menu(cascadeFileMenu);
		cascadeFileMenu.setMenu(fileMenu);
		
		// New Document
		newDocument = new MenuItem(fileMenu, SWT.PUSH);
		newDocument.setText(NEW_DOCUMENT);
		newDocument.setAccelerator(SWT.MOD1 | 'N');
		newDocument.addListener(SWT.Selection, (e) -> application.newDocumentAction());
		
		// Open Document
		openDocument = new MenuItem(fileMenu, SWT.PUSH);
		openDocument.setText(OPEN_DOCUMENT);
		openDocument.setAccelerator(SWT.MOD1 | 'O');
		openDocument.addListener(SWT.Selection, (e) -> application.openDocumentAction());
		
		// Close Document
		closeDocument = new MenuItem(fileMenu, SWT.PUSH);
		closeDocument.setText(CLOSE_DOCUMENT);
		closeDocument.setAccelerator(SWT.MOD1 | 'W');
		closeDocument.setEnabled(false);
		
		// Close All Documents
		closeAllDocuments = new MenuItem(fileMenu, SWT.PUSH);
		closeAllDocuments.setText(CLOSE_ALL_DOCUMENTS);
		closeAllDocuments.setAccelerator(SWT.SHIFT | SWT.MOD1 | 'W');
		closeAllDocuments.setEnabled(false);
		closeAllDocuments.addListener(SWT.Selection, (e) -> application.closeAllDocumentsAction());
		
		// Current Documents
		openDocumentWindows = new MenuItem(fileMenu, SWT.CASCADE);
		openDocumentWindows.setEnabled(false);
		openDocumentWindows.setText(OPEN_DOCUMENTS);
	
		openDocumentsMenu = new Menu(openDocumentWindows);
		openDocumentWindows.setMenu(openDocumentsMenu);
		
		List<DocumentWindow> windows = application.getOpenWindows();
		for (DocumentWindow docWindow : windows)
			addDocumentWindow(docWindow);
		
		// Recent Documents
		recentDocumentWindows = new MenuItem(fileMenu, SWT.CASCADE);
		recentDocumentWindows.setEnabled(false);
		
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
	
	public void addDocumentWindow(DocumentWindow window) {

		MenuItem menuItem = new MenuItem(openDocumentsMenu, SWT.PUSH);
		menuItem.setText(window.getText());
		menuItem.setData(window);
		menuItem.addListener(SWT.Selection, (e) -> {
			if (window.getMinimized())
				window.setMinimized(false);
			window.forceFocus();
		});
		
		openDocumentWindows.setEnabled(true);

		Listener disposeListener = (e) -> removeDocumentWindow(window);
		window.addListener(SWT.Dispose, disposeListener);
		this.addListener(SWT.Dispose, (e) -> window.removeListener(SWT.Dispose, disposeListener));
		
	}
	
	public void removeDocumentWindow(DocumentWindow window) {

		for (int i=0; i<openDocumentsMenu.getItemCount(); i++) {
			MenuItem menuItem = openDocumentsMenu.getItem(i);
			if (menuItem.getData() == window) {
				menuItem.dispose();
				break;
			}
		}
		
		if (openDocumentsMenu.getItemCount()==0)
			openDocumentWindows.setEnabled(false);
		
	}
	
//==[ Update Recent Document Menu ]=================================================================
	
	public void addRecentDocument(File document) {
		// TODO
	}
	
	public void removeRecentDocument(File document) {
		// TODO
	}
	
//==[ Resource Management ]=========================================================================
	
	@Override public void dispose() {
		// ...
		super.dispose();
	}
	
}
