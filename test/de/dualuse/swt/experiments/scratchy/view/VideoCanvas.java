package de.dualuse.swt.experiments.scratchy.view;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import de.dualuse.swt.experiments.scratchy.ResourceManager;
import de.dualuse.swt.experiments.scratchy.cache.ImageCache;
import de.dualuse.swt.experiments.scratchy.video.VideoController;
import de.dualuse.swt.experiments.scratchy.video.VideoController.EditorListener;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;


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

// VideoCanvas
public class VideoCanvas extends LayerCanvas implements EditorListener {
// public class VideoView extends ZoomCanvas implements EditorListener {
	
	Display dsp;
	
	protected VideoController video;
	
	ImageCache cache;
	ImageCache cacheSD;
	
	protected int videoWidth, videoHeight;
	
	ResourceManager<Image> manager;

	protected int currentFrame;
	
//	Animator animator; // XXX same animation code just refactored into its own class somehow slow/laggy?
	
//==[ Constructor ]=================================================================================
	
	public VideoCanvas(Composite parent, int style, VideoController video) {
		super(parent, style | SWT.DOUBLE_BUFFERED); // | NO_BACKGROUND); // | SWT.DOUBLE_BUFFERED);
		
		this.dsp = getDisplay();
		this.video = video;
		
		// addPaintListener(this::paintView);

		addListener(MouseDown, this::down);
		addListener(MouseUp, this::up);
		addListener(MouseMove, this::move);

		addListener(KeyDown, this::keyPressed);
		addListener(KeyUp, this::keyUp);
		
		addListener(Dispose, this::onDispose);
		
		manager = new ResourceManager<Image>(dsp);
		cache = new ImageCache(dsp, video.getVideo(), manager, 64);
		cacheSD = new ImageCache(dsp, video.getVideoSD(), manager);
		
		numFrames = video.getVideo().numFrames();
		fps = video.getVideo().fps();
		videoWidth  = video.getVideo().resolution()[0];
		videoHeight = video.getVideo().resolution()[1];
		
		System.out.println("#frames: " + numFrames);
		System.out.println("fps: " + fps);
		System.out.println("res: " + videoWidth + " x " + videoHeight);
		
		video.addEditorListener(this);
		
		setBackground(dsp.getSystemColor(SWT.COLOR_DARK_GRAY));
		
		
	}
	
	@Override protected void checkSubclass() {}

//==[ Frame Listener ]==============================================================================

	boolean scratching = false;
	
	@Override public void scratchedTo(int from, int to) {
		if (to == displayedFrame) return;
		currentFrame = to;
		scratching = true;
		redraw();
	}

	@Override public void movedTo(int from, int to) {
		if (!scratching && to==displayedFrame) return;
		currentFrame = to;
		scratching = false;
		redraw();
	}
	
//==[ Animation ]===================================================================================
	
	int numFrames;
	double fps;
	
	int startFrame;
	long startTime;
	
	boolean playing = false;
	
	int paintedFrames;

	long fpsStart;
	int fpsIter;
	double fpsAverage;
	
	String fpsString = "";
	
	void start() {
		if (playing) return;
		System.out.println("Starting.");
		
		startTime = System.nanoTime();
		startFrame = video.getPosition();
		
		fpsIter = paintedFrames;
		fpsStart = System.nanoTime();
		fpsAverage = 0;
		
		playing = true;
		
		paintedFrames = 0;
		
		redraw();
	}
	
	void stop() {
		if (!playing) return;
		
		playing = false;
		
		video.moveTo(video.getPosition());
		
		long now = System.nanoTime();
		double elapsed = (now-startTime)/1e9;
		double actualFPS = paintedFrames / elapsed;
		
		System.out.println("Stopping. (average fps: " + actualFPS + ")");
	}
	
	void nextFrame() {
		
		long now = System.nanoTime();
		double elapsed = (now-startTime)/1e9;
		
		int step = (int)(elapsed * fps);

		int nextFrame = (startFrame + step);
		if (nextFrame >= numFrames) {
			nextFrame = nextFrame % numFrames;
			displayedFrame = -1; // XXX requestNearest/jobQueue pruning problems otherwise, as currentFrame<displayedFrame would indicate wrong direction (workaround works, but a bit ugly)
			// just moveTo(0) instead of scratchTo(0) during wraparound
			
			// XXX same problem as with the wraparound when one jumps to a different part (using the timeline) while the animation is active
			//     requestNearest too far off? jumping to another position should trigger moveTo? 
		}
		
		video.scratchTo(nextFrame);
	}

