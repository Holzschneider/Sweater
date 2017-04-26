package de.dualuse.swt.experiments.scratchy.video;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType;
import de.dualuse.swt.experiments.scratchy.view.VideoView;

public class AnnotatedVideoView extends VideoView {

	int canvasWidth, canvasHeight;
	
	Display dsp = getDisplay();
	
	Transform originalTransform = new Transform(dsp);
	Transform canvasTransform = new Transform(dsp);

	Color boundingBoxForeground = dsp.getSystemColor(SWT.COLOR_DARK_CYAN);
	Color boundingBoxBackground = dsp.getSystemColor(SWT.COLOR_CYAN);

	Cursor cursorArrow = dsp.getSystemCursor(SWT.CURSOR_ARROW);
	
	Cursor cursorNW = dsp.getSystemCursor(SWT.CURSOR_SIZENW);
	Cursor cursorNE = dsp.getSystemCursor(SWT.CURSOR_SIZENE);
	Cursor cursorSW = dsp.getSystemCursor(SWT.CURSOR_SIZESW);
	Cursor cursorSE = dsp.getSystemCursor(SWT.CURSOR_SIZESE);
	
	Cursor cursorNS = dsp.getSystemCursor(SWT.CURSOR_SIZENS);
	Cursor cursorWE = dsp.getSystemCursor(SWT.CURSOR_SIZEWE);
	
	Cursor cursorCE = dsp.getSystemCursor(SWT.CURSOR_HAND);
	
	///// Annotations

	List<Annotation> annotations = new ArrayList<Annotation>();

//==[ Constructor ]=================================================================================
	
	public AnnotatedVideoView(Composite parent, int style, VideoEditor editor) {
		super(parent, style, editor);
		
		this.canvasWidth = parent.getSize().x;
		this.canvasHeight = parent.getSize().y;
		
		setCanvasSize(videoWidth, videoHeight);
	}

//==[ Configuration ]===============================================================================
	
	public void setCanvasSize(int width, int height) {
		this.canvasWidth = width;
		this.canvasHeight = height;
	}
	
//==[ Add/Remove Annotations ]======================================================================
	
	public void addAnnotation(Annotation annotation) {
		annotation.added(this);
		annotations.add(0, annotation);
	}
	
	public void removeAnnotation(Annotation annotation) {
		annotations.remove(annotation);
	}

//==[ Controls: Keyboard ]==========================================================================
	
	@Override protected void keyPressed(Event e) {
		super.keyPressed(e);
		
		 if (e.keyCode == SWT.DEL) {
			for (Annotation a : selectedAnnotations)
				annotations.remove(a);
			selectedAnnotations.clear();
			redraw();
		}
	}
	
	@Override protected void keyUp(Event e) {
		super.keyUp(e);
		
	}
	
//==[ Controls: Mouse ]=============================================================================

	@Override protected void down(Event e) {
		super.down(e);
		
		boolean ctrlPressed = (e.stateMask&SWT.CTRL)!=0;
		boolean shiftPressed = (e.stateMask&SWT.SHIFT)!=0;
		
		if (e.button == 1) {
			
			if (!shiftPressed) clearSelection();
			if (hoveredAnnotation!=null) {
				select(hoveredAnnotation);
				startDrag(e);
			} else {
				if (ctrlPressed) startSelection(e);
			}
			
		}
	}

	@Override protected void up(Event e) {
		super.up(e);
		
		if (e.button == 1 && selectionActive) stopSelection(e);
		else if (e.button == 1 && draggingActive) stopDrag(e);
	}
	
	@Override protected void move(Event e) {
		super.move(e);
		
		if (selectionActive) updateSelection(e);
		else if (draggingActive) updateDrag(e);
		else {
			
			point[0] = e.x; point[1] = e.y;
			componentToCanvas(point);
			float x = point[0], y = point[1];
			
			updateHoverState(x, y);
		}
	}

	@Override protected void doubleClick(Event e) {
		super.doubleClick(e);
		
	}
	
//==[ Controls: Selection ]=========================================================================
	
	private boolean selectionActive = false;
	private Point selectFrom = new Point(0,0);
	private Point selectTo = new Point(0,0);
	private Rectangle selectRect = new Rectangle(0,0,0,0);
	
	Color selectionColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	int lineWidth = 2;

	enum SelectionType {
		Selection,
		BoundingBox
	}
	SelectionType type;
	
	Point tmp = new Point(0,0);
	
