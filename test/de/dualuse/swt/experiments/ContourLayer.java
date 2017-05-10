package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.experiments.Contour.Edge;
import de.dualuse.swt.experiments.Contour.Vertex;
import de.dualuse.swt.experiments.LayerKnob.KnobClickListener;
import de.dualuse.swt.experiments.LayerKnob.KnobHoverListener;
import de.dualuse.swt.graphics.PathShape;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;
import de.dualuse.swt.widgets.LayerContainer;

public class ContourLayer extends Layer {

	Contour contour;
	List<VertexKnob> controls = new ArrayList<>();
	VertexKnob first;
	
	static class VertexKnob extends LayerKnob {
		Vertex vertex;
		public VertexKnob(Layer parent, Vertex vertex) {
			
			super(parent, (float)vertex.x, (float)vertex.y);
			
			this.vertex = vertex;
			
			addMoveListener((e, x, y) -> {
				this.vertex.setLocation(x, y);
				parent.redraw();
			});
			
		}
	};
	
//==[ Constructor ]=================================================================================
	
	public ContourLayer(LayerContainer parent, Contour contour) {
		super(parent);
		
		this.contour = contour;
		
		for (int i=0, I=contour.numVertices(); i<I; i++)
			addControl(contour.get(i));
		
		if (!controls.isEmpty())
			setFirst(controls.get(0));
	}
	
//==[ Hover State ]=================================================================================
	
	VertexKnob hoverVertex;
	Edge hoverEdge;
	
	KnobHoverListener hoverListener = new KnobHoverListener() {
		@Override public void hover(LayerKnob src, boolean state) {
			
			if (state) {
				if (hoverVertex != src) {
					hoverVertex = (VertexKnob)src;
					hoverEdge = null;
					redraw();
				}
			} else {
				if (hoverVertex == src) {
					hoverVertex = null;
					redraw();
				}
			}
			
		}
	};
	
//==================================================================================================
	
	private VertexKnob addControl(Vertex vertex) {
		VertexKnob control = new VertexKnob(this, vertex);
		control.addHoverListener(hoverListener);
		controls.add(control);
		return control;
	}
	
	private void setFirst(VertexKnob firstKnob) {
		if (first != null)
			first.removeCLickListener(listener); 
		
		first = firstKnob;
		
		if (first != null)
			first.addClickListener(listener);
	}
	
	KnobClickListener listener = new KnobClickListener() {
		@Override public void clicked(LayerKnob src, float x, float y, int button, int mask) {
			if (!contour.isClosed()) {
				contour.close();
				redraw();
			}
		}
	};
	
//==[ Input Handling ]==============================================================================
	
	@Override protected boolean onMouseDown(float x, float y, int button, int modKeysAndButtons) {
		
		if (button == 1) {
			
			Vertex vertex = contour.add(x,y);
			if (vertex == null) return true;
			
			VertexKnob control = addControl(vertex);
			if (first == null) setFirst(control);
			
			redraw();
			
			return true;
			
		} else if (button == 3) {
			
			Vertex vertex = contour.removeLast();
			Iterator<VertexKnob> it = controls.iterator();
			while(it.hasNext()) {
				VertexKnob control = it.next();
				if (control.vertex == vertex) {
					control.dispose();
					it.remove();
					if (first==control)
						setFirst(null);
				}
			}
			
			redraw();
			
			return true;
		}
		
		return false;
	}

	Point2D insertionPoint;
	
	@Override protected boolean onMouseMove(float x, float y, int modKeysAndButtons) {
		
		if (hoverVertex!=null)
			return true;
		
		Edge selected = null;
		double threshold = 5;
		double minDistance = Double.MAX_VALUE;
		
		for (int i=0, I=contour.numEdges(); i<I; i++) {
			
			Edge edge = contour.getEdge(i);
			double distance = edge.ptSegDist(x, y);
			if (distance < minDistance && distance < threshold) {
				minDistance = distance;
				selected = edge;
			}
			
		}
		
		if (selected != hoverEdge) {
			hoverEdge = selected;
			redraw();
		}

		if (hoverEdge != null) {
			double nx = hoverEdge.x2-hoverEdge.x1;
			double ny = hoverEdge.y2-hoverEdge.y1;
			double l = Math.sqrt(nx*nx+ny*ny);
			nx /= l;
			ny /= l;
			
			double px = x - hoverEdge.x1;
			double py = y - hoverEdge.y1;
			double d = px*nx + py*ny;
			
			double rx = nx*d;
			double ry = ny*d;
			
			insertionPoint = new Point2D.Double(hoverEdge.x1 + rx, hoverEdge.y1 + ry);
			
			redraw();
			
		} else {
			insertionPoint = null;
		}
		
		return true;
		
	}
	
//==[ Rendering ]===================================================================================

	@Override protected void render(GC gc) {
		super.render(gc);
		
		// System.out.println("Rendering (" + controls.size() + ")");
		
		if (controls.size()<2) return;

		Color edgeColor = gc.getDevice().getSystemColor(SWT.COLOR_DARK_CYAN);
		Color hoverEdgeColor = gc.getDevice().getSystemColor(SWT.COLOR_DARK_YELLOW);
		
		if (contour.numEdges()>0) {
			for (int i=0, I=contour.numEdges(); i<I; i++) {
				Edge edge = contour.getEdge(i);
				Path edgePath = new PathShape(gc.getDevice(), edge);
				
				gc.setForeground(edge==hoverEdge ? hoverEdgeColor : edgeColor);
				gc.drawPath(edgePath);
				
				edgePath.dispose();
			}
			
			// XXX if control is dragged, no mousemove events arrive, point updated belatedly
			if (insertionPoint != null && hoverVertex == null) {
				gc.setAlpha(128);
				Ellipse2D ellipse = new Ellipse2D.Double(insertionPoint.getX()-4.5, insertionPoint.getY()-4.5, 9, 9);
				gc.setBackground(hoverEdgeColor);
				Path insertionPath = new PathShape(gc.getDevice(), ellipse);
				gc.fillPath(insertionPath);
				insertionPath.dispose();
			}
		}

	}
	
//==[ Test-Main ]===================================================================================
	
	public static void main(String[] args) {

		Application app = new Application();
		Shell sh = new Shell(app);

		sh.setLayout(new FillLayout());

		LayerCanvas dc = new LayerCanvas(sh, NONE) {
			final Random rng = new Random(1337);
			
			Color random = new Color(getDisplay(), new RGB(rng.nextFloat()*360f,0.8f,0.9f));
			
			@Override protected void renderBackground(Rectangle clip, Transform t, GC gc) {
				gc.setBackground(random);
				gc.fillRectangle(getBounds());
			}
			
			@Override public void dispose() {
				super.dispose();
				random.dispose();
			}
			
		};

		dc.addListener(SWT.KeyDown, (e) -> {
			if (e.keyCode==SWT.ESC)
				sh.dispose();
		});

		Contour contour = new Contour();
//		contour.add(10, 10);
		ContourLayer layer = new ContourLayer(dc, contour);
		// layer.setBounds(0, 0, 1920, 1080);
		
		sh.setBounds(1500, 150, 800, 600);
		sh.setVisible(true);
		app.loop(sh);
	}
	
}
