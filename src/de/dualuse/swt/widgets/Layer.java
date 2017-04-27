package de.dualuse.swt.widgets;

import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.sin;
import static org.eclipse.swt.SWT.MouseDoubleClick;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.MouseWheel;

import java.util.Arrays;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Event;

//missing things
// paint clipping
public class Layer implements LayerContainer, Runnable {
	final static private int T00=0, T01=1, T10=2, T11=3, T02=4, T12=5; //, T20=3, T21=4, T22=5;

	/// Attributes
	private Layer parent; 
	private LayerCanvas root;
	private Layer children[] = {};
	private float M[] = new float[6]; // the local transformation Matrix
	private float W[] = new float[6]; // the world matrix of this Layer, the last time it was rendered
//	private float B[] = new float[8*3]; // the oriented bounds of this Layer in world coordinates
	
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

	public Layer scale(double sx, double sy, double pivotX, double pivotY) { 
		this
				.translate(+pivotX,+pivotY)
				.scale(sx, sy)
				.translate(-pivotX,-pivotY);
		
		return this;
	}
	
	public Layer rotate(double theta, double pivotX, double pivotY) { 
		return this
				.translate(pivotX,pivotY)
				.rotate(theta)
				.translate(-pivotX,-pivotY);
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

		if (redraw)
			redraw();
		
		return this;
	}
	
	
	////////// READOUT
	
	//XXX redo transform with loops and accept variable sized m assuming any 3-by-k Matrices or even a 2-by-1 Vector!
	public Layer transform(float[] m) {
		concatenate(M, m, m);
		return this;
	}



	
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// DRAW EVENTS ////////////////////////////////////////

	
	public void setRedraw(boolean redraw) {
		this.redraw = redraw;
	}
	
