package de.dualuse.swt.experiments.scratchy.view;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import de.dualuse.swt.experiments.scratchy.ResourceManager;
import de.dualuse.swt.experiments.scratchy.cache.ImageCache;
import de.dualuse.swt.experiments.scratchy.video.VideoEditor;
import de.dualuse.swt.experiments.scratchy.video.VideoEditor.EditorListener;
import de.dualuse.swt.widgets.ZoomCanvas;


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

public class VideoView extends Canvas implements EditorListener {
// public class VideoView extends ZoomCanvas implements EditorListener {
	
	Display dsp;
	
	VideoEditor editor;
	
	ImageCache cache;
	ImageCache cacheHD;
	ResourceManager<Image> manager;
	
	///// Annotations
	
	class Annotation {
		public int x, y;
		public Annotation(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public Annotation(Point p) {
			this.x = p.x;
			this.y = p.y;
		}
	}
	
	List<Annotation> controlPoints = new ArrayList<Annotation>();
	
//	Animator animator; // XXX same animation code just refactored into its own class somehow slow/laggy?
	
//==[ Constructor ]=================================================================================
	
	public VideoView(Composite parent, int style, VideoEditor editor) {
		super(parent, style | SWT.DOUBLE_BUFFERED); // | NO_BACKGROUND); // | SWT.DOUBLE_BUFFERED);
		
		this.dsp = getDisplay();
		this.editor = editor;
		
		addPaintListener(this::paintView);

		addListener(MouseDoubleClick, this::doubleClick);
		addListener(MouseDown, this::down);
		addListener(MouseUp, this::up);
		addListener(MouseMove, this::move);

		addListener(KeyDown, this::keyPressed);
		addListener(KeyUp, this::keyUp);
		
		addListener(Dispose, this::disposeResources);
		
		manager = new ResourceManager<Image>(dsp);
		cache = new ImageCache(dsp, editor.getVideo(), manager);
		cacheHD = new ImageCache(dsp, editor.getVideoHD(), manager, 64);
		
		numFrames = editor.getVideo().numFrames();
		fps = editor.getVideo().fps();
		
		System.out.println("#frames: " + numFrames);
		System.out.println("fps: " + fps);
		
		editor.addEditorListener(this);
		
		setBackground(dsp.getSystemColor(SWT.COLOR_DARK_GRAY));
		
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

//==[ Frame Listener ]==============================================================================

	int currentFrame;
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
		startFrame = editor.getPosition();
		
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
		
		editor.moveTo(editor.getPosition());
		
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
		
		editor.scratchTo(nextFrame);
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
	
	private void keyPressed(Event  e) {
		if (keyPressed) keyRepeat = true;
		keyPressed = true;
		
		if (e.keyCode == 32) {
			if (!playing) {
				start();
			} else {
				stop();
			}
		}
		
		if (e.character == 'h' || e.character == 'l' || e.character == 'k' || e.character == 'j') {
			int movement = 0;
			switch(e.character) {
				case 'h': movement = - 1; break;
				case 'j': movement = +16; break;
				case 'k': movement = -16; break;
				case 'l': movement = + 1; break;
			}
			
			if (keyRepeat)
				editor.scratchRelative(movement);
			else
				editor.moveRelative(movement);
			
			return;
		}
		
		// Debug Output for Resource Management
		if (e.character == 'c') {
			System.out.println();
			System.out.println("#cache (SD): " + cache.size());
			cache.countDisposed();
			
			System.out.println();
			System.out.println("#cache (HD): " + cacheHD.size());
			cacheHD.countDisposed();
			
			System.out.println();
			System.out.println("Resource Manager: " + manager.size());
			
			return;
		}
		
		if (e.keyCode == SWT.ESC) {
			getParent().dispose();
		} else if (e.keyCode == SWT.DEL) {
			for (Annotation a : selectedAnnotations)
				controlPoints.remove(a);
			selectedAnnotations.clear();
			redraw();
		}
	}
	
	private void keyUp(Event e) {
		if (keyRepeat)
			editor.moveTo(editor.getPosition());
		
		keyPressed = keyRepeat = false;
	}
	
//==[ Controls: Mouse ]============================================================================1
	
	private Event l = null;
	
	private void down(Event e) {
		if (e.button == 3) {
			startScratch(e);
		} else if (e.button == 1) {
			clearSelection();
			if (hoverActive) {
				select(hoveredAnnotation);
				startDrag(e);
			} else {
				startSelection(e);
			}
		}
		
		l = e;
	}

	private void up(Event e) {
		if (e.button == 3) stopScratch(e);
		else if (e.button == 1 && selectionActive) stopSelection(e);
		else if (e.button == 1 && draggingActive) stopDrag(e);
		
		l = e;
	}
	
	private void move(Event e) {
		if (scratchActive) updateScratch(e);
		else if (selectionActive) updateSelection(e);
		else if (draggingActive) updateDrag(e);
		else detectHover(e);
		
		l = e;
	}

	private void doubleClick(Event e) {
		if (e.button == 1) {
			Point pOnCanvas = componentToCanvas(e.x, e.y);
			controlPoints.add(new Annotation(pOnCanvas));
		}
	}
	
//==[ Controls: Scratching ]========================================================================
	
	private boolean scratchActive = false;
	
	private void startScratch(Event e) {
		scratchActive = true;
	}
	
	private void stopScratch(Event e) {
		scratchActive = false;
		editor.moveTo(editor.getPosition());
	}
	
	private void updateScratch(Event e) {
		editor.scratchRelative(e.x - l.x);
	}
	
//==[ Controls: Selection ]=========================================================================
	
	private boolean selectionActive = false;
	private Point selectFrom = new Point(0,0);
	private Point selectTo = new Point(0,0);
	private Rectangle selectRect = new Rectangle(0,0,0,0);
	
	Color selectionColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	int lineWidth = 2;
	
	private void startSelection(Event e) {
		selectionActive = true;
		selectRect.x = selectFrom.x = selectTo.x = e.x;
		selectRect.y = selectFrom.y = selectTo.y = e.y;
		selectRect.width = 0;
		selectRect.height = 0;
	}
	
	private void stopSelection(Event e) {
		selectionActive = false;
		redraw();
	}
	
	private void updateSelection(Event e) {
		
		// Clear old selection
		redrawSelection();

		// Update selection rectangle
		selectTo.x = e.x;
		selectTo.y = e.y;
		
		// Reorder corners (so that (x1,y1) is the upper left corner of the selection and (x2,y2) the lower left
		int x1 = Math.min(selectFrom.x, selectTo.x);
		int x2 = Math.max(selectFrom.x, selectTo.x);
		int y1 = Math.min(selectFrom.y, selectTo.y);
		int y2 = Math.max(selectFrom.y, selectTo.y);
		
		selectRect.x = x1;
		selectRect.y = y1;
		selectRect.width  = (x2 - x1);
		selectRect.height = (y2 - y1);

		// Convert to canvas coordinates for hit detection
		Point p1 = componentToCanvas(x1, y1);
		Point p2 = componentToCanvas(x2, y2);
		
		// Reset selection and find matching points
		clearSelection();
		for (Annotation a : controlPoints) {
			if ((a.x >= p1.x && a.x <= p2.x) &&
				(a.y >= p1.y && a.y <= p2.y)) {
				
				select(a);
			}
		}
		System.out.println("Selected: " + selectedAnnotations.size());
		
		// Draw new selection
		redrawSelection();
	}
	
	private void redrawSelection() {
		redraw(
			selectRect.x - lineWidth/2, selectRect.y - lineWidth/2,
			selectRect.width + lineWidth, selectRect.height + lineWidth,
			false
		);
	}
	
//==[ Controls: Drag ]==============================================================================
	
	private boolean hoverActive;
	private Annotation hoveredAnnotation;
	
	private Annotation draggedAnnotation;
	
	private Point startMouse = new Point(0,0);
	private Point startPoint = new Point(0,0);
	
	private boolean draggingActive;
	
	private void detectHover(Event e) {
		boolean hit = false;
		
		Point p = componentToCanvas(e.x, e.y);
		for (Annotation a : controlPoints) {
			int dx = p.x - a.x;
			int dy = p.y - a.y;
			int sqDist = dx*dx + dy*dy;
			if (sqDist <= 25) {
				hit = true;
				hoveredAnnotation = a;
				break;
			}
		}
		
		if (hit && !hoverActive)
			setCursor(dsp.getSystemCursor(SWT.CURSOR_HAND));
		else if (!hit && hoverActive)
			setCursor(dsp.getSystemCursor(SWT.CURSOR_ARROW));
		
		hoverActive = hit;
	}
	
	private void startDrag(Event e) {
		draggingActive = true;
		draggedAnnotation = hoveredAnnotation;
		Point p = componentToCanvas(e.x, e.y);
		startMouse.x = p.x;
		startMouse.y = p.y;
		startPoint.x = draggedAnnotation.x;
		startPoint.y = draggedAnnotation.y;
	}
	
	private void stopDrag(Event e) {
		draggingActive = false;
		draggedAnnotation = null;
	}
	
	private void updateDrag(Event e) {
		// Erase previous marker
		redrawAnnotation(draggedAnnotation);
		
		// Compute new position
		Point p = componentToCanvas(e.x, e.y);
		int dx = p.x - startMouse.x;
		int dy = p.y - startMouse.y;
		draggedAnnotation.x = startPoint.x + dx;
		draggedAnnotation.y = startPoint.y + dy;
		
		// Draw marker at new position
		redrawAnnotation(draggedAnnotation);
	}
	
	private void redrawAnnotation(Annotation a) {
		// Convert canvas coordinates to component coordinates
		float[] coords = canvasToComponent(new float[] {
			a.x - annotationSize,
			a.y - annotationSize,
			a.x + annotationSize,
			a.y + annotationSize
		});

		// Redraw the corresponding component region
		int x = (int) coords[0];
		int y = (int) coords[1];
		int w = (int) Math.ceil(coords[2] - coords[0]);
		int h = (int) Math.ceil(coords[3] - coords[1]);
		redraw(x, y, w, h, false);
	}
	
//==[ Manage Selection ]============================================================================
		
	Set<Annotation> selectedAnnotations = new HashSet<Annotation>();
	
	public void select(Annotation a) {
		if (selectedAnnotations.contains(a))
			return;
		
		selectedAnnotations.add(a);
		
		redrawAnnotation(a);
	}
	
	public void deselect(Annotation a) {
		if (!selectedAnnotations.contains(a))
			return;
		
		selectedAnnotations.remove(a);
		
		redrawAnnotation(a);
	}
	
	public void clearSelection() {
		if (selectedAnnotations.isEmpty())
			return;
		
		for (Annotation a : selectedAnnotations)
			redrawAnnotation(a);
		
		selectedAnnotations.clear();
	}
	
//==[ Paint Code ]==================================================================================
	
	Transform originalTransform = new Transform(getDisplay());
	Transform canvasTransform = new Transform(getDisplay());
	
	int displayedFrame = -1;
	Image displayedImage = null;
	boolean displayedHD = false;
	
	// HUD Colors
	Color HUDtextColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	Color HUDfillColor = new Color(Display.getCurrent(), 64, 64, 64, 32);
	
	Color annotationColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	Color annotationColorDark = new Color(Display.getCurrent(), 64, 64, 64);
	
	Color selectedColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	Color selectedColorDark = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);

