package de.dualuse.swt.widgets;

public interface LayerContainer {
	public LayerContainer addLayer( Layer r );
	public LayerContainer removeLayer( Layer r );
	public int indexOf( Layer r );
	
	public Layer[] getLayers();

	public void capture(Layer c);
	
	

	interface LayerTransform { void concatenate(float scX, float shY, float shX, float scY, float tX, float tY); }
	interface LayerTranslation { void translate(float translationX, float translationY); }
//	interface LayerRotation { void scale(float scaleX, float scaleY); }
//	interface LayerScale { void scale(float scaleX, float scaleY); }
	
}

