package de.dualuse.swt.experiments;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.ejml.simple.SimpleMatrix;

import de.dualuse.swt.experiments.Contour.Vertex;
import de.dualuse.swt.experiments.ContourLayer.VertexKnob;
import de.dualuse.swt.experiments.LayerKnob.KnobClickListener;
import de.dualuse.swt.experiments.LayerKnob.KnobMoveListener;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;

public class TransformTest {

	Image img;
	
	LayerCanvas c1;
	LayerCanvas c2;
	Transform transform;
	
	List<VertexKnob> vs = new ArrayList<VertexKnob>();
	List<VertexKnob> us = new ArrayList<VertexKnob>();
	
	Map<VertexKnob,VertexKnob> correspondences = new HashMap<VertexKnob,VertexKnob>();
	
	/////
	
	Set<VertexKnob> verticesCtrl = new HashSet<VertexKnob>();
	Set<VertexKnob> verticesTrans = new HashSet<VertexKnob>();
	
//==[ Compute Transformations from Point Correspondences ]==========================================

	//////////////////// Translation from 1 point correspondence
	
	void from1Correspondence(Transform t, Point2D u1, Point2D v1) {
		float u1x = (float)u1.getX(), u1y = (float)u1.getY();
		float v1x = (float)v1.getX(), v1y = (float)v1.getY();
		from1Correspondence(t, u1x, u1y, v1x, v1y);
	}
	
	void from1Correspondence(Transform t, float u1x, float u1y, float v1x, float v1y) {
		double tx = v1x - u1x;
		double ty = v1y - u1y;
		t.setElements(1, 0, 0, 1, (float)tx, (float)ty);
	}
	
	//////////////////// Euclidean Transform from 2 point correspondences (translation, rotation, uniform scaling)
	
	void from2Correspondences(Transform t, Point2D u1, Point2D u2, Point2D v1, Point2D v2) {
		
		float u1x = (float)u1.getX(), u1y = (float)u1.getY();
		float u2x = (float)u2.getX(), u2y = (float)u2.getY();
		float v1x = (float)v1.getX(), v1y = (float)v1.getY();
		float v2x = (float)v2.getX(), v2y = (float)v2.getY();
	
		from2Correspondences(t, u1x, u1y, u2x, u2y, v1x, v1y, v2x, v2y);
	}
	
	void from2Correspondences(Transform t, float u1x, float u1y, float u2x, float u2y, float v1x, float v1y, float v2x, float v2y) {
		
		double[][] coefficients = new double[][] {
			{ u1x, -u1y, 1, 0 },
			{ u1y,  u1x, 0, 1 },
			{ u2x, -u2y, 1, 0 },
			{ u2y,  u2x, 0, 1 }
		};
		
		double[][] result = new double[][] {
			{ v1x },
			{ v1y },
			{ v2x },
			{ v2y }
		};
		
		SimpleMatrix m = new SimpleMatrix(coefficients);
		SimpleMatrix a = new SimpleMatrix(result);
		SimpleMatrix vars = m.solve(a);
		
		float r1 = (float)vars.get(0);
		float r2 = (float)vars.get(1);
		float tx = (float)vars.get(2);
		float ty = (float)vars.get(3);
		
		t.setElements(r1, r2, -r2, r1, tx, ty);
	}

	//////////////////// AffineTransform from 3 point correspondences
	
	void from3Correspondences(Transform t, Point2D u1, Point2D u2, Point2D u3, Point2D v1, Point2D v2, Point2D v3) {

		float u1x = (float)u1.getX(), u1y = (float)u1.getY();
		float u2x = (float)u2.getX(), u2y = (float)u2.getY();
		float u3x = (float)u3.getX(), u3y = (float)u3.getY();
		
		float v1x = (float)v1.getX(), v1y = (float)v1.getY();
		float v2x = (float)v2.getX(), v2y = (float)v2.getY();
		float v3x = (float)v3.getX(), v3y = (float)v3.getY();

		from3Correspondences(t, u1x, u1y, u2x, u2y, u3x, u3y, v1x, v1y, v2x, v2y, v3x, v3y);
	}
	
	void from3Correspondences(Transform t,
		float u1x, float u1y, float u2x, float u2y, float u3x, float u3y,
		float v1x, float v1y, float v2x, float v2y, float v3x, float v3y) {
		
		double[][] coefficients = new double[][] {
			{ u1x, u1y, 1,   0,   0, 0},
			{   0,   0, 0, u1x, u1y, 1},
			{ u2x, u2y, 1,   0,   0, 0},
			{   0,   0, 0, u2x, u2y, 1},
			{ u3x, u3y, 1,   0,   0, 0},
			{   0,   0, 0, u3x, u3y, 1}
		};
		
		double[][] result = new double[][] {
			{v1x},
			{v1y},
			{v2x},
			{v2y},
			{v3x},
			{v3y}
		};
		
		SimpleMatrix m = new SimpleMatrix(coefficients);
		SimpleMatrix a = new SimpleMatrix(result);
		SimpleMatrix vars = m.solve(a);
		
		float m11 = (float)vars.get(0);
		float m12 = (float)vars.get(1);
		float m13 = (float)vars.get(2);
		float m21 = (float)vars.get(3);
		float m22 = (float)vars.get(4);
		float m23 = (float)vars.get(5);
		
		t.setElements(m11, m21, m12, m22, m13, m23);
	}

