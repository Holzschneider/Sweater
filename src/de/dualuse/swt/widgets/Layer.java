package de.dualuse.swt.widgets;

import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.sin;
import static org.eclipse.swt.SWT.MouseDoubleClick;
import static org.eclipse.swt.SWT.MouseDown;
import static org.eclipse.swt.SWT.MouseEnter;
import static org.eclipse.swt.SWT.MouseExit;
import static org.eclipse.swt.SWT.MouseMove;
import static org.eclipse.swt.SWT.MouseUp;
import static org.eclipse.swt.SWT.MouseWheel;
import static org.eclipse.swt.SWT.Move;
import static org.eclipse.swt.SWT.Paint;
import static org.eclipse.swt.SWT.Resize;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TypedListener;

import de.dualuse.swt.events.Listeners;
import de.dualuse.swt.events.WrappedListener;

public class Layer extends Bounds implements LayerContainer, Runnable {
	final static private int MouseClick = 123456789;
	
//	final static private int T00=0, T01=1, T10=2, T11=3, T02=4, T12=5; //, T20=3, T21=4, T22=5;
	final static private int T00=0, T10=1, T01=2, T11=3, T02=4, T12=5; //, T20=3, T21=4, T22=5;

	/// Attributes
	private Layer parent; 
	private LayerCanvas root;
	private Layer children[] = {};

	private int validationCount = 0;
	private int transformCount = 0;
	private int parentTransformCount = 0;
	private int globalCount = 0;
	
	private float M[] = new float[6]; // the local transformation Matrix
	private float W[] = new float[6]; // the local transformation Matrix
	
	private boolean redraw = true; //whether redraws are triggerd upon change 
	
//	private boolean visible = true; //XXX 
//	private boolean enabled = true;
	
	
//==[ Bounds/Extents & Resize/Move/Control Listener ]===============================================
	
	@Override public Bounds setBounds(Rectangle r) {
		super.setBounds(r);
		return this;
	}
	
	@Override public Layer setExtents(float left, float top, float right, float bottom) {
		boolean changed = this.left!=left || this.top!=top || this.right!=right || this.bottom!=bottom;

		float deltaX = left-this.left, deltaY = top-this.top;
		float deltaW = (right-left)-(this.right-this.left);
		float deltaH = (bottom-top)-(this.bottom-this.top);

		boolean move = deltaX != 0 || deltaY !=0;
		boolean resize = deltaW!=0 || deltaH!=0;
		
		super.setExtents(left, top, right, bottom);

		if (redraw && changed)
			redraw();
		
		if (move)
			onMove(deltaX, deltaY);
		
		if (resize)
			onResize(deltaW, deltaH);
		
		return this;
	}
	
	private Listener onMove = null, onResize = null;
	final public Layer onMove( Listener l ) { return addListener(Move, l); }
	final public Layer onResize( Listener l ) { return addListener(Resize, l); }
	
	public Layer addControlListener( ControlListener cl ) {
		TypedListener tl = new WrappedListener(cl);
		addListener(Resize, tl);
		addListener(Move, tl);
		return this;
	}
	
	public Layer removeControlListener( ControlListener cl ) {
		TypedListener tl = new WrappedListener(cl); //has working equals in place, so remove actually works
		removeListener(Resize, tl);
		removeListener(Move, tl);
		return this;
	}
	
	protected void onMove(float deltax, float deltay) 
	{ if (onMove!=null)  onMove.handleEvent(controlEvent(Move, deltax, deltay)); }
	
	protected void onResize(float deltaWidth, float deltaHeight) 
	{ if (onResize!=null)  onResize.handleEvent(controlEvent(Resize, deltaWidth, deltaHeight)); }
	
