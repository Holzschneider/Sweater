package de.dualuse.swt.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class BorderLayout extends Layout {
	
	public final static String CENTER = "center";
	
	public final static String NORTH = "north";
	public final static String EAST = "east";
	public final static String SOUTH = "south";
	public final static String WEST = "west";
	
	public final static int N = 0, E = 1, S = 2, W = 3;
	Control[] last = new Control[4];
	Point[] pref = new Point[] { new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0) };
	
	@Override protected boolean flushCache(Control control) {
		System.out.println("flushCache: " + control);
		
		for (int i=0; i<4; i++) {
			if (last[i] == control) {
				last[i] = null;
				return true;
			}
		}
		
		return false;
	}
	
	private void cache(int index, Control control, Point prefSize) {
		last[index] = control;
		Point cachedSize = pref[index];
		cachedSize.x = prefSize.x;
		cachedSize.y = prefSize.y;
	}
	
	private void flushAll() {
		last[0] = last[1] = last[2] = last[3] = null;
	}
	
	private Control findControl(Control[] children, String key) {
		for (Control child : children) {
			Object layoutData = child.getLayoutData();
			if (!(layoutData instanceof String)) continue;
			
			String orientation = (String)layoutData;
			if (orientation == key) return child;
		}
		
		return null;
	}
	
	@Override protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		System.out.println("computeSize(" + composite + ", " + wHint + ", " + hHint + ", " + flushCache + ")");
		
		if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
			return new Point(wHint, hHint);

		if (flushCache)
			flushAll();
		
		Rectangle bounds = composite.getBounds();
		Control[] children = composite.getChildren();
		
		Control cCenter = findControl(children, CENTER);		
		Control cNorth = findControl(children, NORTH);
		Control cEast = findControl(children, EAST);
		Control cSouth = findControl(children, SOUTH);
		Control cWest = findControl(children, WEST);
		
		int top = 0, bottom = 0, left = 0, right = 0;
		int centerWidth = 0, centerHeight = 0;
		
		// XXX
		
		return new Point(wHint, hHint);	
	}
	
	@Override protected void layout(Composite composite, boolean flushCache) {
		System.out.println("layout(" + composite + ", " + flushCache + ")");
		
		if (flushCache)
			flushAll();
		
		Rectangle bounds = composite.getBounds();
		Control[] children = composite.getChildren();

//		Control cCenter=null, cNorth=null, cEast=null, cSouth=null, cWest=null;
//		
//		for (Control child : children) {
//			Object layoutData = child.getLayoutData();
//			if (!(layoutData instanceof String)) continue;
//			
//			String orientation = (String)layoutData;
//			switch(orientation) {
//				case CENTER: cCenter = child; break;
//				case NORTH: cNorth = child; break;
//				case EAST: cEast = child; break;
//				case SOUTH: cSouth = child; break;
//				case WEST: cWest = child; break;
//			}
//		}

		Control cCenter = findControl(children, CENTER);		
		Control cNorth = findControl(children, NORTH);
		Control cEast = findControl(children, EAST);
		Control cSouth = findControl(children, SOUTH);
		Control cWest = findControl(children, WEST);
		
		int top = 0;
		if (cNorth != null) {
			Point prefSize = last[N]!=null ? pref[N] : cNorth.computeSize(bounds.width, SWT.DEFAULT);
			if (prefSize.y > bounds.height) prefSize.y = bounds.height;
			
			cache(N, cNorth, prefSize);
			
			cNorth.setBounds(0, 0, prefSize.x, prefSize.y);
			top = prefSize.y;
		}
		
		int bottom = 0;
		if (cSouth != null) {
			Point prefSize = last[S]!=null ? pref[S] : cSouth.computeSize(bounds.width, SWT.DEFAULT);
			if (prefSize.y > bounds.height) prefSize.y = bounds.height;
			
			cache(S, cSouth, prefSize);
			
			cSouth.setBounds(0, bounds.height - prefSize.y, prefSize.x, prefSize.y);
			bottom = prefSize.y;
		}
		
		int left = 0;
		if (cWest != null) {
			Point prefSize = last[W]!=null ? pref[W] : cWest.computeSize(SWT.DEFAULT, bounds.height);
			if (prefSize.x > bounds.width) prefSize.x = bounds.width;
			
			cache(W, cWest, prefSize);
			
			cWest.setBounds(0, 0, prefSize.x, bounds.height);
			left = prefSize.x;
		}
		
		int right = 0;
		if (cEast != null) {
			Point prefSize = last[E]!=null ? pref[E] : cEast.computeSize(SWT.DEFAULT, bounds.height);
			if (prefSize.x > bounds.width) prefSize.x = bounds.width;
			
			cache(E, cEast, prefSize);
			
			cEast.setBounds(bounds.width - prefSize.x, 0, prefSize.x, bounds.height);
			right = prefSize.x;
		}
		
		if (cCenter != null) {
			cCenter.setBounds(left, top, bounds.width-left-right, bounds.height-top-bottom);
		}
		
	}
	
}
