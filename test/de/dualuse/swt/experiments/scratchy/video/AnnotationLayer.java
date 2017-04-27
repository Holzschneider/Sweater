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
import static org.eclipse.swt.SWT.BUTTON1;
import static org.eclipse.swt.SWT.BUTTON2;
import static org.eclipse.swt.SWT.BUTTON3;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerContainer;

public class AnnotationLayer extends Layer {

	AnnotatedVideoCanvas parent;
	
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
	
//==[ Constructor ]=================================================================================
	
	public AnnotationLayer(AnnotatedVideoCanvas parent, float left, float top, float right, float bottom) {
		super(parent);
		
		this.parent = parent;
		
		setBounds(left, top, right, bottom);

		background = dsp.getSystemColor(SWT.COLOR_CYAN);
		foreground = dsp.getSystemColor(SWT.COLOR_DARK_CYAN);
		
		selectedBackground = dsp.getSystemColor(SWT.COLOR_YELLOW);
		selectedForeground = dsp.getSystemColor(SWT.COLOR_DARK_YELLOW);
		
	}

//==[ Rendering ]===================================================================================
	
	@Override protected void render(GC gc) {
		gc.setAntialias(SWT.ON);

		gc.setForeground(isSelected?selectedForeground:foreground);
		gc.setBackground(isSelected?selectedBackground:background);
		
		gc.setLineWidth(2);
		gc.setLineStyle(SWT.LINE_SOLID);
		
		Path path = new Path(dsp);
		path.addRectangle(getLeft(), getTop(), getRight()-getLeft(), getBottom()-getTop());
		
		gc.fillPath(path);
		gc.drawPath(path);
		
		path.dispose();
	}
	
//==[ Selection ]===================================================================================
	
	boolean isSelected;
	
	public void setSelected(boolean isSelected) {
		if (this.isSelected==isSelected) return;
		this.isSelected=isSelected;
		redraw();
	}
	
	public boolean isSelected() {
		return isSelected;
	}
	
//==[ Event Handling ]==============================================================================
	
	float xs, ys;
	float ls, ts, rs, bs;
	boolean modifying;
	
	@Override protected boolean onMouseDown(float x, float y, int button, int modifierKeys) {
		
		if (shiftPressed(modifierKeys)) {
			parent.toggleSelection(this);
		} else {
			parent.selectExclusive(this);
		}
		
		if (hoverType == HoverType.NONE)
			return false;
		
		this.xs = x;
		this.ys = y;
		
		ls = getLeft();
		ts = getTop();
		rs = getRight();
		bs = getBottom();
		
		modifying = true;
		
		return true;
	}
	
	@Override protected boolean onMouseUp(float x, float y, int button, int modifierKeys) {
		if (modifying) {
			modifying = false;
			return true;
		} else return false;
	}
	
	@Override protected boolean onMouseMove(float x, float y, int modifierKeysAndButtons) {
		int buttons = BUTTON1 | BUTTON2 | BUTTON3;

		if (modifying && hoverType != HoverType.NONE) {
			
			resize(x, y);
			return true;
			
		} else if ((modifierKeysAndButtons & buttons)==0) {
			
			updateHover(x, y);
			
		}
		
		return false;
	}

	@Override protected boolean onMouseWheel(float x, float y, int tickCount, int modifierKeys) {
		boolean shiftPressed = (modifierKeys & SWT.SHIFT) != 0;
		boolean ctrlPressed = (modifierKeys & SWT.CTRL) != 0;
		
		grow(tickCount, shiftPressed, ctrlPressed);
		
		return true;
	}
	
	@Override protected boolean onMouseEnter() {
		return true;
	}
	
	@Override protected boolean onMouseExit() {
		getRoot().setCursor(cursorArrow);
		return true;
	}
	
	public static boolean shiftPressed(int statemask) {
		return (statemask & SWT.SHIFT)!=0;
	}
	
	public static boolean ctrlPressed(int statemask) {
		return (statemask & SWT.CTRL)!=0;
	}
	
//==[ Control Shape: Resize Bounds ]================================================================
	
