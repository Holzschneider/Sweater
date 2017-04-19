package de.dualuse.swt.experiments.scratchy.view;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.io.File;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import de.dualuse.swt.experiments.scratchy.cache.CacheImages;
import de.dualuse.swt.experiments.scratchy.util.SimpleReader;
import de.dualuse.swt.experiments.scratchy.view.Timeline;
import de.dualuse.swt.layout.BorderLayout;


/**
 * 
 * Anforderungen
 * 
 * funktioniert
 *  - fucking fast load / rendering
 *  - nice video scratch-experience 
 * 
 * funktioniert noch nicht:
 * - resource-recycling (nur 100 Images oderso, dann disposen (siehe LinkedHashMap mit removeEldestEntry)
 * - resource disposal (z.B. internes Handling von Images, und async zulieferung von ImageData!)
 * - async display vom Closest loaded + nachträgliches Liefern vom neuesten geladenen
 * (wannimmer repainted wird, soll ein Frame, das nah genug am "ziel-Frame" ist gerendert werden können, sofern 
 * das Zielframe noch nicht fertig geladen wurde. Sobald das Zielframe da ist noch mal ein Repaint, ohne ruckler!)
 *  - HD/SD Frame Supply
 *  (ggf Lösung vom Problem: Mouse-Up triggered HD Frame Load + Repaint, Mouse-Drag operates sloppy on SD Frames)
 *  - achtung! mit einem missmatch von "Current Frame" zu "displayed frame" sollte man umgehen 
 *  (overlay objekte sollten ihre Position am Displayed-Frame anlehnen statt am Current frame!)  
 * 
 * @author holzschneider
 *
 */

public abstract class MainView extends Canvas {
	
	Display dsp;
	
	File tripDir = new File("/home/sihlefeld/Documents/footage/trip1");
	File root = new File(tripDir, "frames2");
	
//	File tripDir = new File("/home/sihlefeld/Documents/footage/trip3");
//	File root = new File(tripDir, "frames1");
	
	CacheImages cache;
	
//==[ Constructor ]=================================================================================
	
	public MainView(Composite parent, int style) {
		super(parent, style); // | NO_BACKGROUND); // | SWT.DOUBLE_BUFFERED);
		
		dsp = getDisplay();
		
		super.addPaintListener(this::paintControl);
		
		super.addListener(MouseDown, this::down);
		super.addListener(MouseUp, this::up);
		super.addListener(MouseMove, this::move);

		super.addListener(SWT.KeyDown, this::keyPressed);
		
		cache = new CacheImages(dsp, root);
		totalFrames = cache.frames();
	}
	
	@Override protected void checkSubclass() {}
	
//	@Override public void drawBackground (GC gc, int x, int y, int width, int height) {
//		System.out.println("drawing background1");
//		super.drawBackground(gc, x, y, width, height);
//	}
//	
//	@Override public void drawBackground (GC gc, int x, int y, int width, int height, int offsetX, int offsetY) {
//		System.out.println("drawing background2");
//		super.drawBackground(gc, x, y, width, height, offsetX, offsetY);
//	}

	
//==[ Animation ]===================================================================================
	
	double fps = SimpleReader.loadDouble(new File(tripDir, "fps.txt"), 29.97); // 29.97; // 59.94;
	{ System.out.println("fps: " + fps); }
	
	int startFrame;
	long startTime;
	
	boolean playing = false;
	
	int paintedFrames;

	long fpsStart;
	int fpsIter;
	double fpsAverage;
	
	void start() {
		System.out.println("Starting.");
		
		startTime = System.nanoTime();
		startFrame = currentFrame;
		
		fpsIter = iter;
		fpsStart = System.nanoTime();
		fpsAverage = 0;
		
		playing = true;
		
		paintedFrames = 0;
		
		redraw();
	}
	
	void stop() {
		System.out.println("Stopping.");
		
		// startFrame = displayedFrame+1;
		playing = false;
		
		redraw();
		
		long now = System.nanoTime();
		double elapsed = (now-startTime)/1e9;
		double actualFPS = paintedFrames / elapsed;
		
		System.out.println("fps: " + actualFPS);
	}
	
