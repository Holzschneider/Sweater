package de.dualuse.swt.graphics;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.Random;


class PrimitivePathIterator implements PathIterator {
	final float[] vertices;
	final int n;
	final AffineTransform at;
	final float[][] m;
	final int type;
	
	final public static int LINES = 2;
	final public static int TRIANGLES = 3;
	final public static int QUADS = 4;
	
	final public static int LINE_STRIP= 10000-1;
	final public static int LINE_LOOP = 10000;
	
	Random r = new Random(0);
	public PrimitivePathIterator(float vertices[], int count, int type, AffineTransform at, float[][] m) {
		this.vertices = vertices;
		this.n = count+(type>2?count/type:0)+(type==LINE_LOOP?1:0);
		
		this.at = at;
		this.m = m;
		this.type = type;
	}
	
	public int getWindingRule() { return PathIterator.WIND_NON_ZERO; }
	
	private int i = 0;
	
	public boolean isDone() { return i>=n; }
	public void next() { i++; }

	private float lx = 0, ly=0, lz=0, lw = 0;
	public int currentSegment(float[] coords) {
		int mode = i%(this.type+(type>2?1:0));
		
		int seg = mode==0?SEG_MOVETO: SEG_LINETO;

		int o = i*3;
		switch (type) {
		case TRIANGLES: o = (i/4*3+i%3)*3;break;
		case QUADS: o = (i/5*4+i%4)*3;break;
		case LINE_LOOP: o = i%(n-1); break;
		}
		
		float vecx = vertices[o++];
		float vecy = vertices[o++];
		float vecz = vertices[o++];
		float vecw = 1;

		float x = m[0][0] * vecx + m[0][1] * vecy + m[0][2] * vecz + m[0][3] * vecw;
		float y = m[1][0] * vecx + m[1][1] * vecy + m[1][2] * vecz + m[1][3] * vecw;
		float z = m[2][0] * vecx + m[2][1] * vecy + m[2][2] * vecz + m[2][3] * vecw;
		float w = m[3][0] * vecx + m[3][1] * vecy + m[3][2] * vecz + m[3][3] * vecw;	
		
		if (lz>-1 && z<-1 || lz<-1 && z>-1) {
			float dx = x-lx, dy = y-ly, dz = z-lz, dw = w-lw;
			float t = pdIntersection(0, 0, -1, 0, 0, -1, lx, ly, lz, dx, dy, dz);
			
			x = lx+dx*t;
			y = ly+dy*t;
			z = lz+dz*t;
			w = lw+dw*t;
			
			i--;
		}
		
		
		final float ooW = 1f/w;
		float x_ = x*ooW;
		float y_ = y*ooW;
		
		coords[0] = x_;
		coords[1] = y_;
		
		if (at!=null)
			at.transform(coords, 0, coords, 0, 1);
		
		if (z<-1.001 || lz<-1.001)
			seg = SEG_MOVETO;

		lx = x;
		ly = y;
		lz = z;
		lw = w;
		
		return seg;
	}

	float floatcoords[] = new float[6];
	
	public int currentSegment(double[] coords) {
		int type = this.currentSegment(floatcoords);
		
		for (int i=0;i<6;i++)
			coords[i] = floatcoords[i];
		
		return type;
	}

	static public float pdIntersection( 
    		final float px, final float py, final float pz, final float nx, final float ny, final float nz,
    		final float ax, final float ay, final float az, final float dx, final float dy, final float dz
		)
	{
		final float d = -(px*nx+py*ny+pz*nz);
		
		float t = (-d-(ax*nx+ay*ny+az*nz)) / (dx*nx+dy*ny + dz*nz);  
		
		return t;
	}

}

