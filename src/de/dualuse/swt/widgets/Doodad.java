package de.dualuse.swt.widgets;

import java.util.Arrays;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Event;


interface Renderable {
	public void add( Renderable r );
	public void remove( Renderable r );

	public void transform(float[] m); 
	
	public void setParent( Renderable r );
	public void render( Rectangle clip, Transform t, GC c );
	
	public void onMouse( float x, float y, Event e );
	public Renderable getParent();
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

	public double getWidth() { return width; }
	public double getHeight() { return height; }
	public Renderable getParent() { return parent; }
	
	public Doodad translate(float tx, float ty) { return concatenate(1,0,0,1,tx,ty); }
	public Doodad scale(float sx, float sy) { return concatenate(sx,0,0,sy,0,0); }
	
	public void transform(float[] m) {
		final float l00 = M[T00], l01 = M[T01], l02 = M[T02];
		final float l10 = M[T10], l11 = M[T11], l12 = M[T12];

		final float r00 = m[T00], r01 = m[T01], r02 = m[T02];
		final float r10 = m[T10], r11 = m[T11], r12 = m[T12];
		
		final float m00 = l00*r00+l01*r10, m01 = l00*r01+l01*r11, m02 = l00*r02+l01*r12+l02;
		final float m10 = l10*r00+l11*r10, m11 = l10*r01+l11*r11, m12 = l10*r02+l11*r12+l12;
		
		M[T00] = m00; M[T01] = m01; M[T02] = m02;
		M[T10] = m10; M[T11] = m11; M[T12] = m12;
	}

	interface Coordinate<T> { T set(float x, float y); }
	protected<T> T transform(float x, float y, Coordinate<T> c) {
		float x_ = M[T00]*x+M[T01]*y+M[T02];
		float y_ = M[T10]*x+M[T11]*y+M[T12];
		
		Renderable r = getParent();
		if (r!=null)
			return ((Doodad)r).transform(x_, y_, c);
		else
			return c.set(x_, y_);
	}
	
	protected<T> T inverse(float x, float y, Coordinate<T> c) {
		final float l00 = M[T00], l01 = M[T01], l02 = M[T02];
		final float l10 = M[T10], l11 = M[T11], l12 = M[T12];
		
		identity();
		transform(M);
		
		final float m00 = M[T00], m01 = M[T01], m02 = M[T02];
		final float m10 = M[T10], m11 = M[T11], m12 = M[T12];
		
		float det = m00 * m11 - m01 * m10;
		float i00 = m11/det, i01 =-m01/det, i02 = (m01 * m12 - m11 * m02) / det;
		float i10 =-m10/det, i11 = m00/det, i12 = (m10 * m02 - m00 * m12) / det;
		
		float x_ = i00*x+i01*y+i02;
		float y_ = i10*x+i11*y+i12;
		
		//restore
		M[T00] = l00; M[T01] = l01; M[T02] = l02;
		M[T10] = l10; M[T11] = l11; M[T12] = l12;
		
		return c.set(x_, y_);
	}
	
	public Doodad identity() {
		M[T00] = M[T11] = 1;
		M[T01] = M[T02] = 0;
		M[T10] = M[T12] = 0;
		
		return this;
	}
	
	public Doodad concatenate(float scX, float shY, float shX, float scY, float tx, float ty) {
		final float m00 = scX, m01 = shX, m02 = tx;
		final float m10 = shY, m11 = scY, m12 = ty;
		
		final float M00 = m00*M[T00]+m01*M[T10], M01 = m00*M[T01]+m01*M[T11], M02 = m00*M[T02]+m01*M[T12]+m02;
		final float M10 = m10*M[T00]+m11*M[T10], M11 = m10*M[T01]+m11*M[T11], M12 = m10*M[T02]+m11*M[T12]+m12;
		
		M[T00] = M00; M[T01] = M01; M[T02] = M02;
		M[T10] = M10; M[T11] = M11; M[T02] = M12;
		
		return this;
	}
	
	
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
	public void add(Renderable r) {
		(children = Arrays.copyOf(children, children.length+1))[children.length-1]=r;
	}

	@Override
	public void remove(Renderable r) {
		for (int i=0,I=children.length;i<I;i++)
			if (children[i]==r) {
				r.setParent(null);
				children[i] = children[children.length-1];
				children = Arrays.copyOf(children, children.length-1);
				return;
			}
	}
	
	@Override
	public void setParent(Renderable r) {
		parent = r;
	}
	
	//////////////////
	
	//XXX make Knob by implementing getTransform in a custom way  
	//use render and mouse hit with getTransform(Transform t) stuff - so it works
	
	@Override
	final public void render(Rectangle clip, Transform t, GC c) {
		//read out this matrix
		final float m00 = M[T00], m01 = M[T01], m02 = M[T02];
		final float m10 = M[T10], m11 = M[T11], m12 = M[T12];

		//read world matrix to M
		t.getElements(M);

		//compute M_ = t.m
		final float M00 = M[T00]*m00+M[T01]*m10, M01 = M[T00]*m01+M[T01]*m11, M02 = M[T00]*m02+M[T01]*m12+M[T02];
		final float M10 = M[T10]*m00+M[T11]*m10, M11 = M[T10]*m01+M[T11]*m11, M12 = M[T10]*m02+M[T11]*m12+M[T12];

		//set Matrix to GC
		t.setElements(M00, M01, M10, M11, M02, M12);
		c.setTransform(t);
		
		//render childnodes
		render(clip, t, c, children);
		
		//transform this doodad's bounds with M 
		float ax = M02, ay = M02;
		float bx = width*M00+M02, by = width*M10+M12;
		float cx = height*M01+M02, cy = height*M11+M12;
		float dx = width*M00+height*M01+M02, dy = width*M10+height*M11+M12;
		
		//compute enclosing axis aligned bounding box
		float left = min(min(ax,bx),min(cx,dx));
		float top = min(min(ay,by),min(cy,dy));
		float right = max(max(ax,bx),max(cx,dx));
		float bottom = max(max(ay,by),max(cy,dy));
		
		//only render this Doodad if the GC's screen coordinate-transformed clips intersect that aabb 
		if (clip.intersects((int)left, (int)top, (int)(right-left), (int)(bottom-top)))
			render(c);
		
		//restore c's Transform
		t.setElements(M[0],M[1],M[2],M[3],M[4],M[5]);
		c.setTransform(t);

		//restore own Doodads Matrix
		M[T00] = m00; M[T01] = m01; M[T02] = m02;
		M[T10] = m10; M[T11] = m11; M[T12] = m12;
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

