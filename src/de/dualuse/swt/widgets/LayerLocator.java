package de.dualuse.swt.widgets;

public interface LayerLocator {
	<T> T on(Layer lc, LayerLocation<T> l);
	<T> T on(LayerCanvas lc, LayerLocation<T> l);
}
