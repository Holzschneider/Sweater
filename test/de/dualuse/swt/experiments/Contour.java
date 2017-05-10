package de.dualuse.swt.experiments;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.graphics.PathShape;


public class Contour {

	ArrayList<Vertex> vertices = new ArrayList<Vertex>();
	ArrayList<Edge> edges = new ArrayList<Edge>();
	
	double x1, y1, x2, y2;
	
	boolean closed;
	
//==[ Vertex Class ]================================================================================
	
	public static class Vertex extends Point2D.Double {
		private static final long serialVersionUID = -4562976862917821130L;
		
		public Line2D prev;
		public Line2D next;
		
		public Vertex() {
			super();
		}
		
		public Vertex(double x, double y) {
			super(x,y);
		}
		
		public Vertex(double x, double y, Line2D prev, Line2D next) {
			super(x,y);
			this.prev = prev;
			this.next = next;
			updateLines();
		}
		
		@Override public void setLocation(Point2D p) {
			super.setLocation(p);
			updateLines();
		}
		
		@Override public void setLocation(double x, double y) {
			super.setLocation(x, y);
			updateLines();
		}
		
		private void updateLines() {
			if (prev!=null) {
				Point2D p1 = prev.getP1();
				Point2D p2 = this;
				prev.setLine(p1, p2);
			}
			
			if (next!=null) {
				Point2D p1 = this;
				Point2D p2 = next.getP2();
				next.setLine(p1, p2);
			}
		}
	}
	
//==[ Edge Class ]==================================================================================
	
	public static class Edge extends Line2D.Double {
		private static final long serialVersionUID = 3553112565663988466L;
		
		public Edge(Vertex p1, Vertex p2) {
			super(p1, p2);
		}
	}

//==[ Constructor ]=================================================================================
	
	public Contour() {
		
	}
	
//==[ Vertices ]====================================================================================
	
	public Vertex add(double x, double y) {

		if (closed) return null;
		
		Vertex currVertex;
		int index = vertices.size();
		
		if (index>0) {
			Vertex prevVertex = vertices.get(index-1);
			currVertex = new Vertex(x,y);
			Edge edge = new Edge(prevVertex, currVertex);
			prevVertex.next = edge;
			currVertex.prev = edge;
			edges.add(edge);
		} else {
			currVertex = new Vertex(x, y);
		}
		
		vertices.add(currVertex);
		updateBounds();
		return currVertex;
	}
	
	public Vertex insert(Edge edge, double x, double y) {
		int index = edges.indexOf(edge);
		return insert(index, x, y);
	}
	
	public Vertex insert(int index, double x, double y) {

		// Update vertices
		Vertex prevVertex = get(index);
		Vertex currVertex = new Vertex(x, y);
		Vertex nextVertex = nextVertex(index);
		
		vertices.add(index+1, currVertex);
		
		// Update edges
		Edge prevLine = new Edge(prevVertex, currVertex);
		Edge nextLine = new Edge(currVertex, nextVertex);
		
		prevVertex.next = prevLine;
		currVertex.prev = prevLine;
		
		currVertex.next = nextLine;
		nextVertex.prev = nextLine;
		
		edges.remove(index);
		edges.add(index, nextLine);
		edges.add(index, prevLine);
		
		// Update bounds
		updateBounds(currVertex);
		
		return currVertex;
	}
	
	public void remove(Vertex vertex) {
		int index = vertices.indexOf(vertex);
		remove(index);
	}
	
	// XXX if 3 vertices -> 2 vertices and contour is closed: reopen it
	public Vertex remove(int index) {
		
		// Update Vertices
		Vertex prevVertex = prevVertex(index);
		Vertex currVertex = vertices.get(index);
		Vertex nextVertex = nextVertex(index);
		
		vertices.remove(index);
		
		// Update edges
		if (prevVertex!=null) edges.remove(prevVertex.next);
		if (nextVertex!=null) edges.remove(nextVertex.prev);
		
		// XXX Only create new edge if there is a prev and next vertex.
		//	   If last vertex removed in an open polyline, there's no next vertex.
		
		Edge newEdge = (prevVertex!=null && nextVertex!=null) ? new Edge(prevVertex, nextVertex) : null;
		if (newEdge!=null) {
			if (index==0)
				edges.add(newEdge);
			else
				edges.add(index-1, newEdge);
		}
		
		if (prevVertex!=null) prevVertex.next = newEdge;
		if (nextVertex!=null) nextVertex.prev = newEdge;
		
		// Update bounds
		double px = currVertex.getX();
		double py = currVertex.getY();
		if (px == x1 || px == x2 || py == y1 || py == y2)
			updateBounds();
		
		return currVertex;
	}
	
