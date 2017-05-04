package de.dualuse.swt.widgets;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.util.Arrays;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.dualuse.swt.events.Listeners;
import de.dualuse.swt.events.Runnables;

public class Layer implements LayerContainer, Runnable {
	final static private int T00=0, T01=1, T10=2, T11=3, T02=4, T12=5; //, T20=3, T21=4, T22=5;

	/// Attributes
	private Layer parent; 
	private LayerCanvas root;
	private Layer children[] = {};
	
	boolean invalid = false;
	private float M[] = new float[6]; // the local transformation Matrix
	private float W[] = new float[6]; // the world matrix where this layer is anchored to (the parents' transformations)
	private float B[] = new float[4*2]; // the oriented bounds of this Layer in world coordinates
	
	boolean redraw = true; //whether redraws are triggerd upon change 
	
	///
	private float left = -1f/0f, top= -1f/0f;
	private float right = +1f/0f, bottom = +1f/0f;

	public float getLeft() { return left; }
	public float getRight() { return right; }
	public float getTop() { return top; }
	public float getBottom() { return bottom; }
	
	public float getWidth() { return Float.isFinite(right)&&Float.isFinite(left)?right-left:1f/0f; }
	public float getHeight() { return Float.isFinite(bottom)&&Float.isFinite(top)?bottom-top:1f/0f; }
	
	public Rectangle getBounds() {
		//XXX fix integer truncation to always contain floating point bounds
		return new Rectangle((int)floor(left), (int)floor(top), (int)ceil(getWidth()), (int)ceil(getHeight()));
	}
	
	public boolean isFinite() {
		return Float.isFinite(left) && Float.isFinite(right) && Float.isFinite(top) && Float.isFinite(bottom);
	}
	
	public Layer setBounds(double left, double top, double right, double bottom) {
		boolean changed = this.left!=left || this.top!=top || this.right!=right || this.bottom!=bottom;
		
		this.left = (float) left;
		this.top = (float) top;
		this.right = (float) right;
		this.bottom = (float) bottom;
		
		if (redraw && changed)
			redraw();
		
		return this;
	}
	
