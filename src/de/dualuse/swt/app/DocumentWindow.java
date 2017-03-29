package de.dualuse.swt.app;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

public class DocumentWindow extends Shell {

	@Override protected void checkSubclass() {}
	
	MultiDocumentApplication application;
	File document;

//==[ Constructor ]=================================================================================
	
	public DocumentWindow(MultiDocumentApplication application) {
		super(application);
		this.application = application;
		setupMenu();
	}
	
	public DocumentWindow(MultiDocumentApplication application, File document, int style) {
		super(application, style);
		this.application = application;
		this.document = document;
		setupMenu();
	}
	
	/////
	
	private void setupMenu() {
		Menu menuBar = new Menu(this, SWT.BAR);
		new DocumentMenu(application, this, menuBar);
		setMenuBar(menuBar);
	}
	
//==[ Getter ]======================================================================================
	
	public MultiDocumentApplication getApplication() {
		checkWidget();
		return application;
	}
	
	public File getDocument() {
		checkWidget();
		return document;
	}
	
}
