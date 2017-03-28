package de.dualuse.swt.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;

import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.widgets.AutoShell;

public abstract class MultiDocumentApplication extends Application {
	
	public interface ApplicationListener {
		void documentWindowCreated(DocumentWindow window);
		void documentWindowClosed(DocumentWindow window);
	}
	
	Shell activeWindow;
	List<DocumentWindow> openDocuments = new ArrayList<DocumentWindow>();
	List<File> recentDocuments = new ArrayList<File>();
	
	Menu fileMenu;
	
	Menu currentMenu;
	Menu recentMenu;
	
	MenuItem openDocsItem;
	MenuItem recentDocsItem; // XXX Implement Recent Items List
	
	MenuItem closeItem;
	MenuItem closeAllItem;
	
//	Action newDocumentAction;
//	Action openDocumentAction;
//	
//	Action closeDocumentAction;
//	Action closeAllDocumentAction;
//	
//	List<Shell> openDocuments = new ArrayList<Shell>();
//	List<File> recentDocuments = new ArrayList<File>();
	
//==[ Constructor ]=================================================================================
	
	public MultiDocumentApplication(String name, String version) {
		super(name, version);
		
		setupMenu();
	}
	
//==[ Getter ]======================================================================================
	
	public List<DocumentWindow> getOpenWindows() {
		return new ArrayList<DocumentWindow>(openDocuments);
	}
	
	public List<File> getRecentDocuments() {
		return new ArrayList<File>(recentDocuments);
	}
	
//==[ App Menu ]====================================================================================
	
	private void setupMenu() {

		new DocumentMenu(this);
		// Menu menuBar = getMenuBar();
		// addMenuToBar(menuBar);
		
	}
	
	public void addMenuToBar(Menu menuBar) {
		
		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText("&File");
		
		// Menu fileMenu = new Menu(menubar);
		fileMenu = new Menu(cascadeFileMenu); // works as well
		cascadeFileMenu.setMenu(fileMenu); // Only works for cascade items
		
		MenuItem newItem = new MenuItem(fileMenu, SWT.PUSH);
		newItem.setText("New");
		newItem.setAccelerator(SWT.MOD1 | 'N');
		
		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
		openItem.setText("Open");
		openItem.setAccelerator(SWT.MOD1 | 'O');
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		openDocsItem = new MenuItem(fileMenu, SWT.CASCADE);
		openDocsItem.setEnabled(false);
		openDocsItem.setText("Current");
		
		currentMenu = new Menu(openDocsItem);
		openDocsItem.setMenu(currentMenu);
		
		recentDocsItem = new MenuItem(fileMenu, SWT.CASCADE);
		recentDocsItem.setEnabled(false);
		recentDocsItem.setText("Recent");
		
		recentMenu = new Menu(recentDocsItem);
		recentDocsItem.setMenu(recentMenu);
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		closeItem = new MenuItem(fileMenu, SWT.PUSH);
		closeItem.setText("Close");
		closeItem.setEnabled(false);
		closeItem.setAccelerator(SWT.MOD1 | 'W');
		
		closeAllItem = new MenuItem(fileMenu, SWT.PUSH);
		closeAllItem.setText("Close All");
		closeAllItem.setEnabled(false);
		closeAllItem.setAccelerator(SWT.SHIFT | SWT.MOD1 | 'W');
		
		closeItem.addListener(SWT.Selection, (e) -> closeDocumentAction());
		closeAllItem.addListener(SWT.Selection, (e) -> closeAllDocumentsAction());
		newItem.addListener(SWT.Selection, (e) -> newDocumentAction());
		openItem.addListener(SWT.Selection, (e) -> openDocumentAction());
	}
	
	public Menu getFileMenu() {
		return fileMenu;
	}
	
//==[ Menu Actions ]================================================================================
	
	public void newDocumentAction() {
		DocumentWindow newShell = newDocument();
		addDocumentWindow(newShell);
		newShell.open();
	}
	
