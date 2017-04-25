package de.dualuse.swt.experiments.scratchy.video;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;

public interface Annotation {
			
	void added(AnnotatedCanvas canvas);
	void removed(AnnotatedCanvas canvas);
	
	void onMouse(float x, float y, Event e);
	void onKey(Event e);
	
	void render(Rectangle clip, Transform t, GC gc);
	
	Rectangle getBounds();
	Path getShape();
	
	boolean checkHover(float x, float y);
	
	void setVisible(boolean isVisible);
	boolean isVisible();
	
	void dispose();
	
}
