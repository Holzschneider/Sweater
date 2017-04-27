package de.dualuse.swt.graphics;

import java.awt.Shape;
import java.awt.geom.PathIterator;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Path;

public class PathShape extends Path {

	public PathShape(Device device) {
		super(device);
	}
	
	public PathShape(Device device, Shape s) {
		super(device);
		this.addShape(s);
	}
	
	public PathShape(Device device, Path from, float flatness) {
		super(device, from, flatness);
	}
	
	
	public void addShape(Shape s) {
		float[] c = new float[6];
		
		for (PathIterator pi = s.getPathIterator(null);!pi.isDone();pi.next()) 
			switch (pi.currentSegment(c)) {
			case PathIterator.SEG_CLOSE: close(); break;
			case PathIterator.SEG_MOVETO: moveTo(c[0], c[1]); break;
			case PathIterator.SEG_LINETO: lineTo(c[0], c[1]); break;
			case PathIterator.SEG_QUADTO: quadTo(c[0], c[1], c[2], c[3]); break;
			case PathIterator.SEG_CUBICTO: cubicTo(c[0], c[1], c[2], c[3], c[4], c[5]); break;
			}
	}
	
}