package de.dualuse.swt.widgets;

import static java.lang.Math.*;


import org.eclipse.swt.graphics.Rectangle;

class Bounds {
	protected float left = 1f/0f, top = 1f/0f, right = -1f/0f, bottom = -1f/0f;
	
	public Bounds() { }
	public Bounds(float left, float top, float right, float bottom) { setLimits(left,top,right,bottom); }
	
	public Bounds clear() {
		top = left = 1f/0f;
		bottom = right = -1f/0f;
		
		return this;
	}

	
	public float getLeft() { return left; }
	public float getRight() { return right; }
	public float getTop() { return top; }
	public float getBottom() { return bottom; }

	public Rectangle getBounds() { return getBounds(new Rectangle(0, 0, 0, 0)); }
		
	public Rectangle getBounds(Rectangle r) {
		r.x = (int) floor(left);
		r.y = (int) floor(top);
		r.width = (int)(ceil(right) - floor(left));
		r.height = (int)(ceil(bottom) - floor(top));
		return r;
	}
	
	public Bounds setBounds(Rectangle r) {
		this.left = r.x;
		this.top = r.y;
		this.right = r.x+r.width;
		this.bottom = r.y+r.height;
		return this;
	}
	
	public Bounds setBounds(Bounds that) {
		this.left = that.left;
		this.right = that.right;
		this.top = that.top;
		this.bottom = that.bottom;
		return this;
	}
	
	public Bounds setLimits(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		return this;
	}
	
	final public Bounds extend(float x, float y) {
		return this.setLimits(left<x?left:x, top<y?top:y, right>x?right:x, bottom>y?bottom:y);
	}
	
	final public Bounds extend(float left, float top, float right, float bottom) {
		return this.setLimits(
				this.left<left?this.left:left, 
				this.top<top?this.top:top, 
				this.right>right?this.right:right, 
				this.bottom>bottom?this.bottom:bottom
			);
	}
	
	final public Bounds extend(Bounds that) {
		return this.setLimits(
				this.left<that.left?this.left:that.left,
				this.top<that.top?this.top:that.top,
				this.right>that.right?this.right:that.right,
				this.bottom>that.bottom?this.bottom:that.bottom
			);
	}

	public float getWidth() { return Float.isFinite(right)&&Float.isFinite(left)?right-left:1f/0f; }
	public float getHeight() { return Float.isFinite(bottom)&&Float.isFinite(top)?bottom-top:1f/0f; }

	public boolean isFinite() {
		return Float.isFinite(left) && Float.isFinite(right) && Float.isFinite(top) && Float.isFinite(bottom);
	}

	public boolean isEmpty() { 
		return right<left||bottom<top; 
	}
	
	///////////
	
	@Override
	public boolean equals(Object obj) {
		if (obj==this) return true;
		if (!(obj instanceof Bounds)) return false;
		return equals((Bounds)obj);
	}
	
	public boolean equals(Bounds that) {
		return this.left==that.left && this.right==that.right && this.top==that.top && this.bottom==that.bottom;
	}
	
	@Override
	public String toString() {
		return "Bounds("+left+","+top+","+right+","+bottom+")";
	}

//	
//	public static void main(String[] args) {
//		
//		
//		Bounds b = new Bounds();
//		
//		b.extend(100, 100, 200, 200);
//		
//		System.out.println(b);
//		System.out.println(b.isEmpty());
//		
//		
//		
//	}
}