	//////////////////// Homography from 4 point correspondences
	
	void from4Correspondences(float[][] h,
			Point2D u1, Point2D u2, Point2D u3, Point2D u4,
			Point2D v1, Point2D v2, Point2D v3, Point2D v4) {

			float u1x = (float)u1.getX(), u1y = (float)u1.getY();
			float u2x = (float)u2.getX(), u2y = (float)u2.getY();
			float u3x = (float)u3.getX(), u3y = (float)u3.getY();
			float u4x = (float)u4.getX(), u4y = (float)u4.getY();
			
			float v1x = (float)v1.getX(), v1y = (float)v1.getY();
			float v2x = (float)v2.getX(), v2y = (float)v2.getY();
			float v3x = (float)v3.getX(), v3y = (float)v3.getY();
			float v4x = (float)v4.getX(), v4y = (float)v4.getY();

			from4Correspondences(h,
				u1x, u1y, u2x, u2y, u3x, u3y, u4x, u4y,
				v1x, v1y, v2x, v2y, v3x, v3y, v4x, v4y
			);
	}
	
	void from4Correspondences(float[][] h,
		float u1x, float u1y, float u2x, float u2y, float u3x, float u3y, float u4x, float u4y,
		float v1x, float v1y, float v2x, float v2y, float v3x, float v3y, float v4x, float v4y) {

		double[][] coefficients = new double[][] {
			
			{u1x, u1y, 1,  0 ,  0 , 0, -v1x*u1x, -v1x*u1y },
			{ 0 ,  0 , 0, u1x, u1y, 1, -v1y*u1x, -v1y*u1y },
			
			{u2x, u2y, 1,  0 ,  0 , 0, -v2x*u2x, -v2x*u2y },
			{ 0 ,  0 , 0, u2x, u2y, 1, -v2y*u2x, -v2y*u2y },

			{u3x, u3y, 1,  0 ,  0 , 0, -v3x*u3x, -v3x*u3y },
			{ 0 ,  0 , 0, u3x, u3y, 1, -v3y*u3x, -v3y*u3y },

			{u4x, u4y, 1,  0 ,  0 , 0, -v4x*u4x, -v4x*u4y },
			{ 0 ,  0 , 0, u4x, u4y, 1, -v4y*u4x, -v4y*u4y },
			
		};
		
		double[][] result = new double[8][1];
		
		SimpleMatrix m = new SimpleMatrix(coefficients);
		SimpleMatrix a = new SimpleMatrix(result);
		SimpleMatrix vars = m.solve(a);
		
		float h11 = (float)vars.get(0);
		float h12 = (float)vars.get(1);
		float h13 = (float)vars.get(2);
		float h21 = (float)vars.get(3);
		float h22 = (float)vars.get(4);
		float h23 = (float)vars.get(5);
		float h31 = (float)vars.get(6);
		float h32 = (float)vars.get(7);
		float h33 = 1;
		
		h[0][0] = h11; h[0][1] = h12; h[0][2] = h13;
		h[1][0] = h21; h[1][1] = h22; h[1][2] = h23;
		h[2][0] = h31; h[2][1] = h32; h[2][2] = h33;
	}
	
	//////////////////// Constrainted Homography from 3 point correspondences? (no shear / only uniform scaling)
	// 6 vars: r1, r2 (rotation + scaling, as above), tx, ty (translation), h13, h23 (perspective)

	void from3CorrespondencesHom(float[][] h, Point2D u1, Point2D u2, Point2D u3, Point2D v1, Point2D v2, Point2D v3) {

		float u1x = (float)u1.getX(), u1y = (float)u1.getY();
		float u2x = (float)u2.getX(), u2y = (float)u2.getY();
		float u3x = (float)u3.getX(), u3y = (float)u3.getY();
		
		float v1x = (float)v1.getX(), v1y = (float)v1.getY();
		float v2x = (float)v2.getX(), v2y = (float)v2.getY();
		float v3x = (float)v3.getX(), v3y = (float)v3.getY();

		from3CorrespondencesHom(h,
			u1x, u1y, u2x, u2y, u3x, u3y,
			v1x, v1y, v2x, v2y, v3x, v3y
		);
	}
	
