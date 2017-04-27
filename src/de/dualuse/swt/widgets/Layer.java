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
// auto-redraw on change
public class Layer implements LayerContainer {
	final static private int T00 = 0, T01 = 1, T10 = 2, T11 = 3, T02 = 4, T12 = 5;

	/// Attributes
	private Layer parent; 
	private LayerCanvas root;
	private Layer children[] = {};
	private float M[] = new float[6]; // the local transformation Matrix
	private float W[] = new float[6]; // the world matrix of this doodad, the last time it was rendered

	///
	private float left = -1f/0f, top= -1f/0f;
	private float right = -1f/0f, bottom = -1f/0f;

	public float getLeft() { return left; }
	public float getRight() { return right; }
	public float getTop() { return top; }
	public float getBottom() { return bottom; }
	
	public float getWidth() { return right-left; }
	public float getHeight() { return bottom-top; }
	
	public Layer setBounds(double left, double top, double right, double bottom) {
		this.left = (float) left;
		this.top = (float) top;
		this.right = (float) right;
		this.bottom = (float) bottom;
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
		parent.removeLayer(this);
		setRoot(null);
		parent = null;
	}

	
	public Layer addLayer(Layer r) {
		if (r.setParentLayer(this))
			((children = Arrays.copyOf(children, children.length+1))[children.length-1]=r).setRoot(root);

		return this;
	}

	public Layer removeLayer(Layer r) {
		for (int i=0,I=children.length;i<I;i++)
			if (children[i]==r) {
				r.setParentLayer(null);
				r.setRoot(null);
				children[i] = children[children.length-1];
				children = Arrays.copyOf(children, children.length-1);
				return this;
			}
		
		return this;
	}
	
	
	public boolean setParentLayer(Layer r) {
		if (parent==r)
			return false;
		
		if (parent!=null) 
			parent.removeLayer(this);
			
		parent = r;
		
		if (parent!=null)
			parent.addLayer(this);
		
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
		LayerContainer p = getParentLayer();
		Layer[] cs = p.getLayers();

		int ir = p.indexOf(this);
		
		for (int j=ir;j>=1;j--) {
			Layer t = cs[j];
			cs[j] = cs[j-1];
			cs[j-1] = t;
		}
	}
	
	
	public void moveBelow(Layer r) {
		LayerContainer p = getParentLayer();
		Layer[] cs = p.getLayers();

		int ir = p.indexOf(this);
		
		for (int j=ir,J=cs.length;j<J-1;j++) {
			Layer t = cs[j];
			cs[j] = cs[j+1];
			cs[j+1] = t;
		}		
	}
	
	public Layer getParentLayer() { return parent; }
	
	public LayerCanvas getRoot() { return root; }
	protected void setRoot(LayerCanvas root) { 
		this.root = root;
		for (Layer child: children)
			child.setRoot(root);
	}
	
