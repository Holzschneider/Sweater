package de.dualuse.swt.graphics;


import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Arrays;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Transform;

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

class TransformedShape implements Shape {
	final Shape s;
	final float[][] m;

	public TransformedShape(float[][] t, Shape s) {
		this.s = s;
		this.m = t;
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
		return new FlatteningPathIterator(new TransformedPathIterator(s.getPathIterator(null), at, m),.2,14);
	}

	public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
		return new TransformedPathIterator(s.getPathIterator(null, flatness), at, m);
	}
}


public class RC {
	public final GC gc;
	public final Device device;
	private Transform s,t;
	
	public RC(GC gc) {
		this.gc = gc;
		this.device = gc.getDevice();
		this.t = new Transform(device);
		this.s = new Transform(device);
		
		identity(modelViewProjection);
	}
	
	
	public void dispose() {
		t.dispose();
		s.dispose();
	}
	
	private float[][] modelViewProjection = new float[4][4];
	
	
	private ArrayDeque<float[][]> pushed = new ArrayDeque<float[][]>();
	private ArrayDeque<float[][]> free = new ArrayDeque<float[][]>();
	
	public void pushTransform() {
		float[][] m = free.poll();
		
		if (m==null)
			m = new float[4][4];

		copy(modelViewProjection, m);
		
		pushed.push(m);
	}
	
	
	public void popTransform() {
		float[][] m = pushed.poll();
		copy(m, modelViewProjection);
		free.push(m);
	}


	public boolean isVisible(double px, double py) {
		return isVisible(px, py, 0);
	}

	public boolean isVisible(double px, double py, double pz) {
		final float ovecx=(float)px,ovecy=(float)py,ovecz=(float)pz,ovecw=1;

		float[][] m = modelViewProjection;
		
		final float z = (m[2][0] * ovecx + m[2][1] * ovecy + m[2][2] * ovecz + m[2][3] * ovecw);
		if (z<-1) return false;
		final float ooow = 1f/(m[3][0] * ovecx + m[3][1] * ovecy + m[3][2] * ovecz + m[3][3] * ovecw);
		final float ox = (m[0][0] * ovecx + m[0][1] * ovecy + m[0][2] * ovecz + m[0][3] * ovecw)*ooow;
		final float oy = (m[1][0] * ovecx + m[1][1] * ovecy + m[1][2] * ovecz + m[1][3] * ovecw)*ooow;
		
		return gc.getClipping().contains((int)ox,(int)oy);
	}
	
	
	public float originX() { return modelViewProjection[0][3]/modelViewProjection[3][3]; };
	public float originY() { return modelViewProjection[1][3]/modelViewProjection[3][3]; };
	public float originZ() { return modelViewProjection[2][3]/modelViewProjection[3][3]; };

