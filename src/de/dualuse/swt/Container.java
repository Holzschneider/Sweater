package de.dualuse.swt;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class Container extends Composite {

	public Container(Composite parent, int style) {
		super(parent, style);
		setLayout(new LayoutDelegate().layout(this::onLayout).computeSize(this::onComputeSize));
	}
	
	//// Layout Handler ///////////////////////////////////////
	protected void onLayout(Composite composite, boolean flushCache) {
		
	}
	
	protected Point onComputeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		return new Point(0,0);
	}
	
	
	//// Dispose Handling //////////////////////////////////////
	public void dispose () {
		if (isDisposed ()) 
			return;
		
		super.dispose();
		onDisposed();
	}
		
	protected void onDisposed() {	}
	
}