	protected void startSelection(Event e) {
		if ((e.stateMask&SWT.CTRL) != 0)
			type = SelectionType.BoundingBox;
		else
			type = SelectionType.Selection;
		
		selectionActive = true;
		selectRect.x = selectFrom.x = selectTo.x = e.x;
		selectRect.y = selectFrom.y = selectTo.y = e.y;
		selectRect.width = 0;
		selectRect.height = 0;
	}
	
	protected void stopSelection(Event e) {
		selectionActive = false;
		if (type==SelectionType.BoundingBox) {
			Point p1 = componentToCanvas(selectRect.x, selectRect.y);
			Point p2 = componentToCanvas(selectRect.x + selectRect.width, selectRect.y + selectRect.height);
			
			// addAnnotation(new AnnotationSign(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y));
			
			new AnnotationDoodad(this, selectRect.x, selectRect.y, selectRect.x + selectRect.width, selectRect.y + selectRect.height );
			
		}
		redraw();
	}
	
	protected void updateSelection(Event e) {
		
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
		Rectangle canvasRect = new Rectangle(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y);
		
		// Reset selection and find matching points
		clearSelection();
		
		for (Annotation a : annotations) {
			Rectangle bounds = a.getBounds();
			if (bounds.intersects(canvasRect))
				select(a);
		}
		
		// Draw new selection
		redrawSelection();
	}
	
	void redrawSelection() {
		redraw(
			selectRect.x - lineWidth/2, selectRect.y - lineWidth/2,
			selectRect.width + lineWidth, selectRect.height + lineWidth,
			false
		);
	}

//==[ Hover State ]=================================================================================

	Annotation hoveredAnnotation;
	HoverType hoverType;
	
	void updateHoverState(float x, float y) {
		HoverType hoverInfo = HoverType.NONE;
		HoverType prevType = hoverType;
		
		for (Annotation sign : annotations) {
			if ((hoverInfo = sign.checkHover(x, y)) != HoverType.NONE) {
				hoveredAnnotation = sign;
				hoverType = hoverInfo;
				break;
			}
		}

		if (hoverInfo == HoverType.NONE) {
			hoveredAnnotation = null;
			hoverType = hoverInfo;
		}
		
		if (hoverType != prevType) {
			switch(hoverType) {
				case NONE: setCursor(cursorArrow); break;
				case N: case S: setCursor(cursorNS); break;
				case E: case W: setCursor(cursorWE); break;
				case NE: setCursor(cursorNE); break;
				case NW: setCursor(cursorNW); break;
				case SE: setCursor(cursorSE); break;
				case SW: setCursor(cursorSW); break;
				case C: setCursor(cursorCE); break;
			}
		}
		
	}
	
//==[ Controls: Drag ]==============================================================================
	
	Annotation draggedAnnotation;
	
	Point startMouse = new Point(0,0);
	Point startPoint = new Point(0,0);
	
	boolean draggingActive;
	
	private void startDrag(Event e) {
		draggingActive = true;
		draggedAnnotation = hoveredAnnotation;
		Point p = componentToCanvas(e.x, e.y);
		startMouse.x = p.x;
		startMouse.y = p.y;
		
		draggedAnnotation.startDrag(p.x, p.y, hoverType);
	}
	
	private void stopDrag(Event e) {
		draggingActive = false;
		draggedAnnotation = null;
	}
	
	private void updateDrag(Event e) {
		// Erase previous marker
		redraw(draggedAnnotation);
		
		// Compute new position
		Point p = componentToCanvas(e.x, e.y);
		int dx = p.x - startMouse.x;
		int dy = p.y - startMouse.y;
		
		draggedAnnotation.updateDrag(dx, dy, hoverType);
		
		redraw(draggedAnnotation);
	}
	
//==[ Manage Selection ]============================================================================
		
	Set<Annotation> selectedAnnotations = new HashSet<Annotation>();
	
	public void select(Annotation a) {
		if (selectedAnnotations.contains(a))
			return;
		
		selectedAnnotations.add(a);
		a.setSelected(true);
		
		redraw(a);
	}
	
	public void deselect(Annotation a) {
		if (!selectedAnnotations.contains(a))
			return;
		
		selectedAnnotations.remove(a);
		a.setSelected(false);
		
		redraw(a);
	}
	
	public void clearSelection() {
		if (selectedAnnotations.isEmpty())
			return;
		
		for (Annotation a : selectedAnnotations) {
			a.setSelected(false);
			redraw(a);
		}
		
		selectedAnnotations.clear();
	}
	
//==[ Paint Canvas ]================================================================================