	public boolean isFrontFacing() {
		
		final float ovecx=0,ovecy=0,ovecz=0,ovecw=1;
		final float nvecx = 1, nvecy=0, nvecz=0, nvecw=1;
		final float mvecx = 0, mvecy=1, mvecz=0, mvecw=1;
		
		final float[][] m = modelViewProjection;
		final float m00 = m[0][0], m01 = m[0][1], m02 = m[0][2], m03 = m[0][3];
		final float m10 = m[1][0], m11 = m[1][1], m12 = m[1][2], m13 = m[1][3];
//		final float m20 = m[2][0], m21 = m[2][1], m22 = m[2][2], m23 = m[2][3];
		final float m30 = m[3][0], m31 = m[3][1], m32 = m[3][2], m33 = m[3][3];
		
		final float ooow = 1f/(m30 * ovecx + m31 * ovecy + m32 * ovecz + m33 * ovecw);
		final float ox = (m00 * ovecx + m01 * ovecy + m02 * ovecz + m03 * ovecw)*ooow;
		final float oy = (m10 * ovecx + m11 * ovecy + m12 * ovecz + m13 * ovecw)*ooow;
//		final float oz = (m20 * ovecx + m21 * ovecy + m22 * ovecz + m23 * ovecw)*ooow;
//		final float ow = (m30 * ovecx + m31 * ovecy + m32 * ovecz + m33 * ovecw)*ooow;

		final float noow = 1f/(m30 * nvecx + m31 * nvecy + m32 * nvecz + m33 * nvecw);
		final float nx = (m00 * nvecx + m01 * nvecy + m02 * nvecz + m03 * nvecw)*noow;
		final float ny = (m10 * nvecx + m11 * nvecy + m12 * nvecz + m13 * nvecw)*noow;
//		final float nz = (m20 * nvecx + m21 * nvecy + m22 * nvecz + m23 * nvecw)*noow;
//		final float nw = (m30 * nvecx + m31 * nvecy + m32 * nvecz + m33 * nvecw)*noow;

		final float moow = 1f/(m30 * mvecx + m31 * mvecy + m32 * mvecz + m33 * mvecw);
		final float mx = (m00 * mvecx + m01 * mvecy + m02 * mvecz + m03 * mvecw)*moow;
		final float my = (m10 * mvecx + m11 * mvecy + m12 * mvecz + m13 * mvecw)*moow;
//		final float mz = (m20 * mvecx + m21 * mvecy + m22 * mvecz + m23 * mvecw)*moow;
//		final float mw = (m30 * mvecx + m31 * mvecy + m32 * mvecz + m33 * mvecw)*moow;

		final float ax = nx-ox, ay = ny-oy;
		final float bx = mx-ox, by = my-oy;
		final float cross = (ax*by)-(ay*bx); 
		
		return cross<0;
	}

	
	private Transform approximateTransform(float x, float y, Transform t) {

		float[] vp = { x,y,0,1 }, vx = {x+1,y,0,1}, vy = {x,y+1,0,1};
		
		project(modelViewProjection, vp);
		project(modelViewProjection, vx);
		project(modelViewProjection, vy);
		
		float dx = vx[0]-vp[0], dy = vx[1]-vp[1];
		float cx = vy[0]-vp[0], cy = vy[1]-vp[1];
		
		t.setElements(dx,dy,cx,cy, vp[0], vp[1] );
		return t;
	}

	static private float[][] createMatrixWithTransform(Transform at, float[][] m) {
		identity(m);
		
		float[] elements = {0,0,0,0,0,0};
		at.getElements(elements);
		
		m[0][0] = elements[0];
		m[1][0] = elements[1];
		m[0][1] = elements[2];
		m[1][1] = elements[3];
		
		m[0][3] = elements[4];
		m[1][3] = elements[5];
		
		return m;
	}
	
	static public float[][] createMatrixWithViewport(int x, int y, int width, int height) {
		float m[][] = new float[4][4];
		
		identity(m);
		
		concat(m, 
				1,0,0,x,
				0,1,0,y,
				0,0,1,0,
				0,0,0,1);

		concat(m,
				width,0,0,0,
				0,height,0,0,
				0,0,1,0,
				0,0,0,1	
					);	

		concat(m,
				0.5f,0,0,0,
				0,-0.5f,0,0,
				0,0,1,0,
				0,0,0,1	
					);	

		concat(m, 
				1,0,0,+1,
				0,1,0,-1,
				0,0,1,0,
				0,0,0,1);

		
		
		return m;
	}
	
	static public float[][] createMatrixWithFrustum( float left, float right, float bottom, float top, float nearVal, float farVal ) {
		float m[][] = new float[4][4];

		float A = (right + left)/(right - left);
		float B = (top + bottom)/(top - bottom);
		float C = -(farVal + nearVal)/(farVal - nearVal);
		float D = -(2 * farVal * nearVal) /( farVal -nearVal);
		
		m[0][0] = (2*nearVal)/(right-left);	m[0][1] = 0; 							m[0][2] = A; m[0][3] = 0;
		m[1][0] = 0; 							m[1][1] = (2*nearVal)/(top-bottom); 	m[1][2] = B; m[1][3] = 0;
		m[2][0] = 0; 							m[2][1] = 0; 							m[2][2] = C; m[2][3] = D;
		m[3][0] = 0; 							m[3][1] = 0; 							m[3][2] = -1; m[3][3] = 0;
		
		return m;
	}
	
