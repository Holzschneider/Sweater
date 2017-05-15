package de.dualuse.swt.experiments.scratchy.video.annotation;

import de.dualuse.swt.experiments.Contour;
import de.dualuse.swt.experiments.Contour.Vertex;
import de.dualuse.vecmath.Matrix3d;

public class AnnotationContour extends Annotation<Matrix3d> {

	Contour contour;
	
//==[ Constructor ]=================================================================================
	
	public AnnotationContour(int frame, Matrix3d data) {
		super(frame, data);
		contour = new Contour();
	}

//==[ Key Interpolation ]===========================================================================
	
	@Override protected Matrix3d interpolate(Matrix3d left, Matrix3d right, double r) {
		return null;
	}
	
//==================================================================================================
	
	public Contour getContour() {
		return contour;
	}
	
	public Contour getContour(int frame) {
		
		Matrix3d transform = keys.get(frame);
		if (transform==null) return null;
		
		Contour result = new Contour();
		
		double[] coords = new double[2];
		for (int i=0, I=contour.numVertices(); i<I; i++) {
			
			Vertex v = contour.get(i);
			v.getElements(coords);
			transform.project(coords);
			result.add(coords[0], coords[1]);
			
		}
		
		if (contour.isClosed())
			result.close();
		
		return result;
		
	}

}
