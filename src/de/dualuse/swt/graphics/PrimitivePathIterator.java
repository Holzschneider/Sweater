package de.dualuse.swt.graphics;

import static java.lang.Math.*;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;


public class PrimitivePathIterator implements PathIterator {
	final AffineTransform at;
	final int n;
	final Primitive p;
	
	final public static int POINTS = 1;
	final public static int LINES = 2;
	final public static int TRIANGLES = 3;
	final public static int QUADS = 4;
	final public static int TRIANGLE_FAN = 5;
	
	final public static int LINE_STRIP= 10000-1;
	final public static int LINE_LOOP = 10000;
	
	//XXX acutally hand over Primitive here! in a private constructor
	public PrimitivePathIterator(Primitive p,  AffineTransform at ) {
		this.p = p;
		
		int n = 0, count = p.n/3;
		switch(p.type) {
		case POINTS: n = count*4; break;
		case LINES: n = count; break;
		case TRIANGLES:
		case QUADS: n = count+count/p.type; break;
		case LINE_STRIP: n = count; break;
		case LINE_LOOP: n = count+1; break;
		case TRIANGLE_FAN: n = (count-1)*4; break;
		}
		
		this.n = n;
		this.at = at;
	}
	
	public int getWindingRule() { return PathIterator.WIND_NON_ZERO; }
	private int i = 0;
	
	public boolean isDone() { return i>=n; }
	public void next() { i++; }

	public static boolean debug = false;
	
	private float lx = 0, ly=0, lz=0, lw = -1;
	private float lx_ = 0, ly_=0, lz_=0;
	
			
	public int currentSegment(float[] coords) {
		float sx = 0, sy = 0;
		int offset=0, seg=0;
		
		switch (p.type) {
		case POINTS: 
			offset = (i/4)*3;
			switch (i%4) {
				case 0: sx=sy=-p.pointSize; seg = SEG_MOVETO; break;
				case 1: sy=sx=p.pointSize;seg = SEG_LINETO; break;
				case 2: sy=-(sx=p.pointSize);seg = SEG_MOVETO; break;
				case 3: sx=-(sy=p.pointSize);seg = SEG_LINETO; break;
			}
		break;
		case LINES:
			offset = i*3;
			seg = i%2==0?SEG_MOVETO:SEG_LINETO;
		break;
		case TRIANGLES: 
			offset = (i/4*3+i%3)*3;
			seg = i%4==0?SEG_MOVETO:SEG_LINETO;
		break;
		case QUADS: 
			offset = (i/5*4+i%4)*3;
			seg = i%5==0?SEG_MOVETO:SEG_LINETO;
			break;
		case LINE_STRIP:
			offset = i*3;
			seg = i==0?SEG_MOVETO:SEG_LINETO;
			break;
		case LINE_LOOP: 
			offset = (i%(n-1))*3;
			seg = i==0?SEG_MOVETO:SEG_LINETO;
		case TRIANGLE_FAN:
			int j = 0;
			switch (i%4) {
			case 0: j = 0; seg = SEG_MOVETO; break;
			case 1: j = i/4; seg = SEG_LINETO; break;
			case 2: j = i/4+1; seg = SEG_LINETO; break;
			case 3: j = 0; seg = SEG_CLOSE; break;
			}
			offset = j*3;
		break;
		}

//		if (seg == SEG_CLOSE)
//			lx=ly=lz=lw=0;
		
		float vecx = p.vertices[offset+0];
		float vecy = p.vertices[offset+1];
		float vecz = p.vertices[offset+2];
		float vecw = 1;

		//Transform Model Coordinates to Clip Coordinates: 
		float x = p.m[0][0] * vecx + p.m[0][1] * vecy + p.m[0][2] * vecz + p.m[0][3] * vecw;
		float y = p.m[1][0] * vecx + p.m[1][1] * vecy + p.m[1][2] * vecz + p.m[1][3] * vecw;
		float z = p.m[2][0] * vecx + p.m[2][1] * vecy + p.m[2][2] * vecz + p.m[2][3] * vecw;
		float w = p.m[3][0] * vecx + p.m[3][1] * vecy + p.m[3][2] * vecz + p.m[3][3] * vecw;	

		final float x_ = x/w, y_ = y/w, z_ = z/w;
		
		
		boolean clipZ = z<-w || z>w, clipX = x<-w|| x>w, clipY = y<-w || y>w;
		boolean clip = clipZ|((clipX|clipY)&p.v.viewportClipping);
		
		if (p.type == POINTS && clip) {
			i++; //also skip next point
			return SEG_MOVETO;
		}
		
//		if (lz>-1 && z<-1 || lz<-1 && z>-1) {

//		if ((lz>-lw && z<-w || lz<-lw && z>-w) && type!=POINTS && type!=TRIANGLE_FAN) {
//			float dx = x-lx, dy = y-ly, dz = z-lz, dw = w-lw;
//			float t = pdIntersection(0, 0, -1, 0, 0, -1, lx, ly, lz, dx, dy, dz);
//			
//			x = lx+dx*t;
//			y = ly+dy*t;
//			z = lz+dz*t;
//			w = lw+dw*t;
//			
//			i--;
//		}
		
		coords[0] = x_;
		coords[1] = y_;
		p.v.transform(coords, 0, 1);
		coords[0]+=sx;
		coords[1]+=sy;
		
		
		if (at!=null)
			at.transform(coords, 0, coords, 0, 1);
		
//		if (z<-1.001 || lz<-1.001)
//			seg = SEG_MOVETO;

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

