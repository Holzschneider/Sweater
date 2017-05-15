package de.dualuse.swt.widgets;

public interface LayerIntersector {
	public <T> T with(Layer l, LayerIntersection<T> point );
}
