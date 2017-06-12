package de.dualuse.swt.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.widgets.AutoShell;

public abstract class MultiDocumentApplication extends Application {
	
	static final String DEFAULT_DOCTYPE = "Document";
	
	public interface ApplicationListener {
		
		void documentWindowCreated(DocumentWindow window);
		void documentWindowClosed(DocumentWindow window);
			// Recent Documents? -> use closed Listener
		
		// Document Saved?
		
		// Application Close Listener? (that can cancel the process)
	}
	
	List<DocumentWindow> openDocuments = new ArrayList<DocumentWindow>();
	List<File> recentDocuments = new ArrayList<File>();
	
	boolean canCreate;			// can create new documents
	boolean canOpen;			// can open documents
	boolean directoryDocuments;	// can open directories as documents
	
	String lastDir;				// last directory
	String docType;
	
//==[ Constructor ]=================================================================================
	
	public MultiDocumentApplication(String name, String version) {
		this(name, version, DEFAULT_DOCTYPE, true, true);
	}
	
	public MultiDocumentApplication(String name, String version, String docType) {
		this(name, version, docType, true, true);
	}
	
	public MultiDocumentApplication(String name, String version, String docType, boolean canCreate, boolean canOpen) {
		super(name, version);
		
		this.canCreate = canCreate;
		this.canOpen = canOpen;
		this.docType = docType;
		
		if (this.getMenuBar() == null) {
			
			// Systems without application menu bar (Windows, Linux depending on WM)
			DocumentWindow emptyWindow = emptyWindow();
			emptyWindow.open();
			
		} else {
			
			// Systems that support an application bar (OS X)
			new DocumentMenu(this);
			
		}
	}
	
//==[ Setter & Getter ]=============================================================================
	
	public List<DocumentWindow> getOpenWindows() {
		checkDevice();
		return new ArrayList<DocumentWindow>(openDocuments);
	}
	
	public List<File> getRecentDocuments() {
		checkDevice();
		return new ArrayList<File>(recentDocuments);
	}
	
	public boolean canCreate() {
		return canCreate;
	}
	
	public boolean canOpen() {
		return canOpen;
	}
	
	public void setDirectoryDocuments(boolean directoryDocuments) {
		this.directoryDocuments = directoryDocuments;
	}
	
	public boolean isDirectoryDocuments() {
		return this.directoryDocuments;
	}
	
	public void setLastDirectory(String path) {
		this.lastDir = path;
	}
	
	public String getLastDirectory() {
		return lastDir;
	}
	
//==[ Application Actions ]=========================================================================
	
	// Create new document
	public void createNewDocument() {
		checkDevice();
		
		DocumentWindow newShell = newDocument();

		newShell.open();
		
		addDocumentWindow(newShell);
	}
	
	// Open existing document
	public void openDocument() {
		checkDevice();
		
		try (AutoShell hiddenShell = new AutoShell()){
			
			if (directoryDocuments) {

				DirectoryDialog dialog = new DirectoryDialog(hiddenShell);
				dialog.setText("Open " + docType);
				if (lastDir!=null) dialog.setFilterPath(lastDir);
				
				String result = dialog.open();
				if (result==null) return;
				
				File chosenFile = new File(result);
				lastDir = chosenFile.getParentFile().getAbsolutePath();
				
				DocumentWindow openedShell = openDocument(chosenFile);
				
				openedShell.open();
				
				addDocumentWindow(openedShell);

			} else {

				FileDialog dialog = new FileDialog(hiddenShell);
				dialog.setText("Open " + docType);
				if (lastDir!=null) dialog.setFilterPath(lastDir);
				
				String result = dialog.open();
				if (result==null) return;
				
				File chosenFile = new File(result);
				lastDir = chosenFile.getParentFile().getAbsolutePath();
				
				DocumentWindow openedShell = openDocument(chosenFile);
				
				openedShell.open();
				
				addDocumentWindow(openedShell);
				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Close particular Document
	// could override close request (default: just pass on to window)
	public void closeDocument(DocumentWindow window) {
		checkDevice();
		
		window.close();
	}
	
	// Close all documents
	public void closeAllDocuments() {
		checkDevice();
		
		for (DocumentWindow window : new ArrayList<>(openDocuments))
			window.close();
	}

//==[ Update Current&Recent Menus ]=================================================================
	
	private void addDocumentWindow(final DocumentWindow window) {
		// System.out.println("App: Window added");
		openDocuments.add(window);
		window.addListener(SWT.Dispose, (e) -> {
			removeDocumentWindow(window);
		});
		fireDocumentWindowCreated(window);
	}
	
	private void removeDocumentWindow(final DocumentWindow window) {
		// System.out.println("App: Window removed");
		openDocuments.remove(window);
		fireDocumentWindowClosed(window);
	}
	
//==[ Application Listener ]========================================================================
	
	CopyOnWriteArrayList<ApplicationListener> listeners = new CopyOnWriteArrayList<ApplicationListener>();
	
	public void addApplicationListener(ApplicationListener listener) {
		checkDevice();
		listeners.add(listener);
	}
	
	public void removeApplicationListener(ApplicationListener listener) {
		checkDevice();
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
	
	// Window that displays the application menu on systems that don't support appliation menus independent of windows
	protected abstract DocumentWindow emptyWindow();
	
	// Create window for new document
	protected abstract DocumentWindow newDocument();
	
	// Create window for existing document and load it
	protected abstract DocumentWindow openDocument(File document);

//==[ Test-Main ]===================================================================================
	
	public static void main(String[] args) {

		MultiDocumentApplication app = new MultiDocumentApplication("Example Application", "v1.1.45", "Document") {

			@Override protected DocumentWindow emptyWindow() {
				return null;
			}
			
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
