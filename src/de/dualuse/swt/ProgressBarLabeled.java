package de.dualuse.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;

public class ProgressBarLabeled extends ProgressBar {
	
	private boolean showLabel = true;
	
//==[ Constructor ]=================================================================================
	
	public ProgressBarLabeled(Composite parent, int style) {
		super(parent, style);
		addPaintListener(paintListener);
	}
	
	@Override protected void checkSubclass() {}

//==[ PaintListener ]===============================================================================
	
	PaintListener paintListener = new PaintListener() {
		@Override public void paintControl(PaintEvent e) {
			
			String text = getSelection() + "%";
			
			// Color color = new Color(e.display, 255, 0, 0, 64);
			//Color prevColor = e.gc.getBackground();
			// e.gc.setBackground(color);
			
			Point textExtent = e.gc.textExtent(text);
			int width = textExtent.x;
			int height = textExtent.y;
			
			int x = (e.width - width)/2;
			int y = (e.height - height)/2;
			
			e.gc.drawString(text, x, y);
			
			// e.gc.setBackground(prevColor);
			//color.dispose();
		}
	};
	
//==[ Setter/Getter ]===============================================================================
	
	public void showLabel(boolean show) {
		checkWidget();
		if (show==showLabel) return;
		
		this.showLabel = show;
		if (showLabel) {
			addPaintListener(paintListener);
		} else {
			removePaintListener(paintListener);
		}
		
		redraw();
	}
	
	public boolean getShowLabel() {
		checkWidget();
		return showLabel;
	}
	
}
