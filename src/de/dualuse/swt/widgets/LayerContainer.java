package de.dualuse.swt.widgets;

public interface LayerContainer {
	public LayerContainer addLayer( Layer r );
	public LayerContainer removeLayer( Layer r );
	public int indexOf( Layer r );
	
	public Layer[] getLayers();
}