	public Vertex removeLast() {
		Vertex vertex = null;
		if (!vertices.isEmpty()) {
			int index = vertices.size()-1;
			vertex = vertices.get(index);
			remove(index);
		}
		return vertex;
	}
	
	public Vertex get(int index) {
		return vertices.get(index);
	}
	
	public void set(int index, double x, double y) {
		Point2D p = vertices.get(index);
		p.setLocation(x, y);
	}
	
	public void set(int index, Point2D p) {
		Point2D q = vertices.get(index);
		q.setLocation(p);
	}

	public int numVertices() {
		return vertices.size();
	}

//==[ Edges ]=======================================================================================
	
	public Edge getEdge(int index) {
		return edges.get(index);
	}
	
	public int numEdges() {
		return edges.size();
	}

//==[ Bounds ]======================================================================================
	
	
	public double getMinX() {
		return x1;
	}
	
	public double getMaxX() {
		return x2;
	}
	
	public double getMinY() {
		return y1;
	}
	
	public double getMaxY() {
		return y2;
	}

	public void toPath(Path path) {

		Point2D start = vertices.get(0);
		path.moveTo((float)start.getX(), (float)start.getY());
		
		for (int i=1; i<vertices.size(); i++) {
			Point2D p = vertices.get(i);
			path.lineTo((float)p.getX(), (float)p.getY());
		}
		
	}
	
	public void close() {
		if (isClosed()) return;
		if (vertices.size()<3) throw new IllegalArgumentException("Need at least 3 vertices for closing");
		Vertex last = vertices.get(vertices.size()-1);
		Vertex first = vertices.get(0);
		Edge edge = new Edge(last, first);
		edges.add(edge);
		first.prev = edge;
		last.next = edge;
		closed = true;
	}
	
	public boolean isClosed() {
		return closed;
	}
	
//==[ Private Helper ]==============================================================================
	
//	private Line2D prevLine(int index) {
//		int prevIndex = index-1;
//		if (prevIndex<0 && !closed) return null;
//		if (prevIndex<0) prevIndex += edges.size(); // if closed
//		return edges.get(prevIndex);
//	}
//	
//	private Line2D nextLine(int index) {
//		int nextIndex = index;
//		return edges.get(nextIndex);
//	}
	
	private Vertex prevVertex(int index) {
		if (!closed && index==0) return null;
		int prevIndex = (index - 1);
		if (prevIndex < 0) prevIndex += vertices.size();
		return vertices.get(prevIndex);
	}
	
	private Vertex nextVertex(int index) {
		if (!closed && index==vertices.size()-1) return null;
		int nextIndex = (index + 1) % vertices.size();
		return vertices.get(nextIndex);
	}

	private void updateBounds() {
		x1 = Double.MAX_VALUE;
		x2 = Double.MIN_VALUE;
		y1 = Double.MAX_VALUE;
		y2 = Double.MIN_VALUE;
		for (Vertex v : vertices)
			updateBounds(v);
	}
	
	private void updateBounds(Point2D p) {
		x1 = Math.min(p.getX(), x1);
		x2 = Math.max(p.getX(), x2);
		y1 = Math.min(p.getY(), y1);
		y2 = Math.max(p.getY(), y2);
	}
	
//==[ Test-Main ]===================================================================================
	
	static double KNOB_SIZE = 8;
	
	static Contour contour;
	static Vertex hoverVertex;
	static Edge hoverEdge;
	
	static Vertex draggedVertex;

	public static void drawKnob(GC gc, Point2D p) {
		drawKnob(gc, p.getX(), p.getY());
	}
	
	public static void drawKnob(GC gc, double x, double y) {
		Ellipse2D ellipse = new Ellipse2D.Double(x - KNOB_SIZE/2, y - KNOB_SIZE/2, KNOB_SIZE, KNOB_SIZE);
		PathShape shape = new PathShape(gc.getDevice(), ellipse);
		gc.fillPath(shape);
		shape.dispose();
	}
	
