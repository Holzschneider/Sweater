package de.dualuse.swt.experiments.scratchy.video;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class Animator {
	
	Display dsp;
	Composite canvas;
	VideoController editor;

	double fps;
	int numFrames;
		
	int startFrame;
	long startTime;
	
	boolean playing = false;

//	int paintedFrames;
//
//	long fpsStart;
//	int fpsIter;
//	double fpsAverage;
	
//==[ Constructor ]=================================================================================
	
	public Animator(Composite canvas, VideoController editor) {
		this.dsp = canvas.getDisplay();
		this.canvas = canvas;
		this.editor = editor;
		
		fps = editor.getVideo().fps();
		numFrames = editor.getVideo().numFrames();
	}
	
//==[ Start / Stop Playback ]=======================================================================
	
	public void start() {
		if (playing) return;
		
		System.out.println("Starting.");
		
		startTime = System.nanoTime();
		startFrame = editor.getPosition();
		
//		fpsIter = iter;
//		fpsStart = System.nanoTime();
//		fpsAverage = 0;
		
		playing = true;
		
//		paintedFrames = 0;
		
		canvas.redraw();
	}
	
	public void stop() {
		if (!playing) return;
		
		System.out.println("Stopping.");
		
		playing = false;
		
		editor.moveTo(editor.getPosition());
		
//		redraw();
				
		long now = System.nanoTime();
		double elapsed = (now-startTime)/1e9;
//		double actualFPS = paintedFrames / elapsed;
		
//		System.out.println("fps: " + actualFPS);
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
//==[ Compute Next Frame ]==========================================================================
	
	void nextFrame() {
		long now = System.nanoTime();
		double elapsed = (now-startTime)/1e9;
		
		int step = (int)(elapsed * fps);
		// step = 2*(step/2); // XXX only use every second frame in automatic playback? // could be dependend on framerate
		// could be used for scrolling as well for larger frame distances (when the user isn't trying to select a specific frame with small frame deltas)
		
		int nextFrame = (startFrame + step);
		if (nextFrame >= numFrames) {
			nextFrame = nextFrame % numFrames;
//			displayedFrame = -1; // XXX requestNearest/jobQueue pruning problems otherwise, as currentFrame<displayedFrame would indicate wrong direction (workaround works, but a bit ugly)
			// XXX wraparound: just use moveTo instead of scratchTo for the first frame
		}
		
		editor.scratchTo(nextFrame);
	}
	
	public void tick() {
		if (!playing) return;
		nextFrame();
		// redraw();
		
		dsp.asyncExec(() -> {
			if (!canvas.isDisposed())
				canvas.redraw();
		}); // macOS sometimes doesn't redraw; paint request collapsed with current one? try async redraw request
		
//		if (iter % 30 == 0)
//			 updateFPS();
	}
}