	///
	private boolean clipping = false;
	public void setClipping(boolean clipping) {
		this.clipping = clipping;
	}
	public boolean isClipping() {
		return clipping;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// CONSTRUCTOR & HIERARCHY ///////////////////////////////
	public Layer(LayerContainer parent) {
		parent.addLayer(this);
		
		identity();
	}
	
	public void dispose() {
		getParent().removeLayer(this);
		setRoot(null);
		parent = null;
	}

	public Layer addLayer(Layer r) {
		if (r.setParent(this))
			((children = Arrays.copyOf(children, children.length+1))[children.length-1]=r).setRoot(root);

		if (redraw)
			redraw();

		return this;
	}

	public Layer removeLayer(Layer r) {
		for (int i=0,I=children.length;i<I;i++)
			if (children[i]==r) {
				r.setParent(null);
				r.setRoot(null);
				children[i] = children[children.length-1];
				children = Arrays.copyOf(children, children.length-1);
				return this;
			}
	
		if (redraw)
			redraw();
				
		return this;
	}
	
	protected boolean setParent(Layer r) {
		if (parent==r)
			return false;
		
		if (parent!=null) 
			getParent().removeLayer(this);
			
		parent = r;
		
		if (parent!=null)
			getParent().addLayer(this);
		
		return true;
	}
	
	public Layer[] getLayers() {
		return children;
	}
	
	public int indexOf(Layer r) {
		for (int i=0,I=children.length;i<I;i++)
			if (children[i]==r)
				return i;
				
		return -1;
	}
	
	public void moveAbove(Layer r) {
		LayerContainer p = getParent();
		Layer[] cs = p.getLayers();

		int ir = p.indexOf(this);
		
		for (int j=ir;j>=1 && cs[j]!=r;j--) {
			Layer t = cs[j];
			cs[j] = cs[j-1];
			cs[j-1] = t;
		}
		
		if (redraw)
			redraw();
	}
	
	
	public void moveBelow(Layer r) {
		LayerContainer p = getParent();
		Layer[] cs = p.getLayers();

		int ir = p.indexOf(this);
		
		for (int j=ir,J=cs.length;j<J-1 && cs[j]!=r;j++) {
			Layer t = cs[j];
			cs[j] = cs[j+1];
			cs[j+1] = t;
		}
		
		if (redraw)
			redraw();
	}
	
	public LayerContainer getParent() { return parent==null?root:parent; }
	
	public LayerCanvas getRoot() { return root; }
	protected void setRoot(LayerCanvas root) { 
		this.root = root;
		for (Layer child: children)
			child.setRoot(root);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////// TRANSFORMATION ///////////////////////////////////////////////////
	
	public Layer rotate(double theta) { return concatenate(cos(theta),sin(theta),-sin(theta),cos(theta),0,0); }
	public Layer translate(double tx, double ty) { return concatenate(1,0,0,1,tx,ty); }
	public Layer scale(double s, double px, double py) { return scale(s,s,px,py); }
	public Layer scale(double sx, double sy) { return concatenate(sx,0,0,sy,0,0); }
	public Layer scale(double s) { return concatenate(s,0,0,s,0,0); }

	public Layer scale(double sx, double sy, double x, double y) {
		return this.concatenate(sx, 0, 0, sy, x-sx*x, y-sy*y);
//	     *          [   sx    0    x-sx*x  ]
//	     *          [   0     sy    y-sy*y ]

//		return this
//				.translate(+pivotX,+pivotY)
//				.scale(sx, sy)
//				.translate(-pivotX,-pivotY);
	}
	
	public Layer rotate(double theta, double x, double y) {
		final double cos = cos(theta), sin = sin(theta);
		return this.concatenate(cos, sin, -sin, cos, x-x*cos+y*sin, y-x*sin-y*cos);
//			     *          [   cos(theta)    -sin(theta)    x-x*cos+y*sin  ]
//	    	     *          [   sin(theta)     cos(theta)    y-x*sin-y*cos  ]

//		return this
//				.translate(pivotX,pivotY)
//				.rotate(theta)
//				.translate(-pivotX,-pivotY);
	}
	

	public Layer identity() {
		M[T00] = M[T11] = 1;
		M[T01] = M[T02] = 0;
		M[T10] = M[T12] = 0;
		
		return this;
	}
	
	public Layer concatenate(double scX, double shY, double shX, double scY, double tx, double ty) {
		final float m00 = (float) scX, m01 = (float) shX, m02 = (float) tx;
		final float m10 = (float) shY, m11 = (float) scY, m12 = (float) ty;
		
		final float M00 = M[T00], M01 = M[T01], M02 = M[T02];
		final float M10 = M[T10], M11 = M[T11], M12 = M[T12];

		M[T00]=m10*M01+m00*M00; M[T01]= m11*M01+m01*M00; M[T02]= M02+m12*M01+m02*M00;
		M[T10]=m10*M11+m00*M10; M[T11]= m11*M11+m01*M10; M[T12]= M12+m12*M11+m02*M10;
	
		if (M[T00]!=m00 || M[T10]!=m10 || M[T01]!=m01 || M[T11]!=m11 || M[T02]!=m02 || M[T12]!=m12)
			invalidateTransform();
		
		if (redraw)
			redraw();
		
		return this;
	}
	
	
	public static interface Location<T> { public T set(float x, float y); }
	public<T> T locate(int x, int y, Location<T> c) { //Project?
		float x_ = x;
		float y_ = y;
		
		return c.set(x_, y_);
	}

	
	public static interface Coordinate { public void set(float x, float y); }
	public void locate(int x, int y, Coordinate c) { //Project?
		float x_ = x;
		float y_ = y;
		
		c.set(x_, y_);
	}
	
//	{
//		Layer l = null;
//		l.locate(100, 100, (Coordinate) (x,y) -> System.out.println("huhu") );
//	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// DRAW EVENTS ////////////////////////////////////////
	
	// implement this
	// XXX maybe hand over delta transform?????????????
	public Runnable onTransformed = null;
	public final void onTransformed( Runnable pl ) { onTransformed = new Runnables(pl, onTransformed); }
	protected void onTransformed() { if (onTransformed!=null) onTransformed.run(); }
	
	
	private void invalidateTransform() {
		invalid = true;
		for (Layer child: children)
			child.invalidateTransform();
		
		onTransformed();
	}
	
	private boolean validateTransform() {
		if (!invalid)
			return false;
		
		if (parent==null)
			root.canvasTransform.getElements(W);
		else 
			if (parent.validateTransform())
				concatenate(parent.W,parent.M,W);
	
		invalid = false;
		return true;
	}
	
	public void setRedraw(boolean redraw) {
		this.redraw = redraw;
	}
	
	public void redraw() {
		if (isFinite()) {
			redraw(left, top, right-left, bottom-top, true);
		} else
			root.redraw();
	}
	
	private boolean dirty = false, dirtyAll = false;
	private float dirtyLeft, dirtyTop, dirtyRight, dirtyBottom;
	
	public void redraw(float x, float y, float width, float height, boolean all) {
		if (!dirty) { //if Event has not been scheduled, do so, and initialize dirty bounds 
			root.getDisplay().asyncExec(this);
			
			dirty = true;
			dirtyLeft = x;
			dirtyTop = y;
			dirtyRight = x+width;
			dirtyBottom = y+height;
			dirtyAll = all;
		} else { //otherwise just extend bounds of dirt.
			dirtyLeft = min(dirtyLeft,x);
			dirtyTop = min(dirtyTop,y);
			dirtyRight = max(dirtyRight,x+width);
			dirtyBottom = max(dirtyBottom,y+height);
			dirtyAll |= all;
		}
	}
	
	Bounds global = new Bounds();
	
	private void computeBounds(Bounds b, boolean recursive) {
		B[0] = B[4] = dirtyLeft;
		B[1] = B[3] = dirtyTop;
		B[2] = B[6] = dirtyRight;
		B[5] = B[7] = dirtyBottom;
		
		validateTransform();
		transform(M, B);
		transform(W, B);
		
		b.extend(B[0], B[1]);
		b.extend(B[2], B[3]);
		b.extend(B[4], B[5]);
		b.extend(B[6], B[7]);
		
		if (recursive)
			for (Layer child: children) {
				concatenate(W,M,child.W);
				child.computeBounds(b, recursive);
			}
	}
	
	final public void run() {
		final float globalLeft = global.left, globalTop = global.top;
		final float globalRight = global.right, globalBottom = global.bottom;
		
		computeBounds(global.clear(), dirtyAll&&!clipping);
		global.extend(globalLeft, globalTop).extend(globalRight, globalBottom);

		final int M = 1;
		
		int x = (int) floor(global.left)-M, y = (int) floor(global.top)-M;
		int w = (int) ceil(global.right)-x+M+M;
		int h = (int) ceil(global.bottom)-y+M+M;
		root.redraw(x, y, w, h, dirtyAll);
		dirty = false;
		
		computeBounds(global.clear(), false);
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// RENDERING /////////////////////////////////////////
	
	final public void paint(Rectangle clip, Transform t, Event e) {
		t.getElements(W);
		final float W00 = W[T00], W01 = W[T01], W02 = W[T02];
		final float W10 = W[T10], W11 = W[T11], W12 = W[T12];
		
		concatenate(W, M, W);
		t.setElements(W[T00], W[T10], W[T01], W[T11], W[T02], W[T12]);
		
		B[0] = B[4] = left;
		B[1] = B[3] = top;
		B[2] = B[6] = right;
		B[5] = B[7] = bottom;
		transform(W, B);
		//////
		
		e.gc.setTransform(t);
		
		//re-compute enclosing axis aligned bounding box
		global.clear()
		.extend(B[0], B[1])
		.extend(B[2], B[3])
		.extend(B[4], B[5])
		.extend(B[6], B[7]);
		
		boolean intersects = clip.intersects((int)left, (int)top, (int)(right-left), (int)(bottom-top));
		
		Region r = null, g = null;
		if (clipping) {
			r = new Region(e.display);
			e.gc.getClipping(r);

			
			int x = (int) floor(left), y = (int) floor(top);
			int w = (int) ceil(right)-x, h = (int) ceil(bottom)-y;
			
			g = new Region(e.display);
			g.add(r);
			g.intersect(x,y,w,h);
			
			e.gc.setClipping(g);
		}
		
		//render childnodes 
		//unless they have to be clipped and this Layer isn't visible
		if (!clipping || clipping && intersects)
			paint(clip, t, e, children);
				
		//only render this Layer if the GC's screen coordinate-transformed clips intersect that aabb 
		if (intersects)
			onPaint(e);

		if (clipping) {
			e.gc.setClipping(r);
			r.dispose();
			g.dispose();
		}
		
		//restore c's Transform
		t.setElements(W[T00]=W00,W[T10]=W10,W[T01]=W01,W[T11]=W11,W[T02]=W02,W[T12]=W12);
		e.gc.setTransform(t);
	}
	
	protected void paint(Rectangle clip, Transform t, Event e, Layer[] children) {
//		if (clipping)
		//TODO implement proper clipping
		// Apply to clip and also to GC c
		// but beware GC may be pre-transformed and setClipping may be excected in local coords			
		
		for (int I=children.length-1,i=0;I>=i;I--) {
			int x = clip.x, y = clip.y, w = clip.width, h = clip.height;

			if (clipping)
				global.get(clip);			
			
			
			children[I].paint(clip,t,e);
			
			clip.x = x;
			clip.y = y;
			clip.width = w;
			clip.height = h;
		}
		
		//extend bounds afterwards
		for (Layer child: children)
			global.extend(child.global);
	}
	
	// implement this
	protected void onPaint(Event e) {
		if (onPaint!=null)
			onPaint.handleEvent(e);
	}
	
	public Listener onPaint = null;
	public void onPaint( Listener pl ) {
		onPaint = new Listeners(pl, onPaint);
	}
	
	public final void onPaint( PaintListener pl ) {
		onPaint( (Listener) (e) -> pl.paintControl(new PaintEvent(e)) );
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// EVENT HANDLING //////////////////////////////////////////////////
	
	private final static int CLICK_RADIUS = 3, CLICK_PERIOD = 500;

	private boolean entered = false;
	private Layer captive = null;
	private int downX, downY, downT, downM;
	
	public void capture(Layer c) {
		captive = c;
		getParent().capture(c);
	}
	
	public Layer captive() {
		return captive;
	}
	
	final public void point(Event e) {
		validateTransform();
		final float m00 = W[T00], m01 = W[T01], m02 = W[T02];
		final float m10 = W[T10], m11 = W[T11], m12 = W[T12];
		
		float det = m00 * m11 - m01 * m10;
		float i00 = m11/det, i01 =-m01/det, i02 = (m01 * m12 - m11 * m02) / det;
		float i10 =-m10/det, i11 = m00/det, i12 = (m10 * m02 - m00 * m12) / det;
		
		float x = i00*e.x+i01*e.y+i02;
		float y = i10*e.x+i11*e.y+i12;
		
		boolean hit = x>=left && x<right && y>=top && y<bottom;

		for (Layer r: children)
			if (e.doit && (!clipping || clipping && hit))
				if (r.captive()==captive) //either captive == null, or set to a specific layer
					r.point(e);

		if (hit || captive==this) {
			if (e.doit && !entered) { 
				e.doit = !onMouseEnter();
				entered = true;
			}

			if (e.doit) {
				if (onMouseEvent(x,y,e)) {
					if (e.type==MouseDown)
						capture(this);
					
					e.doit = false;
				}
			}
					
			
			if (e.type==MouseDown) {
				downX = e.x;
				downY = e.y;
				downT = e.time;
				downM = e.stateMask;
			}
			
			if (e.type==MouseUp) {
				if (e.doit && e.time-downT<CLICK_PERIOD && hypot(e.x-downX, e.y-downY)<CLICK_RADIUS)
					onMouseClick(x,y,e.button,downM);
			}
			
		} else {
			if (entered) {
				e.doit = !onMouseExit();
				entered = false;
			}
		}

		if (e.type==MouseUp) 
			capture(null);
		
	}
	
	
	private boolean onMouseEvent(float x, float y, Event e) {
		switch (e.type) {
		default: return false;
		case MouseMove: return onMouseMove(x,y,e.stateMask);
		case MouseUp: return onMouseUp(x,y,e.button,e.stateMask);
		case MouseDown: return onMouseDown(x,y,e.button,e.stateMask);
		case MouseWheel: return onMouseWheel(x,y,e.count,e.stateMask);
		case MouseDoubleClick: return onDoubleClick(x,y,e.button,e.stateMask);
		}
	}

	protected boolean onMouseClick(float x, float y, int button, int modKeysAndButtons) { return false; }
	protected boolean onDoubleClick(float x, float y, int button, int modKeysAndButtons) { return false; }
	protected boolean onMouseDown(float x, float y, int button, int modKeysAndButtons) { return false; }

	protected boolean onMouseUp(float x, float y, int button, int modKeysAndButtons) { return false; }
	protected boolean onMouseMove(float x, float y, int modKeysAndButtons) { return false; }
	protected boolean onMouseWheel(float x, float y, int tickCount, int modKeysAndButtons) { return false; }

	protected boolean onMouseExit() { return false; }
	protected boolean onMouseEnter() { return false; }
	
	
	
	
	//ControlListener?
	//onTransformed!

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// HELPER FUNCTIONS //////////////////////////////////////////////////
	
	static float min(float a, float b) { return a<b?a:b; }
	static float max(float a, float b) { return a>b?a:b; }
	
	static float floor(float a) { return (float)Math.floor(a); }
	static float ceil(float a) { return (float)Math.ceil(a); }


	static void copy(float[] from, float[] to) {
		for (int i=0,I=from.length;i<I;i++)
			to[i] = from[i];
	}
	
	static void concatenate(float[] A, float W00, float W10, float W01, float W11, float W02, float W12) {
		concatenate(A, W00,W10,W01,W11,W02,W12, A);
	}
	
	static void concatenate(float[] A, float W00, float W10, float W01, float W11, float W02, float W12, float[] C ) {
		//read the parent's world matrix to M
		final float w00 = A[T00], w01 = A[T01], w02 = A[T02];
		final float w10 = A[T10], w11 = A[T11], w12 = A[T12];
		
		//compute W = T.(M.I)
		C[T00] = W10*w01+W00*w00; C[T01] = W11*w01+W01*w00; C[T02] = w02+W12*w01+W02*w00;
		C[T10] = W10*w11+W00*w10; C[T11] = W11*w11+W01*w10; C[T12] = w12+W12*w11+W02*w10;
	}
	
	static void concatenate(float[] A, float[] B, float[] C) {
		concatenate(A, B[T00], B[T10], B[T01], B[T11], B[T02], B[T12], C);
	}
	
	
	static void transform(float[] A, float v[]) {
		for (int i=0,I=v.length/2;i<I;i++) {
			final int x = i, y = x+1;
			final float vx = A[0]*v[x]+A[2]*v[y]+A[4];
			final float vy = A[1]*v[x]+A[3]*v[y]+A[5];
			v[x] = vx;
			v[y] = vy;
		}
	}
	
	
	static float min(float a, float b, float c, float d) {
		final float ab = a<b?a:b;
		final float cd = c<d?c:d;
		return ab<cd?ab:cd;
	}
	
	static float max(float a, float b, float c, float d) {
		final float ab = a>b?a:b;
		final float cd = c>d?c:d;
		return ab>cd?ab:cd;
	}
	
	
//	private static void transform(float[] A, float V[]) {
//		for (int col=0,cols=V.length/3, cell =0;col<cols;col++) {
//			final int c0=cell++,c1=cell++,c2=cell++;
//			final float v0=V[cell++], v1=V[cell++], v2=V[cell++];
//			V[c0] = v0*A[T00]+v1*A[T01]+v2*A[T02];
//			V[c1] = v0*A[T10]+v1*A[T11]+v2*A[T12];
//			V[c2] = v0*A[T20]+v1*A[T21]+v2*A[T22];
//		}
//	}
	
	static void identity(float[] W) {
		W[T00] = W[T11] = 1;
		W[T01] = W[T02] = 0;
		W[T10] = W[T12] = 0;
	}

}

