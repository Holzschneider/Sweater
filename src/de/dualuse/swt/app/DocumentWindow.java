package de.dualuse.swt.app;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

public class DocumentWindow extends Shell {

	@Override protected void checkSubclass() {}
	
	MultiDocumentApplication application;
	File document;
	
	public DocumentWindow(MultiDocumentApplication application) {
		super(application);
		this.application = application;
		setupMenu();
		System.out.println(application);
	}
	
	public DocumentWindow(MultiDocumentApplication application, File document, int style) {
		super(application, style);
		this.application = application;
		this.document = document;
		setupMenu();
		System.out.println(application);
	}
	
	private void setupMenu() {
		Menu menuBar = new Menu(this, SWT.BAR);
		new DocumentMenu(this, menuBar);
		setMenu(menuBar);
	}
	
	public MultiDocumentApplication getApplication() {
		return application;
	}
	
	public File getDocument() {
		return document;
	}
	
}