	public void translate(double tx, double ty, double tz) {
		concat( modelViewProjection,
				1,0,0,(float)tx,
				0,1,0,(float)ty,
				0,0,1,(float)tz,
				0,0,0,1);
	}
	
	public void rotate(double ax, double ay, double az, double theta) {
		
		final float s = (float) sin(theta), c = (float) cos(theta), t = 1-c, l = (float) sqrt(ax*ax+ay*ay+az*az);
		final float x = (float) (ax/l), y = (float) (ay/l), z= (float) (az/l);
		final float xz = x*z, xy = x*y, yz = y*z, xx=x*x, yy=y*y, zz=z*z;
		
		concat(modelViewProjection,
				t*xx+c  , t*xy-s*z, t*xz+s*y, 0,
				t*xy+s*z, t*yy+c  , t*yz-s*x, 0,
				t*xz-s*y, t*yz+s*x, t*zz+c  , 0,
				       0,        0,      0,   1);

	}
	
	public void scale(double sx, double sy, double sz) {
		concat(modelViewProjection,
				(float)sx,0,0,0,
				0,(float)sy,0,0,
				0,0,(float)sz,0,
				0,0,0,1	
					);	
	}


	
	public void viewport(int x, int y, int width, int height) {
		concat(modelViewProjection,createMatrixWithViewport(x, y, width, height));
	}
	
	public void frustum(double left, double right, double bottom, double top, double nearVal, double farVal ) {
		concat(modelViewProjection,
				createMatrixWithFrustum(
						(float)left, (float)right, 
						(float)bottom, (float)top, 
						(float)nearVal, (float)farVal
				)
		);
	}
	

	public void translate(double tx, double ty) { translate(tx, ty, 0); }
	public void rotate(double theta) { rotate(0,0,1,theta); }
	public void rotate(double theta, double x, double y) { translate(x,y);rotate(theta);translate(-x,-y); }
	public void scale(double sx, double sy) { scale(sx,sy,1); }
	
//	public void shear(double shx, double shy) { modelviewprojection.mul(createMatrixWithTransform(AffineTransform.getRotateInstance(shx, shy))); }
//	public void transform(AffineTransform Tx) { modelviewprojection.mul(createMatrixWithTransform(Tx)); }
//	public void transform(ProjectiveTransform Tx) { modelviewprojection.mul(Tx.getMatrix(new Matrix4f())); }
//	public void setTransform(AffineTransform Tx) { modelviewprojection.set(createMatrixWithTransform(Tx)); }
//	public void setTransform(ProjectiveTransform Tx) { modelviewprojection.set(Tx.getMatrix()); }
//	public ProjectiveTransform getTransform() { return new ProjectiveTransform(modelviewprojection); 

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	public void draw(Shape s) {
		PathShape ps = new PathShape(device, new TransformedShape(modelViewProjection, s));
		gc.drawPath(ps);
		ps.dispose();
	}
	
	public void fill(Shape s) {
		PathShape ps = new PathShape(device, new TransformedShape(modelViewProjection, s));
		gc.fillPath(ps);
		ps.dispose();
		
	}
	

	final static float EPSILON = 0.005f; 
	public boolean drawImage(Image im, int x, int y) {
		ImageData id = im.getImageData();
		int W = id.width, H = id.height;

		float[] at = new float[6], bt = new float[6];
		
		gc.getTransform(t);
		t.getElements(bt);
		
		drawImageTiled(im, modelViewProjection, at, bt, 0, 0, W, H);
		
		return false;
	}

	
	public float triangleArea(float ax, float ay, float bx, float by, float cx, float cy) {
		float ux = bx-ax, uy = by-ay;
		float vx = cx-ax, vy = cy-ay;
		float area = (ux*vy-vx*uy)/2;  
		
		return (area>0?area:-area);
	}
	