	public static void main(String[] args) {
		
		Display dsp = Display.getDefault();
		Shell shell = new Shell(dsp, SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());
		Canvas canvas = new Canvas(shell, SWT.NO_BACKGROUND);
		
		canvas.addListener(SWT.KeyDown, (e) -> {
			System.out.println("Keypressed");
			if (e.keyCode==27) shell.dispose();
			if (e.keyCode==SWT.DEL) {
				if (hoverVertex!=null) {
					contour.remove(hoverVertex);
					canvas.redraw();
				}
			}
		});
		
//		canvas.addListener(SWT.Paint, (e) -> {
//			System.out.println("Painting");
//			GC gc = e.gc;
//			gc.setForeground(dsp.getSystemColor(SWT.COLOR_WHITE));
//			gc.fillRectangle(0, 0, e.width, e.height);
//		});
		
		canvas.addPaintListener((e) -> {
			System.out.println("Painting");
			GC gc = e.gc;
			
			gc.setBackground(dsp.getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(0, 0, e.width, e.height);
			
			if (contour!=null) {
				
				gc.setForeground(dsp.getSystemColor(SWT.COLOR_BLACK));
				
				for (int i=0, I=contour.numEdges(); i<I; i++) {
					Edge edge = contour.getEdge(i);
					Path swtpath = new PathShape(dsp, edge);
					gc.setForeground(dsp.getSystemColor(SWT.COLOR_BLACK));
					if (edge==hoverEdge)
						gc.setForeground(dsp.getSystemColor(SWT.COLOR_RED));
					gc.drawPath(swtpath);
					swtpath.dispose();
				}
				
//				Path swtpath = new Path(dsp);
//				contour.toPath(swtpath);
//				gc.drawPath(swtpath);
//				swtpath.dispose();
				
				for (int i=0, I=contour.numVertices(); i<I; i++) {
					Vertex vertex = contour.get(i);
					gc.setBackground(dsp.getSystemColor(SWT.COLOR_GREEN));
					if (vertex==hoverVertex)
						gc.setBackground(dsp.getSystemColor(SWT.COLOR_RED));
					drawKnob(gc, vertex);
					gc.drawString(String.valueOf(i), (int)vertex.getX()+5, (int)vertex.getY()-5);
				}
				System.out.println("#vertices: " + contour.numVertices());
			}
		});
		
		canvas.addListener(SWT.MouseDown, (e) -> {
			
			if (e.button==1) {
				if (contour==null) {
					
					// Create new contour
					contour = new Contour();
					contour.add(e.x, e.y);
					canvas.redraw();
					
				} else if (!contour.isClosed()) {
					
					// Extend contour
					if (hoverVertex!=null && hoverVertex==contour.get(0)) {
						contour.close();
					} else {
						contour.add(e.x, e.y);
					}
					canvas.redraw();
					
				} else if (hoverVertex!=null) {
					
					// drag vertex
					if (e.count==1)
						draggedVertex = hoverVertex;
					else if (e.count==2) {
						contour.remove(hoverVertex);
						hoverVertex = null;
						canvas.redraw();
					}
				// } else if (hoverEdge!=null && e.count==2) {
				} else if (hoverEdge!=null) {
					
					// insert new vertex
					Vertex vertex = contour.insert(hoverEdge, e.x, e.y);
					hoverVertex = vertex;
					draggedVertex = vertex;
					hoverEdge = null;
					canvas.redraw();
					
				}
				
			} else if (e.button==3) {
				
				if (contour!=null) contour.removeLast();
				canvas.redraw();
				
			}
			
		});
		
		canvas.addListener(SWT.MouseMove, (e) -> {

			if (contour!=null) {
				
				if (draggedVertex!=null) {
					draggedVertex.setLocation(e.x, e.y);
					canvas.redraw();
					return;
				}
				
				Point2D p = new Point2D.Double(e.x, e.y);
				
				double minDist = Double.MAX_VALUE;
				Vertex chosenVertex = null;
				
				for (int i=0, I=contour.numVertices(); i<I; i++) {
					Vertex v = contour.get(i);
					double dist = v.distance(p);
					if (dist < KNOB_SIZE && dist < minDist) {
						minDist = dist;
						chosenVertex = v;
					}
				}
				
				if (chosenVertex!=hoverVertex) {
					hoverVertex=chosenVertex;
					if (chosenVertex!=null) hoverEdge = null;
					canvas.redraw();
					return;
				}
				
				if (chosenVertex!=null)
					return;
				
				minDist = Double.MAX_VALUE;
				Edge chosenEdge = null;
				for (int i=0, I=contour.numEdges(); i<I; i++) {
					Edge edge = contour.getEdge(i);
					
					double dist = edge.ptSegDist(p);
					if (dist < KNOB_SIZE && dist < minDist) {
						minDist = dist;
						chosenEdge = edge;
					}
				}
				
				if (chosenEdge!=hoverEdge) {
					hoverEdge = chosenEdge;
					if (hoverEdge!=null) hoverVertex = null;
					canvas.redraw();
				}
			}
			
		});
		
		canvas.addListener(SWT.MouseUp, (e) -> {
			draggedVertex = null;
			canvas.redraw();
		});
		
		shell.open();
		
		while(!shell.isDisposed())
			while(!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
		
	}
}
