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
import de.dualuse.swt.experiments.scratchy.video.VideoEditor;
import de.dualuse.swt.experiments.scratchy.view.Timeline;
import de.dualuse.swt.experiments.scratchy.view.VideoView;
import de.dualuse.swt.layout.BorderLayout;
import de.dualuse.swt.util.Sleak;

public class ScratchApp {

	public static void main(String[] args) throws Exception {
		
		// XXX Lösung ausdenken für diese "resource dependency" (z.B. Sub-Modules? maven build-Scripts? oder?)
//		File tripDir = new File("/home/sihlefeld/Documents/footage/trip1");
//		File root = new File(tripDir, "frames2");
//		File rootHD = new File(tripDir, "frames1");

		File tripDir = new File("/home/sihlefeld/Documents/footage/trip4");
		File root = new File(tripDir, "frames2");
		File rootHD = new File(tripDir, "frames1");
		
//		File tripDir = new File("/home/sihlefeld/Documents/footage/trip3");
//		File root = new File(tripDir, "frames1");

		// macOS
//		File tripDir = new File("/Users/ihlefeld/Downloads/Schlangenbader.strip/");
//		File root = new File(tripDir, "frames");

		Video video = new VideoDir(root);
		Video videoHD = new VideoDir(rootHD);
		
		VideoEditor editor = new VideoEditor(video, videoHD);
		
		///// Window Setup
		
		DeviceData data = new DeviceData();
		data.tracking = true;
		
		Display dsp = new Display(data);
		
		Sleak sleak = new Sleak();
		sleak.open();
		
		//Display dsp = new Display();
		Shell sh = new Shell(dsp, SHELL_TRIM); // | NO_BACKGROUND); // | DOUBLE_BUFFERED);
		// sh.setLayout(new FillLayout());
		sh.setLayout(new BorderLayout());
		sh.setText("MainView");

		VideoView scratcher = new VideoView(sh, NO_BACKGROUND, editor);
		
		Timeline timeline = new Timeline(sh, SWT.NONE, scratcher, video.numFrames());
		scratcher.addFrameListener(timeline);
		
		scratcher.setLayoutData(BorderLayout.CENTER);
		timeline.setLayoutData(BorderLayout.SOUTH);
		
		
		sh.addListener(SWT.Activate, (e) -> {
			System.out.println("Focus: " + scratcher.setFocus());	
		});
		
		sh.setBounds(100, 100, 1200, 800);
		sh.setVisible(true);
		
		sh.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				scratcher.dispose();
			}
		});
		
//		Thread concurrentExperiment = new Thread(() -> {
//			Experiment3ConcurrentImageConstruction.run(dsp);
//		});
//		concurrentExperiment.start();
		
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