	@Override protected void renderBackground(Rectangle clip, Transform t, GC gc) {
		super.renderBackground(clip,  t, gc);
		paintAnnotations(clip, gc);
	}
	
//	@Override protected void paintView(PaintEvent e) {
//		super.paintView(e);
//		paintAnnotations(e);
//	}
	
//	protected void paintAnnotations(PaintEvent e) {
	protected void paintAnnotations(Rectangle e, GC gc) {
//		GC gc = e.gc;
		
		Point p1 = componentToCanvas(e.x, e.y);
		Point p2 = componentToCanvas(e.x + e.width, e.y + e.height);

		int x1 = p1.x, x2 = p2.x;
		int y1 = p1.y, y2 = p2.y;
		
		Rectangle clip = new Rectangle(e.x, e.y, e.width, e.height);
		
		gc.getTransform(originalTransform);
		gc.getTransform(canvasTransform);
		updateCanvasTransform(canvasTransform);
		gc.setTransform(canvasTransform);
		
		// gc.getClipping(); // slows down rendering
		
		for (Annotation annotation : annotations) {
			if (!annotation.isVisible())
				continue;
			
			Rectangle bounds = annotation.getBounds();
			if (bounds.x > x2 || (bounds.x+bounds.width) < x1)
				continue;
			if (bounds.y > y2 || (bounds.y+bounds.height) < y1)
				continue;
			
			annotation.render(clip, canvasTransform, gc);
		}

		gc.setTransform(originalTransform);
		
		paintSelection(gc);
	}

	// Paint Selection
	private void paintSelection(GC gc) {
		if (!selectionActive) return;
		
		if (type == SelectionType.Selection) {
			gc.setForeground(selectionColor);
			gc.setLineWidth(lineWidth);
			gc.setLineStyle(SWT.LINE_DASH);
			gc.drawRectangle(selectRect);
		} else if (type == SelectionType.BoundingBox) {
			gc.setForeground(boundingBoxForeground);
			gc.setBackground(boundingBoxBackground);
			gc.fillRectangle(selectRect);
			gc.drawRectangle(selectRect);
		}
	}

//==[ Redraw Canvas Space ]=========================================================================

	float[] rect = new float[4];
	
	public void redraw(Annotation annotation) {
		Path shape = annotation.getShape();
		shape.getBounds(rect);
		redrawCanvas(rect[0], rect[1], rect[2]+lineWidth, rect[3]+lineWidth);
	}

	protected void redrawCanvas(float x, float y, float w, float h) {
		rect[0] = x; rect[1] = y; rect[2] = x+w; rect[3] = y+h;
		canvasToComponent(rect);
		redraw(
			(int)rect[0],
			(int)rect[1],
			(int)(rect[2]-rect[0] + 1),
			(int)(rect[3]-rect[1] + 1),
			false
		);
	}
	
//==[ Update Canvas Transform ]=====================================================================
	
	Transform invertedCanvasTransform = new Transform(getDisplay());
	
	// Compute & Set the Canvas Transform (canvas coordinates -> video coordinates)
	private void updateCanvasTransform(Transform transform) {
		Point p = getSize();
		float scale = min(p.x*1f/canvasWidth,p.y*1f/canvasHeight);
		
		float scaledWidth = canvasWidth*scale;
		float scaledHeight = canvasHeight*scale;

		float tx = (getSize().x - scaledWidth)/2;
		float ty = (getSize().y - scaledHeight)/2;
		
		transform.translate(tx, ty);
		transform.scale(scale, scale);
		
		transform.getElements(elements);
		
		invertedCanvasTransform.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
		invertedCanvasTransform.invert();
	}
	
//==[ Coordinate Transformations ]==================================================================

	float[] elements = new float[6];
	float[] point = new float[2];
	
	///// Component -> Canvas
	
	public Point componentToCanvas(int x, int y) {
		return arrayToPoint(componentToCanvas(coordsToArray(x,y)));
	}
	
	public Point componentToCanvas(Point p) {
		return arrayToPoint(componentToCanvas(pointToArray(p)));
	}
	
	public float[] componentToCanvas(float[] p) {
		invertedCanvasTransform.transform(p);
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

//==[ Dispose Annotations ]=========================================================================
	
	@Override protected void onDispose(Event e) {
		super.onDispose(e);
		
		for (Annotation annotation : annotations)
			annotation.dispose();
		
		originalTransform.dispose();
		canvasTransform.dispose();
		invertedCanvasTransform.dispose();
	}
	
}