	public void openDocumentAction() {
		try (AutoShell hiddenShell = new AutoShell()){
			
			FileDialog dialog = new FileDialog(hiddenShell);
			String result = dialog.open();
			if (result==null) return;
			
			File chosenFile = new File(result);
			
			DocumentWindow openedShell = openDocument(chosenFile);
			addDocumentWindow(openedShell);
			openedShell.open();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void closeDocumentAction() {
		if (activeWindow != null)
			activeWindow.close();
	}
	
	public void closeAllDocumentsAction() {
		
		List<Shell> openShells = new ArrayList<Shell>();
		for (int i=0, I=currentMenu.getItemCount(); i<I; i++) {
			MenuItem item = currentMenu.getItem(i);
			openShells.add((Shell)item.getData());
		}
		
		for (Shell shell : openShells)
			shell.close();
		
	}

//==[ Update Current&Recent Menus ]=================================================================
	
	private void addDocumentWindow(final DocumentWindow documentWindow) {

		MenuItem menuItem = new MenuItem(currentMenu, SWT.PUSH);
		menuItem.setText(documentWindow.getText());
		menuItem.setData(documentWindow);
		menuItem.addListener(SWT.Selection, (e) -> {
			if (documentWindow.getMinimized())
				documentWindow.setMinimized(false);
			documentWindow.forceFocus();
		});
		
		openDocsItem.setEnabled(true);
		
		documentWindow.addListener(SWT.Dispose, (e) -> {
			
			System.out.println("Disposed: " + documentWindow.getText());
			
			removeDocumentWindow(documentWindow);
			if (activeWindow == documentWindow)
				setActiveWindow(null);
		});

		documentWindow.addListener(SWT.Show, (e) -> {
			System.out.println("Show: " + documentWindow.getText());
		});
		
		documentWindow.addListener(SWT.Activate, (e) -> {
			
			System.out.println("Activate: " + documentWindow.getText());
			
			setActiveWindow(documentWindow);
		});
		
		documentWindow.addListener(SWT.Deactivate, (e) -> {
			
			System.out.println("Deactivate:");
			System.out.println("\t" + activeWindow);
			System.out.println("\t" + documentWindow);
			
			if (activeWindow == documentWindow)
				setActiveWindow(null);
		});
		
		openDocuments.add(documentWindow);
	}

	private void removeDocumentWindow(final Shell documentWindow) {
		
		for (int i=0; i<currentMenu.getItemCount(); i++) {
			MenuItem menuItem = currentMenu.getItem(i);
			if (menuItem.getData() == documentWindow) {
				menuItem.dispose();
				break;
			}
		}
		
		if (currentMenu.getItemCount()==0)
			openDocsItem.setEnabled(false);
		
	}
	
	/////
	
	private void setActiveWindow(Shell shell) {
		
		activeWindow = shell;
		System.out.println("Active Window: " + (activeWindow!=null ? activeWindow.getText() : "null"));
		
		if (activeWindow==null && closeItem.isEnabled()) {
			closeItem.setEnabled(false);
			closeAllItem.setEnabled(false);
			System.out.println("Close disabled");
		} if (activeWindow!=null && !closeItem.isEnabled()) {
			closeItem.setEnabled(true);
			closeAllItem.setEnabled(true);
			System.out.println("Close enabled");
		}
	}
	
//==[ Application Listener ]========================================================================
	
	CopyOnWriteArrayList<ApplicationListener> listeners = new CopyOnWriteArrayList<ApplicationListener>();
	
	public void addApplicationListener(ApplicationListener listener) {
		listeners.add(listener);
	}
	
	public void removeApplicationListener(ApplicationListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireDocumentWindowCreated(DocumentWindow window) {
		for (ApplicationListener listener : listeners)
			listener.documentWindowCreated(window);
	}
	
	protected void fireDocumentWindowClosed(DocumentWindow window) {
		for (ApplicationListener listener : listeners)
			listener.documentWindowClosed(window);
	}
	
//==[ Document Handling ]===========================================================================
	
	protected abstract DocumentWindow newDocument();
	protected abstract DocumentWindow openDocument(File document);

//==[ Test-Main ]===================================================================================
	
	public static void main_(String[] args) {

		MultiDocumentApplication app = new MultiDocumentApplication("Example Application", "v1.1.45") {

			@Override protected DocumentWindow newDocument() {
				return null;
			}

			@Override protected DocumentWindow openDocument(File document) {
				return null;
			}
			
			@Override public void onDisposed() {
				System.out.println("Disposed");
			}
			
		};
		
		System.out.println(SWT.getPlatform());

		Shell shell = new Shell(app);
		shell.setBounds(100, 100, 600, 400);
		shell.addListener(SWT.Dispose, (e) -> app.dispose());
		shell.open();
		
		app.loop();
		
	}
	
}
