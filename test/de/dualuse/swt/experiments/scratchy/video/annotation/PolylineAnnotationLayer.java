package de.dualuse.swt.experiments.scratchy.video.annotation;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;

import de.dualuse.swt.experiments.Contour;
import de.dualuse.swt.experiments.Contour.Edge;
import de.dualuse.swt.experiments.Contour.Vertex;
import de.dualuse.swt.graphics.PathShape;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerContainer;
import de.dualuse.vecmath.Matrix3d;

// XXX deadend... better use Layer transforms if they support 8 element homographies
//				  or inverse transform Mouse Event coordinates and forward transform shapes only during rendering 

public class PolylineAnnotationLayer extends Layer {

	AnnotationContour annotation;
	int frame;
	
//==[ Constructor ]=================================================================================
	
	public PolylineAnnotationLayer(LayerContainer parent, AnnotationContour annotation, int frame) {
		super(parent);
		this.annotation = annotation;
		this.frame = frame;
		setFrame(frame);
	}

//==[ Set Frame ]===================================================================================

	Contour contour;
	Contour ghost;
	
	public void setFrame(int frame) {
		
		this.frame = frame;
		
		Matrix3d transform = annotation.getKey(frame);
		if (transform != null) {
			
			contour = annotation.getContour();
			ghost = null;
			
		} else {
			
			Integer key = annotation.floorKey(frame);
			if (key==null) key = annotation.ceilingKey(frame);

			contour = null;
			ghost = annotation.getContour(key);
			
		}
		
		redraw();
		
	}
	
//==[ Input Handling ]==============================================================================
	
	Edge hoverEdge;
	Vertex hoverVertex;
	Vertex draggedVertex;
	
	boolean editMode = true;
	
	@Override protected boolean onMouseDown(float x, float y, int button, int modKeysAndButtons) {
		
		if (button==1) {
			if (!contour.isClosed()) {
				
				// Extend contour
				if (hoverVertex!=null && hoverVertex==contour.get(0)) {
					contour.close();
					editMode = false;
				} else {
					contour.add(x, y);
				}
				redraw();
				
			} else if (hoverVertex!=null) {
				
				// drag vertex
				draggedVertex = hoverVertex;
			
//				XXX no count available currently
//				if (e.count==1)
//					draggedVertex = hoverVertex;
//				else if (e.count==2) {
//					contour.remove(hoverVertex);
//					hoverVertex = null;
//					redraw();
//				}
				
			} else if (hoverEdge!=null) {
				
				// insert new vertex
				Vertex vertex = contour.insert(hoverEdge, x, y);
				hoverVertex = vertex;
				draggedVertex = vertex;
				hoverEdge = null;
				redraw();
				
			}
			
		} else if (button==3) {
			
			if (hoverVertex!=null) {
				contour.remove(hoverVertex);
				hoverVertex = null;
				redraw();
			} else {
				if (editMode) {
					contour.removeLast();
					redraw();
				} else {
					System.out.println("Not editing, returning false");
					return false;
				}
			}
			
		}
		
		
		return true;
	}
	
	@Override protected boolean onMouseMove(float x, float y, int modKeysAndButtons) {
		Contour c = contour;
		if (c==null) c = ghost;
		if (c==null) return false;
		
		// Drag vertex
		if (draggedVertex!=null) {
			draggedVertex.setLocation(x, y);
			redraw();
			return true;
		}

		// Detect hover vertex
		Point2D p = new Point2D.Double(x, y);
		
		double minDist = Double.MAX_VALUE;
		Vertex chosenVertex = null;

		for (int i=0, I=c.numVertices(); i<I; i++) {
			Vertex v = c.get(i);
			double dist = v.distance(p);
			if (dist < HANDLE_SIZE && dist < minDist) {
				minDist = dist;
				chosenVertex = v;
			}
		}
		
		if (chosenVertex!=hoverVertex) {
			hoverVertex=chosenVertex;
			if (chosenVertex!=null) hoverEdge = null;
			redraw();
			return true;
		}
		
		if (chosenVertex!=null)
			return true;
		
		// Detect hover edge
		minDist = Double.MAX_VALUE;
		Edge chosenEdge = null;
		for (int i=0, I=c.numEdges(); i<I; i++) {
			Edge edge = c.getEdge(i);
			
			double dist = edge.ptSegDist(p);
			if (dist < HANDLE_SIZE && dist < minDist) {
				minDist = dist;
				chosenEdge = edge;
			}
		}
		
		if (chosenEdge!=hoverEdge) {
			hoverEdge = chosenEdge;
			if (hoverEdge!=null) hoverVertex = null;
			redraw();
		}
		
		return editMode;
	}
	
	@Override protected boolean onMouseUp(float x, float y, int button, int modKeysAndButtons) {
		draggedVertex = null;
		redraw();
		return false;
	}
	
//==[ Rendering ]===================================================================================

	static final double HANDLE_SIZE = 8;
	
	@Override protected void render(Rectangle clip, Transform t, GC gc, Layer[] children) {
		super.render(clip, t, gc, children);
		
		Device dsp = gc.getDevice();
		Contour c = contour;
		if (contour==null && ghost!=null) {
			gc.setAlpha(64);
			c = ghost;
		}
		
		gc.setForeground(dsp.getSystemColor(SWT.COLOR_BLACK));
		
		gc.setBackground(dsp.getSystemColor(SWT.COLOR_DARK_CYAN));
		Path path = new Path(dsp);
		c.toPath(path);
		gc.fillPath(path);
		path.dispose();
		
		if (!editMode)
			gc.setLineWidth(2);
		
		for (int i=0, I=c.numEdges(); i<I; i++) {
			Edge edge = c.getEdge(i);
			Path swtpath = new PathShape(dsp, edge);
			gc.setForeground(dsp.getSystemColor(SWT.COLOR_CYAN));
			if (edge==hoverEdge)
				gc.setForeground(dsp.getSystemColor(SWT.COLOR_RED));
			gc.drawPath(swtpath);
			swtpath.dispose();
		}
		
		for (int i=0, I=c.numVertices(); i<I; i++) {
			Vertex vertex = c.get(i);
			gc.setBackground(dsp.getSystemColor(SWT.COLOR_CYAN));
			if (vertex==hoverVertex)
				gc.setBackground(dsp.getSystemColor(SWT.COLOR_RED));
			drawHandle(gc, vertex);
			// gc.drawString(String.valueOf(i), (int)vertex.getX()+5, (int)vertex.getY()-5);
		}
	}

	public static void drawHandle(GC gc, Point2D p) {
		drawHandle(gc, p.getX(), p.getY());
	}
	
	public static void drawHandle(GC gc, double x, double y) {
		Ellipse2D ellipse = new Ellipse2D.Double(x - HANDLE_SIZE/2, y - HANDLE_SIZE/2, HANDLE_SIZE, HANDLE_SIZE);
		PathShape shape = new PathShape(gc.getDevice(), ellipse);
		gc.fillPath(shape);
		shape.dispose();
	}
	
}
