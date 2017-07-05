package de.dualuse.swt.graphics;

import java.util.Arrays;

class Viewport {
	public int x=-1, y=1, width=2, height=-2;
	public boolean viewportClipping = false;
	
	public Viewport() {	}
	public Viewport(Viewport that) { set(that.x,that.y,that.width,that.height); }
	public void set(Viewport that) { set(that.x,that.y,that.width,that.height); }
	
	public void set(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		viewportClipping = !(x==-1 && y==1 && width==2 && height==-2);
	}

	public void transform(double[] coords, int offset, int count) {
		
		for (int i=0,o=offset,p=offset;i<count;i++) {
			final double xnd = coords[o++];
			final double ynd = coords[o++];
			
			// see 'man glViewport' 
			final double xw = (+xnd+1)*(width/2)+x;
			final double yw = (-ynd+1)*(height/2)+y;
			
			coords[p++] = xw;
			coords[p++] = yw;
		}

	}

	public void transform(float[] coords, int offset, int count) {
		
		for (int i=0,o=offset,p=offset;i<count;i++) {
			final float xnd = coords[o++];
			final float ynd = coords[o++];
			
			// see 'man glViewport' 
			final float xw = (+xnd+1)*(width/2.0f)+x;
			final float yw = (-ynd+1)*(height/2.0f)+y;
			
			coords[p++] = xw;
			coords[p++] = yw;
		}

	}
	
	public double transformX( double xnd ) { return (+xnd+1)*(width/2)+x; } 
	public double transformY( double ynd ) { return (-ynd+1)*(height/2)+y; } 
	public double transformZ( double znd ) { return znd; } 

	public float transformX( float xnd ) { return (+xnd+1)*(width/2)+x; } 
	public float transformY( float ynd ) { return (-ynd+1)*(height/2)+y; } 
	public float transformZ( float znd ) { return znd; }
	
	public static void main(String[] args) {
		Viewport v = new Viewport();
		
//		v.set(100, 100, 300, 400);
		
//		double coords[] = { -1,-1,   1,-1, -1,1, 1,1 };
//		v.transform(coords, 0, 4);
		
		double coords[] = { 0,0, 200,300 };
		v.transform(coords, 0, 2);
		
		
		
		System.out.println(Arrays.toString(coords));
		
	}
}
