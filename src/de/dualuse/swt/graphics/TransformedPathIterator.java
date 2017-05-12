package de.dualuse.swt.graphics;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

class TransformedPathIterator implements PathIterator {

	final PathIterator w;
	final AffineTransform at;
	final float[][] m;
	final float[] floatCoords = { 0,0,0,0,0,0};

	public TransformedPathIterator(PathIterator w, AffineTransform at, float[][] m) {
		this.w=w;
		this.at=at;
		this.m=m;
	}
	
	public int getWindingRule() { return w.getWindingRule(); }
	public boolean isDone() { return w.isDone(); }
	public void next() { w.next(); }

	public int currentSegment(float[] coords) {
		int type = w.currentSegment(coords);
		
		int size = 0;
		switch (type) {
		case SEG_CLOSE: size=0; break;
		case SEG_LINETO: 
		case SEG_MOVETO: size=1; break;
		case SEG_QUADTO: size=2; break;
		case SEG_CUBICTO: size=3; break;
		}
		
		for (int i=0,l=size*2;i<l;i+=2) {
			float vecx = coords[i+0];
			float vecy = coords[i+1];
			float vecz = 0;
			float vecw = 1;
	
			float x = m[0][0] * vecx + m[0][1] * vecy + m[0][2] * vecz + m[0][3] * vecw;
			float y = m[1][0] * vecx + m[1][1] * vecy + m[1][2] * vecz + m[1][3] * vecw;
//			float z = m[2][0] * vecx + m[2][1] * vecy + m[2][2] * vecz + m[2][3] * vecw;
			float w = m[3][0] * vecx + m[3][1] * vecy + m[3][2] * vecz + m[3][3] * vecw;

			final float ooW = 1f/w;
			x *= ooW;
			y *= ooW;
//			z *= ooW;
//			v.w *= ooW;
			
			coords[i+0] = x;
			coords[i+1] = y;
		}
		
		if (at!=null)
			at.transform(coords, 0, coords, 0, size);
		
		return type;
	}
	
	public int currentSegment(double [] coords) {
		for (int i=0;i<6;i++)
			floatCoords[i] = (float) coords[i];
		
		int type = currentSegment(floatCoords);
		
		for (int i=0;i<6;i++)
			coords[i] = floatCoords[i];
		
		return type;
	}
}