	private Event controlEvent(int type, float xd, float yd) { 
		Event e = new Event();
		e.type = type;
		e.widget = root;
		e.x = (int) left;
		e.y = (int) top;
		e.width = (int) (right-left);
		e.height = (int) (bottom-top);
		e.xDirection = (int) xd;
		e.yDirection = (int) yd;
		return e;
	}
	
	
	///
	private boolean clipping = false;
	public Layer setClipping(boolean clipping) {
		this.clipping = clipping;
		return this;
	}
	public boolean isClipping() {
		return clipping;
	}

//==[ Constructor & Resource Disposal ]=============================================================
	
	public Layer(LayerContainer parent) {
		parent.addLayer(this);
		
		setExtents(-1/0f, -1f/0f, +1f/0f, +1f/0f);
		identity();
	}
	
//==[ Resource Disposal ]===========================================================================
	
	boolean isDisposed;
	
	public void dispose() {
		for (Layer child : children)
			child.dispose();
		
		getParent().removeLayer(this);
		setRoot(null);
		parent = null;
		isDisposed=true;
		
		onDispose();
	}
	
	protected void onDispose() {
		// subclasses can add additional disposal code here
	}
	
	public boolean isDisposed() {
		return isDisposed;
	}

//==[ Hierarchy ]===================================================================================

	///// Root
	
	protected void setRoot(LayerCanvas root) { 
		this.root = root;
		for (Layer child: children)
			child.setRoot(root);
	}

	public LayerCanvas getRoot() { return root; }
	
	///// Parent

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
	
	public LayerContainer getParent() { return parent==null?root:parent; }
	
	///// Children (add, get, remove, z-order)
	
	public Layer addLayer(Layer r) {
		if (r.setParent(this)) {
			((children = Arrays.copyOf(children, children.length+1))[children.length-1]=r).setRoot(root);
			
			if (redraw)
				redraw();
			
			this.mouseListeners += r.mouseListeners;
		}
		
		return this;
	}