	static public boolean triangleContains(float px, float py, float ax, float ay, float bx, float by, float cx, float cy) {
		final float v0x = cx-ax, v0y = cy-ay;
		final float v1x = bx-ax, v1y = by-ay;
		final float v2x = px-ax, v2y = py-ay;
		
		final float dot00 = v0x*v0x+v0y*v0y;
		final float dot01 = v0x*v1x+v0y*v1y;
		final float dot02 = v0x*v2x+v0y*v2y;
		final float dot11 = v1x*v1x+v1y*v1y;
		final float dot12 = v1x*v2x+v1y*v2y;
		
		final float invDenom = 1 / (dot00*dot11-dot01*dot01);
		final float u = (dot11*dot02-dot01*dot12)*invDenom;
		final float v = (dot00*dot12-dot01*dot02)*invDenom;
		
		return (u>0) && (v>0) && (u+v<1);
	}


	
	
	int counter = 0;
	
	int recursions = 0;
	float PERSPECTIVE_RIM = 0, MAX_PERSPECTIVE_DEVIATION = 2, MAX_PERSPECTIVE_ERROR = MAX_PERSPECTIVE_DEVIATION*MAX_PERSPECTIVE_DEVIATION;
	private void drawImageTiled(Image im, float[][] m, float[] at, float[] bt, int x1, int y1, int x2, int y2) {
		
		if (x1==x2 || y1==y2)
			return;
		
		float ax = x1, ay = y1, bx = x2, by = y1, cx = x2, cy = y2, dx = x1, dy = y2;
		
		float aoow = 1f/(m[3][0]* ax+ m[3][1]*ay + m[3][2] * 0 + m[3][3] *1 );
		float ax_ = (m[0][0]*ax+ m[0][1]*ay+m[0][2]*0+m[0][3]*1)*aoow;
		float ay_ = (m[1][0]*ax+ m[1][1]*ay+m[1][2]*0+m[1][3]*1)*aoow;
		
		float boow = 1f/(m[3][0]* bx+ m[3][1]*by + m[3][2] * 0 + m[3][3] *1 );
		float bx_ = (m[0][0]*bx+ m[0][1]*by+m[0][2]*0+m[0][3]*1)*boow;
		float by_ = (m[1][0]*bx+ m[1][1]*by+m[1][2]*0+m[1][3]*1)*boow;
		
		float coow = 1f/(m[3][0]* cx+ m[3][1]*cy + m[3][2] * 0 + m[3][3] *1 );
		float cx_ = (m[0][0]*cx+ m[0][1]*cy+m[0][2]*0+m[0][3]*1)*coow;
		float cy_ = (m[1][0]*cx+ m[1][1]*cy+m[1][2]*0+m[1][3]*1)*coow;
		
		float doow = 1f/(m[3][0]* dx+ m[3][1]*dy + m[3][2] * 0 + m[3][3] *1 );
		float dx_ = (m[0][0]*dx+ m[0][1]*dy+m[0][2]*0+m[0][3]*1)*doow;
		float dy_ = (m[1][0]*dx+ m[1][1]*dy+m[1][2]*0+m[1][3]*1)*doow;
		float tileW = x2-x1, tileH = y2-y1, error = 0;

		float ax__ = dx_+bx_-cx_, ay__ = dy_+by_-cy_;
		float bx__ = ax_+cx_-dx_, by__ = ay_+cy_-dy_;
		float cx__ = bx_+dx_-ax_, cy__ = by_+dy_-ay_;
		float dx__ = ax_+cx_-bx_, dy__ = ay_+cy_-by_;
		
		if (triangleContains(cx_, cy_, dx_, dy_, bx_, by_, cx__, cy__)) { //paDAB > all (A ist Anker)
			float deltaX = cx__-cx_, deltaY = cy__-cy_;
			error = deltaX*deltaX + deltaY*deltaY;
			
			set(at, (bx_-ax_)/tileW, (by_-ay_)/tileW,   (dx_-ax_)/tileH, (dy_-ay_)/tileH,    ax_, ay_);
			
		} else
		if (triangleContains(dx_,dy_, ax_,ay_, cx_,cy_, dx__,dy__)) { //paABC > all (B ist Anker)
			float deltaX = dx__-dx_, deltaY = dy__-dy_;
			error = deltaX*deltaX + deltaY*deltaY;
	
			set(at, (bx_-ax_)/tileW, (by_-ay_)/tileW,    (dx__-ax_)/tileH, (dy__-ay_)/tileH,    ax_, ay_);
		} else
		if (triangleContains(ax_,ay_, dx_,dy_,bx_,by_,ax__,ay__)) { //paBCD > all (C ist Anker)
			float deltaX = ax__-ax_, deltaY = ay__-ay_;
			error = deltaX*deltaX + deltaY*deltaY;
			
			set(at, (bx_-ax__)/tileW, (by_-ay__)/tileW,    (dx_-ax__)/tileH, (dy_-ay__)/tileH,    ax__, ay__);
		} else { //paCDA > all (D ist Anker)
			float deltaX = bx__-bx_, deltaY = by__-by_;
			error = deltaX*deltaX + deltaY*deltaY;
	
			set(at, (bx__-ax_)/tileW, (by__-ay_)/tileW,    (dx_-ax_)/tileH, (dy_-ay_)/tileH,    ax_, ay_);
		}
 
		
		if (error<MAX_PERSPECTIVE_ERROR) {
			s.setElements(at[0],at[1],at[2],at[3],at[4],at[5]);

			gc.getTransform(t);
			t.multiply(s);
			gc.setTransform(t);
			
			gc.drawImage(im, (int)x1, (int)y1, (int)x2, (int)y2, 0, 0, (int)(x2-x1), (int)(y2-y1)); 

//			Stroke s = g.getStroke();
//			g.setStroke(new BasicStroke(0.1f));
//			g.draw(new Rectangle2D.Double(0,0,(int)(x2-x1), (int)(y2-y1)));
//			g.setStroke(s);
			
			t.setElements(bt[0],bt[1],bt[2],bt[3],bt[4],bt[5]);
			gc.setTransform(t);
		} else {
			recursions++;
			int mx = (int)((x1+x2)/2), my = (int)((y1+y2)/2);
			drawImageTiled(im, m, at, bt, x1, y1, mx, my );
			drawImageTiled(im, m, at, bt, mx, y1, x2, my);
			drawImageTiled(im, m, at, bt, mx, my, x2, y2 );
			drawImageTiled(im, m, at, bt, x1, my, mx, y2 );
			recursions--;
		}
		
	}
	
	
	public void drawString(String str, int x, int y) {
		gc.getTransform(t);
		t.multiply(approximateTransform(x, y, s));

		gc.getTransform(s);
		gc.setTransform(t);
		
		gc.drawString(str, x, y);
		
		gc.setTransform(s);
	}
	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static interface ScreenCoordinate<T> { T define(float x, float y); }
	public<T> T project(double x, double y, double z, ScreenCoordinate<T> sc) {
//		return project( x,y,z, (vx,vy,vz) -> sc.define(vx, vy));
		return project(x, y, z, new ViewportCoordinate<T>() {
			public T define(float x, float y, float z) {
				return sc.define(x, y);
			}
		});
	}
	
