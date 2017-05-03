package de.dualuse.swt.experiments.scratchy.video;

import static java.lang.Math.min;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import de.dualuse.swt.experiments.scratchy.view.VideoCanvas;

public class AnnotatedVideoCanvas extends VideoCanvas {

	int canvasWidth, canvasHeight;
	
	Display dsp = getDisplay();
	
	Transform originalTransform = new Transform(dsp);
	// Transform canvasTransform = new Transform(dsp);

	Color boundingBoxForeground = dsp.getSystemColor(SWT.COLOR_DARK_CYAN);
	Color boundingBoxBackground = dsp.getSystemColor(SWT.COLOR_CYAN);
	
	///// Annotations

//	List<AnnotationLayer> annotations = new ArrayList<AnnotationLayer>();
	Set<Annotation_<Rectangle2D>> selectedAnnotations = new HashSet<>();
	
	/////
	
	Annotations<Rectangle2D> annotations = new Annotations<>();
	
	Map<Annotation_<Rectangle2D>,AnnotationLayer> layers = new HashMap<>();
	
//==[ Constructor ]=================================================================================
	
	public AnnotatedVideoCanvas(Composite parent, int style, VideoEditor editor) {
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
	
//	public void addAnnotation(AnnotationLayer annotation) {
//		annotations.add(annotation);
//	}
	
	public void removeAnnotation(AnnotationLayer layer) {
		Annotation_<Rectangle2D> annotation = layer.getAnnotation();
		annotations.removeAnnotation(annotation);
		layers.remove(layer);
		layer.dispose();
		
//		annotations.remove(annotation);
//		selectedAnnotations.remove(annotation);
//		annotation.dispose();
	}

//==[ Controls: Keyboard ]==========================================================================
	
	@Override protected void keyPressed(Event e) {
		super.keyPressed(e);
		
		if (e.keyCode == SWT.DEL) {
			
//			for (AnnotationLayer a : new ArrayList<>(selectedAnnotations))
//				removeAnnotation(a);
			
		} else if (e.keyCode == 'a') {
			
			System.out.println("Selected: " + selectedAnnotations.size());
			
			for (Annotation_<Rectangle2D> annotation : selectedAnnotations) {
				
				annotation.setKey(currentFrame, annotation.getKey(annotation.floorKey(currentFrame)));
				
			}
			
			updateLayers();
		}
		 
	}
	
	@Override protected void keyUp(Event e) {
		super.keyUp(e);
		
	}
	
//==[ Controls: Mouse ]=============================================================================

	@Override protected void down(Event e) {
		super.down(e);
		if (!e.doit) return;
		
		boolean shiftPressed = (e.stateMask&SWT.SHIFT)!=0;
		
		if (e.button == 1) {
			
			if (!shiftPressed) clearSelection();
			startSelection(e);
			
		}
	}

	@Override protected void up(Event e) {
		super.up(e);
		if (!e.doit) return;
		
		if (e.button == 1 && selectionActive) stopSelection(e);
	}
	
	@Override protected void move(Event e) {
		super.move(e);
		if (!e.doit) return;
		
		if (selectionActive) updateSelection(e);
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
		LayerCreation
	}
	
	SelectionType selectionType;
	
	Point tmp = new Point(0,0);
	
	protected void startSelection(Event e) {
		if ((e.stateMask&SWT.CTRL) != 0)
			selectionType = SelectionType.LayerCreation;
		else
			selectionType = SelectionType.Selection;
		
		selectionActive = true;
		selectRect.x = selectFrom.x = selectTo.x = e.x;
		selectRect.y = selectFrom.y = selectTo.y = e.y;
		selectRect.width = 0;
		selectRect.height = 0;
	}
	
	protected void stopSelection(Event e) {
		selectionActive = false;
		if (selectionType==SelectionType.LayerCreation) {
			
			Point p1 = componentToCanvas(selectRect.x, selectRect.y);
			Point p2 = componentToCanvas(selectRect.x + selectRect.width, selectRect.y + selectRect.height);
			
//			Rectangle rect = new Rectangle(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y);
			Rectangle2D rect = new Rectangle2D.Double(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y);
			Annotation_<Rectangle2D> annotation = new AnnotationBounds(currentFrame, rect);
			
			// annotations.add(new AnnotationLayer(this, p1.x, p1.y, p2.x, p2.y ));
			annotations.addAnnotation(annotation);
			
			updateLayers();
			
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

		// Draw new selection
		redrawSelection();
		
		if (selectionType != SelectionType.Selection)
			return;
		
		// Convert to canvas coordinates for hit detection
		Rectangle2D.Float canvasRect = new Rectangle2D.Float(x1, y1, x2-x1, y2-y1);
		transformComponentToCanvas(canvasRect);
		
		// Reset selection and find matching points
		clearSelection();
		
		// Select AnnotationLayers that intersect selection rectangle
		for (AnnotationLayer layer : layers.values()) {
			
			float left=layer.getLeft(), top=layer.getTop(), right=layer.getRight(), bottom=layer.getBottom();
			Rectangle2D layerRect = new Rectangle2D.Float(left, top, right-left, bottom-top);
			
			boolean hit = layerRect.intersects(canvasRect);
			if (hit) select(layer);
				
		}
		
	}
	
	void redrawSelection() {
		redraw(
			selectRect.x - lineWidth/2, selectRect.y - lineWidth/2,
			selectRect.width + lineWidth, selectRect.height + lineWidth,
			false
		);
	}
	
//==[ Manage Selection ]============================================================================
	
	public void select(AnnotationLayer a) {
		a.setSelected(true);
		selectedAnnotations.add(a.getAnnotation());
	}

	public void deselect(AnnotationLayer a) {
		a.setSelected(false);
		selectedAnnotations.remove(a.getAnnotation());
	}
	
	public void selectExclusive(AnnotationLayer a) {
		clearSelection();
		select(a);
	}
	
	public void toggleSelection(AnnotationLayer a) {
		if (!a.isSelected())
			select(a);
		else
			deselect(a);
	}
	
	public void clearSelection() {
//		for (AnnotationLayer a : selectedAnnotations)
//			a.setSelected(false);
		selectedAnnotations.clear();
	}
	
//==[ Paint Canvas ]================================================================================

	int lastCurrentFrame;
	Set<Annotation_<Rectangle2D>> collector = new HashSet<>();
	
	@Override protected void renderBackground(Rectangle clip, Transform t, GC gc) {
		if (lastCurrentFrame != currentFrame) {
			lastCurrentFrame = currentFrame;
			updateLayers();
		}
		
		super.renderBackground(clip,  t, gc);
		paintSelection(gc);
	}
	
	// Paint Selection
	private void paintSelection(GC gc) {
		if (!selectionActive) return;
		
		if (selectionType == SelectionType.Selection) {
			gc.setForeground(selectionColor);
			gc.setLineWidth(lineWidth);
			gc.setLineStyle(SWT.LINE_DASH);
			gc.drawRectangle(selectRect);
		} else if (selectionType == SelectionType.LayerCreation) {
			gc.setForeground(boundingBoxForeground);
			gc.setBackground(boundingBoxBackground);
			gc.fillRectangle(selectRect);
			gc.drawRectangle(selectRect);
		}
	}

	private void updateLayers() {
		
		collector.clear();
		annotations.fetchAnnotations(currentFrame, collector);
		
		for (Annotation_<Rectangle2D> annotation : collector) {
			
			Rectangle2D bounds = annotation.getValue(currentFrame);
			double left = bounds.getMinX();
			double right = bounds.getMaxX();
			double top = bounds.getMinY();
			double bottom = bounds.getMaxY();
			
			AnnotationLayer layer = layers.get(annotation);
			if (layer==null) {
				AnnotationLayer newLayer = new AnnotationLayer(this, annotation, currentFrame) {
					@Override public void onResize() {
						Rectangle2D rectangle = new Rectangle2D.Double(getLeft(), getTop(), getRight()-getLeft(), getBottom()-getTop());
						annotation.setKey(currentFrame, rectangle);
					}
				};
				
				layers.put(annotation, newLayer);
			} else {
				layer.setBounds(left, top, right, bottom);
			}
		}
		
		// if associated annotation is not in 'annotations', dispose and remove layer
		for (Iterator<Entry<Annotation_<Rectangle2D>,AnnotationLayer>> it = layers.entrySet().iterator(); it.hasNext();) {
			
			Entry<Annotation_<Rectangle2D>,AnnotationLayer> entry = it.next();
			AnnotationLayer layer = entry.getValue();
			Annotation_<Rectangle2D> annotation = layer.getAnnotation();
			
			if (!collector.contains(annotation)) {
				
				layer.dispose();
				selectedAnnotations.remove(layer);
				
				it.remove();
			}
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
	
	// XXX now uses canvasTrasnform from parent LayerCanvas; works, but should be looked at in more detail
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

	// XXX probably should be migrated/merged to LayerCanvas along with the whole canvasTransform handling
	
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
	
	/////

	void transformComponentToCanvas(Point2D.Float p) {
		point[0] = p.x; point[1] = p.y;
		componentToCanvas(point);
		p.x = point[0]; p.y = point[1];
	}
	
	float[] coords = new float[4];
	void transformComponentToCanvas(Rectangle2D.Float rect) {
		coords[0] = rect.x; coords[1] = rect.y;
		coords[2] = rect.x + rect.width; coords[3] = rect.y + rect.height;
		componentToCanvas(coords);
		rect.x = coords[0]; rect.y = coords[1];
		rect.width = coords[2] - coords[0]; rect.height = coords[3] - coords[1];
	}
	
//==[ Dispose Annotations ]=========================================================================
	
	@Override protected void onDispose(Event e) {
		super.onDispose(e);
		
//		for (Annotation annotation : annotations)
//			annotation.dispose();
		
		originalTransform.dispose();
		canvasTransform.dispose();
		invertedCanvasTransform.dispose();
	}
	
}
