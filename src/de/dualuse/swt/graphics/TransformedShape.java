package de.dualuse.swt.graphics;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class TransformedShape implements Shape {
	final Shape s;
	final Viewport v;
	final float[][] m;

	public TransformedShape(Viewport v, float[][] t, Shape s) {
		this.v = v;
		this.m = t;
		this.s = s;
	}

	public Rectangle getBounds() { return getBounds2D().getBounds(); }
	public Rectangle2D getBounds2D() { throw new RuntimeException("Unsupported"); }

	public boolean contains(double x, double y) { return true; }
	public boolean contains(Point2D p) { return true; }
	public boolean intersects(double x, double y, double w, double h) { return true; }
	public boolean intersects(Rectangle2D r) { return true; }
	public boolean contains(double x, double y, double w, double h) { return true; }
	public boolean contains(Rectangle2D r) { return true; }
	
	public PathIterator getPathIterator(AffineTransform at) {
		return new FlatteningPathIterator(new TransformedPathIterator(s.getPathIterator(null), at, v, m),.2,14);
	}

	public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
		return new TransformedPathIterator(s.getPathIterator(null, flatness), at, v, m);
	}
}