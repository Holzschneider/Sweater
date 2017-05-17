package de.dualuse.swt.experiments.scratchy;

import static org.eclipse.swt.SWT.NO_BACKGROUND;
import static org.eclipse.swt.SWT.SHELL_TRIM;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.experiments.scratchy.video.Video;
import de.dualuse.swt.experiments.scratchy.video.VideoDir;
import de.dualuse.swt.experiments.scratchy.video.VideoController;
import de.dualuse.swt.experiments.scratchy.view.Timeline;
import de.dualuse.swt.experiments.scratchy.view.VideoCanvas;
import de.dualuse.swt.layout.BorderLayout;
import de.dualuse.swt.util.Sleak;

public class ScratchApp {

//==[ Local Resource Dependencies for Testing Purposes ]============================================
	
	// XXX Lösung ausdenken für diese "resource dependency" (z.B. Sub-Modules? maven build-Scripts? oder?)
	
	static File tripDir = new File("/home/sihlefeld/Documents/footage/trip1");
	static File root = new File(tripDir, "frames1");
	static File rootSD = new File(tripDir, "frames2");

//	static File tripDir = new File("/home/sihlefeld/Documents/footage/trip4");
//	static File rootSD = new File(tripDir, "frames2");
//	static File root = new File(tripDir, "frames1");
	
//	static File tripDir = new File("/home/sihlefeld/Documents/footage/trip3");
//	static File root = new File(tripDir, "frames1");

	// macOS
//	static File tripDir = new File("/Users/ihlefeld/Downloads/Schlangenbader.strip/");
//	static File root = new File(tripDir, "frames");

//==[ App-Main ]====================================================================================
	
	public static void main(String[] args) throws Exception {

		///// Debug SWT Resource Monitoring with Sleak
		
		Display dsp = new Display();

		///// Sleak Debugging
		
		// DeviceData data = new DeviceData();
		// data.tracking = true;
		// Display dsp = new Display(data);
		// Sleak sleak = new Sleak();
		// sleak.open();

		///// Setup Test Data
		
		Video video = new VideoDir(root);
		Video videoSD = new VideoDir(rootSD);
		
		VideoController editor = new VideoController(video, videoSD);
	
		///// Setup UI

		Shell sh = new Shell(dsp, SHELL_TRIM); // | NO_BACKGROUND); // | DOUBLE_BUFFERED);
		// sh.setLayout(new FillLayout());
		sh.setLayout(new BorderLayout());
		sh.setText("MainView");

		// VideoView videoview = new VideoView(sh, NO_BACKGROUND, editor);
		VideoCanvas videoview = new VideoCanvas(sh, NO_BACKGROUND, editor);
		Timeline timeline = new Timeline(sh, SWT.NONE, editor);
		
		videoview.setLayoutData(BorderLayout.CENTER);
		timeline.setLayoutData(BorderLayout.SOUTH);
		
		sh.addListener(SWT.Activate, (e) -> videoview.setFocus() );
		
		sh.setBounds(100, 100, 1200, 800);
		sh.setVisible(true);
		
		///// Event Loop
		
		while(!sh.isDisposed()) try {
			if (!dsp.readAndDispatch())
				dsp.sleep();
		} catch (Exception e) {
			// original.println("Gotcha (" + e.getMessage() + ")");
			e.printStackTrace();
			throw e;
		}
		
		dsp.dispose();
		
		System.out.println("Event Loop stopped");
	}
	
}
