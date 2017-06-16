package de.dualuse.swt.graphics;


import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static org.eclipse.swt.SWT.DRAW_DELIMITER;
import static org.eclipse.swt.SWT.DRAW_TAB;
import static org.eclipse.swt.SWT.DRAW_TRANSPARENT;
import static org.eclipse.swt.SWT.FILL_WINDING;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;

public class RC implements Closeable {

	public final GC gc;
	public final Device device;
	private Transform s, t;
	private boolean disposeOnDispose = false;

	private float[][] modelViewProjection = new float[4][4];
	
//==[ Constructor ]=================================================================================
	
	public RC(Image im) {
		this(new GC(im));
		disposeOnDispose = true;
	}
	
	public RC(GC gc) {
		
		this.gc = gc;
		this.device = gc.getDevice();
		
		this.t = new Transform(device);
		this.s = new Transform(device);

		initState();
		identity(modelViewProjection);
		
	}
	
//==[ Free Resources ]==============================================================================
	
	public void dispose() {
		t.dispose();
		s.dispose();
		
		if (foregroundCreated!=null) {
			foregroundCreated.dispose();
			foregroundCreated = null;
		}
		
		if (backgroundCreated!=null) {
			backgroundCreated.dispose();
			backgroundCreated = null;
		}
		
		if (disposeOnDispose)
			gc.dispose();
	}
	
	@Override
	public void close() {
		dispose();
	}
	
//==[ Matrix Stack ]================================================================================
	
	private ArrayDeque<float[][]> pushed = new ArrayDeque<float[][]>();
	private ArrayDeque<float[][]> free = new ArrayDeque<float[][]>();
	
	public RC pushTransform() {
		float[][] m = free.poll();
		
		if (m==null)
			m = new float[4][4];

		copy(modelViewProjection, m);
		
		pushed.push(m);
		return this;
	}
	
	
	public RC popTransform() {
		float[][] m = pushed.poll();
		copy(m, modelViewProjection);
		free.push(m);
		return this;
	}
	
	
	public RC loadIdentity() {
		identity(modelViewProjection);
		return this;
	}
	
	public static interface ModelViewProjection<T> {
		T define(
				double m00, double m01, double m02, double m03,
				double m10, double m11, double m12, double m13,
				double m20, double m21, double m22, double m23,
				double m30, double m31, double m32, double m33
				);
	}
	
	public<T> T getTransform(ModelViewProjection<T> mvp) {
		final float[][] m = modelViewProjection;
		return mvp.define( 
				m[0][0], m[0][1], m[0][2], m[0][3], 
				m[1][0], m[1][1], m[1][2], m[1][3], 
				m[2][0], m[2][1], m[2][2], m[2][3], 
				m[3][0], m[3][1], m[3][2], m[3][3] 
			);
	}
	
//==================================================================================================

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

//	private static float[][] createMatrixWithTransform(Transform at, float[][] m) {
//		identity(m);
//		
//		float[] elements = {0,0,0,0,0,0};
//		at.getElements(elements);
//		
//		m[0][0] = elements[0];
//		m[1][0] = elements[1];
//		m[0][1] = elements[2];
//		m[1][1] = elements[3];
//		
//		m[0][3] = elements[4];
//		m[1][3] = elements[5];
//		
//		return m;
//	}
	
