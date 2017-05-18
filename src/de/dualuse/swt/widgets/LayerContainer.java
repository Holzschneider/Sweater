package de.dualuse.swt.widgets;

public interface LayerContainer {
	public LayerContainer addLayer( Layer r );
	public LayerContainer removeLayer( Layer r );
	public int indexOf( Layer r );
	
	public Layer[] getLayers();

	public void capture(Layer c);
	

	//////////////
	
	public void redraw();
	public void redraw(float x, float y, float w, float h, boolean all);
	
	
	//////////////
	
	public<T> T transform(double x, double y, TransformedCoordinate<T> i);
	public<T> T transform(double x, double y, Layer b, TransformedCoordinate<T> i);
	
	public interface TransformedCoordinate<T> { T define(float x, float y); }

	
	interface LayerTransform { void transformation(float scX, float shY, float shX, float scY, float tX, float tY); }
	interface LayerTranslation { void translation(float translationX, float translationY); }
//	interface LayerRotation { void rotation(float scaleX, float scaleY); }
//	interface LayerScale { void scale(float scaleX, float scaleY); }
	
}

