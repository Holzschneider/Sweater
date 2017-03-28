package de.dualuse.swt.example;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.AutoShell;
import de.dualuse.swt.ProgressDialog;
import de.dualuse.swt.ProgressDialog.Task;
import de.dualuse.swt.ProgressDialog.TaskProgress;
import de.dualuse.swt.app.MultiDocumentApplication;

public class TextApplication extends MultiDocumentApplication {

//==[ Constructor ]=================================================================================
	
	public TextApplication() {
		super("Text", "1.0");
	}
	
//==[ Document Handling Implementations for Parent Class ]==========================================
	
	@Override protected Shell newDocument() {
		System.out.println("New Document");
		TextShell shell = new TextShell(this);
		shell.setText("New Document");
		return shell;
	}

	// XXX Exception -> to cancel opening a document, return null? IOException?
	@Override protected Shell openDocument(File document) {
		
//		Shell hiddenShell = new Shell(this, SWT.NONE);
//		HiddenShell hiddenShell = new HiddenShell();
		try (AutoShell hiddenShell = new AutoShell()) {
			ProgressDialog<String> progress = new ProgressDialog<String>(hiddenShell, "Opening Document...");
			
			String result = progress.open(new ProgressDialog.SimpleTask<String>() {
	
				@Override public void execute(TaskProgress<String> tp) {
					
					// tp.indeterminate();
					
					String totalLabel = "Loading Document...";
					String objectLabel = "Loading Object...";
					
					int totalProgress = tp.createProgress(totalLabel, 0, 100);
					int objectProgress = tp.createProgress(objectLabel, 0, 100);
					
					for (int i=0; i<10; i++) {
						
						tp.setProgress(objectProgress, objectLabel, 0, 100);
						for (int j=0; j<100; j++) {
							// try { Thread.sleep(1); } catch (InterruptedException e) {}
							tp.update(objectProgress, j);
						}
						
						tp.update(totalProgress, 10*i);
						
						yield();
					}
					
	//				try { Thread.sleep(10000); } catch (InterruptedException e) {}
					
	//				for (int i=0; i<100; i++) {
	//					tp.update(i);
	//					try { Thread.sleep(100); } catch (InterruptedException e) { break; }
	//				}
					
					tp.done("Result");
				}
			});
			
			System.out.println(result);
			
			try {
				return new TextShell(this, document);
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		
		return null;
	}
	
//==[ App-Main ]====================================================================================
	
	public static void main(String[] args) {
		
		TextApplication app = new TextApplication();
		app.loop();
		
	}

}
