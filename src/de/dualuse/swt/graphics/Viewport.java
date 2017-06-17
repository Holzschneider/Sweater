package de.dualuse.swt.graphics;

class Viewport {
	public int x=+1, y=-1, width=2, height=-2;
	
	public Viewport() {	}
	public Viewport(Viewport that) { 
		this.x = that.x;
		this.y = that.x;
		this.width = that.width;
		this.height = that.height;
	}
	
	public void set(Viewport that) {
		this.x = that.x;
		this.y = that.x;
		this.width = that.width;
		this.height = that.height;
	}
	
	public void set(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
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
			final float xw = (+xnd+1)*(width/2)+x;
			final float yw = (-ynd+1)*(height/2)+y;
			
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
	
//	public static void main(String[] args) {
//		Viewport v = new Viewport();
//		
//		v.set(100, 100, 300, 400);
//		
//		double coords[] = { -1,-1,   1,-1, -1,1, 1,1 };
//		v.transform(coords, 0, 4);
//		
//		System.out.println(Arrays.toString(coords));
//		
//	}
}