	public static interface ViewportCoordinate<T> { T define(float x, float y, float z); }
	public<T> T project(double x, double y, double z, ViewportCoordinate<T> sc) {
		float[][] m = modelViewProjection;
		final float m00 = m[0][0], m01 = m[0][1], m02 = m[0][2], m03 = m[0][3];
		final float m10 = m[1][0], m11 = m[1][1], m12 = m[1][2], m13 = m[1][3];
		final float m20 = m[2][0], m21 = m[2][1], m22 = m[2][2], m23 = m[2][3];
		final float m30 = m[3][0], m31 = m[3][1], m32 = m[3][2], m33 = m[3][3];

		final double vw = m30*x+m31*y+m32*z+m33;
		final double vx = (m00*x+m01*y+m02*z+m03)/vw; 
		final double vy = (m10*x+m11*y+m12*z+m13)/vw; 
		final double vz = (m20*x+m21*y+m22*z+m23)/vw; 
		
		return sc.define((float)vx, (float)vy, (float)vz);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void set(float[] m, float ... v) {
		for (int i=0,I=v.length;i<I;i++)
			m[i]=v[i];
	}
	
	private static void copy(float[][] m, float[][] n) {
		for (int row=0;row<m.length;row++)
			for (int col=0;col<m[row].length;col++)
				n[row][col] = m[row][col];
	}
	
	private static void transform( float[][] m, float[] v ) {
		float x = v[0], y = v[1], z = v[2], w = v[3]; //v.length>3?v[3]:1;

		final float m00 = m[0][0], m01 = m[0][1], m02 = m[0][2], m03 = m[0][3];
		final float m10 = m[1][0], m11 = m[1][1], m12 = m[1][2], m13 = m[1][3];
		final float m20 = m[2][0], m21 = m[2][1], m22 = m[2][2], m23 = m[2][3];
		final float m30 = m[3][0], m31 = m[3][1], m32 = m[3][2], m33 = m[3][3];

		v[0] = m00*x+m01*y+m02*z+m03*w; 
		v[1] = m10*x+m11*y+m12*z+m13*w; 
		v[2] = m20*x+m21*y+m22*z+m23*w; 
		v[3] = m30*x+m31*y+m32*z+m33*w;
	}

	private static void project( float[][] m, float[] v ) {
		transform(m,v);
		scale(v,1/v[3]);
	}
	
	private static void scale( float[] v, double s ) {
		for (int i=0,I=v.length;i<I;i++)
			v[i] *= s;
	}
	
	private static void identity( float[][] m ) {
		for (int r=0,R=m.length;r<R;r++)
			for (int c=0,C=m[r].length;c<C;c++)
				m[r][c] = r==c?1:0;
	}
	
	private static void concat( float[][] m, float[][] n) {
		final float n00 = n[0][0], n01 = n[0][1], n02 = n[0][2], n03 = n[0][3];
		final float n10 = n[1][0], n11 = n[1][1], n12 = n[1][2], n13 = n[1][3];
		final float n20 = n[2][0], n21 = n[2][1], n22 = n[2][2], n23 = n[2][3];
		final float n30 = n[3][0], n31 = n[3][1], n32 = n[3][2], n33 = n[3][3];
		concat(m, n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
	}
	
	private static void concat( float[][] m, 
			float n00, float n01, float n02, float n03,
			float n10, float n11, float n12, float n13,
			float n20, float n21, float n22, float n23,
			float n30, float n31, float n32, float n33
			) {
		
		final float m00 = m[0][0], m01 = m[0][1], m02 = m[0][2], m03 = m[0][3];
		final float m10 = m[1][0], m11 = m[1][1], m12 = m[1][2], m13 = m[1][3];
		final float m20 = m[2][0], m21 = m[2][1], m22 = m[2][2], m23 = m[2][3];
		final float m30 = m[3][0], m31 = m[3][1], m32 = m[3][2], m33 = m[3][3];

		m[0][0] = m00*n00+m01*n10+m02*n20+m03*n30;
		m[0][1] = m00*n01+m01*n11+m02*n21+m03*n31;
		m[0][2] = m00*n02+m01*n12+m02*n22+m03*n32;
		m[0][3] = m00*n03+m01*n13+m02*n23+m03*n33;
		
		m[1][0] = m10*n00+m11*n10+m12*n20+m13*n30;
		m[1][1] = m10*n01+m11*n11+m12*n21+m13*n31;
		m[1][2] = m10*n02+m11*n12+m12*n22+m13*n32;
		m[1][3] = m10*n03+m11*n13+m12*n23+m13*n33;
		
		m[2][0] = m20*n00+m21*n10+m22*n20+m23*n30;
		m[2][1] = m20*n01+m21*n11+m22*n21+m23*n31;
		m[2][2] = m20*n02+m21*n12+m22*n22+m23*n32;
		m[2][3] = m20*n03+m21*n13+m22*n23+m23*n33;
		
		m[3][0] = m30*n00+m31*n10+m32*n20+m33*n30;
		m[3][1] = m30*n01+m31*n11+m32*n21+m33*n31;
		m[3][2] = m30*n02+m31*n12+m32*n22+m33*n32;
		m[3][3] = m30*n03+m31*n13+m32*n23+m33*n33;
	}
}
