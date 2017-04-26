package de.dualuse.swt.experiments.scratchy.video;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import static de.dualuse.swt.experiments.scratchy.video.Annotation.HoverType.*;

public class AnnotationSign implements Annotation {

	private AnnotatedVideoView canvas;
	private int x, y, width, height;
	
	private Rectangle bounds;
	private Path shape;
	
	private boolean isVisible;
	
	private Color foreground;
	private Color background;
	
	private Color selectedForeground;
	private Color selectedBackground;

//==[ Constructor ]=================================================================================
	
	public AnnotationSign(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
//		background = Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA);
//		foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA);
		
		background = Display.getCurrent().getSystemColor(SWT.COLOR_CYAN);
		foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
		
		selectedBackground = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
		selectedForeground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);
		
		isVisible = true;
	}
	
//==[ Getter/Setter ]===============================================================================
	
	public void setX(int x) {
		if (this.x == x) return;
		this.x = x;
		invalidateShape();
	}
	
	public void setY(int y) {
		if (this.y == y) return;
		this.y = y;
		invalidateShape();
	}
	
	public void setWidth(int width) {
		if (this.width==width) return;
		this.width = width;
		invalidateShape();
	}
	
	public void setHeight(int height) {
		if (this.height==height) return;
		this.height = height;
		invalidateShape();
	}

	public void setSize(int width, int height) {
		if (this.width==width && this.height==height) return;
		this.width = width;
		this.height = height;
		invalidateShape();
	}
	
	public void setBounds(int x, int y, int width, int height) {
		if (this.x==x && this.y==y && this.width==width && this.height==height) return;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		invalidateShape();
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
//==[ Add to/remove from AnnotatedCanvas ]==========================================================
	
	@Override public void added(AnnotatedVideoView canvas) {
		this.canvas = canvas;
		// add additional listeners
	}

	@Override public void removed(AnnotatedVideoView canvas) {
		if (this.canvas!=canvas) return;
		// remove additional listeners
		this.canvas = null;
	}

//==[ Paint Annotation ]============================================================================
	
	@Override public void render(Rectangle clip, Transform t, GC gc) {
		int oldAlpha = gc.getAlpha();
		gc.setAlpha(128);
		
		gc.setForeground(isSelected ? selectedForeground : foreground);
		gc.setBackground(isSelected ? selectedBackground : background);
		
		gc.fillRectangle(x, y, width, height);
		gc.drawRectangle(x, y, width, height);
		
		gc.setAlpha(oldAlpha);
	}

//==[ Return Annotation Shape ]=====================================================================

	private void invalidateShape() {
		if (shape!=null) {
			shape.dispose();
			shape=null;
		}
		if (bounds!=null)
			bounds=null;
	}
	
	private static Path createShape(int x, int y, int w, int h) {
		Path shape = new Path(Display.getCurrent());
		shape.addRectangle(x, y, w, h);
		shape.close();
		return shape;
	}
	
	@Override public Rectangle getBounds() {
		if (bounds==null)
			bounds = new Rectangle(x, y, width, height);
		return bounds;
	}
	
	@Override public Path getShape() {
		if (shape==null)
			shape = createShape(x, y, width, height);
		return shape;
	}
	
//==[ Event Handling (Filtered by AnnotatedCanvas) ]================================================
	
	int r = 3;
	
	@Override public void onKey(Event e) {
		
	}

	@Override public void onMouse(float mx, float my, Event e) {
		
	}
	
//==[ Update HOver Information ]====================================================================
	
	@Override public HoverType checkHover(float mx, float my) {

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
		return hit(x-r, y-r, x+width+r, y+height+r, mx, my);
	}
	
	private boolean hitTop(float mx, float my) {
		return hit(x-r, y-r, x+width+r, y+r, mx, my);
	}
	
	private boolean hitLeft(float mx, float my) {
		return hit(x-r, y-r, x+r, y+height+r, mx, my);
	}
	
	private boolean hitBottom(float mx, float my) { // Rock 
		return hit(x-r, y+height-r, x+width+r, y+height+r, mx, my);
	}
	
	private boolean hitRight(float mx, float my) {
		return hit(x+width-r, y-r, x+width+r, y+height+r, mx, my);
	}
	
	private static boolean hit(int x1, int y1, int x2, int y2, float mx, float my) {
		return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
	}
	
//==[ Drag Controls ]===============================================================================
	
	int startx, starty, startWidth, startHeight;
	
	@Override public void startDrag(float mx, float my, HoverType type) {
		startx = x;
		starty = y;
		startWidth = width;
		startHeight = height;
	}
	
	@Override public void updateDrag(float dx, float dy, HoverType type) {
		
		int _dx = (int)Math.round(dx);
		int _dy = (int)Math.round(dy);
		
		switch(type) {
			case N: updateTop(_dx, _dy); break;
			case W: updateLeft(_dx, _dy); break;
			case S: updateBottom(_dx, _dy); break;
			case E: updateRight(_dx, _dy); break;
			case NE:
				updateTop(_dx, _dy);
				updateRight(_dx, _dy);
				break;
			case NW:
				updateTop(_dx, _dy);
				updateLeft(_dx, _dy);
				break;
			case SE:
				updateBottom(_dx, _dy);
				updateRight(_dx, _dy);
				break;
			case SW:
				updateBottom(_dx, _dy);
				updateLeft(_dx, _dy);
				break;
			case C:
				translate(_dx, _dy);
				break;
		}
		
		invalidateShape();
	}
	
	private void updateTop(int dx, int dy) {
		y = starty + dy;
		height = startHeight - dy;
	}
	
	private void updateLeft(int dx, int dy) {
		x = startx + dx;
		width = startWidth - dx;
	}
	
	private void updateBottom(int dx, int dy) {
		height = startHeight + dy;
	}
	
	private void updateRight(int dx, int dy) {
		width = startWidth + dx;
	}
	
	private void translate(int dx, int dy) {
		x = startx + dx;
		y = starty + dy;
	}
	
//==[ Visibility ]==================================================================================
	
	@Override public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	@Override public boolean isVisible() {
		return isVisible;
	}
	
//==[ Disposal: Free Resources ]====================================================================
	
	@Override public void dispose() {
		invalidateShape();
	}

	
	boolean isSelected;
	
	@Override public void setSelected(boolean selected) {
		this.isSelected = selected;
	}

	@Override public boolean isSelected() {
		return isSelected;
	}

}
