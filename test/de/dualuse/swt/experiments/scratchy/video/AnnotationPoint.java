package de.dualuse.swt.experiments.scratchy.video;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public class AnnotationPoint implements Annotation {

	int x, y;
	int annotationSize = 21;

	Color selectedColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
	Color selectedColorDark = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
	
	Color annotationColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
	Color annotationColorDark = new Color(Display.getCurrent(), 64, 64, 64);
	
	boolean isVisible = true;
	boolean isSelected;
	
	Rectangle bounds;
	Path shape;
	
//==[ Constructor ]=================================================================================
	
	public AnnotationPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
//==[ Getter/Setter ]===============================================================================
	
	public void setX(int x) {
		if (this.x==x) return;
		this.x = x;
		setBounds();
		invalidateShape();
	}
	
	public void setY(int y) {
		if (this.y==y) return;
		this.y = y;
		setBounds();
		invalidateShape();
	}
	
	private void setBounds() {
		if (bounds==null) bounds = new Rectangle(0,0,annotationSize,annotationSize);
		bounds.x = x - (annotationSize/2+1);
		bounds.y = y - (annotationSize/2+1);
	}
	
//==[ Added/Removed from Parent ]===================================================================
	
	@Override public void added(AnnotatedVideoCanvas canvas) {}

	@Override public void removed(AnnotatedVideoCanvas canvas) {}

//==[ Handle Input ]================================================================================
	
	@Override public void onMouse(float x, float y, Event e) {
		// TODO Auto-generated method stub
	}

	@Override public void onKey(Event e) {
		// TODO Auto-generated method stub
	}

//==[ Paint Annotation ]============================================================================
	
	@Override public void render(Rectangle clip, Transform t, GC gc) {
		
		float qs = annotationSize/4.0f;
		float hs = annotationSize/2.0f;
		
		if (isSelected) gc.setAlpha(255); else gc.setAlpha(192);
		
		gc.setBackground(annotationColorDark);
		gc.setBackground(isSelected ? selectedColorDark : annotationColorDark);
		Path path = new Path(gc.getDevice());
		path.addArc(x - hs, y - hs, 2*hs, 2*hs, 0, 360);
		gc.fillPath(path);
		path.dispose();
		
		gc.setBackground(isSelected ? selectedColor : annotationColor);
		path = new Path(gc.getDevice());
		path.addArc(x - qs, y - qs, 2*qs, 2*qs, 0, 360);
		gc.fillPath(path);
		path.dispose();
		
	}

//==[ Get Shape / Bounds ]==========================================================================

	private void invalidateShape() {
		if (shape!=null) {
			shape.dispose();
			shape=null;
		}
	}
	
	@Override public Rectangle getBounds() {
		return bounds;
	}

	@Override public Path getShape() {
		if (shape==null) {
			float hs = annotationSize/2.0f;
			shape = new Path(Display.getCurrent());
			shape.addArc(x - hs, y - hs, 2*hs, 2*hs, 0, 360);
			shape.close();
		}
		return shape;
	}

	@Override public void setVisible(boolean isVisible) {
		if (this.isVisible == isVisible) return;
		this.isVisible = isVisible;
	}

	@Override public boolean isVisible() {
		return isVisible;
	}

	@Override public HoverType checkHover(float x, float y) {
		// XXX todo
		return HoverType.NONE;
	}
	
//==[ Dispose: Free Resources ]=====================================================================
	
	@Override public void dispose() {
		annotationColorDark.dispose();
	}

	@Override
	public void startDrag(float x, float y, HoverType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDrag(float dx, float dy, HoverType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelected(boolean selected) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}

}
