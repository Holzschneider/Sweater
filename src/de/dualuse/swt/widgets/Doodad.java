package de.dualuse.swt.widgets;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.awt.geom.AffineTransform;
import java.util.Arrays;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Event;


interface Renderable {
	public Renderable add( Renderable r );
	public Renderable remove( Renderable r );

	public Renderable transform(float[] m); 
	
	public void setParentRenderable( Renderable r );
	public Renderable getParentRenderable();

	public void render( Rectangle clip, Transform t, GC c );
	public void onMouse( float x, float y, Event e );
}

class Doodad implements Renderable {
	final static private int T00 = 0;
	final static private int T01 = 1;
	final static private int T10 = 2;
	final static private int T11 = 3;
	final static private int T02 = 4;
	final static private int T12 = 5;
	
	private Renderable parent; 
	private Renderable children[] = {};
	private float M[] = new float[6];
	private float width, height;
	
	private AffineTransform at = new AffineTransform();

	public double getWidth() { return width; }
	public double getHeight() { return height; }
	
	public Doodad setSize(double width, double height) {
		this.width = (float)width;
		this.height = (float)height;
		return this;
	}
	
	public Renderable getParentRenderable() { return parent; }
	
	public Doodad set(AffineTransform at) {
		M[T00] = (float) at.getScaleX(); M[T01] = (float) at.getShearX(); M[T02] = (float) at.getTranslateX();
		M[T10] = (float) at.getShearY(); M[T11] = (float) at.getScaleY(); M[T12] = (float) at.getTranslateY();
		this.at.setTransform(at);
		return this;
	}
	
	public Doodad rotate(double theta) { return concatenate(cos(theta),sin(theta),-sin(theta),cos(theta),0,0); }
	public Doodad translate(double tx, double ty) { return concatenate(1,0,0,1,tx,ty); }
	public Doodad scale(double sx, double sy) { return concatenate(sx,0,0,sy,0,0); }

	public Doodad scale(double sx, double sy, double pivotX, double pivotY) { 
		return this
				.translate(+pivotX,+pivotY)
				.scale(sx, sy)
				.translate(-pivotX,-pivotY); 
	}
	
	public Doodad rotate(double theta, double pivotX, double pivotY) { 
		return this
				.translate(-pivotX,-pivotY)
				.rotate(theta)
				.translate(pivotX,pivotY); 
	}
	
	
	
	//XXX redo transform with loops and accept variable sized m assuming any 3-by-k Matrices or even a 2-by-1 Vector!
	public Doodad transform(float[] m) {
		AffineTransform l = new AffineTransform(M[T00], M[T10], M[T01], M[T11], M[T02], M[T12]);
		AffineTransform r = new AffineTransform(m[T00], m[T10], m[T01], m[T11], m[T02], m[T12]);
		
		l.concatenate(r);
		
		m[T00] = (float) l.getScaleX(); m[T01] = (float) l.getShearX(); m[T02] = (float) l.getTranslateX();
		m[T10] = (float) l.getShearY(); m[T11] = (float) l.getScaleY(); m[T12] = (float) l.getTranslateY();
		
		return this;
		
//		final float l00 = M[T00], l01 = M[T01], l02 = M[T02];
//		final float l10 = M[T10], l11 = M[T11], l12 = M[T12];
//
//		final float r00 = m[T00], r01 = m[T01], r02 = m[T02];
//		final float r10 = m[T10], r11 = m[T11], r12 = m[T12];
//				
//		m[T00]=r10*l01+r00*l00; m[T01]= r11*l01+r01*l00; m[T02]= l02+r12*l01+r02*l00;
//		m[T10]=r10*l11+r00*l10; m[T11]= r11*l11+r01*l10; m[T12]= l12+r12*l11+r02*l10;
//		
//		M[T00] = l00; M[T01] = l01; M[T02] = l02;
//		M[T10] = l10; M[T11] = l11; M[T12] = l12;
	}

	public Doodad identity() {
		M[T00] = M[T11] = 1;
		M[T01] = M[T02] = 0;
		M[T10] = M[T12] = 0;
		
		return this;
	}
	
