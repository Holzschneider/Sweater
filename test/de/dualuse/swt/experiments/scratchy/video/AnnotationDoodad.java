package de.dualuse.swt.experiments.scratchy.video;

import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.C;
import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.E;
import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.N;
import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.NE;
import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.NONE;
import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.NW;
import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.S;
import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.SE;
import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.SW;
import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.W;
import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.ALT;
import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.BUTTON2;
import static org.eclipse.swt.SWT.BUTTON3;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType;
import de.dualuse.swt.widgets.Doodad;
import de.dualuse.swt.widgets.DoodadCanvas;
import de.dualuse.swt.widgets.Renderable;

public class AnnotationDoodad extends Doodad {

	float xl, yl;
	DoodadCanvas parent;

	private Color foreground;
	private Color background;
	
	private Color selectedForeground;
	private Color selectedBackground;

	Display dsp = Display.getCurrent();
	
	Cursor cursorArrow = dsp.getSystemCursor(SWT.CURSOR_ARROW);
	
	Cursor cursorNW = dsp.getSystemCursor(SWT.CURSOR_SIZENW);
	Cursor cursorNE = dsp.getSystemCursor(SWT.CURSOR_SIZENE);
	Cursor cursorSW = dsp.getSystemCursor(SWT.CURSOR_SIZESW);
	Cursor cursorSE = dsp.getSystemCursor(SWT.CURSOR_SIZESE);
	
	Cursor cursorNS = dsp.getSystemCursor(SWT.CURSOR_SIZENS);
	Cursor cursorWE = dsp.getSystemCursor(SWT.CURSOR_SIZEWE);
	
	Cursor cursorCE = dsp.getSystemCursor(SWT.CURSOR_HAND);
	
	public AnnotationDoodad(Renderable parent, float left, float top, float right, float bottom) {
		super(parent);
		this.parent = (DoodadCanvas)parent; // XXX just for testing purposes now, since I will add them to the canvas directly
		this.setBounds(left, top, right, bottom);

		background = Display.getCurrent().getSystemColor(SWT.COLOR_CYAN);
		foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
		
		selectedBackground = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
		selectedForeground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);
		
	}

	@Override protected void render(GC gc) {
		gc.setForeground(foreground);
		gc.setBackground(background);
		gc.fillRectangle((int)getLeft(), (int)getTop(), (int)getWidth(), (int)getHeight());
		gc.drawRectangle((int)getLeft(), (int)getTop(), (int)getWidth(), (int)getHeight());
	}
	
	float ls, ts, rs, bs;
	boolean modifying;
	
	@Override protected boolean onMouseDown(float x, float y, int button, int modifierKeys) {
		this.xl = x;
		this.yl = y;
		
		ls = getLeft();
		ts = getTop();
		rs = getRight();
		bs = getBottom();
		
		if (hoverType != HoverType.NONE)
			modifying = true;
		
		return true;
	}
	
	@Override protected boolean onMouseUp(float x, float y, int button, int modifierKeys) {
		modifying = false;
		return true;
	}
	
	@Override protected boolean onMouseMove(float x, float y, int modifierKeysAndButtons) {
		int buttons = BUTTON1 | BUTTON2 | BUTTON3;
		// if (modifierKeysAndButtons==BUTTON1) {
		if (modifying && hoverType != HoverType.NONE) {
			
			float l = getLeft();
			float t = getTop();
			float r = getRight();
			float b = getBottom();
			
			switch(hoverType) {
				case N: t = ts + (y-yl); break;
				case S: b = bs + (y-yl); break;
				case E: r = rs + (x-xl); break;
				case W: l = ls + (x-xl); break;
				case NE: t = ts + (y-yl); r = rs + (x-xl); break;
				case NW: t = ts + (y-yl); l = ls + (x-xl); break;
				case SE: b = bs + (y-yl); r = rs + (x-xl); break;
				case SW: b = bs + (y-yl); l = ls + (x-xl); break;
				case C:
					t = ts + (y-yl); l = ls + (x-xl);
					b = bs + (y-yl); r = rs + (x-xl);
					break;
			}
			
			setBounds(l, t, r, b);
			
//			translate(x-xl, y-yl);
			parent.redraw();
			return true;
			
		} else if ((modifierKeysAndButtons & buttons)==0) {
			updateHover(x, y);
		}
		return true;
	}
	

	@Override protected boolean onMouseWheel(float x, float y, int tickCount, int modifierKeys) {
//		System.out.println(tickCount);
		
//		translate(x,y);
//		scale( pow(1.0337, tickCount));
//		translate(-x,-y);

		float width = getWidth();
		float height = getHeight();
		
		float dw = width - (float)pow(1.0337, tickCount) * width;
		float dh = height - (float)pow(1.0337, tickCount) * height;
		
		setBounds(getLeft() - dw/2, getTop()-dh/2, getRight() + dw/2, getBottom() + dh/2);
		
		parent.redraw();
		return true;
	}
	
	@Override protected boolean onMouseExit() {
		parent.setCursor(cursorArrow);
		return true;
	}