	void from3CorrespondencesHom(float[][] h,
		float u1x, float u1y, float u2x, float u2y, float u3x, float u3y,
		float v1x, float v1y, float v2x, float v2y, float v3x, float v3y) {

		double[][] coefficients = new double[][] {
			{ u1x, -u1y, 1, 0, -v1x*u1x, -v1x*u1y },
			{ u1y,  u1x, 0, 1, -v1y*u1x, -v1y*u1y },

			{ u2x, -u2y, 1, 0, -v2x*u2x, -v2x*u2y },
			{ u2y,  u2x, 0, 1, -v2y*u2x, -v2y*u2y },

			{ u3x, -u3y, 1, 0, -v3x*u3x, -v3x*u3y },
			{ u3y,  u3x, 0, 1, -v3y*u3x, -v3y*u3y },
		};

		double[][] result = new double[6][1];
		
		SimpleMatrix m = new SimpleMatrix(coefficients);
		SimpleMatrix a = new SimpleMatrix(result);
		SimpleMatrix vars = m.solve(a);
		
		float r1  = (float) vars.get(0);
		float r2  = (float) vars.get(1);
		float tx  = (float) vars.get(2);
		float ty  = (float) vars.get(3);
		float h31 = (float) vars.get(4);
		float h32 = (float) vars.get(5);
		
		float h11 =  r1;
		float h12 = -r2;
		float h13 =  tx;
		float h21 =  r2;
		float h22 =  r1;
		float h23 =  ty;
		// h31 = h31
		// h32 = h32
		float h33 = 1;

		h[0][0] = h11; h[0][1] = h12; h[0][2] = h13;
		h[1][0] = h21; h[1][1] = h22; h[1][2] = h23;
		h[2][0] = h31; h[2][1] = h32; h[2][2] = h33;
	}
	
//==[ Matrix Transform helper ]=====================================================================
	
	static float transformX(float[][] m, float x, float y) {
		float xh = m[0][0]*x + m[0][1]*y + m[0][2];
		float w  = m[2][0]*x + m[2][1]*y + m[2][2];
		return xh/w;
	}
	
	static float transformY(float[][] m, float x, float y) {
		float yh = m[1][0]*x + m[1][1]*y + m[1][2];
		float w  = m[2][0]*x + m[2][1]*y + m[2][2];
		return yh/w;
	}
	
	static float[] transform(float[][] m, float[] p) {
		float x = p[0], y = p[1];
		float xh = m[0][0]*x + m[0][1]*y + m[0][2];
		float yh = m[1][0]*x + m[1][1]*y + m[1][2];
		float w  = m[2][0]*x + m[2][1]*y + m[2][2];
		p[0] = xh/w;
		p[1] = yh/w;
		return p;
	}
	
//==================================================================================================
	
	public void setTransformFromCorrespondences(Transform transform) {
		if (correspondences.isEmpty()) {
			transform.identity();
		} else if (correspondences.size()==1) {
			Vertex u1 = us.get(0).getVertex();
			Vertex v1 = vs.get(0).getVertex();
			from1Correspondence(transform, u1, v1);
		} else if (correspondences.size()==2) {
			Vertex u1 = us.get(0).getVertex();
			Vertex u2 = us.get(1).getVertex();
			Vertex v1 = vs.get(0).getVertex();
			Vertex v2 = vs.get(1).getVertex();
			from2Correspondences(transform, u1, u2, v1, v2);
		} else if (correspondences.size()==3) {
			Vertex u1 = us.get(0).getVertex();
			Vertex u2 = us.get(1).getVertex();
			Vertex u3 = us.get(2).getVertex();
			Vertex v1 = vs.get(0).getVertex();
			Vertex v2 = vs.get(1).getVertex();
			Vertex v3 = vs.get(2).getVertex();
			from3Correspondences(transform, u1, u2, u3, v1, v2, v3);
		}
	}
	
//==[ Main ]========================================================================================
	
	float[] elements = new float[6];
	
