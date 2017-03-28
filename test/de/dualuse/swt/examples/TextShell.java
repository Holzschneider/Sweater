package de.dualuse.swt.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import de.dualuse.swt.app.DocumentWindow;

public class TextShell extends DocumentWindow {
	
	private TextApplication parent;
	
	private File document;
	private Text text;
	private Font font;

	// Without overriding this method, throws exception that is silently caught in the SWT Event loop,
	// so the code after super(parent) is never executed and control returns/continues in the SWT Event Loop.
	@Override protected void checkSubclass() {}
	
//==[ Constructor ]=================================================================================
	
	// XXX super(parent) -> constructor of DocumentWindow, tries to setup DocumentMenu
	//			DocumentMenu calls window.getApplication()
	//			TextShell overrides getApplication() to return TextApplication instance,
	//			but TextApplication isn't set yet during super(parent) call, only gets set later
	
	public TextShell(TextApplication parent) {
		super(parent);
		new RuntimeException().printStackTrace();
		this.parent = parent;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// open();
	}
	
	public TextShell(TextApplication parent, File document) throws IOException {
		super(parent);
		this.parent = parent;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setText(document.getAbsolutePath());
		
		this.document = document;
		text.setText(loadDocument(document));
		
		// open();
	}
	
	
	
	private void init() {
		setBounds(100, 100, 640, 480);
		setLayout(new FillLayout());
		text = new Text(this, SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL) {
			@Override protected void checkSubclass() {}
			@Override public void dispose() {
				super.dispose();
				System.out.println("Text disposed");
			}
		};
		
		font = new Font(getDisplay(), "Monospaced", 16, SWT.NONE);
		text.setFont(font);
		
		setupMenu();
	}
	
	private void setupMenu() {

		Menu menu = new Menu(this, SWT.BAR);

//		parent.addMenuToBar(menu);
		
//		Menu fileMenu = parent.getFileMenu();
//		MenuItem cascadeFileMenu = new MenuItem(menu, SWT.CASCADE);
//		cascadeFileMenu.setText("File");
//		cascadeFileMenu.setMenu(fileMenu);
		
		MenuItem cascadeWindowMenu = new MenuItem(menu, SWT.CASCADE);
		cascadeWindowMenu.setText("Window");
		
		Menu windowMenu = new Menu(cascadeWindowMenu);
		cascadeWindowMenu.setMenu(windowMenu);
		
		MenuItem testItem = new MenuItem(windowMenu, SWT.PUSH);
		testItem.setText("Test");
		testItem.setAccelerator(SWT.MOD1 | 'T');
		
		setMenuBar(menu);
	}

	private String loadDocument(File document) throws IOException {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(document))) {
			for (String line = br.readLine(); line != null; line = br.readLine())
				builder.append(line);
		}
		return builder.toString();
	}

//==[ Getter / Setter ]=============================================================================
	
	public File getDocument() {
		checkWidget();
		return document;
	}
	
	public void setDocument(File document) throws IOException {
		checkWidget();
		this.document = document;
		loadDocument(document);
	}

	public String getContent() {
		checkWidget();
		return text.getText();
	}
	
	public void setContent(String tex) {
		checkWidget();
		text.setText(tex);
	}
	
	@Override public TextApplication getApplication() {
		return parent;
	}
	
//==[ Resource Management ]=========================================================================
	
	@Override public void dispose() {
		super.dispose();
		text.dispose();
		font.dispose();
	}
	
}
