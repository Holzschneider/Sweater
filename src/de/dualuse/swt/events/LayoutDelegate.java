package de.dualuse.swt.events;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import de.dualuse.swt.layout.Layouter;

public class LayoutDelegate extends Layout {

	private Point size = new Point(0,0);
	
	private ComputeSizeFunction computeSize = this::computeSize;
	private ComputeSizeFunction computeSizeHandler = computeSize;
	
	private LayoutFunction layout = this::layout;
	private LayoutFunction layoutHandler = layout;
	
	private FlushCacheFunction flushCache = this::flushCache;
	private FlushCacheFunction flushHandler = flushCache;
	
//==[ Delegate Interfaces ]=========================================================================
	
	public static interface ComputeSizeFunction {
		public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache);
	}

	public static interface LayoutFunction {
		public void layout(Composite composite, boolean flushCache);
	}
	
	public static interface FlushCacheFunction {
		public boolean flushCache(Control control);
	}
	
//==[ Constructors ]================================================================================
	
	public LayoutDelegate() {
		
	}
	
	public LayoutDelegate(Point precomputedSize) {
		size = precomputedSize;
	}
	
	public LayoutDelegate(Layouter layouter) {
		computeSize(layouter::computeSize);
		flushCache(layouter::flushCache);
		layout(layouter::layout);
	}
	
//==[ Fluent Interface ]============================================================================
	
	public LayoutDelegate computeSize(ComputeSizeFunction sizeFunction) { 
		this.computeSizeHandler = sizeFunction; 
		return this; 
	}
	
	public LayoutDelegate layout(LayoutFunction layoutFunction) { 
		this.layoutHandler = layoutFunction; 
		return this; 
	}
	
	public LayoutDelegate flushCache(FlushCacheFunction flushCacheFunction) {
		this.flushHandler = flushCacheFunction;
		return this;
	}
	
//==[ Layout Implementation ]=======================================================================
	
	@Override protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		if (computeSizeHandler!=computeSize)
			return computeSizeHandler.computeSize(composite, wHint, hHint, flushCache);
		else 
			return size;
	}
	
	@Override protected void layout(Composite composite, boolean flushCache) {
		if (layout!=layoutHandler)
			layoutHandler.layout(composite, flushCache); 
	}

	@Override protected boolean flushCache(Control control) {
		if (flushCache!=flushHandler)
			return flushHandler.flushCache(control);
		else
			return false;
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