//==[ Update Hover Information ]====================================================================
	
	HoverType hoverType = HoverType.NONE;
	int r = 3;
	
	private void updateHover(float x, float y) {
		hoverType = checkHover(x, y);
		switch(hoverType) {
			case NONE: parent.setCursor(cursorArrow); break;
			case N: case S: parent.setCursor(cursorNS); break;
			case E: case W: parent.setCursor(cursorWE); break;
			case NE: parent.setCursor(cursorNE); break;
			case NW: parent.setCursor(cursorNW); break;
			case SE: parent.setCursor(cursorSE); break;
			case SW: parent.setCursor(cursorSW); break;
			case C: parent.setCursor(cursorCE); break;
		}
	}
	
	public HoverType checkHover(float mx, float my) {

		if (!hitAtAll(mx, my)) return NONE;
		
		boolean hitTop = hitTop(mx, my);
		boolean hitLeft = hitLeft(mx, my);
		boolean hitBottom = hitBottom(mx, my);
		boolean hitRight = hitRight(mx, my);
		
		if (hitTop && hitLeft) {
			return NW;
		} else if (hitTop && hitRight) {
			return NE;
		} else if (hitBottom && hitLeft) {
			return SW;
		} else if (hitBottom && hitRight) {
			return SE;
		} else if (hitTop) {
			return N;
		} else if (hitBottom) {
			return S;
		} else if (hitLeft) {
			return W;
		} else if (hitRight) {
			return E;
		} else {
			return C;
		}
		
	}
	
	private boolean hitAtAll(float mx, float my) {
		int x = (int)getLeft();
		int y = (int)getTop();
		int width = (int) Math.ceil(getWidth());
		int height = (int) Math.ceil(getHeight());
		return hit(x-r, y-r, x+width+r, y+height+r, mx, my);
	}
	
	private boolean hitTop(float mx, float my) {
		int x = (int)getLeft();
		int y = (int)getTop();
		int width = (int) Math.ceil(getWidth());
		int height = (int) Math.ceil(getHeight());
		return hit(x-r, y-r, x+width+r, y+r, mx, my);
	}
	
	private boolean hitLeft(float mx, float my) {
		int x = (int)getLeft();
		int y = (int)getTop();
		int width = (int) Math.ceil(getWidth());
		int height = (int) Math.ceil(getHeight());
		return hit(x-r, y-r, x+r, y+height+r, mx, my);
	}
	
	private boolean hitBottom(float mx, float my) { // Rock
		int x = (int)getLeft();
		int y = (int)getTop();
		int width = (int) Math.ceil(getWidth());
		int height = (int) Math.ceil(getHeight());
		return hit(x-r, y+height-r, x+width+r, y+height+r, mx, my);
	}
	
	private boolean hitRight(float mx, float my) {
		int x = (int)getLeft();
		int y = (int)getTop();
		int width = (int) Math.ceil(getWidth());
		int height = (int) Math.ceil(getHeight());
		return hit(x+width-r, y-r, x+width+r, y+height+r, mx, my);
	}
	
	private static boolean hit(int x1, int y1, int x2, int y2, float mx, float my) {
		return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
	}
	
}