	void animTick() {
		if (!playing) return;
		
		nextFrame();
		// redraw();
		dsp.asyncExec(() -> {
			if (!isDisposed())
				redraw();
		}); // macOS sometimes doesn't redraw; paint request collapsed with current one? try async redraw request
		
		if (paintedFrames % 30 == 0)
			 updateFPS();
	}
	
	// Every x frames update the average fps of this period
	private void updateFPS() {
		 long now = System.nanoTime();
		 
		 double elapsed = (now - fpsStart)/1e9;
		 fpsAverage = (paintedFrames - fpsIter) / elapsed;
		 
		 fpsStart = now;
		 fpsIter = paintedFrames;
		 
		 fpsString = " (" + ((int)(100*fpsAverage))/100.0 + "fps)";
	}

//==[ Controls: Keys ]==============================================================================

	private boolean keyPressed = false;
	private boolean keyRepeat = false;
	
	protected void keyPressed(Event  e) {
		if (keyPressed) keyRepeat = true;
		keyPressed = true;
		
		// Start/stop playback?
		if (e.keyCode == 32) {
			if (!playing) {
				start();
			} else {
				stop();
			}
		}
		
		// Key-Navigation (vim)
		if (e.character == 'h' || e.character == 'l' || e.character == 'k' || e.character == 'j') {
			int movement = 0;
			switch(e.character) {
				case 'h': movement = - 1; break;
				case 'j': movement = +16; break;
				case 'k': movement = -16; break;
				case 'l': movement = + 1; break;
			}
			
			if (keyRepeat)
				video.scratchRelative(movement);
			else
				video.moveRelative(movement);
			
			return;
		}
		
		// Debug Output for Resource Management
		if (e.character == 'c') {
			System.out.println();
			System.out.println("#cache (HD): " + cache.size());
			cache.countDisposed();
			
			System.out.println();
			System.out.println("#cache (SD): " + cacheSD.size());
			cacheSD.countDisposed();
			
			System.out.println();
			System.out.println("Resource Manager: " + manager.size());
			
			return;
		}
		
		// Quit?
		if (e.keyCode == SWT.ESC) {
			getParent().dispose();
		}
	}
	
	protected void keyUp(Event e) {
		if (keyRepeat)
			video.moveTo(video.getPosition());
		
		keyPressed = keyRepeat = false;
	}

//==[ Controls: Mouse ]============================================================================1
	
	private Event l = null;

	protected void down(Event e) {
		if (!e.doit) return;
		if (e.button == 3) startScratch(e);
		l = e;
	}

	protected void up(Event e) {
		if (!e.doit) return;
		if (e.button == 3) stopScratch(e);
		l = e;
	}
	
	protected void move(Event e) {
		if (!e.doit) return;
		if (scratchActive) updateScratch(e);
		l = e;
	}

//==[ Controls: Scratching ]========================================================================
	
	private boolean scratchActive = false;
	
	private void startScratch(Event e) {
		scratchActive = true;
	}
	
	private void stopScratch(Event e) {
		scratchActive = false;
		video.moveTo(video.getPosition());
	}
	
	private void updateScratch(Event e) {
		video.scratchRelative(e.x - l.x);
	}
	
//==[ Paint Code ]==================================================================================
	
	Transform originalTransform = new Transform(getDisplay());
	Transform imageTransform = new Transform(getDisplay());
	
	int displayedFrame = -1;
	Image displayedImage = null;
	boolean displayedHD = false;
	
	// HUD Colors
	Color HUDtextColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	Color HUDfillColor = new Color(Display.getCurrent(), 64, 64, 64, 32);
	
	Layer backgroundLayer = new Layer(this);
	{
		backgroundLayer.addPaintListener(this::paintView);
	}
	
//	@Override protected void renderBackground(Rectangle clip, Transform t, GC gc) {
//		paintView(gc);
//	}
	
