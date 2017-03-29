package de.dualuse.swt.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import de.dualuse.swt.app.DocumentWindow;

public class TextShell extends DocumentWindow {
	
	private TextApplication parent;
	
	private File document;
	private Text text;
	private Font font;

	// To allow subclassing
	@Override protected void checkSubclass() {}
	
//==[ Constructor ]=================================================================================
	
	public TextShell(TextApplication parent) {
		super(parent);
		this.parent = parent;
		init();
	}
	
	public TextShell(TextApplication parent, File document) throws IOException {
		this(parent);
		
		setText(document.getAbsolutePath());
		
		this.document = document;
		
		String content = loadDocument(document);
		text.setText(content);
	}
	
	/////
	
	private void init() {
		setBounds(100, 100, 640, 480);
		setLayout(new FillLayout());
		
		text = new Text(this, SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		font = new Font(getDisplay(), "Monospaced", 16, SWT.NONE);
		text.setFont(font);
		
		addListener(SWT.Dispose, (e) -> disposeResources());
		
		addListener(SWT.Close, (e) -> {
			e.doit = false;
			MessageBox mbox = new MessageBox(this, SWT.APPLICATION_MODAL | SWT.OK | SWT.CANCEL);
			mbox.setText("Close Document?");
			mbox.setMessage("Close document and discard changes?");
			int choice = mbox.open();
			e.doit = choice == SWT.OK;
		});
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
		checkWidget();
		return parent;
	}
	
//==[ Resource Management ]=========================================================================
	
	private void disposeResources() {
		font.dispose();
	}
	
}