	public Layer removeLayer(Layer r) {
		
		int i = indexOf(r);
		if (i>=0) {

			if (redraw)
				redraw();

			// Remove child without changing the z-order of the remaining children 
			Layer[] newchildren = new Layer[children.length-1];
			for (int j=0, J=children.length, k=0; j<J; j++) {
				if (j==i) continue;
				newchildren[k++] = children[j];
			}
			children = newchildren;
			
			// setParent to null after child has been removed from the children array to prevent ping pong between removeLayer and setParent
			r.setParent(null);
			r.setRoot(null);
					
			this.mouseListeners -= r.mouseListeners;
			
			return this;
		}
		
//		for (int i=0,I=children.length;i<I;i++)
//			if (children[i]==r) {
//				
//				r.setParent(null);
//				r.setRoot(null);
//
//				// XXX z-order modified due to element swap during removal
//				children[i] = children[children.length-1];
//				children = Arrays.copyOf(children, children.length-1);
//
//				if (redraw)
//					redraw();
//									
//				this.mouseListeners -= r.mouseListeners;
//				
//				return this;
//			}
		
		return this;
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
	
	// Moves Layer above the specified sibling Layer r
	// (if r==null, moves Layer to the first child position at index 0)
	public void moveBottom() { moveBelow(null); }
	public void moveBelow(Layer r) {
		LayerContainer p = getParent();
		Layer[] cs = p.getLayers();

		int ir = p.indexOf(this);
		
		for (int j=ir; j>=1 && cs[j]!=r; j--) {
			
			Layer tmp = cs[j];
			cs[j] = cs[j-1];
			cs[j-1] = tmp;
			
			if (redraw)
				tmp.redraw();
		}
	}
	
	// Moves Layer below the specified sibling Layer r
	// (if r==null, moves Layer to the last child position at length-1)
	public void moveTop() { moveAbove(null); }
	public void moveAbove(Layer r) {
		LayerContainer p = getParent();
		Layer[] cs = p.getLayers();

		int ir = p.indexOf(this);
		
		for (int j=ir,J=cs.length; j<J-1 && cs[j]!=r; j++) {
			
			Layer tmp = cs[j];
			cs[j] = cs[j+1];
			cs[j+1] = tmp;
			
			if (redraw)
				tmp.redraw();
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////// TRANSFORMATION ///////////////////////////////////////////////////
	
	public Layer rotate(double theta) { return concatenate(cos(theta),sin(theta),-sin(theta),cos(theta),0,0); }
	public Layer translate(double tx, double ty) { return concatenate(1,0,0,1,tx,ty); }
	public Layer scale(double s, double px, double py) { return scale(s,s,px,py); }
	public Layer scale(double sx, double sy) { return concatenate(sx,0,0,sy,0,0); }
	public Layer scale(double s) { return concatenate(s,0,0,s,0,0); }

	public Layer scale(double sx, double sy, double x, double y) 
	{ return this.concatenate(sx, 0, 0, sy, x-sx*x, y-sy*y); }
	
	public Layer rotate(double theta, double x, double y) {
		final double cos = cos(theta), sin = sin(theta);
		return this.concatenate(cos, sin, -sin, cos, x-x*cos+y*sin, y-x*sin-y*cos);
	}
	
	public Layer postRotate(double theta) { return postConcatenate(cos(theta),sin(theta),-sin(theta),cos(theta),0,0); }
	public Layer postTranslate(double tx, double ty) { return postConcatenate(1,0,0,1,tx,ty); }
	public Layer postScale(double s, double px, double py) { return postScale(s,s,px,py); }
	public Layer postScale(double sx, double sy) { return postConcatenate(sx,0,0,sy,0,0); }
	public Layer postScale(double s) { return postConcatenate(s,0,0,s,0,0); }

	public Layer postScale(double sx, double sy, double x, double y) 
	{ return this.postConcatenate(sx, 0, 0, sy, x-sx*x, y-sy*y); }
	
	public Layer postRotate(double theta, double x, double y) {
		final double cos = cos(theta), sin = sin(theta);
		return this.postConcatenate(cos, sin, -sin, cos, x-x*cos+y*sin, y-x*sin-y*cos);
	}
	

	public Layer postConcatenate(double scX, double shY, double shX, double scY, double tx, double ty) {
		final float M00 = (float) scX, M01 = (float) shX, M02 = (float) tx;
		final float M10 = (float) shY, M11 = (float) scY, M12 = (float) ty;
		
		final float m00 = M[T00], m01 = M[T01], m02 = M[T02];
		final float m10 = M[T10], m11 = M[T11], m12 = M[T12];

		M[T00]=m10*M01+m00*M00; M[T01]= m11*M01+m01*M00; M[T02]= M02+m12*M01+m02*M00;
		M[T10]=m10*M11+m00*M10; M[T11]= m11*M11+m01*M10; M[T12]= M12+m12*M11+m02*M10;
	
		if (M[T00]!=m00 || M[T10]!=m10 || M[T01]!=m01 || M[T11]!=m11 || M[T02]!=m02 || M[T12]!=m12)
			invalidateTransform();
		
		if (redraw && !isValidatingTransform())
			redraw();
		
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
		
		if (redraw && !isValidatingTransform())
			redraw();
		
		return this;
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
	

	public<T> T transform(double x, double y, TransformedCoordinate<T> i) {
		validateTransform();
		float w00 = W[T00], w01 = W[T01], w02 = W[T02];
		float w10 = W[T00], w11 = W[T01], w12 = W[T02];
		
		return i.define(w00*(float)x+w01*(float)y+w02, w10*(float)x+w11*(float)y+w12); 
	}
	
	
	public<T> T transform(double x, double y, Layer b, TransformedCoordinate<T> i) {
		validateTransform();
		float w00 = W[T00], w01 = W[T01], w02 = W[T02];
		float w10 = W[T00], w11 = W[T01], w12 = W[T02];
		
		return b.invert(w00*(float)x+w01*(float)y+w02, w10*(float)x+w11*(float)y+w12, i);
	}
	
	public<T> T invert(double x, double y, TransformedCoordinate<T> i) {
		validateTransform();
		float det = W[T00] * W[T11] - W[T01] * W[T10];
		float i00 = W[T11]/det, i01 =-W[T01]/det, i02 = (W[T01] * W[T12] - W[T11] * W[T02]) / det;
		float i10 =-W[T10]/det, i11 = W[T00]/det, i12 = (W[T10] * W[T02] - W[T00] * W[T12]) / det;

		return i.define(i00*(float)x+i01*(float)y+i02, i10*(float)x+i11*(float)y+i12); 
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////// DRAW EVENTS ////////////////////////////////////////
	
	
	public Layer readLayerTranslation(LayerTranslationConsumer lt) {
		lt.translate(M[T02],M[T12]);
		return this;
	}
	
	public Layer readCanvasTranslation(LayerTranslationConsumer lt) {
		validateTransformInternal();
		lt.translate(W[T02],W[T12]);
		return this;
	}
	
	public Layer readLayerTransform(LayerTransformConsumer lt) {
		lt.transform(M[T00],M[T10],M[T01],M[T11],M[T02],M[T12]);
		return this;
	}
	
	public Layer readCanvasTransform(LayerTransformConsumer lt) {
		validateTransformInternal();
		lt.transform(W[T00],W[T10],W[T01],W[T11],W[T02],W[T12]);
		return this;
	}
	
	public<T> T getLayerTranslation(LayerTranslationFunction<T> lt) {
		return lt.translate(M[T02],M[T12]);
	}
	
	public<T> T getCanvasTranslation(LayerTranslationFunction<T> lt) {
		validateTransformInternal();
		return lt.translate(W[T02],W[T12]);
	}
	
	public<T> T getLayerTransform(LayerTransformFunction<T> lt) {
		return lt.transform(M[T00],M[T10],M[T01],M[T11],M[T02],M[T12]);
	}
	
	public<T> T getCanvasTransform(LayerTransformFunction<T> lt) {
		validateTransformInternal();
		return lt.transform(W[T00],W[T10],W[T01],W[T11],W[T02],W[T12]);
	}
	
	protected void invalidateTransform() {
		this.transformCount++;
		root.globalCount++;
	}
	
	private int validationReentry = 0;
	protected boolean isValidatingTransform() { return validationReentry!=0; }
	
	private boolean validateTransformInternal() {
		boolean validated = false;
		if (validationReentry++==0) 
			validated = validateTransform();
		validationReentry--;
		return validated;
	}
	
	protected boolean validateTransform() {
		if (root.globalCount==globalCount || paintEvent)
			return false;
		
		if (parent==null) {
			
			if (root.transformCount!=parentTransformCount || transformCount!=validationCount) {
				
				root.canvasTransform.getElements(W);
				concatenate(W,M,W);
				validationCount = transformCount;
				parentTransformCount = root.transformCount;
				return true;
				
			} else {
				return false;
			}
			
		} else {
			
			if (parent.validateTransform() || //the parent had to be revalidated, due to it's parent or own change 
				parent.transformCount!=parentTransformCount || //the parent had an change before  
				transformCount!=validationCount)  //this Layer had been changed
			{
				concatenate(parent.W, M, W);
				validationCount = transformCount;
				parentTransformCount = parent.transformCount;
				return true;
			} else {
				return false;
			}
			
		}
	}
	

	public Layer setRedraw(boolean redraw) {
		this.redraw = redraw;
		return this;
	}
	
	public boolean isRedraw() {
		return this.redraw;
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
	
	public void redraw(float x, float y, float width, float height, boolean all) {
		if (!dirty) { //if Event has not been scheduled, do so, and initialize dirty bounds
			if (!root.isDisposed())
				root.getDisplay().asyncExec(this);
			
			dirty = true;
			local.setExtents(x, y, x+width, y+height);
			
			dirtyAll = all;
		} else { //otherwise just extend bounds of dirt.
			local.extend(x, y);
			local.extend(x+width, y+height);
			
			dirtyAll |= all;
		}
	}
	
	private Bounds local = new Bounds();
	private Bounds global = new Bounds();
	private float B[] = new float[4*2]; // the oriented bounds of this Layer in world coordinates

	private Bounds computeDirtyBounds(Bounds b, boolean recursive) {
		B[0] = B[4] = local.getLeft();
		B[1] = B[3] = local.getTop();
		B[2] = B[6] = local.getRight();
		B[5] = B[7] = local.getBottom();
		
		validateTransformInternal();
		transform(W, B);
		
		b.extend(B[0], B[1]);
		b.extend(B[2], B[3]);
		b.extend(B[4], B[5]);
		b.extend(B[6], B[7]);
		
		if (recursive)
			for (Layer child: children) {
//				child.validateTransform();
				child.globalCount = globalCount;
				child.parentTransformCount = transformCount;
				child.validationCount = child.transformCount;
				concatenate(W, child.M, child.W);
				child.computeDirtyBounds(b, recursive);
			}
		
		return b;
	}
	
	final public void run() {
		if (root==null || root.isDisposed() || isDisposed()) // async exec, check whether request still valid
			return;
		
		final float globalLeft = global.left, globalTop = global.top;
		final float globalRight = global.right, globalBottom = global.bottom;
		
		global.clear();
		computeDirtyBounds(global, dirtyAll&&!clipping); // also requires a valid root, as indirectly calls validateTransform()
		global.extend(globalLeft, globalTop).extend(globalRight, globalBottom);
		
		int M = 1; // due to antialiasing
		// if (root!=null && !root.isDisposed())
			root.redraw( // root / canvas coordinates are never negative, so integer truncation effects do not matter
					(int)global.left-M, (int) global.top-M, 
					(int)global.right -(int)global.left+2*M, 
					(int)global.bottom-(int)global.top+2*M, 
					dirtyAll);
			
		dirty = false;
		
		computeDirtyBounds(global.clear(), false); //necessary?
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// RENDERING /////////////////////////////////////////
	
	private boolean paintEvent = false;
	public boolean isPaintEvent() {
		return paintEvent;
	}
	
	


	final public void paint(Rectangle clip, Transform t, Event e) {
		paintEvent = true;
		//////////////// update Transform
		// read out parent's transform and store it
		t.getElements(W);
		final float P00 = W[T00], P01 = W[T01], P02 = W[T02];
		final float P10 = W[T10], P11 = W[T11], P12 = W[T12];

		// compute most current transform and set it
		concatenate(W, M, W);
		
		// allow validateTransform to modify model matrix accordingly
		validateTransformInternal();
		
		// recompute world Matrix
		t.getElements(W);
		concatenate(W, M, W);
		t.setElements(W[T00],W[T10],W[T01],W[T11],W[T02],W[T12]);

		//forcefully validate transform
		globalCount = root.globalCount;
		validationCount = transformCount;
		parentTransformCount = parent==null?root.transformCount:parent.transformCount;
		
		//re-compute enclosing axis aligned bounding box
		//dirty.setBounds(this)
		local.setBounds(this);
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
				onPaint(e);
				paint(clip, t, e, children);
				//// PAINT ////
				
				t.setElements(P00,P10,P01,P11,P02,P12);
				e.gc.setTransform(t);
				e.gc.setClipping(parentClipRegion);
	
				parentClipRegion.dispose();
				layerClipRegion.dispose();
			}
		} else {
			e.gc.setTransform(t);

			//only render this Layer if the GC's screen coordinate-transformed clips intersect that aabb 
			if (intersects)
				onPaint(e);
			
			paint(clip, t, e, children);
					
			
			t.setElements(P00,P10,P01,P11,P02,P12);
			e.gc.setTransform(t);
		}
		
		// remember global dirty bounds to be  
//		computeDirtyBounds(global.clear(), false);
		// XXX SOME REPAINT BUG HERE

		paintEvent = false;
	}
	
	protected void paint(Rectangle clip, Transform t, Event e, Layer[] children) {
//		if (clipping)
		//TODO implement proper clipping
		// Apply to clip and also to GC c
		// but beware GC may be pre-transformed and setClipping may be excected in local coords			
		
		// for (int I=children.length-1,i=0;I>=i;I--) {
		for (int I=0,i=children.length-1; I<=i; I++) {
			int x = clip.x, y = clip.y, w = clip.width, h = clip.height;

			if (clipping) { 
				//if clipping is enabled, then tighten clipping rectangle by this layer's bounds
				global.getBounds(clip);
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
	public final Layer onPaint( Listener pl ) { return addListener(Paint, pl); }
	public final Layer addPaintListener( PaintListener pl ) { return addListener(Paint,new WrappedListener(pl)); };
	public final Layer removePaintListener( PaintListener pl ) { return removeListener(Paint,new WrappedListener(pl)); }
	
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
	private int downX, downY, downT;
	
	public void capture(Layer c) {
		captive = c;
		if (getParent()!=null)
			getParent().capture(c);
	}
	
	public Layer captive() {
		return captive;
	}
	
	final public void point(Event e) {
		if (mouseListeners==0)
			return ;
		
		validateTransformInternal();
		final float w00 = W[T00], w01 = W[T01], w02 = W[T02];
		final float w10 = W[T10], w11 = W[T11], w12 = W[T12];
		
		float det = w00 * w11 - w01 * w10;
		float i00 = w11/det, i01 =-w01/det, i02 = (w01 * w12 - w11 * w02) / det;
		float i10 =-w10/det, i11 = w00/det, i12 = (w10 * w02 - w00 * w12) / det;
		
		float x = i00*e.x+i01*e.y+i02;
		float y = i10*e.x+i11*e.y+i12;
		
		boolean hit = x>=left && x<right && y>=top && y<bottom;

//		for (Layer r: children) {
		for (int i=children.length-1, I=0; i>=I; i--) {
			Layer r = children[i];
			if (r.mouseListeners>0) //only if there are mouse listeners anyways
				if (e.doit) //only if it shall happen
					if (!clipping || clipping && hit) // if clipping hits or there s no clipping at all
						if (r.captive()==captive) //either captive == null, or set to a specific layer
							r.point(e);
		}
		
		if (hit || captive==this) {
			if (e.doit && !entered) { 
				onMouseEnter(x,y,e);
				entered = true;
			}
			
			if (e.doit) {
				e.doit = false;
				
				if (onMouseEvent(x,y,e)) {
					if (e.type==MouseDown && !e.doit) // only capture if e.doit was not modified
						capture(this);
				}
			}
			
			if (e.type==MouseDown) {
				downX = e.x;
				downY = e.y;
				downT = e.time;
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
	
	////////////
	
	
	private boolean onMouseEvent(float x, float y, Event e) {
		switch (e.type) {
			case MouseMove: onMouseMove(x,y,e); break;
			case MouseUp: onMouseUp(x,y,e); break;
			case MouseDown: onMouseDown(x,y,e); break;
			case MouseWheel: onMouseWheel(x,y,e); break;
			case MouseDoubleClick: onMouseDoubleClick(x,y,e); break;
			default: return false;
		}
		return true;
	}
	
	
	//XXX maybe replace this by just using the mouse event functions and probe whether they have been overwritten just once (one mousevent passthru does not hurt, does it?)
	private static Class<?>[] parameterTypes = new Class<?>[] { float.class,float.class,Event.class };
	public Collection<String> names = Arrays.asList(methodNames); 
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

	private int mouseListeners = isMouseHandler()?1:0;
	protected boolean isMouseHandler() {
		Class<?> c = this.getClass();
		
		if (c.equals(Layer.class)) 
			return false;
		
		try {
			for (String methodName: methodNames)
				if (!c.getMethod(methodName,parameterTypes).getDeclaringClass().equals(Layer.class))
					return true;
		} catch (Exception ex) {
			throw new Error(ex);
		}
		
		return false;	
	}
	
	private void countMouseListeners(int num) { 
		if (parent!=null)
			parent.countMouseListeners(num);
		
		mouseListeners+=num;
	}
	
	private Listener onMouseClick = null, onDoubleClick = null, onMouseDown = null, onMouseUp = null;
	private Listener onMouseMove = null, onMouseWheel = null, onMouseEnter = null, onMouseExit = null;
	
	public final Layer onMouseClick(Listener l) { return addListener(MouseClick,l); }
	public final Layer onMouseDoubleClick(Listener l) { return addListener(MouseDoubleClick,l); }
	public final Layer onMouseDown(Listener l) { return addListener(MouseDown,l); }
	public final Layer onMouseUp(Listener l) { return addListener(MouseUp,l); }
	public final Layer onMouseMove(Listener l) { return addListener(MouseMove,l); }
	public final Layer onMouseWheel(Listener l) { return addListener(MouseWheel,l); }
	public final Layer onMouseEnter(Listener l) { return addListener(MouseEnter,l); }
	public final Layer onMouseExit(Listener l) { return addListener(MouseExit,l); }
	
	public final Layer addMouseWheelListener( MouseWheelListener pl ) { return addListener(MouseWheel,new WrappedListener(pl)); };
	public final Layer removeMouseWheelListener( MouseWheelListener pl ) { return removeListener(MouseWheel,new WrappedListener(pl)); }
	
	// Default handlers don't consume event if not overridden/no listeners installed (reset e.doit back to true)
	public void onMouseClick(float x, float y, Event e) { defaultHandleEvent(onMouseClick, e, false); }
	public void onMouseDoubleClick(float x, float y, Event e) { defaultHandleEvent(onDoubleClick, e, false); }
	public void onMouseDown(float x, float y, Event e) { defaultHandleEvent(onMouseDown, e, false); }
	public void onMouseUp(float x, float y, Event e) { defaultHandleEvent(onMouseUp, e, false); }
	public void onMouseMove(float x, float y, Event e) { defaultHandleEvent(onMouseMove, e, false); }
	public void onMouseWheel(float x, float y, Event e) { defaultHandleEvent(onMouseWheel, e, false); }
	public void onMouseEnter(float x, float y, Event e) { defaultHandleEvent(onMouseEnter, e, false); }
	public void onMouseExit(float x, float y, Event e) { defaultHandleEvent(onMouseExit, e, false); }

	// Can be called if onMouse... methods overridden to call installed listeners as well (without resetting e.doit back to True)
	protected void fireOnMouseClick(float x, float y, Event e) { defaultHandleEvent(onMouseClick, e, true); }
	protected void fireOnMouseDoubleClick(float x, float y, Event e) { defaultHandleEvent(onDoubleClick, e, true); }
	protected void fireOnMouseDown(float x, float y, Event e) { defaultHandleEvent(onMouseDown, e, true); }
	protected void fireOnMouseUp(float x, float y, Event e) { defaultHandleEvent(onMouseUp, e, true); }
	protected void fireOnMouseMove(float x, float y, Event e) { defaultHandleEvent(onMouseMove, e, true); }
	protected void fireOnMouseWheel(float x, float y, Event e) { defaultHandleEvent(onMouseWheel, e, true); }
	protected void fireOnMouseEnter(float x, float y, Event e) { defaultHandleEvent(onMouseEnter, e, true); }
	protected void fireOnMouseExit(float x, float y, Event e) { defaultHandleEvent(onMouseExit, e, true); }
	
	
	private void defaultHandleEvent(Listener l, Event e, boolean consume) {
		if (l!=null) {
			l.handleEvent(e);
		} else {
			if (!consume) 
				e.doit = true; // if no event handler defined, let event pass through
		}
	}

	public Layer addListener(int eventType, Listener l) {
		switch (eventType) {
		case Move: onMove = Listeners.join(onMove, l); break;
		case Resize: onResize = Listeners.join(onResize,l); break;
		
		case Paint: onPaint = Listeners.join(onPaint, l); break;
		
		case MouseClick: onMouseClick = Listeners.join(onMouseClick,l); countMouseListeners(+1); break;
		case MouseDoubleClick: onDoubleClick = Listeners.join(onDoubleClick,l); countMouseListeners(+1); break;
		case MouseDown: onMouseDown = Listeners.join(onMouseDown,l); countMouseListeners(+1); break;
		case MouseUp: onMouseUp = Listeners.join(onMouseUp,l); countMouseListeners(+1); break;
		case MouseMove: onMouseMove = Listeners.join(onMouseMove,l); countMouseListeners(+1); break;
		case MouseWheel: onMouseWheel = Listeners.join(onMouseWheel,l); countMouseListeners(+1); break;
		case MouseEnter: onMouseEnter = Listeners.join(onMouseEnter,l); countMouseListeners(+1); break;
		case MouseExit: onMouseExit = Listeners.join(onMouseExit,l); countMouseListeners(+1); break;
		
		default:
			throw new IllegalArgumentException("Event Type not supported by Layer");
		}
		
		return this;
	}
	
	public Layer removeListener(int eventType, Listener l) {
		switch (eventType) {
		case Move: onMove = Listeners.exclude(onMove, l); break;
		case Resize: onResize = Listeners.exclude(onResize, l); break;
		
		case Paint: onPaint = Listeners.exclude(onPaint, l); break;
		
		case MouseClick: onMouseClick = Listeners.exclude(onMouseClick,l); countMouseListeners(-1); break;
		case MouseDoubleClick: onDoubleClick = Listeners.exclude(onDoubleClick,l); countMouseListeners(-1); break;
		case MouseDown: onMouseDown = Listeners.exclude(onMouseDown,l); countMouseListeners(-1); break;
		case MouseUp: onMouseUp = Listeners.exclude(onMouseUp,l); countMouseListeners(-1); break;
		case MouseMove: onMouseMove = Listeners.exclude(onMouseMove,l); countMouseListeners(-1); break;
		case MouseWheel: onMouseWheel = Listeners.exclude(onMouseWheel,l); countMouseListeners(-1); break;
		case MouseEnter: onMouseEnter = Listeners.exclude(onMouseEnter,l); countMouseListeners(-1); break;
		case MouseExit: onMouseExit = Listeners.exclude(onMouseExit,l); countMouseListeners(-1); break;
		
		default:
			throw new IllegalArgumentException("Event Type not supported by Layer");
		}
		
		return this;
	}
	
	
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
	
	static void concatenate(float[] A, float B00, float B10, float B01, float B11, float B02, float B12, float[] C ) {
		//read the parent's world matrix to M
		final float A00 = A[T00], A01 = A[T01], A02 = A[T02];
		final float A10 = A[T10], A11 = A[T11], A12 = A[T12];
		
		//compute W = T.(M.I)
		C[T00] = B10*A01+B00*A00; C[T01] = B11*A01+B01*A00; C[T02] = A02+B12*A01+B02*A00;
		C[T10] = B10*A11+B00*A10; C[T11] = B11*A11+B01*A10; C[T12] = A12+B12*A11+B02*A10;
	}
	
	static void concatenate(float[] A, float[] B, float[] C) {
		concatenate(A, B[T00], B[T10], B[T01], B[T11], B[T02], B[T12], C);
	}
	
	
	static void transform(float[] A, float v[]) {
		for (int i=0,I=v.length;i<I;i+=2) {
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