	public Doodad concatenate(double scX, double shY, double shX, double scY, double tx, double ty) {
		final float m00 = (float) scX, m01 = (float) shX, m02 = (float) tx;
		final float m10 = (float) shY, m11 = (float) scY, m12 = (float) ty;
		
		final float M00 = M[T00], M01 = M[T01], M02 = M[T02];
		final float M10 = M[T10], M11 = M[T11], M12 = M[T12];

		M[T00]=m10*M01+m00*M00; M[T01]= m11*M01+m01*M00; M[T02]= M02+m12*M01+m02*M00;
		M[T10]=m10*M11+m00*M10; M[T11]= m11*M11+m01*M10; M[T12]= M12+m12*M11+m02*M10;

//		System.out.println(new AffineTransform(M));
		
		return this;
	}

	
//	interface Coordinate<T> { T set(float x, float y); }
//	protected<T> T transform(float x, float y, Coordinate<T> c) {
//		float x_ = M[T00]*x+M[T01]*y+M[T02];
//		float y_ = M[T10]*x+M[T11]*y+M[T12];
//		
//		Renderable r = getParent();
//		if (r!=null)
//			return ((Doodad)r).transform(x_, y_, c);
//		else
//			return c.set(x_, y_);
//	}
//	
//	protected<T> T inverse(float x, float y, Coordinate<T> c) {
//		final float l00 = M[T00], l01 = M[T01], l02 = M[T02];
//		final float l10 = M[T10], l11 = M[T11], l12 = M[T12];
//		
//		identity();
//		transform(M);
//		
//		final float m00 = M[T00], m01 = M[T01], m02 = M[T02];
//		final float m10 = M[T10], m11 = M[T11], m12 = M[T12];
//		
//		float det = m00 * m11 - m01 * m10;
//		float i00 = m11/det, i01 =-m01/det, i02 = (m01 * m12 - m11 * m02) / det;
//		float i10 =-m10/det, i11 = m00/det, i12 = (m10 * m02 - m00 * m12) / det;
//		
//		float x_ = i00*x+i01*y+i02;
//		float y_ = i10*x+i11*y+i12;
//		
//		//restore
//		M[T00] = l00; M[T01] = l01; M[T02] = l02;
//		M[T10] = l10; M[T11] = l11; M[T12] = l12;
//		
//		return c.set(x_, y_);
//	}
	

	
	////////
	
	public Doodad(Renderable parent) {
		this.parent = this;
		parent.add(this);
		
		identity();
	}
	
	public void dispose() {
		parent.remove(this);
		parent = null;
	}

	@Override
	public Doodad add(Renderable r) {
		(children = Arrays.copyOf(children, children.length+1))[children.length-1]=r;
		return this;
	}

	@Override
	public Doodad remove(Renderable r) {
		for (int i=0,I=children.length;i<I;i++)
			if (children[i]==r) {
				r.setParentRenderable(null);
				children[i] = children[children.length-1];
				children = Arrays.copyOf(children, children.length-1);
				return this;
			}
		
		return this;
	}
	
	@Override
	public void setParentRenderable(Renderable r) {
		parent = r;
	}
	
	//////////////////
	
	//XXX make Knob by implementing getTransform in a custom way  
	//use render and mouse hit with getTransform(Transform t) stuff - so it works

	private float T[] = new float[6];
	