	private void resize(float x, float y) {

		float l = getLeft();
		float t = getTop();
		float r = getRight();
		float b = getBottom();
		
		switch(hoverType) {
			case N:  t = ts + (y-ys); break;
			case S:  b = bs + (y-ys); break;
			case E:  r = rs + (x-xs); break;
			case W:  l = ls + (x-xs); break;
			case NE: t = ts + (y-ys); r = rs + (x-xs); break;
			case NW: t = ts + (y-ys); l = ls + (x-xs); break;
			case SE: b = bs + (y-ys); r = rs + (x-xs); break;
			case SW: b = bs + (y-ys); l = ls + (x-xs); break;
			case C:
				t = ts + (y-ys); l = ls + (x-xs);
				b = bs + (y-ys); r = rs + (x-xs);
				break;
			case NONE:
		}
		
		if (b<t) { // swap top&bottom
			float tmp = b, tmps = bs;
			b = t; bs = ts;
			t = tmp; ts = tmps;
			
			if (hoverType==N) hoverType = S;
			else if (hoverType==S) hoverType = N;
			else if (hoverType==NE) hoverType = SE;
			else if (hoverType==NW) hoverType = SW;
			else if (hoverType==SE) hoverType = NE;
			else if (hoverType==SW) hoverType = NW;
		}
		
		if (r<l) { // swap left&right
			float tmp = r, tmps = rs;
			r = l; rs = ls;
			l = tmp; ls = tmps;
			
			if (hoverType==E) hoverType = W;
			else if (hoverType==W) hoverType = E;
			else if (hoverType==NE) hoverType = NW;
			else if (hoverType==NW) hoverType = NE;
			else if (hoverType==SE) hoverType = SW;
			else if (hoverType==SW) hoverType = SE;
		}
		
		setBounds(l, t, r, b);
		
	}
	
//==[ Control Shape: Shrink/Grow ]==================================================================
	
	private void grow(int steps, boolean constrainHorizontal, boolean constrainVertical) {
		float width = getWidth(), dw = 0;
		float height = getHeight(), dh = 0;
		
		if (!constrainVertical)
			dh = height - (float)pow(1.0337, steps) * height;
		
		if (!constrainHorizontal)
			dw = width - (float)pow(1.0337, steps) * width;
		
		setBounds(getLeft() - dw/2, getTop()-dh/2, getRight() + dw/2, getBottom() + dh/2);
	}
	
	
//==[ Update Hover Information ]====================================================================
	
	HoverType hoverType = HoverType.NONE;
	int hoverRadius = 5;
	
	private void updateHover(float x, float y) {
		hoverType = checkHover(x, y);
		Canvas root = getRoot();
		switch(hoverType) {
			case NONE: root.setCursor(cursorArrow); break;
			case N: case S: root.setCursor(cursorNS); break;
			case E: case W: root.setCursor(cursorWE); break;
			case NE: root.setCursor(cursorNE); break;
			case NW: root.setCursor(cursorNW); break;
			case SE: root.setCursor(cursorSE); break;
			case SW: root.setCursor(cursorSW); break;
			case C: root.setCursor(cursorCE); break;
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
		return hit(x-hoverRadius, y-hoverRadius, x+width+hoverRadius, y+height+hoverRadius, mx, my);
	}
	
	private boolean hitTop(float mx, float my) {
		int x = (int)getLeft();
		int y = (int)getTop();
		int width = (int) Math.ceil(getWidth());
		return hit(x-hoverRadius, y-hoverRadius, x+width+hoverRadius, y+hoverRadius, mx, my);
	}
	
	private boolean hitLeft(float mx, float my) {
		int x = (int)getLeft();
		int y = (int)getTop();
		int height = (int) Math.ceil(getHeight());
		return hit(x-hoverRadius, y-hoverRadius, x+hoverRadius, y+height+hoverRadius, mx, my);
	}
	
	private boolean hitBottom(float mx, float my) { // Rock
		int x = (int)getLeft();
		int y = (int)getTop();
		int width = (int) Math.ceil(getWidth());
		int height = (int) Math.ceil(getHeight());
		return hit(x-hoverRadius, y+height-hoverRadius, x+width+hoverRadius, y+height+hoverRadius, mx, my);
	}
	
	private boolean hitRight(float mx, float my) {
		int x = (int)getLeft();
		int y = (int)getTop();
		int width = (int) Math.ceil(getWidth());
		int height = (int) Math.ceil(getHeight());
		return hit(x+width-hoverRadius, y-hoverRadius, x+width+hoverRadius, y+height+hoverRadius, mx, my);
	}
	
	private static boolean hit(int x1, int y1, int x2, int y2, float mx, float my) {
		return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
	}
	
}
