package de.dualuse.swt.widgets;

import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Listener;

@SuppressWarnings("unchecked")
public class Gizmo<T extends Gizmo<?>> extends Layer {
	public Gizmo(LayerContainer parent) { super(parent); } 
 	@Override public Bounds setBounds(Rectangle r) { return (T)super.setBounds(r); }
 	@Override public T setExtents(float left, float top, float right, float bottom) { return (T)super.setExtents(left, top, right, bottom); }
 	@Override public T addControlListener(ControlListener cl) { return (T)super.addControlListener(cl); }
 	@Override public T removeControlListener(ControlListener cl) { return (T)super.removeControlListener(cl); }
 	@Override public T setClipping(boolean clipping) { return (T)super.setClipping(clipping); }
 	@Override public T addLayer(Layer r) { return (T)super.addLayer(r); }
 	@Override public T removeLayer(Layer r) { return (T)super.removeLayer(r); }

 	@Override public T rotate(double theta) { return (T)super.rotate(theta); }
 	@Override public T scale(double s, double px, double py) { return (T)super.scale(s, px, py); }
 	@Override public T scale(double sx, double sy) { return (T)super.scale(sx, sy); }
 	@Override public T scale(double s) { return (T)super.scale(s); }
 	@Override public T translate(double tx, double ty) { return (T)super.translate(tx, ty); }
 	@Override public T scale(double sx, double sy, double x, double y) { return (T)super.scale(sx, sy, x, y); }
 	@Override public T rotate(double theta, double x, double y) { return (T)super.rotate(theta, x, y); }
 	@Override public T postRotate(double theta) { return (T)super.postRotate(theta); }
 	@Override public T postTranslate(double tx, double ty) { return (T)super.postTranslate(tx, ty); }
 	@Override public T postScale(double s, double px, double py) { return (T)super.postScale(s, px, py); }
 	@Override public T postScale(double sx, double sy) { return (T)super.postScale(sx, sy); }
 	@Override public T postScale(double s) { return (T)super.postScale(s); }
 	@Override public T postScale(double sx, double sy, double x, double y) { return (T)super.postScale(sx, sy, x, y); }
 	@Override public T postRotate(double theta, double x, double y) { return (T)super.postRotate(theta, x, y); }
 	@Override public T postConcatenate(double scX, double shY, double shX, double scY, double tx, double ty) { return (T)super.postConcatenate(scX, shY, shX, scY, tx, ty); }
 	@Override public T concatenate(double scX, double shY, double shX, double scY, double tx, double ty) { return (T)super.concatenate(scX, shY, shX, scY, tx, ty); }
 	@Override public T identity() { return (T)super.identity(); }
 	@Override public T getLayerTranslation(LayerTranslation lt) { return (T)super.getLayerTranslation(lt); }
 	@Override public T getCanvasTranslation(LayerTranslation lt) { return (T)super.getCanvasTranslation(lt); }
 	@Override public T getLayerTransform(LayerTransform lt) { return (T)super.getLayerTransform(lt); }
 	@Override public T getCanvasTransform(LayerTransform lt) { return (T)super.getCanvasTransform(lt); }
 	@Override public T setRedraw(boolean redraw) { return (T)super.setRedraw(redraw); }
 	@Override public T addListener(int eventType, Listener l) { return (T)super.addListener(eventType, l); }
 	@Override public T removeListener(int eventType, Listener l) { return (T)super.removeListener(eventType, l); }  
}