	@Override
	final public void render(Rectangle clip, Transform t, GC c) {
		//read out and store this matrix
//		final float s00 = M[T00], s01 = M[T01], s02 = M[T02];
//		final float s10 = M[T10], s11 = M[T11], s12 = M[T12];

//		T[T00] = T[T11] = 1;
//		T[T01] = T[T02] = 0;
//		T[T10] = T[T12] = 0;

		//transform the identity matrix by this Doodad's matrix
//		transform(T);
		
		//read out this matrix
//		final float m00 = T[T00], m01 = T[T01], m02 = T[T02];
//		final float m10 = T[T10], m11 = T[T11], m12 = T[T12];
//		
		//read world matrix to M
		t.getElements(M);

//		final float M00 = M[T00], M01 = M[T01], M02 = M[T02];
//		final float M10 = M[T10], M11 = M[T11], M12 = M[T12];
//		//compute N = t.m
////		final float N00 = m10*M01+m00*M00, N01 = m11*M01+m01*M00, N02 = M02+m12*M01+m02*M00;
////		final float N10 = m10*M11+m00*M10, N11 = m11*M11+m01*M10, N12 = M12+m12*M11+m02*M10;
//		
//		AffineTransform m = new AffineTransform(m00, m10, m01, m11, m02, m12);
//		AffineTransform n = new AffineTransform(M00, M10, M01, M11, M02, M12);
//		
//		n.concatenate(m);
//
		
		AffineTransform n  = at;
		final float N00 = (float) n.getScaleX(), N01 = (float) n.getShearX(), N02 = (float) n.getTranslateX();
		final float N10 = (float) n.getShearY(), N11 = (float) n.getScaleY(), N12 = (float) n.getTranslateY();
		

		//set Matrix to GC
//		t.setElements(
//				(float)n.getScaleX(), 
//				(float)n.getShearY(), 
//				(float)n.getShearX(), 
//				(float)n.getScaleY(), 
//				(float)n.getTranslateX(), 
//				(float)n.getTranslateY()
//		);
		
		t.identity();
		t.translate(100, 100);
		t.scale(.5f, .5f);
		t.translate(+50, +50);
//		t.rotate(44.999f);
		t.translate(-50, -50);
		
//		t.setElements(N00, N10, N01, N11, N02, N12);
//		t.setElements(M11, M12, m21, m22, dx, dy);
		
		System.out.println(t);
		c.setTransform(t);
		
		//render childnodes
		render(clip, t, c, children);
		
		//transform this doodad's bounds with M 
//		float ax = M02, ay = M02;
//		float bx = width*M00+M02, by = width*M10+M12;
//		float cx = height*M01+M02, cy = height*M11+M12;
//		float dx = width*M00+height*M01+M02, dy = width*M10+height*M11+M12;
//		
//		//compute enclosing axis aligned bounding box
//		float left = min(min(ax,bx),min(cx,dx));
//		float top = min(min(ay,by),min(cy,dy));
//		float right = max(max(ax,bx),max(cx,dx));
//		float bottom = max(max(ay,by),max(cy,dy));
//		
//		//only render this Doodad if the GC's screen coordinate-transformed clips intersect that aabb 
//		if (clip.intersects((int)left, (int)top, (int)(right-left), (int)(bottom-top)))
			render(c);
		
		//restore c's Transform
		t.setElements(M[0],M[1],M[2],M[3],M[4],M[5]);
		c.setTransform(t);

		//restore own Doodads Matrix
//		M[T00] = s00; M[T01] = s01; M[T02] = s02;
//		M[T10] = s10; M[T11] = s11; M[T12] = s12;
		
	}
	
	protected void render(Rectangle clip, Transform t, GC c, Renderable[] children) {
		for (Renderable r: children)
			r.render(clip, t, c);
	}
	
	protected void render(GC c) {
		/// implement this
	}
	
	//////////////
	//XXX no relative step by step, use Event e only, so it can rely on getTransform(Transform t) 
	// or getTransform(float[] transform)
	
	@Override
	final public void onMouse(float x, float y, Event e) {
		final float m00 = M[T00], m01 = M[T01], m02 = M[T02];
		final float m10 = M[T10], m11 = M[T11], m12 = M[T12];
		
		float det = m00 * m11 - m01 * m10;
		float i00 = m11/det, i01 =-m01/det, i02 = (m01 * m12 - m11 * m02) / det;
		float i10 =-m10/det, i11 = m00/det, i12 = (m10 * m02 - m00 * m12) / det;
		
		float x_ = i00*x+i01*y+i02;
		float y_ = i10*x+i11*y+i12;
		
		for (Renderable r: children)
			if (e.doit)
				r.onMouse(x_, y_, e);
		
		//call local mouse events here
	}
	
	//Focus?
	//Keyboard Events?
	//ControlListener?
	//onTransformed!
	
	//////////////
	
	private static float min(float a, float b) {
		return a<b?a:b;
	}
	
	private static float max(float a, float b) {
		return a>b?a:b;
	}

}

