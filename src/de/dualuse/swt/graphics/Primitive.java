package de.dualuse.swt.graphics;

import java.awt.Rectangle;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;



public class Primitive implements Shape, Cloneable {
//	private static final long serialVersionUID = 1L;

	
	private int n = 0;
	private float[] vertices;
	
	private int type;
	private final float[][] m;
	private Viewport v;
	private double pointSize;

	public int getType() {
		return type;
	}
	
	public Primitive() {
		this.m = new float[4][4];
		this.vertices = new float[12*4];
	}

	
//	public Primitive(float[][] t, int type) {
//		this.m = new float[4][4];
//		RC.copy(t,m);
//		this.type = type;
//		this.vertices = new float[12*4];
//	}
//	public Primitive(float[][] t, int type, float vertices[], int n) {
//		this.m = new float[4][4];
//		RC.copy(t,m);
//		this.type = type;
//		this.vertices = Arrays.copyOf(vertices, n);
//		this.n = n;
//	}
	
	public void addVertex(float x, float y, float z) {
		if (n>=vertices.length)
			vertices = Arrays.copyOf(vertices, (vertices.length*3/2)/3*3);

		vertices[n++] = x;
		vertices[n++] = y;
		vertices[n++] = z;
	}
	
	public Primitive reset(Viewport v, float[][] m, int type, double pointSize) {
		RC.copy(m, this.m);
		this.type = type;
		this.n = 0;
		this.v = v;
		this.pointSize = pointSize;
		
		return this;
	}

//	public Primitive clone() {
//		return new Primitive(m, type, vertices, n);
//	}
	
	
	public Rectangle getBounds() { return getBounds2D().getBounds(); }
	public Rectangle2D getBounds2D() { throw new RuntimeException("Unsupported"); }

	public boolean contains(double x, double y) { return true; }
	public boolean contains(Point2D p) { return true; }
	public boolean intersects(double x, double y, double w, double h) { return true; }
	public boolean intersects(Rectangle2D r) { return true; }
	public boolean contains(double x, double y, double w, double h) { return true; }
	public boolean contains(Rectangle2D r) { return true; }
	
	
	public PathIterator getPathIterator(AffineTransform at) {
		return new PrimitivePathIterator(vertices, n/3, type, at, v, m, (float) pointSize);
	}

	public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
		return new PrimitivePathIterator(vertices, n/3, type, at, v, m, (float) pointSize);
	}
	
}