	public void redraw() {
		Layer r = getParentLayer();
		if (r!=null)
			r.redraw();
		
	}
	
	
	public void redraw(float x, float y, float width, float height, boolean all) {
		redraw();
		//XXX also do this with forward transformed region 
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////// TRANSFORMATION ///////////////////////////////////////////////////
	
	public Layer rotate(double theta) { return concatenate(cos(theta),sin(theta),-sin(theta),cos(theta),0,0); }
	public Layer translate(double tx, double ty) { return concatenate(1,0,0,1,tx,ty); }
	public Layer scale(double sx, double sy) { return concatenate(sx,0,0,sy,0,0); }
	public Layer scale(double s) { return concatenate(s,0,0,s,0,0); }

	public Layer scale(double sx, double sy, double pivotX, double pivotY) { 
		return this
				.translate(+pivotX,+pivotY)
				.scale(sx, sy)
				.translate(-pivotX,-pivotY); 
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

		return this;
	}
	
	////////// READOUT
	
	//XXX redo transform with loops and accept variable sized m assuming any 3-by-k Matrices or even a 2-by-1 Vector!
	public Layer transform(float[] m) {
		final float l00 = M[T00], l01 = M[T01], l02 = M[T02];
		final float l10 = M[T10], l11 = M[T11], l12 = M[T12];

		final float r00 = m[T00], r01 = m[T01], r02 = m[T02];
		final float r10 = m[T10], r11 = m[T11], r12 = m[T12];
				
		m[T00]=r10*l01+r00*l00; m[T01]= r11*l01+r01*l00; m[T02]= l02+r12*l01+r02*l00;
		m[T10]=r10*l11+r00*l10; m[T11]= r11*l11+r01*l10; m[T12]= l12+r12*l11+r02*l10;
		
		M[T00] = l00; M[T01] = l01; M[T02] = l02;
		M[T10] = l10; M[T11] = l11; M[T12] = l12;
		return this;
		
	}



	////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// RENDERING /////////////////////////////////////////
	
	
	
	final public void render(Rectangle clip, Transform t, GC c) {
		//read out and store this matrix
		final float s00 = M[T00], s01 = M[T01], s02 = M[T02];
		final float s10 = M[T10], s11 = M[T11], s12 = M[T12];
		
		//////// BUILD THE CURRENT WORLD MATRIX: W = T.(M.I) 
		W[T00] = W[T11] = 1;
		W[T01] = W[T02] = 0;
		W[T10] = W[T12] = 0;

		//transform the identity matrix by this Doodad's matrix
		transform(W); /// W = M.I
		
		//read out this matrix
		final float m00 = W[T00], m01 = W[T01], m02 = W[T02];
		final float m10 = W[T10], m11 = W[T11], m12 = W[T12];
		
		//read world matrix to M
		t.getElements(M);

		final float M00 = M[T00], M01 = M[T01], M02 = M[T02];
		final float M10 = M[T10], M11 = M[T11], M12 = M[T12];
		
		//compute W = T.(M.I)
		final float N00 = m10*M01+m00*M00, N01 = m11*M01+m01*M00, N02 = M02+m12*M01+m02*M00;
		final float N10 = m10*M11+m00*M10, N11 = m11*M11+m01*M10, N12 = M12+m12*M11+m02*M10;

		
		W[T00] = N00; W[T01] = N01; W[T02] = N02;
		W[T10] = N10; W[T11] = N11; W[T12] = N12;
		
		//////////////////////// Render Node and Childnodes
		
		t.setElements(
				N00, N10, N01, N11, 
				N02, N12);
		
		
		c.setTransform(t);
		
		//transform this doodad's bounds with M
		float ax = left*M00+top*M01+M02, ay = left*M10+top*M11+M12;
		float bx = right*M00+top*M01+M02, by = right*M10+top*M11+M12;
		float cx = left*M00+bottom*M01+M02, cy = left*M10+bottom*M11+M12;
		float dx = right*M00+bottom*M01+M02, dy = right*M10+bottom*M11+M12;

		//compute enclosing axis aligned bounding box
		float left = min(min(ax,bx),min(cx,dx));
		float top = min(min(ay,by),min(cy,dy));
		float right = max(max(ax,bx),max(cx,dx));
		float bottom = max(max(ay,by),max(cy,dy));
		
		boolean intersects = clip.intersects((int)left, (int)top, (int)(right-left), (int)(bottom-top));
		
		//render childnodes 
		//unless they have to be clipped and this Doodad isn't visible
		if (!clipping || clipping && intersects)
			render(clip, t, c, children);
				
		//only render this Doodad if the GC's screen coordinate-transformed clips intersect that aabb 
		if (intersects)
			render(c);
		
		//restore c's Transform
		t.setElements(M[0],M[1],M[2],M[3],M[4],M[5]);
		c.setTransform(t);

		//restore own Doodads Matrix
		M[T00] = s00; M[T01] = s01; M[T02] = s02;
		M[T10] = s10; M[T11] = s11; M[T12] = s12;
	}
	
	protected void render(Rectangle clip, Transform t, GC c, Layer[] children) {
//		if (clipping)
		//TODO implement proper clipping
		// Apply to clip and also to GC c
		// but beware GC may be pre-transformed and setClipping may be excected in local coords			
		
		for (Layer r: children)
			r.render(clip, t, c);
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
		Layer r = getParentLayer();
		if (r!=null)
			r.capture(c);
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
			if (e.doit && (!clipping || clipping && hit)) { //TODO test mouse clipping!
				if (r==null)
					System.out.println("WHAT?");
				if (r.captive()==captive)
					r.point(e);
			}

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
	
	//////////////
	
	private static float min(float a, float b) { return a<b?a:b; }
	private static float max(float a, float b) { return a>b?a:b; }

}

