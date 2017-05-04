package de.dualuse.swt.widgets;

import static java.lang.Math.*;


import org.eclipse.swt.graphics.Rectangle;

class Bounds {
	protected float left = 1f/0f, top = 1f/0f, right = -1f/0f, bottom = -1f/0f;
	
	public Bounds() { }
	public Bounds(float left, float top, float right, float bottom) { set(left,top,right,bottom); }
	
	public Bounds clear() {
		top = left = 1f/0f;
		bottom = right = -1f/0f;
		
		return this;
	}
	
	public Bounds get(Rectangle r) {
		r.x = (int) floor(left);
		r.y = (int) floor(top);
		r.width = (int) ceil(right) - r.x;
		r.height = (int) ceil(bottom) - r.y;
		return this;
	}
	
	public Bounds set(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		return this;
	}
	
	public Bounds extend(float x, float y) {
		return this.set(left<x?left:x, top<y?top:y, right>x?right:x, bottom>y?bottom:y);
	}
	
	public Bounds extend(float left, float top, float right, float bottom) {
		return this.set(
				this.left<left?this.left:left, 
				this.top<top?this.top:top, 
				this.right>right?this.right:right, 
				this.bottom>bottom?this.bottom:bottom
			);
	}
	
	public Bounds extend(Bounds that) {
		return this.set(
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

	
	public static void main(String[] args) {
		
		
		Bounds b = new Bounds();
		
		b.extend(100, 100, 200, 200);
		
		System.out.println(b);
		System.out.println(b.isEmpty());
		
		
		
	}
}