	static float[][] createMatrixWithViewport(int x, int y, int width, int height) {
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
	
	public static float[][] createMatrixWithFrustum( float left, float right, float bottom, float top, float nearVal, float farVal ) {
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

	
	public static interface MatrixValues {
		double element(int row, int col); 
	}
	
	public RC multMatrix( MatrixValues v ) {
		float[][] m = modelViewProjection;
		setToConcatenation(	
					m[0][0], m[1][0], m[2][0], m[3][0],				
					m[0][1], m[1][1], m[2][1], m[3][1],				
					m[0][2], m[1][2], m[2][2], m[3][2],				
					m[0][3], m[1][3], m[2][3], m[3][3],				
				
					(float)v.element(0,0), (float)v.element(1,0), (float)v.element(2,0), (float)v.element(3,0),				
					(float)v.element(0,1), (float)v.element(1,1), (float)v.element(2,1), (float)v.element(3,1),				
					(float)v.element(0,2), (float)v.element(1,2), (float)v.element(2,2), (float)v.element(3,2),				
					(float)v.element(0,3), (float)v.element(1,3), (float)v.element(2,3), (float)v.element(3,3)				
				);
		return this;
	}
	
	public RC multMatrix( double[][] n ) {
		float[][] m = modelViewProjection;
		setToConcatenation(	
					m[0][0], m[1][0], m[2][0], m[3][0],				
					m[0][1], m[1][1], m[2][1], m[3][1],				
					m[0][2], m[1][2], m[2][2], m[3][2],				
					m[0][3], m[1][3], m[2][3], m[3][3],				
				
					(float)n[0][0], (float)n[1][0], (float)n[2][0], (float)n[3][0],				
					(float)n[0][1], (float)n[1][1], (float)n[2][1], (float)n[3][1],				
					(float)n[0][2], (float)n[1][2], (float)n[2][2], (float)n[3][2],				
					(float)n[0][3], (float)n[1][3], (float)n[2][3], (float)n[3][3]				
				);
		return this;
	}
	
	/**
	 * multMatrix multiplies the current matrix with the one specified using m, and replaces the current matrix 
	 * with the product.
	 * 
	 * Caution! The array expects the matrix in column order!
	 * 
	 *  If the current matrix is C, and the coordinates to be transformed are, v = (v[0], v[1], v[2], v[3]).  
	 *  Then the current transformation is C X v, or
	 *          c[0]  c[4]  c[8]  c[12]     v[0]
	 *          c[1]  c[5]  c[9]  c[13]     v[1]
	 *          c[2]  c[6]  c[10] c[14]  X  v[2]
	 *          c[3]  c[7]  c[11] c[15]     v[3]
	 *          
	 *          
	 *  Calling glMultMatrix with an argument of m = m[0], m[1], ..., m[15] replaces the current transformation 
	 *  with (C X M) x v, or
	 *        c[0]  c[4]  c[8]  c[12]   m[0]  m[4]  m[8]  m[12]   v[0]
	 *        c[1]  c[5]  c[9]  c[13]   m[1]  m[5]  m[9]  m[13]   v[1]
	 *        c[2]  c[6]  c[10] c[14] X m[2]  m[6]  m[10] m[14] X v[2]
	 *        c[3]  c[7]  c[11] c[15]   m[3]  m[7]  m[11] m[15]   v[3]
	 *
	 * Where 'X' denotes matrix multiplication, and v is represented as a 4 X 1 matrix.
	 * @param n
	 */
	public RC multMatrix( double[] n ) {
		float[][] m = modelViewProjection;
		setToConcatenation(	
					m[0][0], m[1][0], m[2][0], m[3][0],				
					m[0][1], m[1][1], m[2][1], m[3][1],				
					m[0][2], m[1][2], m[2][2], m[3][2],				
					m[0][3], m[1][3], m[2][3], m[3][3],				
				
					(float)n[ 0], (float)n[ 1], (float)n[ 2], (float)n[ 3],
					(float)n[ 4], (float)n[ 5], (float)n[ 6], (float)n[ 7],
					(float)n[ 8], (float)n[ 9], (float)n[10], (float)n[11],
					(float)n[12], (float)n[13], (float)n[14], (float)n[15] );
		
		return this;
	}
	
	//XXX be super careful!! these matrices are transposed!
	private void setToConcatenation(	
			float m00, float m10, float m20, float m30, 
			float m01, float m11, float m21, float m31,
			float m02, float m12, float m22, float m32,
			float m03, float m13, float m23, float m33,
			
			float n00, float n10, float n20, float n30, 
			float n01, float n11, float n21, float n31,
			float n02, float n12, float n22, float n32,
			float n03, float n13, float n23, float n33
			) 
	{
		final float[][] m = modelViewProjection;

		m[0][0] = m00 * n00 + m01 * n10 + m02 * n20 + m03 * n30;
		m[0][1] = m00 * n01 + m01 * n11 + m02 * n21 + m03 * n31;
		m[0][2] = m00 * n02 + m01 * n12 + m02 * n22 + m03 * n32;
		m[0][3] = m00 * n03 + m01 * n13 + m02 * n23 + m03 * n33;

		m[1][0] = m10 * n00 + m11 * n10 + m12 * n20 + m13 * n30;
		m[1][1] = m10 * n01 + m11 * n11 + m12 * n21 + m13 * n31;
		m[1][2] = m10 * n02 + m11 * n12 + m12 * n22 + m13 * n32;
		m[1][3] = m10 * n03 + m11 * n13 + m12 * n23 + m13 * n33;
		
		m[2][0] = m20 * n00 + m21 * n10 + m22 * n20 + m23 * n30;
		m[2][1] = m20 * n01 + m21 * n11 + m22 * n21 + m23 * n31;
		m[2][2] = m20 * n02 + m21 * n12 + m22 * n22 + m23 * n32;
		m[2][3] = m20 * n03 + m21 * n13 + m22 * n23 + m23 * n33;
		
		m[3][0] = m30 * n00 + m31 * n10 + m32 * n20 + m33 * n30;
		m[3][1] = m30 * n01 + m31 * n11 + m32 * n21 + m33 * n31;
		m[3][2] = m30 * n02 + m31 * n12 + m32 * n22 + m33 * n32;
		m[3][3] = m30 * n03 + m31 * n13 + m32 * n23 + m33 * n33;
		
	}
	
	public RC translate(double tx, double ty, double tz) {
		concat( modelViewProjection,
				1,0,0,(float)tx,
				0,1,0,(float)ty,
				0,0,1,(float)tz,
				0,0,0,1);
		return this;
	}
	
	public RC quaternion(double qx, double qy, double qz, double qw) {
		float x = (float) qx, y = (float) qy, z = (float) qz, w = (float) qw;
		
		final float ww = w*w, xx = x*x, yy= y*y, zz = z*z;
		final float xy = x*x, xz = x*z, xw = x*w;
		final float yz = y*z, yw = y*w, zw = z*w; 
		
		concat(modelViewProjection,
				ww+xx-yy-zz, 2*xy-2*zw, 2*xz+2*yw, 0f,
				2*xy+2*zw, ww-xx+yy-zz, 2*yz-2*xw, 0f,						
				2*xz-2*yw, 2*yz+2*xw, ww-xx-yy+zz, 0f,
				0f, 0f, 0f, 	ww+xx+yy+zz);

		return this;
	}
	
	public RC rotate(double theta, double ax, double ay, double az) {
		final float s = (float) sin(theta), c = (float) cos(theta), t = 1-c, l = (float) sqrt(ax*ax+ay*ay+az*az);
		final float x = (float) (ax/l), y = (float) (ay/l), z= (float) (az/l);
		final float xz = x*z, xy = x*y, yz = y*z, xx=x*x, yy=y*y, zz=z*z;
		
		concat(modelViewProjection,
				t*xx+c  , t*xy-s*z, t*xz+s*y, 0,
				t*xy+s*z, t*yy+c  , t*yz-s*x, 0,
				t*xz-s*y, t*yz+s*x, t*zz+c  , 0,
				       0,        0,      0,   1);
		return this;
	}
	

	public RC scale(double s) { return scale(s,s,s); }
	
	public RC scale(double sx, double sy, double sz) {
		concat(modelViewProjection,
				(float)sx,0,0,0,
				0,(float)sy,0,0,
				0,0,(float)sz,0,
				0,0,0,1	
					);
		
		return this;
	}


	
	public RC viewport(int x, int y, int width, int height) {
		concat(modelViewProjection,createMatrixWithViewport(x, y, width, height));
		return this;
	}
	
	public RC camera(float fx, float fy, float cx, float cy) {
		concat(modelViewProjection,new float[][] {
					{ fx,  0, cx,  0 },
					{  0, fy, cy,  0 },
					{  0,  0,  0,  1 },
					{  0,  0,  1,  0 },
				});
		
		return this;
	}
	
	public RC ortho(double left, double right, double bottom, double top, double near, double far) {
		float tx = (float) (-(right+left)/(right-left));
		float ty = (float) (-(top+bottom)/(top-bottom));
		float tz = (float) (-(far+near)/(far-near));
		
		concat(modelViewProjection, 
				new float[][] {
					{ 2/(float)(left-right), 0,0, tx },
					{ 0, 2/(float)(top-bottom), 0,0, ty},
					{ 0, 0, 2/(float)(top-bottom), 0, tz},
					{ 0, 0, 0, 1} 
				});
		
		return this;
	}
	
//	public RC perspective(double fovy, double aspect, double near, double far) {
//		float f = (float)(1/tan(fovy/2)), a = (float) aspect;
//		float c = (float)((far+near)/(near-far));
//		float d = (float)(2*far*near/(near-far));
//		float[][] m;
//		concat(modelViewProjection, m = new float[][] {
//			{ f/a, 0, 0, 0 },
//			{   0, f, 0, 0 },
//			{   0, 0, c, d },
//			{   0, 0,-1, 0 }
//		});
//		
//		for (int r=0;r<4;r++)
//			System.out.println(Arrays.toString(m[r]));
//			
//		return this;
//	}
	
	public RC frustum(double left, double right, double bottom, double top, double nearVal, double farVal ) {
		final float[][] m;
		concat(modelViewProjection,
				m = createMatrixWithFrustum(
						(float)left, (float)right, 
						(float)bottom, (float)top, 
						(float)nearVal, (float)farVal
				)
		);
		
		System.out.println("-------");
		for (int r=0;r<4;r++)
			System.out.println(Arrays.toString(m[r]));
		
		return this;
	}
	

	public RC translate(double tx, double ty) { translate(tx, ty, 0); return this;}
	public RC rotate(double theta) { rotate(0,0,1,theta); return this;}
	public RC rotate(double theta, double x, double y) { translate(x,y);rotate(theta);translate(-x,-y); return this;}
	public RC scale(double sx, double sy) { scale(sx,sy,1); return this; }
	
//	public RC shear(double shx, double shy) { modelviewprojection.mul(createMatrixWithTransform(AffineTransform.getRotateInstance(shx, shy))); }
//	public RC transform(AffineTransform Tx) { modelviewprojection.mul(createMatrixWithTransform(Tx)); }
//	public RC transform(ProjectiveTransform Tx) { modelviewprojection.mul(Tx.getMatrix(new Matrix4f())); }
//	public RC setTransform(AffineTransform Tx) { modelviewprojection.set(createMatrixWithTransform(Tx)); }
//	public RC setTransform(ProjectiveTransform Tx) { modelviewprojection.set(Tx.getMatrix()); }
//	public ProjectiveTransform getTransform() { return new ProjectiveTransform(modelviewprojection); 

//==[ Rendering: Shapes ]===========================================================================
	
	public RC draw(Shape s) {
		Shape ts = new TransformedShape(modelViewProjection, s);
		
		if (stroke!=NULL_STROKE) 
			drawStroked(ts);
		else
			drawPlain(ts);
		return this;
	}
	
	public RC fill(Shape s) {
		applyState();
		PathShape ps = new PathShape(device, new TransformedShape(modelViewProjection, s));
		
		int fillRule = gc.getFillRule();
		gc.fillPath(ps);
		gc.setFillRule(fillRule);
		
		ps.dispose();
		restoreState();
		return this;
	}

	private void drawFilled(Shape p) {
		applyState();
		PathShape ps = new PathShape(device, p);
		gc.fillPath( ps );
		ps.dispose();
		restoreState();
	}
	
	private void drawPlain(Shape p) {
		applyState();
		PathShape ps = new PathShape(device, p);
		gc.drawPath( ps );
		ps.dispose();
		restoreState();
	}
	
	private void drawStroked(Shape p) {
		PathShape ps = new PathShape(device, stroke.createStrokedShape(p));
		applyState();
		
		int fillRule = gc.getFillRule();
		gc.setFillRule(FILL_WINDING);
		Color background = gc.getBackground();
		gc.setBackground( gc.getForeground() );
		gc.fillPath( ps );
		gc.setBackground( background );
		gc.setFillRule(fillRule);
		
		restoreState();
		ps.dispose();					
	}
	
//==[ Rendering: Images ]===========================================================================

	final static float EPSILON = 0.005f; 
	public boolean drawImage(Image im, int x, int y) {
		applyState();
		ImageData id = im.getImageData();
		int W = id.width, H = id.height;

		float[] at = new float[6], bt = new float[6];
		
		gc.getTransform(t);
		t.getElements(bt);
		t.getElements(at);
		
		tiles.clear();
		drawImageTiled(im, W,H, modelViewProjection, at, bt, 0, 0, W, H, tiles);
		
		if (tiles.size()>0)
			quicksort(tiles);
	
		gc.setLineAttributes(new LineAttributes(1));
		for (ImageTile s: tiles) {
			t.setElements(s.n00, s.n10, s.n01, s.n11, s.n02, s.n12);
			gc.setTransform(t);
			gc.drawImage(im, s.sourceX, s.sourceY, s.width, s.height, s.destX, s.destY, s.width, s.height);
//			gc.fillRectangle(s.destX, s.destY, s.width, s.height);
//			gc.drawRectangle(s.destX, s.destY, s.width, s.height);
		}
		
		t.setElements(bt[0], bt[1], bt[2], bt[3], bt[4], bt[5]);
		gc.setTransform(t);
		
		restoreState();
		return false;
	}

	private ArrayList<ImageTile> tiles = new ArrayList<ImageTile>();
	
	
	public static <T extends Comparable<? super T>> void quicksort(List<T> elements) { quicksort(elements, 0, elements.size()-1); }
	public static <T extends Comparable<? super T>> void quicksort(List<T> elements, int low, int high) {
		int i = low, j = high;
		T pivot = elements.get((low + high) / 2);

		while (i <= j) {
			while (elements.get(i).compareTo(pivot)<0)  i++;
			while (elements.get(j).compareTo(pivot)>0)  j--;
			if (!(i <= j)) continue;
			T t = elements.get(i); 
			elements.set(i, elements.get(j)); 
			elements.set(j, t); 
			i++; j--;
		}
		if (low < j) quicksort(elements,low,j);
		if (i < high) quicksort(elements,i,high);
	}

	public float triangleArea(float ax, float ay, float bx, float by, float cx, float cy) {
		float ux = bx-ax, uy = by-ay;
		float vx = cx-ax, vy = cy-ay;
		float area = (ux*vy-vx*uy)/2;  
		
		return (area>0?area:-area);
	}
	
	public static boolean triangleContains(float px, float py, float ax, float ay, float bx, float by, float cx, float cy) {
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


	static class ImageTile implements Comparable<ImageTile> {
		float n00, n01, n02;
		float n10, n11, n12;
		int sourceX,sourceY;
		int destX,destY;
		
		int width, height;
		
		float z = 0;

		@Override
		public int compareTo(ImageTile that) {
			return Float.compare(this.z, that.z);
		}
	}
	
	int counter = 0;
	int recursions = 0;
	
	float SKIRT = 1, MAX_PERSPECTIVE_DEVIATION = 1.337f, MAX_PERSPECTIVE_ERROR = MAX_PERSPECTIVE_DEVIATION*MAX_PERSPECTIVE_DEVIATION;
	private void drawImageTiled(Image im, int width, int height, float[][] m, float[] at, float[] bt, int x1, int y1, int x2, int y2, ArrayList<ImageTile> tiles) {
		if (x1==x2 || y1==y2)
			return;

		int M = (int)SKIRT;
		int ml = M, mr = M, mt = M, mb = M;
		
		float ax = x1, ay = y1, bx = x2, by = y1, cx = x2, cy = y2, dx = x1, dy = y2;
		
		float aoow = 1f/(m[3][0]* ax+ m[3][1]*ay + m[3][2] * 0 + m[3][3] *1 );
		float ax_ = (m[0][0]*ax+ m[0][1]*ay+m[0][2]*0+m[0][3]*1)*aoow;
		float ay_ = (m[1][0]*ax+ m[1][1]*ay+m[1][2]*0+m[1][3]*1)*aoow;
		float az_ = (m[2][0]*ax+ m[2][1]*ay+m[2][2]*0+m[2][3]*1)*aoow;
		
		float boow = 1f/(m[3][0]* bx+ m[3][1]*by + m[3][2] * 0 + m[3][3] *1 );
		float bx_ = (m[0][0]*bx+ m[0][1]*by+m[0][2]*0+m[0][3]*1)*boow;
		float by_ = (m[1][0]*bx+ m[1][1]*by+m[1][2]*0+m[1][3]*1)*boow;
		float bz_ = (m[2][0]*bx+ m[2][1]*by+m[2][2]*0+m[2][3]*1)*boow;
		
		float coow = 1f/(m[3][0]* cx+ m[3][1]*cy + m[3][2] * 0 + m[3][3] *1 );
		float cx_ = (m[0][0]*cx+ m[0][1]*cy+m[0][2]*0+m[0][3]*1)*coow;
		float cy_ = (m[1][0]*cx+ m[1][1]*cy+m[1][2]*0+m[1][3]*1)*coow;
		float cz_ = (m[2][0]*cx+ m[2][1]*cy+m[2][2]*0+m[2][3]*1)*coow;
		
		float doow = 1f/(m[3][0]* dx+ m[3][1]*dy + m[3][2] * 0 + m[3][3] *1 );
		float dx_ = (m[0][0]*dx+ m[0][1]*dy+m[0][2]*0+m[0][3]*1)*doow;
		float dy_ = (m[1][0]*dx+ m[1][1]*dy+m[1][2]*0+m[1][3]*1)*doow;
		float dz_ = (m[2][0]*dx+ m[2][1]*dy+m[2][2]*0+m[2][3]*1)*doow;
		
		
		float tileW = (x2-x1), tileH = (y2-y1), error = 0;
		
		float ax__ = dx_+bx_-cx_, ay__ = dy_+by_-cy_;
		float bx__ = ax_+cx_-dx_, by__ = ay_+cy_-dy_;
		float cx__ = bx_+dx_-ax_, cy__ = by_+dy_-ay_;
		float dx__ = ax_+cx_-bx_, dy__ = ay_+cy_-by_;
		
		if (triangleContains(cx_, cy_, dx_, dy_, bx_, by_, cx__, cy__)) { //paDAB > all (A ist Anker)
			float deltaX = cx__-cx_, deltaY = cy__-cy_;
			error = deltaX*deltaX + deltaY*deltaY;
			
			set(at, (bx_-ax_)/tileW, (by_-ay_)/tileW,   (dx_-ax_)/tileH, (dy_-ay_)/tileH,    ax_, ay_);
			
			ml = 0; mt = 0;
		} else
		if (triangleContains(dx_,dy_, ax_,ay_, cx_,cy_, dx__,dy__)) { //paABC > all (B ist Anker)
			float deltaX = dx__-dx_, deltaY = dy__-dy_;
			error = deltaX*deltaX + deltaY*deltaY;
	
			set(at, (bx_-ax_)/tileW, (by_-ay_)/tileW,    (dx__-ax_)/tileH, (dy__-ay_)/tileH,    ax_, ay_);
			
			mr = 0; mt = 0;
		} else
		if (triangleContains(ax_,ay_, dx_,dy_,bx_,by_,ax__,ay__)) { //paBCD > all (C ist Anker)
			float deltaX = ax__-ax_, deltaY = ay__-ay_;
			error = deltaX*deltaX + deltaY*deltaY;
			
			set(at, (bx_-ax__)/tileW, (by_-ay__)/tileW,    (dx_-ax__)/tileH, (dy_-ay__)/tileH,    ax__, ay__);
			
			mr = 0; mb = 0;
		} else { //paCDA > all (D ist Anker)
			float deltaX = bx__-bx_, deltaY = by__-by_;
			error = deltaX*deltaX + deltaY*deltaY;
	
			set(at, (bx__-ax_)/tileW, (by__-ay_)/tileW,    (dx_-ax_)/tileH, (dy_-ay_)/tileH,    ax_, ay_);
			ml = 0; mb = 0;
		}
 
		final int T00 = 0, T10 = 1, T01 = 2, T11 = 3, T02 = 4, T12 = 5;
		
		if (error<MAX_PERSPECTIVE_ERROR) {
			ImageTile tile = new ImageTile();
			
			tile.n00 = bt[T00]*at[T00]+bt[T01]*at[T10];
			tile.n01 = bt[T00]*at[T01]+bt[T01]*at[T11];
			tile.n02 = bt[T00]*at[T02]+bt[T01]*at[T12]+bt[T02];
			
			tile.n10 = bt[T10]*at[T00]+bt[T11]*at[T10];
			tile.n11 = bt[T10]*at[T01]+bt[T11]*at[T11];
			tile.n12 = bt[T10]*at[T02]+bt[T11]*at[T12]+bt[T12];
			
			int left = max(0,x1-ml), right = min(width,x2+mr);
			int top = max(0,y1-mt), bottom = min(height,y2+mb);
			
			tile.sourceX = left; tile.sourceY = top;
			tile.destX = left-x1; tile.destY = top-y1;
			tile.width = right-left; tile.height = bottom-top;
			
			tile.z = max(max(az_,bz_),max(cz_,dz_));
			tiles.add(tile);
		} else {
			if (recursions<6) { //Hacky safety measure
				recursions++;
				int mx = (int)((x1+x2)/2), my = (int)((y1+y2)/2);
				drawImageTiled(im, width, height, m, at, bt, x1, y1, mx, my, tiles );
				drawImageTiled(im, width, height, m, at, bt, mx, y1, x2, my, tiles);
				drawImageTiled(im, width, height, m, at, bt, mx, my, x2, y2, tiles );
				drawImageTiled(im, width, height, m, at, bt, x1, my, mx, y2, tiles );
				recursions--;
			}
		}
		
	}
	
//==[ Rendering: Text ]=============================================================================
	
	public RC drawText(String str, int x, int y) {
		this.drawText(str, x, y, false);
		return this;
	}	
	
	public RC drawText(String str, int x, int y, boolean transparent) {
		drawText(str, x, y, DRAW_DELIMITER|DRAW_TAB|DRAW_TRANSPARENT);
		return this;
	}
	
	public RC drawText(String str, int x, int y, int flags) {
		gc.getTransform(t);
		t.multiply(approximateTransform(x, y, s));

		gc.getTransform(s);
		gc.setTransform(t);
		
		gc.drawText(str, x, y, flags);
		
		gc.setTransform(s);
		return this;
	}
	
	
	public Point stringExtent(String s) {
		return gc.stringExtent(s);
	}
	
	public int getCharWidth(char ch) {
		return gc.getCharWidth(ch);
	}
	
	public int getAdvanceWidth(char ch) {
		return gc.getAdvanceWidth(ch);
	}
	
	public RC drawString(String str, int x, int y, boolean transparent) {
		gc.getTransform(t);
		t.multiply(approximateTransform(x, y, s));

		gc.getTransform(s);
		gc.setTransform(t);
		
		gc.drawString(str, x, y, transparent);
		
		gc.setTransform(s);
		return this;
	}
	
	public RC drawString(String str, int x, int y) {
		this.drawString(str, x, y, false);
		return this;
	}	
	
//==[ Rendering: OpenGL Primitives ]================================================================
	
	public static final int POINTS = PrimitivePathIterator.POINTS;
	public static final int LINES = PrimitivePathIterator.LINES;
	public static final int TRIANGLES = PrimitivePathIterator.TRIANGLES;
	public static final int TRIANGLE_FAN = PrimitivePathIterator.TRIANGLE_FAN;
	public static final int QUADS = PrimitivePathIterator.QUADS;
	public static final int LINE_STRIP = PrimitivePathIterator.LINE_STRIP;
	public static final int LINE_LOOP = PrimitivePathIterator.LINE_LOOP;
	public static final int POLYGON = -PrimitivePathIterator.LINE_LOOP;
	
	public static final int FRONT = 1;
	public static final int BACK = 2;
	public static final int FRONT_AND_BACK = FRONT | BACK;
	
	public static final int POINT = 1;
	public static final int LINE = 2;
	public static final int FILL = 3;	
	

	private Primitive cached = new Primitive();
	private Primitive p = null;
	
	@SuppressWarnings("unused")
	private int backPolygonMode = LINE;
	private int frontPolygonMode = LINE;
	
	public RC polygonMode(int mode) {
		polygonMode(FRONT_AND_BACK, mode);
		
		return this;
	}
	
	private RC polygonMode(int face, int mode) {
		if ((face&FRONT)!=0)
			frontPolygonMode = mode;
		
		if ((face&BACK)!=0)
			backPolygonMode = mode;
		
		return this;
	}
	
	public RC begin(int type) {
		p = cached.reset(modelViewProjection, type);
		return this;
	}

	
	public RC vertex(int x, int y) { addVertex(x, y, 0f); return this; }
	public RC vertex(float x, float y) { addVertex(x, y, 0f); return this; }
	public RC vertex(double x, double y) { addVertex((float)x, (float)y, 0f); return this;  }
	
	public RC vertex(int x, int y, int z) { addVertex(x, y, z); return this;  }
	public RC vertex(float x, float y, float z) { addVertex(x, y, z); return this;  }
	public RC vertex(double x, double y, double z) { addVertex((float)x, (float)y, (float)z); return this; }

	private RC addVertex(float x, float y, float z) {
		p.addVertex(x, y, z); 
		return this; 
	}

	
	public RC end() {
		boolean strokeSet = stroke!=NULL_STROKE;
		
		switch (p.getType()) {
		case POINTS:
			drawPlain(p);
			break;
		case LINES:
		case LINE_STRIP:
		case LINE_LOOP:
			if (strokeSet) 
				drawStroked(p);
			else 
				drawPlain(p);
			

			break;
		case TRIANGLE_FAN:
		case POLYGON:
		case TRIANGLES:
		case QUADS:
			if (frontPolygonMode==FILL)
				drawFilled(p);
			else 
				if (strokeSet) 
					drawStroked(p);
				else 
					drawPlain(p);
				
			break;
		}
		p = null;
		
		return this;
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
	
	static void set(float[] m, float ... v) {
		for (int i=0,I=v.length;i<I;i++)
			m[i]=v[i];
	}
	
	static void copy(float[][] m, float[][] n) {
		for (int row=0;row<m.length;row++)
			for (int col=0;col<m[row].length;col++)
				n[row][col] = m[row][col];
	}
	
	static void transform( float[][] m, float[] v ) {
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

	static void project( float[][] m, float[] v ) {
		transform(m,v);
		scale(v,1/v[3]);
	}
	
	static void scale( float[] v, double s ) {
		for (int i=0,I=v.length;i<I;i++)
			v[i] *= s;
	}
	
	static void identity( float[][] m ) {
		for (int r=0,R=m.length;r<R;r++)
			for (int c=0,C=m[r].length;c<C;c++)
				m[r][c] = r==c?1:0;
	}
	
	static void concat( float[][] m, float[][] n) {
		final float n00 = n[0][0], n01 = n[0][1], n02 = n[0][2], n03 = n[0][3];
		final float n10 = n[1][0], n11 = n[1][1], n12 = n[1][2], n13 = n[1][3];
		final float n20 = n[2][0], n21 = n[2][1], n22 = n[2][2], n23 = n[2][3];
		final float n30 = n[3][0], n31 = n[3][1], n32 = n[3][2], n33 = n[3][3];
		concat(m, n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
	}
	
	static void concat( float[][] m, 
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

//==[ Shadow State ]================================================================================
	
	private void initState() {
//		alpha = gc.getAlpha();
//		fillRule = gc.getFillRule();
//		foreground = gc.getForeground();
//		background = gc.getBackground();
//		
//		font = gc.getFont();
//		
//		lineAttributes = gc.getLineAttributes();
	}
	
	
	Font font, fontSaved;
	Integer alpha, alphaSaved;
	Integer fillRule, fillRuleSaved;
	Color foreground, foregroundSaved, foregroundCreated;
	Color background, backgroundSaved, backgroundCreated;

	final static private BasicStroke NULL_STROKE = new BasicStroke(1);
	BasicStroke stroke = NULL_STROKE;
	
	public RC setStroke(BasicStroke stroke) {
		if (stroke!=null)
			this.stroke = stroke;
		else
			this.stroke = NULL_STROKE;
		return this;
	}
	
	public BasicStroke getStroke() {
		return stroke;
	}
	
	LineAttributes lineAttributes, lineAttributesSaved;
	public RC setLineAttributes(LineAttributes lineAttributes) {
		this.stroke = NULL_STROKE;
		this.lineAttributes = lineAttributes;
		return this;
	}
	
	public LineAttributes getLineAttributes() {
		return lineAttributes;
	}
	
	
	public RC setFont(Font font) {
		this.font = font;
		return this;
	}
	
	public Font getFont() {
		return font;
	}
	
	public RC setFillRule(int fillRule) {
		this.fillRule = fillRule;
		return this;
	}
	
	public int getFillRule() {
		return fillRule;
	}
	
	
	public RC setAlpha(int alpha) {
		this.alpha = alpha;
		return this;
	}
	
	public int getAlpha() {
		return alpha;
	}
	
	public RC setForeground(RGB color) {
		if (foregroundCreated!=null)
			foregroundCreated.dispose();
		
		foregroundCreated = new Color(device,color);
		setForeground(foregroundCreated);
		return this;
	}
	
	public RC setForeground(Color foreground) {
		if (foregroundCreated!=foreground) {
			foregroundCreated.dispose();
			foregroundCreated = null;
		}
		
		this.foreground = foreground;
		return this;
	}
	
	public Color getForeground() {
		return foreground;
	}
	

	public RC setBackground(RGB color) {
		if (backgroundCreated!=null)
			backgroundCreated.dispose();
		
		backgroundCreated = new Color(device,color);
		setBackground(backgroundCreated);
		return this;
	}

	
	public RC setBackground(Color background) {
		if (backgroundCreated!=background) {
			backgroundCreated.dispose();
			backgroundCreated = null;
		}
		
		this.background = background;
		return this;
	}
	
	public Color getBackground() {
		return background;
	}
	
	///////////////
	private void applyState() {
		if (font!=null) {
			fontSaved = gc.getFont();
			if (!fontSaved.equals(font))
				gc.setFont(font);
			else
				fontSaved = null;
		}
		
		if (alpha!=null) {
			alphaSaved = gc.getAlpha();
			if (!alphaSaved.equals(alpha)) 
				gc.setAlpha(alpha);
			else
				alphaSaved = null;
		}
		
		if (fillRule!=null) {
			fillRuleSaved = gc.getFillRule();
			if (!fillRuleSaved.equals(fillRule))
				gc.setFillRule(fillRule);
			else
				fillRuleSaved = null;
		}
		
		if (foreground!=null) {
			foregroundSaved = gc.getForeground();
			if (!foregroundSaved.equals(foreground))
				gc.setForeground(foreground);
			else
				foregroundSaved = null;
		}
		
		if (background!=null) { 
			backgroundSaved = gc.getBackground();
			if (!backgroundSaved.equals(background))
				gc.setBackground(background);
			else
				backgroundSaved = null;
		}
		
		if (stroke==NULL_STROKE) {
			if (lineAttributes!=null) {
				lineAttributesSaved = gc.getLineAttributes();
				if (!lineAttributesSaved.equals(lineAttributes))
					gc.setLineAttributes(lineAttributes);
				else
					lineAttributesSaved = null;
			}
		}
	}
	
	
	private void restoreState() {
		if (fontSaved!=null) {
			gc.setFont(fontSaved);
			fontSaved = null;
		}

		if (alphaSaved!=null) {
			gc.setAlpha(alphaSaved);
			alphaSaved = null;
		}
		
		if (fillRuleSaved!=null) {
			gc.setFillRule(fillRuleSaved);
			fillRuleSaved=null;
		}
		
		if (foregroundSaved!=null) {
			gc.setBackground(foregroundSaved);
			foregroundSaved = null;
		}
		if (backgroundSaved!=null) {
			gc.setBackground(backgroundSaved);
			backgroundSaved = null;
		}
		
		if (stroke==NULL_STROKE) {
			if (lineAttributesSaved!=null) {
				gc.setLineAttributes(lineAttributesSaved);
				lineAttributesSaved = null;
			}
 		}
			
	}
}