	int counter=0;
	void nextFrame() {
		long now = System.nanoTime();
		double elapsed = (now-startTime)/1e9;
		
		int step = (int)(elapsed * fps);
		// step = 2*(step/2); // XXX only use every second frame in automatic playback? // could be dependend on framerate
		// XXX could be used for scrolling as well for larger frame distances (when the user isn't trying to select a specific frame with small frame deltas)
		
		int nextFrame = (startFrame + step);
		if (nextFrame >= cache.frames()) {
			nextFrame = nextFrame % cache.frames();
			displayedFrame = -1; // XXX requestNearest/jobQueue pruning problems otherwise, as currentFrame<displayedFrame would indicate wrong direction (workaround works, but a bit ugly)
		}
		
		gotoFrame(nextFrame);
		
		counter++;
	}
	
//==[ Controls ]====================================================================================
	
	private boolean pressed = false;
	private Event l = null;
	
	private void down(Event e) {
		if (e.button != 3) return;
		pressed = true;
		l = e;
	}

	private void up(Event e) {
		if (e.button != 3) return;
		pressed = false;
	}
	
	private void move(Event e) {
		if (pressed) {
			
			int newFrame = currentFrame + (e.x - l.x);
			gotoFrame(newFrame);
			
			l = e;
		}
	}
	
	private void keyPressed(Event  e) {
		if (e.keyCode == 32) {
			if (!playing) {
				start();
			} else {
				stop();
			}
		}
		
		if (e.character == 'h') {
			moveFrames(-1);
		} else if (e.character == 'l') {
			moveFrames(+1);
		} else if (e.character == 'j') {
			moveFrames(+16);
		} else if (e.character == 'k') {
			moveFrames(-16);
		} else if (e.character == 'c') {
			cache.countDisposed();
		}
		
		if (e.keyCode == 27) {
			getParent().dispose();
		}
	}
	
//==[ Frame Movement ]==============================================================================

	int currentFrame;
	int totalFrames;
	
	public void moveFrames(int step) {
		gotoFrame(currentFrame + step);
	}
	
	public void gotoFrame(int nextFrame) {
		int lastFrame = currentFrame;
		if (lastFrame == nextFrame) return;
		
		int last = currentFrame;
		int next = max(0, min(cache.frames()-1, nextFrame));
		
		currentFrame = next;
		
		fireFrameChanged(last, next);
		
		redraw();
	}
	
	public int numFrames() {
		return cache.frames();
	}
	
//==[ Paint Code ]==================================================================================
	
	Transform originalTransform = new Transform(getDisplay());
	Transform t = new Transform(getDisplay());
	
	int displayedFrame = -1;
	Image displayedImage = null;
	
	int iter = 0;
	