	public void redraw() {
		if (isFinite())
			redraw(left, top, right-left, bottom-top, true);
		else
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
	
	final public void run() {
		//TODO check whether the world transformation actually changed
		LayerContainer r = getParent();
	
		float left = dirtyLeft, top = dirtyTop, right = dirtyRight, bottom = dirtyBottom;
		
		// Bounding box as it's been last time in drawing
		final float _ax = left*W[T00]+top*W[T01]+W[T02], _ay = left*W[T10]+top*W[T11]+W[T12];
		final float _bx = right*W[T00]+top*W[T01]+W[T02], _by = right*W[T10]+top*W[T11]+W[T12];
		final float _cx = left*W[T00]+bottom*W[T01]+W[T02], _cy = left*W[T10]+bottom*W[T11]+W[T12];
		final float _dx = right*W[T00]+bottom*W[T01]+W[T02], _dy = right*W[T10]+bottom*W[T11]+W[T12];

		identity(W);
		transform(W); /// W = M.I

		final float s00 = M[T00], s01 = M[T01], s02 = M[T02];
		final float s10 = M[T10], s11 = M[T11], s12 = M[T12];

		identity(M);
		r.transform(M);
		concatenate(M, W, W);
		M[T00] = s00; M[T01] = s01; M[T02] = s02;
		M[T10] = s10; M[T11] = s11; M[T12] = s12;
		
		// Bounds as it's after rebuilding W 
		final float ax_ = left*W[T00]+top*W[T01]+W[T02], ay_ = left*W[T10]+top*W[T11]+W[T12];
		final float bx_ = right*W[T00]+top*W[T01]+W[T02], by_ = right*W[T10]+top*W[T11]+W[T12];
		final float cx_ = left*W[T00]+bottom*W[T01]+W[T02], cy_ = left*W[T10]+bottom*W[T11]+W[T12];
		final float dx_ = right*W[T00]+bottom*W[T01]+W[T02], dy_ = right*W[T10]+bottom*W[T11]+W[T12];
		
		/// Merge Bounds (TODO aaaactually if the bounds dont overlap due to large changes, there's no need for merging)
		final float ax = min(_ax,ax_), ay = min(_ay,ay_), Ax = max(_ax,ax_), Ay = max(_ay,ay_);
		final float bx = min(_bx,bx_), by = min(_by,by_), Bx = max(_bx,bx_), By = max(_by,by_);
		final float cx = min(_cx,cx_), cy = min(_cy,cy_), Cx = max(_cx,cx_), Cy = max(_cy,cy_);
		final float dx = min(_dx,dx_), dy = min(_dy,dy_), Dx = max(_dx,dx_), Dy = max(_dy,dy_);
		
		//compute enclosing axis aligned bounding box
		left = floor(min(min(ax,bx),min(cx,dx)));
		top = floor(min(min(ay,by),min(cy,dy)));
		right = ceil(max(max(Ax,Bx),max(Cx,Dx)));
		bottom = ceil(max(max(Ay,By),max(Cy,Dy)));
		
		if (dirtyAll&&!clipping) {
			//TODO also traverse childnodes and extend the rectangle by their transformed bounding boxes
		}
		
		final int M = 2; 
		root.redraw((int)left-M,(int)top-M, (int)right-(int)left+M+M, (int)bottom-(int)top+M+M, dirtyAll);
		dirty = false;
	};
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// RENDERING /////////////////////////////////////////
	
	
	final public void render(Rectangle clip, Transform t, GC c) {
		//read out and store this matrix
		final float s00 = M[T00], s01 = M[T01], s02 = M[T02];
		final float s10 = M[T10], s11 = M[T11], s12 = M[T12];
		
		//////// BUILD THE CURRENT WORLD MATRIX: W = T.(M.I) 
		identity(W); /// = I
		
		//transform the identity matrix by this Layers's matrix
		transform(W); /// = M.I
		
		t.getElements(M); //read world matrix to M
		
		concatenate(M, W, W); /// = T.M.I
		
		//////////////////////// Render Node and Childnodes

		//XXX CAUTION! the method's parameter description seems to be wrong, it sez: T00, T01, T10, T11, T02, T12
		t.setElements( 	W[T00], W[T10],  W[T01], W[T11],  W[T02], W[T12]  );
		
		c.setTransform(t);
		
		//transform this Layers's bounds with M
		
		final float W00 = W[T00], W01 = W[T01], W02 = W[T02];
		final float W10 = W[T10], W11 = W[T11], W12 = W[T12];

		final float ax = left*W00+top*W01+W02, ay = left*W10+top*W11+W12;
		final float bx = right*W00+top*W01+W02, by = right*W10+top*W11+W12;
		final float cx = left*W00+bottom*W01+W02, cy = left*W10+bottom*W11+W12;
		final float dx = right*W00+bottom*W01+W02, dy = right*W10+bottom*W11+W12;

		//compute enclosing axis aligned bounding box
		final float left = min(min(ax,bx),min(cx,dx));
		final float top = min(min(ay,by),min(cy,dy));
		final float right = max(max(ax,bx),max(cx,dx));
		final float bottom = max(max(ay,by),max(cy,dy));
		
		boolean intersects = clip.intersects((int)left, (int)top, (int)(right-left), (int)(bottom-top));
		
		//render childnodes 
		//unless they have to be clipped and this Layer isn't visible
		if (!clipping || clipping && intersects)
			render(clip, t, c, children);
				
		//only render this Layer if the GC's screen coordinate-transformed clips intersect that aabb 
		if (intersects)
			render(c);
		
		//restore c's Transform
		t.setElements(M[0],M[1],M[2],M[3],M[4],M[5]);
		c.setTransform(t);

		//restore own Layers Matrix
		M[T00] = s00; M[T01] = s01; M[T02] = s02;
		M[T10] = s10; M[T11] = s11; M[T12] = s12;
	}
	
	protected void render(Rectangle clip, Transform t, GC c, Layer[] children) {
//		if (clipping)
		//TODO implement proper clipping
		// Apply to clip and also to GC c
		// but beware GC may be pre-transformed and setClipping may be excected in local coords			
		
		for (int I=children.length-1,i=0;I>=i;I--)
			children[I].render(clip,t,c);

	}
	
	// implement this
	protected void render(GC c) {}

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

	static void concatenate(float[] A, float B[], float[] AB) {
		//read out this matrix
		final float W00 = B[T00], W01 = B[T01], W02 = B[T02];
		final float W10 = B[T10], W11 = B[T11], W12 = B[T12];
		
		//read the parent's world matrix to M
		final float w00 = A[T00], w01 = A[T01], w02 = A[T02];
		final float w10 = A[T10], w11 = A[T11], w12 = A[T12];
		
		//compute W = T.(M.I)
		AB[T00] = W10*w01+W00*w00; AB[T01] = W11*w01+W01*w00; AB[T02] = w02+W12*w01+W02*w00;
		AB[T10] = W10*w11+W00*w10; AB[T11] = W11*w11+W01*w10; AB[T12] = w12+W12*w11+W02*w10;
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

