package de.dualuse.swt.experiments.scratchy.video;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;

public interface Annotation {

	public enum HoverType {
		NW, N, NE,
		W, C, E,
		SW, S, SE,
		NONE
	}
	
	void added(AnnotatedVideoCanvas canvas);
	void removed(AnnotatedVideoCanvas canvas);
	
	void onMouse(float x, float y, Event e);
	void onKey(Event e);
	
	void render(Rectangle clip, Transform t, GC gc);
	
	Rectangle getBounds();
	Path getShape();
	
	void startDrag(float x, float y, HoverType type);
	void updateDrag(float dx, float dy, HoverType type);
	
	HoverType checkHover(float x, float y);

	void setSelected(boolean selected);
	boolean isSelected();
	
	void setVisible(boolean isVisible);
	boolean isVisible();
	
	void dispose();
	
}