	// protected void paintView(GC gc) {
	protected void paintView(PaintEvent e) {
		GC gc = e.gc;
		try {
			
			int currentFrame = video.getPosition();
			
			// Init Graphics settings
			gc.setAntialias(OFF);
			gc.setInterpolation(NONE);
			
			// If animation loop is active, compute the current frame and request the next animation frame to be drawn
			animTick();

			// Has current frame changed since last time a frame was painted? (or has requested resolution changed?)
			if (currentFrame != displayedFrame || displayedHD == scratching)
				requestFrameImage(currentFrame);
			
			// Debug me
			// System.out.println("painting - current:" + currentFrame + ", displayed: " + displayedFrame);
			if (displayedImage!=null && displayedImage.isDisposed())
				System.err.println(System.identityHashCode(displayedImage) + " was disposed");
			
			// Display image (if one is available)
			if (displayedImage != null) { //  && !displayedImage.isDisposed()) {
				
				gc.getTransform(originalTransform);
				gc.getTransform(imageTransform);
				
				updateImageTransform(imageTransform, displayedImage);
				gc.setTransform(imageTransform);
				
				// XXX on macOS it still happens that disposed images survive to this point and cause an exception
				gc.drawImage(displayedImage, 0, 0);

				gc.setTransform(originalTransform);
			}
			
			// Paint HUD (Extra Information)
			paintHUD(gc);

		} catch (Exception ex) {
			System.err.println("Paint exception: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			paintedFrames++;
		}
	}
	
	// Paint HUD/Overlay of extra information on top of frame image
	private void paintHUD(GC gc) {
		String output = String.valueOf(currentFrame+1) + " / " + numFrames;
		
		if (playing && fpsAverage>0)
			output += fpsString;
		
		Point size = gc.textExtent(output);

		gc.setBackground(HUDfillColor);
		gc.setForeground(HUDtextColor);
		gc.setAlpha(128);
		
		gc.fillRectangle(16, 16, size.x, size.y);
		
		gc.drawString(output, 16, 16, true); // XXX flickers on Windows, not on macOS/Linux (does double buffering fix this issue?)
	}
	
	// Compute & Set the Image Transform (differs from the CanvasTransform if an SD preview frame is shown)
	private void updateImageTransform(Transform transform, Image image) {
		ImageData id = image.getImageData();
		Point p = getSize();
		float scale = min(p.x*1f/id.width,p.y*1f/id.height);
		
		float scaledWidth = id.width*scale;
		float scaledHeight = id.height*scale;

		float tx = (getSize().x - scaledWidth)/2;
		float ty = (getSize().y - scaledHeight)/2;
		
		transform.translate(tx, ty);
		transform.scale(scale, scale);
	}
	
//==[ Request Next Frame Image from Image Cache ]===================================================
	
	// If current frame has changed, request frame image from cache
	private void requestFrameImage(int currentFrame) {
		if (scratching) {
			requestSDFrame(currentFrame);
		} else { // use HD frames
			if (!requestHDFrame(currentFrame))
				requestSDFrame(currentFrame); // fallback until HD frame available
		}
	}
	
	// Request SD frames (requests 'closest' frame according to movement direction along timeline, doesn't require an exact match,
	// will trigger another redraw once closer frames become available)
	private void requestSDFrame(int currentFrame) {
		// Request the frame image (or the one closest to it according to the current movment direction)
		Entry<Integer,Image> entry = cacheSD.requestNearest(displayedFrame, currentFrame, (k,v) -> {
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
			
			// Refcounting && update displayedImage/Frame information
			manager.release(displayedImage);
			displayedFrame = nextFrame;
			displayedImage = nextImage;
			displayedHD = false;
		}
	}
	
	private boolean requestHDFrame(int currentFrame) {
		
		// Request image from cache, will be null if not present (but asynchronously triggers redraw once available)
		Image frameImage = cache.request(currentFrame, (k,v) -> {
			if (currentFrame!=k) return; // too late, view has moved on
			if (displayedFrame==k && displayedHD) return; // has already been displayed
			if (!isDisposed())
				redraw();
		});
		
		// cache.request(currentFrame); // request SD frame as well, just to have it cached
		// prevents fallback requestFrameImage to get issued and no callback gets called?

		if (frameImage==null)
			return false;
		
		// Refcounting && update displayedImage/Frame information
		manager.release(displayedImage);
		displayedFrame = currentFrame;
		displayedImage = frameImage;
		displayedHD = true;

		return true;
	}
	
//==[ Disposal ]====================================================================================
	
	protected void onDispose(Event e) {
		cache.dispose();
		cacheSD.dispose();
		
		originalTransform.dispose();
		imageTransform.dispose();
		
		HUDfillColor.dispose();
	}
	
//==[ Debug & Logging ]=============================================================================
	
	void log(String msg) {
		// System.out.println("paint (" + iter + "): " + msg);
	}
	
//==[ Test-Main ]===================================================================================
	
	public static void main_(String[] args) {
		
	}

}
