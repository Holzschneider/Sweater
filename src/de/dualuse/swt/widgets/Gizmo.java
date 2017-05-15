package de.dualuse.swt.widgets;

@SuppressWarnings("unchecked")
public class Gizmo<T extends Gizmo<?>> extends Layer {

	public Gizmo(LayerContainer parent) {
		super(parent);
	}
	
	@Override
	public T translate(double tx, double ty) {
		return (T)super.translate(tx, ty);
	}
	
	
}
