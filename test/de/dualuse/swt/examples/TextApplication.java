package de.dualuse.swt.examples;

import java.io.File;
import java.io.IOException;

import de.dualuse.swt.app.DocumentWindow;
import de.dualuse.swt.app.MultiDocumentApplication;
import de.dualuse.swt.widgets.AutoShell;
import de.dualuse.swt.widgets.ProgressDialog;
import de.dualuse.swt.widgets.ProgressDialog.Progress;
import de.dualuse.swt.widgets.ProgressDialog.TaskProgress;

public class TextApplication extends MultiDocumentApplication {

//==[ Constructor ]=================================================================================
	
	public TextApplication() {
		super("Text", "1.0");
	}
	
//==[ Document Handling Implementations for Parent Class ]==========================================

	@Override protected DocumentWindow emptyWindow() {
		TextShell shell = new TextShell(this);
		shell.setText("TextApp");
		return shell;
	}

	@Override protected DocumentWindow newDocument() {
		TextShell shell = new TextShell(this);
		shell.setText("New Document");
		
		this.isDisposed();
		return shell;
	}

	@Override protected DocumentWindow openDocument(File document) {
		
//		Shell hiddenShell = new Shell(this, SWT.NONE);
//		HiddenShell hiddenShell = new HiddenShell();
		try (AutoShell hiddenShell = new AutoShell()) {
			
			ProgressDialog<String> progress = new ProgressDialog<String>(hiddenShell, "Opening Document...");
			
			String result = progress.open(new ProgressDialog.SimpleTask<String>() {
	
				@Override public void execute(TaskProgress<String> tp) {
//					
//					Progress progress = tp.createIndeterminateProgress();
//					
//					try { Thread.sleep(8000); } catch (InterruptedException e) {}
//					
//					tp.done("done");
//					
//					if (true) return;
					
					// tp.indeterminate();
					
					Progress totalProgress = tp.createProgress();
					Progress objectProgress = tp.createProgress();
					
					totalProgress.setLabel("Loading Document...");
					objectProgress.setLabel("Loading Object...");
					
					for (int i=0; i<=10; i++) {
						
						objectProgress.setValues(0, 100, 0);
						for (int j=0; j<=100; j++) {
							try { Thread.sleep(32); } catch (InterruptedException e) {}
							objectProgress.setValue(j);
						}
						
						totalProgress.setValue(10*i);
						
						yield();
					}
					
					try { Thread.sleep(1000); } catch (InterruptedException e) {}
					
					Progress thirdProgress = tp.createProgress();
					
					for (int i=0; i<=100; i++) {
						
						try { Thread.sleep(25); } catch (InterruptedException e) {}
						thirdProgress.setValue(i);
						
						yield();
					}
					
					// tp.abort(new RuntimeException("test"));
					// tp.abort();
					tp.done("Result");
					
					
					/*
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
					*/
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
