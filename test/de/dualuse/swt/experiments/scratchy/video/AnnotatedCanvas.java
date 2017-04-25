package de.dualuse.swt.experiments.scratchy.video;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public class AnnotatedCanvas extends Canvas {

	int canvasWidth, canvasHeight;
	
	Transform originalTransform = new Transform(getDisplay());
	Transform canvasTransform = new Transform(getDisplay());

	Color boundingBoxForeground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
	Color boundingBoxBackground = Display.getCurrent().getSystemColor(SWT.COLOR_CYAN);

	///// Annotations

	List<Annotation> annotations = new ArrayList<Annotation>();

//==[ Constructor ]=================================================================================
	
	public AnnotatedCanvas(Composite parent, int style) {
		super(parent, style);
		
		this.canvasWidth = parent.getSize().x;
		this.canvasHeight = parent.getSize().y;
		
		this.addListener(SWT.Dispose, this::onDispose);
//		this.addListener(SWT.Paint, this::onPaint);
		
		this.addListener(SWT.MouseUp, this::onMouseUp);
		this.addListener(SWT.MouseDown, this::onMouseDown);
		this.addListener(SWT.MouseMove, this::onMouseMove);
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

//==[ Controls: Mouse ]============================================================================1
	
	private Event l = null;
	
	protected void down(Event e) {
		rect[0] = e.x; rect[1] = e.y;
		componentToCanvas(rect);
		float x = rect[0], y = rect[1];
		
		boolean shiftPressed = (e.stateMask&SWT.SHIFT  ) != 0;
		boolean ctrlPressed =  (e.stateMask&SWT.CONTROL) != 0;
		
		if (e.button == 1) {
			
			if (hoveredAnnotation!=null) {
				hoveredAnnotation.onMouse(x, y, e);
				return;
			}
			
//			clearSelection();
//			if (hoverActive) {
//				select(hoveredAnnotation);
//				startDrag(e);
//			} else {
//				createSign = ctrlPressed;
//				startSelection(e);
//			}
			startSelection(e);
		}
		
		l = e;
	}

	protected void up(Event e) {
		
		if (e.button == 1 && selectionActive) stopSelection(e);
//		else if (e.button == 1 && draggingActive) stopDrag(e);
		
		l = e;
	}
	
	Annotation hoveredAnnotation;
	protected void move(Event e) {
		rect[0] = e.x; rect[1] = e.y;
		componentToCanvas(rect);
		float x = rect[0], y = rect[1];
		
		if (selectionActive) updateSelection(e);
//		else if (draggingActive) updateDrag(e);
//		else detectHover(e);
		else { // check hover
			boolean hit = false;
			for (Annotation sign : annotations) {
				
				if (hit = sign.checkHover(x, y)) {
					hoveredAnnotation = sign;
					break;
				}
			}
			if (!hit) {
				setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
				hoveredAnnotation = null;
			}
		}
		
		l = e;
	}

	protected void doubleClick(Event e) {
		if (e.button == 1) {
			Point pOnCanvas = componentToCanvas(e.x, e.y);
			// controlPoints.add(new Annotation(pOnCanvas));
		}
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
	
	boolean createSign = true;
	protected void stopSelection(Event e) {
		selectionActive = false;
		if (createSign) {
			System.out.println("Adding Sign (" + selectRect.x + ", " + selectRect.y + ", " + selectRect.width + ", " + selectRect.height +")");
		
			Point p1 = componentToCanvas(selectRect.x, selectRect.y);
			Point p2 = componentToCanvas(selectRect.x + selectRect.width, selectRect.y + selectRect.height);
			
			// signs.add(new SignAnnotation(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y));
			
			if (type==SelectionType.BoundingBox)
				addAnnotation(new AnnotationSign(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y));
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

		System.out.println("Updating Selection (" + selectRect + ")");
		
		// Convert to canvas coordinates for hit detection
		Point p1 = componentToCanvas(x1, y1);
		Point p2 = componentToCanvas(x2, y2);
		
		// Reset selection and find matching points
		clearSelection();
		
//		for (Annotation a : controlPoints) {
//			if ((a.x >= p1.x && a.x <= p2.x) &&
//				(a.y >= p1.y && a.y <= p2.y)) {
//				
//				select(a);
//			}
//		}
//		System.out.println("Selected: " + selectedAnnotations.size());
		
		// Draw new selection
		redrawSelection();
	}
	
	protected void redrawSelection() {
		redraw(
			selectRect.x - lineWidth/2, selectRect.y - lineWidth/2,
			selectRect.width + lineWidth, selectRect.height + lineWidth,
			false
		);
	}
	
	float[] rect = new float[4];
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

//==[ Controls: Drag ]==============================================================================

	/*
	boolean createSign;
	enum Side {
		TOP, LEFT, BOTTOM, RIGHT,
		TOPLEFT, TOPRIGHT, BOTTOMLEFT, BOTTOMRIGHT,
		CENTER
	}; // CENTER -> move sign around ~ control points only have center
	// XXX allow corners to be dragged
	
	SignAnnotation hoveredSign;
	Side hoveredSide;
	
	/////
	
	private boolean hoverActive;
	private Annotation hoveredAnnotation;
	
	private Annotation draggedAnnotation;
	
	private Point startMouse = new Point(0,0);
	private Point startPoint = new Point(0,0);
	
	private boolean draggingActive;
	
	private void detectHover(Event e) {
		boolean hit = false;
		
		// Hover over control point?
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
		if (hit) return;
		
		/////
		
		// Hover over sign?
		int r = 3;
		for (SignAnnotation a : signs) {
			// Upper boundary?
			int x1 = a.x-r, x2 = a.x+a.width+r;
			int y1 = a.y-r, y2 = a.y+r;
			boolean hitTop = p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2;
			
			// Lower boundary?
			y1 = a.y+a.height-r; y2 = y1+2*r;
			boolean hitBottom = p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2;
			
			// Left boundary?
			x1 = a.x-r; x2 = x1 + 2*r;
			y1 = a.y-r; y2 = a.y+a.height+r;
			boolean hitLeft = p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2;
			
			// Right boundary?
			x1 = a.x+a.width-r; x2 = x1 + 2*r;
			boolean hitRight = p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2;
				
			if (hitTop && hitLeft) {
				setCursor(dsp.getSystemCursor(SWT.CURSOR_SIZENW));
				hoveredSign = a;
				hoveredSide = Side.TOPLEFT;
				hoverActive = true;
				return;
			} else if (hitTop && hitRight) {
				setCursor(dsp.getSystemCursor(SWT.CURSOR_SIZENE));
				hoveredSign = a;
				hoveredSide = Side.TOPRIGHT;
				hoverActive = true;
				return;
			} else if (hitBottom && hitLeft) {
				setCursor(dsp.getSystemCursor(SWT.CURSOR_SIZESW));
				hoveredSign = a;
				hoveredSide = Side.BOTTOMLEFT;
				hoverActive = true;
				return;
			} else if (hitBottom && hitRight) {
				setCursor(dsp.getSystemCursor(SWT.CURSOR_SIZESE));
				hoveredSign = a;
				hoveredSide = Side.BOTTOMRIGHT;
				hoverActive = true;
				return;
			}
			
			if (hitTop) {
				setCursor(dsp.getSystemCursor(SWT.CURSOR_SIZENS));
				hoveredSign = a;
				hoveredSide = Side.TOP;
				hoverActive = true;
				return;
			}
			
			
			if (hitBottom) {
				setCursor(dsp.getSystemCursor(SWT.CURSOR_SIZENS));
				hoveredSign = a;
				hoveredSide = Side.BOTTOM;
				hoverActive = true;
				return;
			}
			
			if (hitLeft) {
				setCursor(dsp.getSystemCursor(SWT.CURSOR_SIZEWE));
				hoveredSign = a;
				hoveredSide = Side.LEFT;
				hoverActive = true;
				return;
			}
			
			if (hitRight) {
				setCursor(dsp.getSystemCursor(SWT.CURSOR_SIZEWE));
				hoveredSign = a;
				hoveredSide = Side.RIGHT;
				hoverActive = true;
				return;
			}
			
			// Center?
			x1 = a.x + r; x2 = a.x + a.width - r;
			y1 = a.y + r; y2 = a.y + a.height - r;
			
			if (p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2) {
				setCursor(dsp.getSystemCursor(SWT.CURSOR_HAND));
				hoveredSign = a;
				hoveredSide = Side.CENTER;
				hoverActive = true;
				return;
			}
		}
		
		setCursor(dsp.getSystemCursor(SWT.CURSOR_ARROW));
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
		
		System.out.println("annotation(" + draggedAnnotation.x + ", " + draggedAnnotation.y + ")");
		
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
	*/
	
//==[ Manage Selection ]============================================================================
		
	Set<Annotation> selectedAnnotations = new HashSet<Annotation>();
	
	public void select(Annotation a) {
		if (selectedAnnotations.contains(a))
			return;
		
		selectedAnnotations.add(a);
		
//		redrawAnnotation(a);
	}
	
	public void deselect(Annotation a) {
		if (!selectedAnnotations.contains(a))
			return;
		
		selectedAnnotations.remove(a);
		
//		redrawAnnotation(a);
	}
	
	public void clearSelection() {
		if (selectedAnnotations.isEmpty())
			return;
		
//		for (Annotation a : selectedAnnotations)
//			redrawAnnotation(a);
		
		selectedAnnotations.clear();
	}
	
//==[ Paint Canvas ]================================================================================

	protected void paintAnnotations(PaintEvent e) {
		
		GC gc = e.gc;
		
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

	float[] bounds = new float[4];
	public void redraw(Annotation annotation) {
		
		Path shape = annotation.getShape();
		shape.getBounds(bounds);
		redraw(
			(int)bounds[0],
			(int)bounds[1],
			(int)Math.ceil(bounds[0] + bounds[2]),
			(int)Math.ceil(bounds[1] + bounds[3]),
			false
		);

	}

	// Paint Selection
	private void paintSelection(GC gc) {
		System.out.println("paintSelection (" + selectionActive + ")");
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
	
	///// Component -> Canvas
	
	public Point componentToCanvas(int x, int y) {
		return arrayToPoint(componentToCanvas(coordsToArray(x,y)));
	}
	
	public Point componentToCanvas(Point p) {
		return arrayToPoint(componentToCanvas(pointToArray(p)));
	}
	
	float[] elements = new float[6];
	public float[] componentToCanvas(float[] p) {
//		canvasTransform.getElements(elements);
//		Transform temp = new Transform(getDisplay(), elements);
//		temp.invert();
//		temp.transform(p);
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

//==[ Mouse Event Handling ]========================================================================
	
	protected void onMouseUp(Event e) {
		fireMouseEvent(e);
	}
	
	protected void onMouseDown(Event e) {
		fireMouseEvent(e);
	}
	
	protected void onMouseMove(Event e) {
		fireMouseEvent(e);
	}
	
	protected void fireMouseEvent(Event e) {
//		for (Annotation annotation : annotations) {
//			annotation.onMouse(e.x, e.y, e);
//			if (!e.doit) break;
//		}
	}

//==[ Dispose Annotations ]=========================================================================
	
	protected void onDispose(Event e) {
		for (Annotation annotation : annotations)
			annotation.dispose();
		
		originalTransform.dispose();
		canvasTransform.dispose();
		invertedCanvasTransform.dispose();
	}
	
}
