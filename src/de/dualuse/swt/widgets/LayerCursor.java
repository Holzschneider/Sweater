package de.dualuse.swt.widgets;

public class LayerCursor implements LayerLocator, LayerLocation {
	Layer source = null, destination = null;
	LayerCanvas sourceRoot = null, destinationRoot = null;
	
	float sourceX, sourceY;
	float destX, destY;
	
	public LayerCursor() { }
	
	public LayerCursor locate(float x, float y) {
		this.sourceX = x;
		this.sourceY = y;
		
		return this;
	}
	
	@Override
	public void set(float x, float y) { 
		destX = x; 
		destY = y; 
	}
	
	public float getX() { return destX; }
	public float getY() { return destY; }
	
	
	
	
	
	public LayerCursor from(Layer layer) {
		source = layer;
		sourceRoot = layer.getRoot();
		return this;
	}

	public LayerCursor from(LayerCanvas canvas) {
		source = null;
		sourceRoot = canvas;
		return this;
	}
	
	@Override
	public LayerCursor on(Layer lc, LayerLocation l) {
		destination = lc;
		destinationRoot = lc.getRoot();
		
		return null;
	}

	@Override
	public LayerCursor on(LayerCanvas lc, LayerLocation l) {
		destination = null;
		destinationRoot = lc;
		
		return this;
	}
	
	@Override
	public LayerCursor on(Layer lc) {
		return null;
	}

	@Override
	public LayerCursor on(LayerCanvas lc) {
		return null;
	}

	

}