	protected void paintControl(PaintEvent e) {
		try {
			
			// Init Graphics settings
			e.gc.setAntialias(OFF);
			e.gc.setInterpolation(NONE);
			
			// If animation loop is active, compute the current frame and request the next animation frame to be drawn
			if (playing) {
				nextFrame();
				// redraw();
				dsp.asyncExec(() -> {
					if (!isDisposed())
						redraw();
				}); // macOS sometimes doesn't redraw; paint request collapsed with current one? try async redraw request
			}
			
			// Request the frame image (or the one closest to it according to the current movment direction)
			Entry<Integer,Image> entry = cache.requestNearest(displayedFrame, currentFrame, (k,v) -> {
				if (k > displayedFrame && k <= currentFrame || // forward direction (implicit: displayedFrame <= currentFrame)
					k < displayedFrame && k >= currentFrame) // backward direction (implicit: currentFrame <= displayedFrame)
					if (!isDisposed())
						redraw();
			});
			
			// Ref counting for used image (prevent saved Image to be released by the resourcemanager if it gets thrown out of the cache)
			// (release held image reference to the previous frame that isn't used anymore)
			if (entry != null) {
	
				int nextFrame = entry.getKey();
				Image nextImage = entry.getValue();
				
				if (displayedImage != nextImage) {
					int from = System.identityHashCode(displayedImage);
					int to = System.identityHashCode(nextImage);
					
					log("release : " + from);
					cache.getManager().release(displayedImage);
					
					log("paint - register: " + to);
					cache.getManager().register(nextImage);
					
					log("\n");
				}
				
				displayedFrame = nextFrame;
				displayedImage = nextImage;
				
			}
			
			// Debug me
			System.out.println("painting - current:" + currentFrame + ", displayed: " + displayedFrame);
			if (displayedImage!=null && displayedImage.isDisposed())
				System.err.println(System.identityHashCode(displayedImage) + " was disposed");
			
			// Display image (if one is available)
			if (displayedImage != null && !displayedImage.isDisposed()) {
				
				ImageData id = displayedImage.getImageData();
				
				Point p = getSize();
				float scale = max(p.x*1f/id.width,p.y*1f/id.height);
				
				e.gc.getTransform(originalTransform);
				e.gc.getTransform(t);
				t.scale(scale, scale);
				e.gc.setTransform(t);
				
				// XXX on macOS it still happens that disposed images survive to this point and cause an exception
				e.gc.drawImage(displayedImage, 0, 0);
				
				e.gc.setTransform(originalTransform);
			}
			
			if (playing) {
				 if (iter % 30 == 0) {
					 long now = System.nanoTime();
					 
					 double elapsed = (now - fpsStart)/1e9;
					 fpsAverage = (iter - fpsIter) / elapsed;
					 
					 fpsStart = now;
					 fpsIter = iter;
					 
					 fpsString = " (" + ((int)(100*fpsAverage))/100.0 + "fps)";
				 }
			}
			
			paintHUD(e.gc);
		
			paintedFrames++;
			
		} catch (Exception ex) {
			System.err.println("Paint exception: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			iter++;
		}
	}

	Color textColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	Color fillColor = new Color(Display.getCurrent(), 64, 64, 64, 32);
	String fpsString = "";
	
	private void paintHUD(GC gc) {
		String output = String.valueOf(currentFrame+1) + " / " + totalFrames;
		
		if (playing && fpsAverage>0)
			output += fpsString;
		
		Point size = gc.textExtent(output);

		gc.setBackground(fillColor);
		gc.setForeground(textColor);
		gc.setAlpha(128);
		
		gc.fillRectangle(16, 16, size.x, size.y);
		
		gc.drawString(output, 16, 16, true); // XXX flickers on Windows
	}
	
	{
		this.addListener(SWT.Dispose, (e) -> {
			fillColor.dispose();
		});
	}
	
//==[ Frame Listener ]==============================================================================

	interface FrameListener {
		void currentFrame(int last, int current);
		// void displayedFrame(int frame);
	}
	
	CopyOnWriteArrayList<FrameListener> listeners = new CopyOnWriteArrayList<FrameListener>();
	
	public void addFrameListener(FrameListener listener) {
		listeners.add(listener);
	}
	
	public void removeFrameListener(FrameListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireFrameChanged(int lastFrame, int currentFrame) {
		for (FrameListener listener : listeners)
			listener.currentFrame(lastFrame, currentFrame);
	}
	
//==[ Debug & Logging ]=============================================================================
	
	void log(String msg) {
		// System.out.println("paint (" + iter + "): " + msg);
	}
	
//==[ Test-Main ]===================================================================================
	
	public static void main(String[] args) throws Exception {
		
		PrintStream original = System.out;
		
//		PrintStream printer = new PrintStream(new File("/home/sihlefeld/log.txt"));
//		System.setOut(printer);
//		System.setErr(printer);
		
		///// Window Setup
		
		Display dsp = new Display();
		Shell sh = new Shell(dsp, SHELL_TRIM); // | NO_BACKGROUND); // | DOUBLE_BUFFERED);
		// sh.setLayout(new FillLayout());
		sh.setLayout(new BorderLayout());

		MainView scratcher = new MainView(sh, NO_BACKGROUND ) {

			// XXX Lösung ausdenken für diese "resource dependency" (z.B. Sub-Modules? maven build-Scripts? oder?)
			// File root = new File("/home/sihlefeld/Documents/footage/trip1/frames2");
			// root = new File("/Users/ihlefeld/Downloads/Schlangenbader.strip/frames");
			// root = new File("/Users/holzschneider/Archive/Geenee/Geenee Strips/bigbangtheory-clip1.mov.strip");
			
			// CacheFirstTest cache = new CacheFirstTest(dsp, root);
			
			{
				addDisposeListener((e) -> {
					cache.dispose();
					t.dispose();
					originalTransform.dispose();
				});
			}
			
			/*
			int paintControlCounter = 0;
			@Override
			protected void paintControl(PaintEvent e) {
				// sh.setText("Paint: "+paintControlCounter+++" Load: "+loadCounter+" Cache: "+cacheCounter);
				
				super.paintControl(e);
//				if (specialRedraw)
//					new Throwable().printStackTrace();
//				specialRedraw=false;
				
			}
			*/
			
//			@Override int frames() {
//				return cache.frames();
//			}
//			
//			@Override Entry<Integer, Image> frame(int frameNumber) {
//				return cache.frame(frameNumber);
//			}
			
		};
		
		Timeline timeline = new Timeline(sh, SWT.NONE, scratcher, scratcher.numFrames());
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
			original.println("Gotcha (" + e.getMessage() + ")");
			e.printStackTrace();
			throw e;
		}
		
		dsp.dispose();
		
		System.out.println("Event Loop stopped");
	}
	
}
