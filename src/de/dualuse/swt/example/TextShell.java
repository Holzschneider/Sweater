package de.dualuse.swt.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TextShell extends Shell {
	
	private File document;
	private Text text;
	private Font font;

	// Without overriding this method, throws exception that is silently caught in the SWT Event loop,
	// so the code after super(parent) is never executed and control returns/continues in the SWT Event Loop.
	@Override protected void checkSubclass() {}
	
//==[ Constructor ]=================================================================================
	
	public TextShell(TextApplication parent) {
		super(parent);
		init();
		// open();
	}
	
	public TextShell(TextApplication parent, File document) throws IOException {
		super(parent);
		init();
		
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
	
//==[ Resource Management ]=========================================================================
	
	@Override public void dispose() {
		super.dispose();
		text.dispose();
		font.dispose();
	}
	
}