	public TransformTest() {

		Display dsp = Display.getDefault();
		
		transform = new Transform(dsp);
		
		Shell window = new Shell(dsp);
		window.setLayout(new FillLayout());
		
		c1 = new LayerCanvas(window, SWT.NONE) {
			@Override protected void renderBackground(Rectangle clip, Transform t, GC gc) {
				Color color = dsp.getSystemColor(SWT.COLOR_BLUE);
				gc.setAlpha(64);
				gc.setBackground(color);
				gc.fillRectangle(0, 0, getBounds().width, getBounds().height);
				gc.setAlpha(255);
				gc.drawImage(img, 100, 100);
			}
		};
		
		c2 = new LayerCanvas(window, SWT.NONE) {
			@Override protected void renderBackground(Rectangle clip, Transform t, GC gc) {
				
				Color color = dsp.getSystemColor(SWT.COLOR_RED);
				gc.setAlpha(64);
				gc.setBackground(color);
				gc.fillRectangle(0, 0, getBounds().width, getBounds().height);
				gc.setAlpha(255);

				Transform current = new Transform(gc.getDevice());
				Transform copy = new Transform(gc.getDevice());
				
				gc.getTransform(current);

				transform.getElements(elements);
				copy.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
				
				copy.multiply(current);
				gc.setTransform(copy);
				
				gc.drawImage(img, 100, 100);
				
				current.dispose();
				copy.dispose();
			}
		};
		
		Layer l1 = new Layer(c1);
		Layer l2 = new Layer(c2);
	
		LayerKnob.KnobMoveListener updateListener = new LayerKnob.KnobMoveListener() {
			@Override public void moved(LayerKnob src, float x, float y) {
				update();
			}
		};
		
		c1.addListener(SWT.KeyDown, (e) -> {
			if (e.keyCode == SWT.ESC)
				window.dispose();
		});
		
		c2.addListener(SWT.KeyDown, (e) -> {
			if (e.keyCode == SWT.ESC)
				window.dispose();
		});
		 
		Listener canvasListener = (e) -> {
			if (e.count!=2) return;			
			if (us.size()>=3) return;

			float[] coords = new float[] { e.x, e.y };

			final VertexKnob u;
			final VertexKnob v;
			
			if (e.widget == c1) {

				transform.transform(coords);

				u = new VertexKnob(l1, new Vertex(e.x, e.y));
				v = new VertexKnob(l2, new Vertex(coords[0], coords[1]));
				
			} else {
				
				Transform copy = new Transform(dsp);
				transform.getElements(elements);
				copy.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
				copy.invert();
				copy.transform(coords);
				copy.dispose();
				
				u = new VertexKnob(l1, new Vertex(coords[0], coords[1]));
				v = new VertexKnob(l2, new Vertex(e.x, e.y));
			}
			
			
			us.add(u);
			vs.add(v);
			correspondences.put(u, v);
			
			KnobClickListener removeListener = new KnobClickListener() {
				@Override public void clicked(LayerKnob src, float x, float y, int button, int mask) {
					if (button==3) {
						
						us.remove(u);
						vs.remove(v);
						u.dispose();
						v.dispose();
						correspondences.remove(u);
						
						// update transform
						setTransformFromCorrespondences(transform);
						
						c1.redraw();
						c2.redraw();
						
					} else if (button==1) {
						
						boolean ctrlPressed = (mask & SWT.CTRL) != 0;
						if (ctrlPressed) verticesCtrl.add(v); else verticesCtrl.remove(v);
						
					}
				}
			};
			
			u.addClickListener(removeListener);
			v.addClickListener(removeListener);

			u.addMoveListener(updateListener);
			v.addMoveListener(updateListener);
			
			KnobMoveListener knobListener = new KnobMoveListener() {
				@Override public void moved(LayerKnob src, float x, float y) {
					// if normal movement: update transform
					// if ctrl movement: update corresponding point in other canvas (and prevent listener of other knob to trigger another move event...)
					if (src==v && !verticesTrans.contains(v) && verticesCtrl.contains(src)) {
						// move corresonding point

						Transform copy = new Transform(dsp);
						transform.getElements(elements);
						copy.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
						copy.invert();
						float[] coords = new float[] { x, y };
						copy.transform(coords);
						copy.dispose();
						
						verticesTrans.add(u);
						u.setLocation(coords[0], coords[1]);
						verticesTrans.remove(u);
						
					} else if (src==u && !verticesTrans.contains(u)){
						
						float[] coords = new float[] { x, y };
						transform.transform(coords);
						
						verticesTrans.add(v);
						v.setLocation(coords[0], coords[1]);
						verticesTrans.remove(v);
						
					} else {
						// update transform
						setTransformFromCorrespondences(transform);
					}
				}
			};
			
			u.addMoveListener(knobListener);
			v.addMoveListener(knobListener);
			
		};
		
		c1.addListener(SWT.MouseDown, canvasListener);
		c2.addListener(SWT.MouseDown, canvasListener);
		
		c1.redraw();
		c2.redraw();
		
		img = new Image(dsp, getClass().getResourceAsStream("arbeitsstelle_sm.png"));
		// img = new Image(dsp, getClass().getResourceAsStream("frame.jpg"));
		
		window.addDisposeListener((e) -> {
			img.dispose();
			transform.dispose();
		});
		
		window.setBounds(100, 100, 800, 600);
		window.open();
		
		while (!window.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
		
	}
	
	void update() {
		
		c1.redraw();
		c2.redraw();
	}
	
	public static void main(String[] args) {
		new TransformTest();
	}
}
