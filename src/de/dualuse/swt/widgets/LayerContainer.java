package de.dualuse.swt.widgets;

public interface LayerContainer {

	///// Interfaces
	
	interface TransformedCoordinate<T> { T define(float x, float y); }
	interface TransformedCoordinateConsumer { void define(float x, float y); }
	
	interface LayerTransformFunction<T> { T transform(double scX, double shY, double shX, double scY, double tX, double tY); }
	interface LayerTranslationFunction<T> { T translate(double translationX, double translationY); }
	interface LayerScaleFunction<T> { T scale(double scaleX, double scaleY); }
	// interface LayerRotationFunction<T> { T rotation(double scaleX, double scaleY); }

	interface LayerTransformConsumer { void transform(double scX, double shY, double shX, double scY, double tX, double tY); }
	interface LayerTranslationConsumer { void translate(double translationX, double translationY); }
	interface LayerScaleConsumer { void scale(double scaleX, double scaleY); }
	// interface LayerRotationConsumer { void rotation(double scaleX, double scaleY); }

	///// Parent
	public LayerContainer getParentContainer();
	
	///// Child Layers
	
	public Layer[] getLayers();
	public LayerContainer addLayer( Layer r );
	public LayerContainer removeLayer( Layer r );
	public int indexOf( Layer r );

	///// Given layer captures events
	
	public Layer captive();
	// public void resetCaptive();
	
	public void capture(Layer c);
	public void setCaptive(Layer c);
	
	////// Redraw container (region)
	
	public void redraw();
	public void redraw(float x, float y, float w, float h, boolean all);
	
	///// Coordinate Transformations
	
	public<T> T transform(double x, double y, TransformedCoordinate<T> i);
	public<T> T transform(double x, double y, Layer b, TransformedCoordinate<T> i);

	///// Debug Methods

	public default void printLayerTree() {
		printLayerTree(this);
	}
	
	public static void printLayerTree(LayerContainer container) {
		printLayerTree(container, "", 1);
	}
	
	public static void printLayerTree(LayerContainer container, String indent, int num) {
		Layer captive = container.captive();
		Layer[] children = container.getLayers();
		
		int index = -1; // no captive
		for (int i=0, I=children.length; i<I; i++) {
			if (children[i]==captive) {
				index = i;
				break;
			}
		}

		String captiveString = container.captive()==null ? "null" : container.captive().getClass().toString();
		System.out.println(indent + num + ": " + container.getClass() + " (captive: " + index + ", " + captiveString + ")");
		
		index = 1;
		for (LayerContainer child : container.getLayers()) {
			printLayerTree(child, indent + "    ", index++);
		}
	}
	
}