	int annotationSize = 21;
	
	protected void paintView(PaintEvent e) {
		try {
			
			int currentFrame = editor.getPosition();
			
			// Init Graphics settings
			e.gc.setAntialias(OFF);
			e.gc.setInterpolation(NONE);
			
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
				
				ImageData id = displayedImage.getImageData();
				
				Point p = getSize();
				float scale = min(p.x*1f/id.width,p.y*1f/id.height);
				
				float scaledWidth = id.width*scale;
				float scaledHeight = id.height*scale;

				float tx = (getSize().x - scaledWidth)/2;
				float ty = (getSize().y - scaledHeight)/2;
				
				e.gc.getTransform(originalTransform);
				e.gc.getTransform(canvasTransform);
				
				canvasTransform.translate(tx, ty);
				canvasTransform.scale(scale, scale);
								
				e.gc.setTransform(canvasTransform);
				
				// XXX on macOS it still happens that disposed images survive to this point and cause an exception
				e.gc.drawImage(displayedImage, 0, 0);

				// Paint Annotations
				paintAnnotations(e.gc);
				// XXX must be canvasTransform independent (0..1, 0..1?). If resolution dependent, then switch between HD and SD
				//	   frames would mess up coordinates
				
				e.gc.setTransform(originalTransform);
			}
			
			// Paint HUD (Extra Information)
			paintHUD(e.gc);
			
			// Paint Selection
			paintSelection(e.gc);

		} catch (Exception ex) {
			System.err.println("Paint exception: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			paintedFrames++;
		}
	}

