package de.dualuse.swt.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class CenterLayout extends Layout {
	
	int hAlign;
	int vAlign;
	
	public CenterLayout(int hAlignment, int vAlignment) {
		
		if (hAlignment!=SWT.LEFT && hAlignment!=SWT.CENTER && hAlignment!=SWT.RIGHT && hAlignment!=SWT.FILL)
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		
		if (vAlignment!=SWT.TOP && vAlignment!=SWT.CENTER && vAlignment!=SWT.BOTTOM && vAlignment!=SWT.FILL)
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);

		hAlign = hAlignment;
		vAlign = vAlignment;
		
	}

	@Override protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		return new Point(wHint, hHint);
	}

	@Override protected void layout(Composite composite, boolean flushCache) {

		Rectangle bounds = composite.getBounds();
		
		Control[] children = composite.getChildren();
		if (children.length!=1)
			SWT.error(SWT.ERROR_INVALID_RANGE);
		
		Control child = children[0];
		
		int wHint = (hAlign==SWT.FILL) ? bounds.width : SWT.DEFAULT;
		int hHint = (vAlign==SWT.FILL) ? bounds.height : SWT.DEFAULT;
		
		System.out.println("wHint: " + wHint);
		System.out.println("hHint: " + hHint);
		
		Point prefSize = child.computeSize(wHint, hHint);
		int x = 0;
		int y = 0;
		int width = prefSize.x;
		int height = prefSize.y;
		
		if (hAlign == SWT.CENTER) {
			x = (bounds.width - width) / 2;
		} else if (hAlign == SWT.RIGHT) {
			x = bounds.width - width;
		}
		
		if (vAlign == SWT.CENTER) {
			System.out.println("bounds.y: " + bounds.y);
			System.out.println("height: " + height);
			y = (bounds.height - height) / 2;
		} else if (vAlign == SWT.BOTTOM) {
			y = bounds.height - height;
		}
		
		child.setBounds(x, y, width, height);
	}
}
