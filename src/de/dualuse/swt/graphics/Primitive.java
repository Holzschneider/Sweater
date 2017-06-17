package de.dualuse.swt.graphics;

import static java.lang.Math.*;

import java.awt.Rectangle;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;



public class Primitive implements Shape, Cloneable {
//	private static final long serialVersionUID = 1L;
	int n = 0,o=0;
	float[] vertices;
	
	VertexMode type;
	final float[][] m;
	Viewport v;
	float pointSize;

	public Primitive() {
		this.m = new float[4][4];
		this.vertices = new float[12*4];
	}

	
	public void addVertex(float x, float y, float z, float w) {
		if (o>=vertices.length)
			vertices = Arrays.copyOf(vertices, (vertices.length*3/2)/4*4);

		n++;
		vertices[o++] = x;
		vertices[o++] = y;
		vertices[o++] = z;
		vertices[o++] = w;
	}
	
	public Primitive reset(Viewport v, float[][] m, VertexMode type, double pointSize) {
		RC.copy(m, this.m);
		this.type = type;
		this.o = 0;
		this.n = 0;
		this.v = v;
		this.pointSize = max(1,(float)pointSize/2f);
		
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
		return new PrimitivePathIterator(this, at);
	}

	public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
		return new PrimitivePathIterator(this, at);
	}
	
}