	// Paint Annotations
	private void paintAnnotations(GC gc) {

		gc.setAntialias(ON);
	
		for (Annotation annotation : controlPoints) {

			float qs = annotationSize/4.0f;
			float hs = annotationSize/2.0f;
			
			boolean selected = selectedAnnotations.contains(annotation);
			
			if (selected) gc.setAlpha(255); else gc.setAlpha(192);
			
			gc.setBackground(annotationColorDark);
			gc.setBackground(selected ? selectedColorDark : annotationColorDark);
			Path path = new Path(dsp);
			path.addArc(annotation.x - hs, annotation.y - hs, 2*hs, 2*hs, 0, 360);
			gc.fillPath(path);
			path.dispose();
			
			gc.setBackground(selected ? selectedColor : annotationColor);
			path = new Path(dsp);
			path.addArc(annotation.x - qs, annotation.y - qs, 2*qs, 2*qs, 0, 360);
			gc.fillPath(path);
			path.dispose();
			
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
	
	// Paint Selection
	private void paintSelection(GC gc) {
		if (!selectionActive) return;
		
		gc.setForeground(selectionColor);
//		gc.setLineWidth(lineWidth);
		gc.setLineStyle(SWT.LINE_DASH);
		gc.drawRectangle(selectRect);
	}
	
//==[ Coordinate Transformations ]==================================================================
	
	///// Component -> Canvas
	
	public Point componentToCanvas(int x, int y) {
		return arrayToPoint(componentToCanvas(coordsToArray(x,y)));
	}
	
	public Point componentToCanvas(Point p) {
		return arrayToPoint(componentToCanvas(pointToArray(p)));
	}
	
	public float[] componentToCanvas(float[] p) {
		float[] elements = new float[6];
		canvasTransform.getElements(elements);
		Transform temp = new Transform(getDisplay(), elements);
		temp.invert();
		temp.transform(p);
		return p;
	}
	
	///// Canvas -> Component

	public Point canvasToComponent(int x, int y) {
		return arrayToPoint(canvasToComponent(coordsToArray(x, y)));
	}
	
	public Point canvasToComponent(Point p) {
		return arrayToPoint(canvasToComponent(pointToArray(p)));
	}
	
	public float[] canvasToComponent(float[] p) {
		canvasTransform.transform(p);
		return p;
	}

	///// Helper methods
	
	private static float[] coordsToArray(int x, int y) {
		return new float[] { x, y };
	}
	
	private static float[] pointToArray(Point p) {
		return new float[] { p.x, p.y };
	}
	
	private static Point arrayToPoint(float[] array) {
		if (array.length != 2) throw new IllegalArgumentException("Illegal dimension for point conversion.");
		return new Point(Math.round(array[0]), Math.round(array[1]));
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
			
			// Refcounting && update displayedImage/Frame information
			cache.getManager().release(displayedImage);
			displayedFrame = nextFrame;
			displayedImage = nextImage;
			displayedHD = false;
		}
	}
	
	private boolean requestHDFrame(int currentFrame) {
		
		// Request image from cache, will be null if not present (but asynchronously triggers redraw once available)
		Image frameImage = cacheHD.request(currentFrame, (k,v) -> {
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
		cache.getManager().release(displayedImage);
		displayedFrame = currentFrame;
		displayedImage = frameImage;
		displayedHD = true;

		return true;
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
	
//==[ Disposal ]====================================================================================
	
	private void disposeResources(Event e) {
		cache.dispose();
		cacheHD.dispose();
		
		canvasTransform.dispose();
		originalTransform.dispose();
		
		HUDfillColor.dispose();
		annotationColorDark.dispose();
	}
	
//==[ Debug & Logging ]=============================================================================
	
	void log(String msg) {
		// System.out.println("paint (" + iter + "): " + msg);
	}
	
//==[ Test-Main ]===================================================================================
	
	public static void main_(String[] args) {
		
	}

}
