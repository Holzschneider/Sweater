package de.dualuse.swt.experiments.scratchy;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import de.dualuse.swt.util.SWTTimer;


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

public abstract class ImageSequenceScratcherTest extends Canvas {
	
	Display dsp;
	
	File root = new File("/home/sihlefeld/Documents/footage/trip1/frames1");
	CacheImages cache;
	
//==[ Constructor ]=================================================================================
	
	public ImageSequenceScratcherTest(Composite parent, int style) {
		super(parent, style); // | NO_BACKGROUND); // | SWT.DOUBLE_BUFFERED);
		
		dsp = getDisplay();
		
		super.addPaintListener(this::paintControl);
		super.addListener(MouseDown, this::down);
		super.addListener(MouseUp, this::up);
		super.addListener(MouseMove, this::move);

		super.addListener(SWT.KeyDown, this::keyPressed);
		
		cache = new CacheImages(dsp, root);
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
	
	int startFrame;
	long start;
	double fps = 59.94;
	boolean playing = false;
	
	int paintedFrames;
	
	void start() {
		System.out.println("Starting.");
		start = System.nanoTime();
		playing = true;
		paintedFrames = 0;
		redraw();
	}
	
	void stop() {
		System.out.println("Stopping.");
		startFrame = displayedFrame+1;
		playing = false;
		redraw();
		
		long now = System.nanoTime();
		double elapsed = (now-start)/1e9;
		double actualFPS = paintedFrames / elapsed;
		
		System.out.println("fps: " + actualFPS);
	}
	
	int counter=0;
	void nextFrame() {
		long now = System.nanoTime();
		double elapsed = (now-start)/1e9;
		
		currentFrame = (startFrame + (int)(elapsed * fps));
		if (currentFrame >= cache.frames()) {
			currentFrame = currentFrame % cache.frames();
			displayedFrame = -1; // XXX direction bug during wraparound?
		}
		counter++;
		if (counter%60==0)
			System.out.println("current: " + currentFrame);
	}
	
//==[ Controls ]====================================================================================
	
	private boolean pressed = false;
	private Event l = null;
	
	private void down(Event e) {
		pressed = true;
		l = e;
	}

	private void up(Event e) {
		pressed = false;
	}
	
	private void move(Event e) {
		if (pressed) {
			int last = currentFrame, next = currentFrame+(e.x-l.x);
			
			if (last!=next)
				this.redraw();

			currentFrame = max(0,min(cache.frames()-1,next));
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
		}
		
		if (e.keyCode == 27) {
			getParent().dispose();
		}
	}
	
	private void moveFrames(int step) {
		currentFrame = (currentFrame + step) % cache.frames();
		if (currentFrame < 0) currentFrame += cache.frames();
		redraw();
	}

//==[ Frames ]======================================================================================
	
	int currentFrame;
	
//	public int getCurrentFrame() {
//		return frame;
//	}
//
//	/////
//	
//	abstract int frames();
//	abstract Entry<Integer,Image> frame(int frameNumber);
//	
//==[ Paint Code ]==================================================================================
	
	Transform t = new Transform(getDisplay());
	
	int displayedFrame = -1;
	Image displayedImage = null;
	
//	Map<Integer,Image> images = new HashMap<Integer,Image>();
	
	protected void paintControl(PaintEvent e) {
		try {
		if (playing) {
			nextFrame();
			redraw();
		}
		
		e.gc.setAntialias(OFF);
		e.gc.setInterpolation(NONE);
		
		// Entry<Integer,Image> entry = frame(frame);
		Entry<Integer,Image> entry = cache.requestNearest(displayedFrame, currentFrame, (k,v) -> {
			if (k > displayedFrame && k <= currentFrame || // forward direction (implicit: displayedFrame <= currentFrame)
				k < displayedFrame && k >= currentFrame) // backward direction (implicit: currentFrame <= displayedFrame)
				redraw();
		});
		
		if (entry != null) {

			int nextFrame = entry.getKey();
			Image nextImage = entry.getValue();
			
			if (displayedImage != nextImage) {
				cache.getManager().release(displayedImage);
				cache.getManager().register(nextImage);
			}
			
			displayedFrame = nextFrame;
			displayedImage = nextImage;
			
//			ImageData imgData = entry.getValue();
//			
//			if (!images.containsKey(entry.getKey())) {
//					images.put(entry.getKey(), new Image(dsp, entry.getValue()));
//					System.out.println("image cache: #" + images.size());
//			}  
//			
//			lastFrame = entry.getKey();
//			lastImage = images.get(lastFrame);
			
		}
		
		if (displayedImage != null) {
			
			ImageData id = displayedImage.getImageData();
			
			Point p = getSize();
			float scale = max(p.x*1f/id.width,p.y*1f/id.height);
			
			e.gc.getTransform(t);
			t.scale(scale, scale);
			e.gc.setTransform(t);
			
//			Image tempImage = new Image(dsp, id);
//			e.gc.drawImage(tempImage, 0, 0);
//			tempImage.dispose();
			
			 e.gc.drawImage(displayedImage, 0, 0);
			
			// e.gc.drawString("" + lastFrame, 16, 16);
			
		}
	
		paintedFrames++;
		
		} catch (Exception ex) {
			System.err.println("Paint exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
//==[ Test-Main ]===================================================================================
	
	public static void main(String[] args) {

		///// Window Setup
		
		Display dsp = new Display();
		Shell sh = new Shell(dsp, SHELL_TRIM ); // | NO_BACKGROUND); // | DOUBLE_BUFFERED);
		sh.setLayout(new FillLayout());

		ImageSequenceScratcherTest scratcher = new ImageSequenceScratcherTest(sh, NO_BACKGROUND ) {

			// XXX Lösung ausdenken für diese "resource dependency" (z.B. Sub-Modules? maven build-Scripts? oder?)
			// File root = new File("/home/sihlefeld/Documents/footage/trip1/frames2");
			// root = new File("/Users/ihlefeld/Downloads/Schlangenbader.strip/frames");
			// root = new File("/Users/holzschneider/Archive/Geenee/Geenee Strips/bigbangtheory-clip1.mov.strip");
			
			// CacheFirstTest cache = new CacheFirstTest(dsp, root);
			
			{
				addDisposeListener((e) -> cache.dispose() );
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
		
		while(!sh.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
		
		System.out.println("Event Loop stopped");
	}
	
}
