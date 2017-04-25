package de.dualuse.swt.experiments.scratchy.video;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public class AnnotationSign implements Annotation {

	private AnnotatedCanvas canvas;
	private int x, y, width, height;
	
	private Rectangle bounds;
	private Path shape;
	
	private boolean isVisible;
	
	private Color foreground;
	private Color background;

	Display dsp = Display.getCurrent();
	Cursor cursorNW = dsp.getSystemCursor(SWT.CURSOR_SIZENW);
	Cursor cursorNE = dsp.getSystemCursor(SWT.CURSOR_SIZENE);
	Cursor cursorSW = dsp.getSystemCursor(SWT.CURSOR_SIZESW);
	Cursor cursorSE = dsp.getSystemCursor(SWT.CURSOR_SIZESE);
	
	Cursor cursorNS = dsp.getSystemCursor(SWT.CURSOR_SIZENS);
	Cursor cursorWE = dsp.getSystemCursor(SWT.CURSOR_SIZEWE);
	
	Cursor cursorCE = dsp.getSystemCursor(SWT.CURSOR_HAND);
	
	enum DragType {
		NW, N, NE,
		W, C, E,
		SW, S, SE
	}
	DragType type;
	
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
	
	@Override public void added(AnnotatedCanvas canvas) {
		this.canvas = canvas;
		// add additional listeners
	}

	@Override public void removed(AnnotatedCanvas canvas) {
		if (this.canvas!=canvas) return;
		// remove additional listeners
		this.canvas = null;
	}

//==[ Paint Annotation ]============================================================================
	
	@Override public void render(Rectangle clip, Transform t, GC gc) {
		System.out.println("Rendering annotation (" + bounds + ")");
		
		int oldAlpha = gc.getAlpha();
		gc.setAlpha(128);
		
		gc.setForeground(foreground);
		gc.setBackground(background);
		
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
		
		if (e.type == SWT.MouseDown && e.button == 1) {
			
			type = evaluateHover(mx, my);
			
		} else if (e.type == SWT.MouseUp && e.button == 1) {
			
		}
	}
	
	@Override public boolean checkHover(float mx, float my) {

		if (!hitAtAll(mx, my)) return false;
		
		boolean hitTop = hitTop(mx, my);
		boolean hitLeft = hitLeft(mx, my);
		boolean hitBottom = hitBottom(mx, my);
		boolean hitRight = hitRight(mx, my);
		
		if (hitTop && hitLeft) {
			canvas.setCursor(cursorNW);
			return true;
		} else if (hitTop && hitRight) {
			canvas.setCursor(cursorNE);
			return true;
		} else if (hitBottom && hitLeft) {
			canvas.setCursor(cursorSW);
			return true;
		} else if (hitBottom && hitRight) {
			canvas.setCursor(cursorSE);
			return true;
		} else if (hitTop) {
			canvas.setCursor(cursorNS);
			return true;
		} else if (hitBottom) {
			canvas.setCursor(cursorNS);
			return true;
		} else if (hitLeft) {
			canvas.setCursor(cursorWE);
			return true;
		} else if (hitRight) {
			canvas.setCursor(cursorWE);
			return true;
		} else {
			canvas.setCursor(cursorCE);
			return true;
		}
		
	}
	
	private DragType evaluateHover(float mx, float my) {

		if (!hitAtAll(mx, my)) return null;
		
		boolean hitTop = hitTop(mx, my);
		boolean hitLeft = hitLeft(mx, my);
		boolean hitBottom = hitBottom(mx, my);
		boolean hitRight = hitRight(mx, my);
		
		if (hitTop && hitLeft) {
			return DragType.NW;
		} else if (hitTop && hitRight) {
			return DragType.NE;
		} else if (hitBottom && hitLeft) {
			return DragType.SW;
		} else if (hitBottom && hitRight) {
			return DragType.SE;
		} else if (hitTop) {
			return DragType.N;
		} else if (hitBottom) {
			return DragType.S;
		} else if (hitLeft) {
			return DragType.W;
		} else if (hitRight) {
			return DragType.E;
		} else {
			return DragType.C;
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
	
	private boolean hitCenter(float mx, float my) {
		return hit(x+r, y+r, x+width-r, y+height-r, mx, my);
	}
	
	private static boolean hit(int x1, int y1, int x2, int y2, float mx, float my) {
		return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
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

}
