package de.dualuse.swt.experiments.scratchy.video.annotation;

import java.awt.geom.Rectangle2D;

public class AnnotationBounds extends Annotation<Rectangle2D> {

	public AnnotationBounds(int frame, Rectangle2D rect) {
		super(frame, rect);
	}
	
	@Override protected Rectangle2D interpolate(Rectangle2D left, Rectangle2D right, double r) {
		double s = 1-r;
		
		double lx1 = left.getX(), lx2 = lx1 + left.getWidth();
		double ly1 = left.getY(), ly2 = ly1 + left.getHeight();
		
		double rx1 = right.getX(), rx2 = rx1 + right.getWidth();
		double ry1 = right.getY(), ry2 = ry1 + right.getHeight();
		
		double x1 = r*lx1 + s*rx1, x2 = r*lx2 + s*rx2;
		double y1 = r*ly1 + s*ry1, y2 = r*ly2 + s*ry2;
		
		return new Rectangle2D.Double(x1, y1, x2-x1, y2-y1);
	}

}
