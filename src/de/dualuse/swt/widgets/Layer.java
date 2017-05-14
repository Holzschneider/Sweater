package de.dualuse.swt.widgets;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.dualuse.swt.events.Listeners;
import de.dualuse.swt.events.Runnables;

public class Layer implements LayerLocator, LayerContainer, Runnable {
	final static private int T00=0, T01=1, T10=2, T11=3, T02=4, T12=5; //, T20=3, T21=4, T22=5;

	/// Attributes
	private Layer parent; 
	private LayerCanvas root;
	private Layer children[] = {};
	
	private int transformCount = 0;
	private int parentCount = 0;
	private int rootCount = 0;
	
	///XXX need M Model Matrix, W World Matrix, P Parent Matrix -> no inversion ever
	
	private float M[] = new float[6]; // the local transformation Matrix
	private float W[] = new float[6]; // the local transformation Matrix
	private float P[] = new float[6]; // the world matrix where this layer is anchored to (the parents' transformations)
	private float I[] = new float[6]; // the inverse world matrix
	
	private float B[] = new float[4*2]; // the oriented bounds of this Layer in world coordinates
	
	private boolean redraw = true; //whether redraws are triggerd upon change 
	
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
	
	//XXX add bounds changing listener with own Listener type with event supplying the changes in bounds!
	
	
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
		if (M[T00]==1 && M[T11]==1 && M[T01]==0 && M[T02]==0 && M[T10]==0 && M[T12]==0)
			return this;
		
		M[T00] = M[T11] = 1;
		M[T01] = M[T02] = 0;
		M[T10] = M[T12] = 0;

		invalidateTransform();
		
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
	
	
	
	private float cursorX=0, cursorY=0;
	public LayerLocator locate(final double x, final double y) {
		this.cursorX = (float) x;
		this.cursorY = (float) y;
		return this;
	}
	
	@Override
	public <T> T on(Layer lay, LayerLocation<T> loc) {
		validateTransform();
		float X = W[T00]*cursorX+W[T01]*cursorY+W[T02], Y = W[T10]*cursorX+W[T11]*cursorY+W[T12];
		
		lay.validateTransform();
		float det = W[T00] * W[T11] - W[T01] * W[T10];
		float i00 = W[T11]/det, i01 =-W[T01]/det, i02 = (W[T01] * W[T12] - W[T11] * W[T02]) / det;
		float i10 =-W[T10]/det, i11 = W[T00]/det, i12 = (W[T10] * W[T02] - W[T00] * W[T12]) / det;
		
		return loc.define(i00*X+i01*Y+i02, i10*X+i11*Y+i12);
	}			
	
