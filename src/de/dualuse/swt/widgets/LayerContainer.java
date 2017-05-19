package de.dualuse.swt.widgets;

public interface LayerContainer {

	////// Child Layers
	
	public Layer[] getLayers();
	public LayerContainer addLayer( Layer r );
	public LayerContainer removeLayer( Layer r );
	public int indexOf( Layer r );

	///// Given layer captures events
	
	public void capture(Layer c);

	////// Redraw container (region)
	
	public void redraw();
	public void redraw(float x, float y, float w, float h, boolean all);
	
	///// Coordinate Transformations
	
	public<T> T transform(double x, double y, TransformedCoordinate<T> i);
	public<T> T transform(double x, double y, Layer b, TransformedCoordinate<T> i);
	
	public interface TransformedCoordinate<T> { T define(float x, float y); }
	
	interface LayerTransformFunction<T> { T transform(double scX, double shY, double shX, double scY, double tX, double tY); }
	interface LayerTranslationFunction<T> { T translate(double translationX, double translationY); }
//	interface LayerRotationFunction<T> { T rotation(double scaleX, double scaleY); }
//	interface LayerScaleFunction<T> { T scale(double scaleX, double scaleY); }

	interface LayerTransformConsumer { void transform(double scX, double shY, double shX, double scY, double tX, double tY); }
	interface LayerTranslationConsumer { void translate(double translationX, double translationY); }
//	interface LayerRotationConsumer { void rotation(double scaleX, double scaleY); }
//	interface LayerScaleConsumer { void scale(double scaleX, double scaleY); }
	
}
