package de.dualuse.swt.widgets;

public interface LayerLocator {
	public LayerLocator on(Layer lc, LayerLocation l);
	public LayerLocator on(LayerCanvas lc, LayerLocation l);
	
	public LayerCursor on(Layer lc);
	public LayerCursor on(LayerCanvas lc);

}
