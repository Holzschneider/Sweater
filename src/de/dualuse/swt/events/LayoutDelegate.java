package de.dualuse.swt.events;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class LayoutDelegate extends Layout {
	public static interface ComputeSizeFunction {
		public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache);
	}

	public static interface LayoutFunction {
		public void layout(Composite composite, boolean flushCache);
	}
	
	private Point size = new Point(0,0);
	private ComputeSizeFunction computeSize = this::computeSize, computeSizeHandler = computeSize;
	private LayoutFunction layout = this::layout, layoutHandler = layout;
	
	public LayoutDelegate() {}
	public LayoutDelegate(Point precomputedSize) { size = precomputedSize; }
	
	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		if (computeSizeHandler!=computeSize)
			return computeSizeHandler.computeSize(composite, wHint, hHint, flushCache);
		else 
			return size;
	}
	
	public LayoutDelegate computeSize(ComputeSizeFunction sizeComputer) { 
		this.computeSizeHandler = sizeComputer; 
		return this; 
	}

	
	
	@Override
	protected void layout(Composite composite, boolean flushCache) {
		if (layout!=layoutHandler)
			layoutHandler.layout(composite, flushCache); 
	}
	
	public LayoutDelegate layout(LayoutFunction layouter) { 
		this.layoutHandler = layouter; 
		return this; 
	}

	
	/////////////
//	
//	public static void main(String[] args) {
//		
//		LayoutDelegate classic = new LayoutDelegate(new Point(100,100)) {
//			@Override
//			protected void layout(Composite composite, boolean flushCache) {
//			}
//		};
//		
//		LayoutDelegate cool = new LayoutDelegate().layout(this::doLayout).computeSize(that::computeSize);
//		
//	}
}