	@Override
	public <T> T on(LayerCanvas lc, LayerLocation<T> l) {
		if (lc != root)
			throw new IllegalArgumentException("Layer can only be located on the LayerCanvas, it has been added to.");
		
		validateTransform();
		float X = W[T00]*cursorX+W[T01]*cursorY+W[T02], Y = W[T10]*cursorX+W[T11]*cursorY+W[T12];
		
		return l.define(X, Y);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// DRAW EVENTS ////////////////////////////////////////
	
//	// implement this
	public Runnable onTransformed = null; //XXX maybe define own listener type with delta transformation in event
	public final void onTransformed( Runnable pl ) { onTransformed = new Runnables(pl, onTransformed); }
	protected void onTransformed() { 
		if (onTransformed!=null) 
			onTransformed.run(); 
	}
	
	public static interface LayerTransform {
		void set(double scalex, double shearY, double shearX, double scaleY, double translationX, double translationY);
	}
	
	public void getLayerTransform(LayerTransform lt) {
		lt.set(M[T00],M[T10],M[T01],M[T11],M[T02],M[T12]);		
	}
	
	public void getCanvasTransform(LayerTransform lt) {
		validateTransform();
		lt.set(W[T00],W[T10],W[T01],W[T11],W[T02],W[T12]);
	}
	
	protected void invalidateTransform() {
		transformCount++;
		root.rootCount++;
		
		onTransformed();
	}
	
	protected boolean validateTransform() {
		if (root.rootCount==rootCount)
			return false;
		
		if (parent==null) {
			if (root.transformCount!=parentCount) {
				root.canvasTransform.getElements(W);
				concatenate(W, M, W);
				parentCount = root.transformCount;
				return true;
			} else 
				return false;
		} else {
			parent.validateTransform();
			
			if (parent.transformCount!=parentCount) {
				concatenate(parent.W, M, W);
				parentCount = parent.transformCount;
				return true;
			} else
				return false;
		}			
	}
	
	public void setRedraw(boolean redraw) {
		this.redraw = redraw;
	}
	
	public void redraw() {
		if (isFinite()) {
			redraw(left, top, right-left, bottom-top, true);
		} else
			if (!dirty) {
				root.redraw();
				dirty = true;
			}
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
	
	private void computeDirtyBounds(Bounds b, boolean recursive) {
		B[0] = B[4] = dirtyLeft;
		B[1] = B[3] = dirtyTop;
		B[2] = B[6] = dirtyRight;
		B[5] = B[7] = dirtyBottom;
		
		validateTransform();
		transform(W, B);
		
		b.extend(B[0], B[1]);
		b.extend(B[2], B[3]);
		b.extend(B[4], B[5]);
		b.extend(B[6], B[7]);
		
		if (recursive)
			for (Layer child: children) {
//				child.validateTransform();
				child.rootCount = rootCount;
				child.parentCount = transformCount;
				concatenate(W, child.M, child.W);
				child.computeDirtyBounds(b, recursive);
			}
	}
	
	final public void run() {
		final float globalLeft = global.left, globalTop = global.top;
		final float globalRight = global.right, globalBottom = global.bottom;
		
		computeDirtyBounds(global.clear(), dirtyAll&&!clipping);
		global.extend(globalLeft, globalTop).extend(globalRight, globalBottom);

		// XXX if this redraw has been triggered by transforms only and global bounds havenot been changed 
		// -> dont trigger redraw 
		
		root.redraw( // root / canvas coordinates are never negative, so integer truncation effects do not matter
				(int)global.left, (int) global.top, 
				(int)global.right -(int)global.left, 
				(int)global.bottom-(int)global.top, 
				dirtyAll);
		
		dirty = false;
		
		computeDirtyBounds(global.clear(), false); //necessary?
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// RENDERING /////////////////////////////////////////
	
	final public void paint(Rectangle clip, Transform t, Event e) {
		// update Transform
		t.getElements(W);
		
		final float W00 = W[T00], W01 = W[T01], W02 = W[T02];
		final float W10 = W[T10], W11 = W[T11], W12 = W[T12];

		concatenate(W, M, W);

		//re-compute enclosing axis aligned bounding box
		//dirty.setBounds(this)
		dirtyLeft = left;
		dirtyRight = right;
		dirtyTop = top;
		dirtyBottom = bottom;
		dirty = false;
		computeDirtyBounds(global, false);

		boolean intersects = clip.intersects(
				(int)global.left, (int)global.top, 
				(int)global.getWidth(), (int)global.getHeight());
		
		
		if (clipping) {
			if (intersects) {
				Region parentClipRegion = new Region(e.display);
				Region layerClipRegion = new Region(e.display);
	
				e.gc.getClipping(parentClipRegion); //store parent's clip region in parent's coordinate system
				
				e.gc.setTransform(t); // apply layer's Transform;
				e.gc.getClipping(layerClipRegion); //read parent's clip region in layer's coordinates (int truncation!)
				int x = (int) floor(left), y = (int) floor(top);
				int w = (int) ceil(right)-x, h = (int) ceil(bottom)-y;
				layerClipRegion.intersect(x,y,w,h); //intersect with layer's bounds
				
				e.gc.setClipping(layerClipRegion); //set clipping
				
				//// PAINT ////
				paint(clip, t, e, children);
				onPaint(e);
				//// PAINT ////
				
				t.setElements(W00,W10,W01,W11,W02,W12);
				e.gc.setTransform(t);
				e.gc.setClipping(parentClipRegion);
	
				parentClipRegion.dispose();
				layerClipRegion.dispose();
			}
		} else {
			e.gc.setTransform(t);

			//
			paint(clip, t, e, children);
					
			//only render this Layer if the GC's screen coordinate-transformed clips intersect that aabb 
			if (intersects)
				onPaint(e);
			
			t.setElements(W00,W10,W01,W11,W02,W12);
			e.gc.setTransform(t);
		}
		
	}
	
	protected void paint(Rectangle clip, Transform t, Event e, Layer[] children) {
//		if (clipping)
		//TODO implement proper clipping
		// Apply to clip and also to GC c
		// but beware GC may be pre-transformed and setClipping may be excected in local coords			
		
		for (int I=children.length-1,i=0;I>=i;I--) {
			int x = clip.x, y = clip.y, w = clip.width, h = clip.height;

			if (clipping) { 
				//if clipping is enabled, then tighten clipping rectangle by this layer's bounds
				global.get(clip);
				clip.intersects(x, y, w, h);
			}
			
			children[I].paint(clip,t,e);
			
			clip.x = x;
			clip.y = y;
			clip.width = w;
			clip.height = h;
		}
		
		//extend bounds afterwards
		if (!clipping)
			for (Layer child: children)
				global.extend(child.global);
	}
	
	
	private Listener onPaint = null;
	public void onPaint( Listener pl ) { onPaint = new Listeners(pl, onPaint); }
	public final void onPaint( PaintListener pl ) { onPaint( (Listener) (e) -> pl.paintControl(new PaintEvent(e)) ); }
	public final void addPaintListener( PaintListener pl ) { onPaint(pl); };
	
	// implement this
	protected void onPaint(Event e) {
		if (onPaint!=null)
			onPaint.handleEvent(e);
	}
	

	

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////// MOUSE EVENT HANDLING ///////////////////////////////////////////////
	
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
		final float w00 = W[T00], w01 = W[T01], w02 = W[T02];
		final float w10 = W[T10], w11 = W[T11], w12 = W[T12];
		
		float det = w00 * w11 - w01 * w10;
		float i00 = w11/det, i01 =-w01/det, i02 = (w01 * w12 - w11 * w02) / det;
		float i10 =-w10/det, i11 = w00/det, i12 = (w10 * w02 - w00 * w12) / det;
		
		float x = i00*e.x+i01*e.y+i02;
		float y = i10*e.x+i11*e.y+i12;
		
		boolean hit = x>=left && x<right && y>=top && y<bottom;

		for (Layer r: children)
			if (r.mouseListenerCount>0) //only if there are mouse listeners anyways
				if (e.doit) //only if it shall happen
					if (!clipping || clipping && hit) // if clipping hits or there s no clipping at all
					if (r.captive()==captive) //either captive == null, or set to a specific layer
						r.point(e);

		if (hit || captive==this) {
			if (e.doit && !entered) { 
				onMouseEnter(x,y,e);
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
					onMouseClick(x,y,e);
			}
			
		} else {
			if (entered) {
				onMouseExit(x,y,e);
				entered = false;
			}
		}

		if (e.type==MouseUp) 
			capture(null);
		
	}
	
	
	private boolean onMouseEvent(float x, float y, Event e) {
		switch (e.type) {
		default: return false;
		case MouseMove: onMouseMove(x,y,e); break;
		case MouseUp: onMouseUp(x,y,e); break;
		case MouseDown: onMouseDown(x,y,e); break;
		case MouseWheel: onMouseWheel(x,y,e); break;
		case MouseDoubleClick: onMouseDoubleClick(x,y,e); break;
		}
		return true;
	}
	

	private Listeners joinListeners(Listener a, Listener b) {
		addMouseListeners(+1);

		return new Listeners(a,b); 
	} 
	
	private Listener excludeListeners(Listener group, Listener leaver) {
		if (group==leaver)
			return null;

		addMouseListeners(-1);
		return ((Listeners)group).exclude(leaver); 
	}
	
	private int mouseListenerCount = hasMouseHandler()?1:0;
	private void addMouseListeners(int count) {
		if (parent!=null)
			parent.addMouseListeners(count);
		
		mouseListenerCount+=count;
	}
	
	
	private static Class<?>[] parameterTypes = new Class<?>[] { float.class,float.class,Event.class };
	private static String[] methodNames = { 
			"onMouseDown", 
			"onMouseUp", 
			"onMouseClick", 
			"onMouseDoubleClick", 
			"onMouseMove",
			"onMouseEnter",
			"onMouseExit",
			"onMouseWheel",
		};
	
	protected boolean hasMouseHandler() {
		Class<?> c = this.getClass();
		
		if (c.equals(Layer.class)) 
			return false;
		
		try {
			for (String methodName: methodNames)
				if (!c.getMethod(methodName,parameterTypes).getDeclaringClass().equals(Layer.class))
					return true;
		} catch (Exception ex) {
			throw new Error(ex);
		};
		
		return false;	
	}
	
	
	private Listener onMouseClick = null, onMouseDoubleClick = null, onMouseDown = null, onMouseUp = null;
	private Listener onMouseMove = null, onMouseWheel = null, onMouseEnter = null, onMouseExit = null;
	protected Layer onMouseClick(Listener l) { onMouseClick = joinListeners(l,onMouseClick); return this; }
	protected Layer onMouseDoubleClick(Listener l) { onMouseDoubleClick = joinListeners(l,onMouseDoubleClick); return this; }
	protected Layer onMouseDown(Listener l) { onMouseDown = joinListeners(l,onMouseDown); return this; }
	protected Layer onMouseUp(Listener l) { onMouseUp = joinListeners(l,onMouseUp); return this;  }
	protected Layer onMouseMove(Listener l) { onMouseMove = joinListeners(l,onMouseMove); return this; }
	protected Layer onMouseWheel(Listener l) { onMouseWheel = joinListeners(l,onMouseWheel); return this; }
	protected Layer onMouseEnter(Listener l) { onMouseEnter = joinListeners(l,onMouseEnter); return this; }
	protected Layer onMouseExit(Listener l) { onMouseExit = joinListeners(l,onMouseExit); return this; }

	protected void onMouseClick(float x, float y, Event e) { defaultHandleMouseEvent(onMouseClick, e); }
	protected void onMouseDoubleClick(float x, float y, Event e) { defaultHandleMouseEvent(onMouseDoubleClick, e); }
	protected void onMouseDown(float x, float y, Event e) { defaultHandleMouseEvent(onMouseDown, e); }
	protected void onMouseUp(float x, float y, Event e) { defaultHandleMouseEvent(onMouseUp, e); }
	protected void onMouseMove(float x, float y, Event e) { defaultHandleMouseEvent(onMouseMove, e); }
	protected void onMouseWheel(float x, float y, Event e) { defaultHandleMouseEvent(onMouseWheel, e); }
	protected void onMouseEnter(float x, float y, Event e) { defaultHandleMouseEvent(onMouseEnter, e); }
	protected void onMouseExit(float x, float y, Event e) { defaultHandleMouseEvent(onMouseExit, e); }

	private void defaultHandleMouseEvent(Listener l,Event e) { if (l!=null) l.handleEvent(e); }
	

	public void addListener(int eventType, Listener l) {
		switch (eventType) {
		case SWT.Paint: onPaint = joinListeners(l,onPaint); break;
		
		case SWT.MouseDoubleClick: onMouseDoubleClick = joinListeners(l,onMouseDoubleClick); break;
		case SWT.MouseDown: onMouseDown = joinListeners(l,onMouseDown); break;
		case SWT.MouseUp: onMouseUp = joinListeners(l, onMouseUp); break;
		case SWT.MouseMove: onMouseMove = joinListeners(l, onMouseMove); break;
		case SWT.MouseWheel: onMouseWheel = joinListeners(l, onMouseWheel); break;
		case SWT.MouseEnter: onMouseEnter = joinListeners(l, onMouseEnter); break;
		case SWT.MouseExit: onMouseExit = joinListeners(l, onMouseExit); break;
		
		default:
			throw new IllegalArgumentException("Event Type not supported by Layer");
		}
	}
	
	public void removeListener(int eventType, Listener l) {
		switch (eventType) {
		case SWT.Paint: onPaint = excludeListeners(l,onPaint); break;
		
		case SWT.MouseDoubleClick: onMouseDoubleClick = excludeListeners(l,onMouseDoubleClick); break;
		case SWT.MouseDown: onMouseDown = excludeListeners(l,onMouseDown); break;
		case SWT.MouseUp: onMouseUp = excludeListeners(l, onMouseUp); break;
		case SWT.MouseMove: onMouseMove = excludeListeners(l, onMouseMove); break;
		case SWT.MouseWheel: onMouseWheel = excludeListeners(l, onMouseWheel); break;
		case SWT.MouseEnter: onMouseEnter = excludeListeners(l, onMouseEnter); break;
		case SWT.MouseExit: onMouseExit = excludeListeners(l, onMouseExit); break;

		default:
			throw new IllegalArgumentException("Event Type not supported by Layer");
		}
	}
	
	
	
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

