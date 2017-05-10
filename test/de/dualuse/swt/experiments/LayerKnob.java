package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.NONE;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.PathShape;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;
import de.dualuse.swt.widgets.LayerContainer;

public class LayerKnob extends Layer {

	public interface KnobClickListener {
		void clicked(LayerKnob src, float x, float y, int button, int mask);
	}
	
	public interface KnobMoveListener {
		void moved(LayerKnob src, float x, float y);
	}
	
	public interface KnobHoverListener {
		void hover(LayerKnob src, boolean state);
	}
	
	Rectangle2D shape = new Rectangle2D.Double();
	
	Color foreground = Display.getCurrent().getSystemColor(SWT.COLOR_CYAN);
	Color background = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN);
	
	Color hoverForeground = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
	Color hoverBackground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);
	
	final float DEFAULT_SIZE = 6;
	
//==[ Constructor ]=================================================================================
	
	public LayerKnob(LayerContainer parent) {
		super(parent);
	}
	
	public LayerKnob(LayerContainer parent, float x, float y) {
		super(parent);
		setBounds(x-DEFAULT_SIZE/2, y-DEFAULT_SIZE/2, x+DEFAULT_SIZE/2, y+DEFAULT_SIZE/2);
	}

//==[ Additional Getter ]===========================================================================
	
	public float getCenterX() {
		return (float)(shape.getX() + shape.getWidth()/2);
	}
	
	public float getCenterY() {
		return (float)(shape.getY() + shape.getHeight()/2);
	}
	
	public boolean isHovering() {
		return hover;
	}
	
//==[ React to size changes ]=======================================================================
	
	@Override public void onResize() {
		double left = getLeft();
		double top  = getTop();
		double width = getRight() - left;
		double height = getBottom() - top;
		shape.setFrame(left, top, width, height);
		fireKnobMoved(getCenterX(), getCenterY());
	}
	
//==[ Knob Listener ]===============================================================================
	
	CopyOnWriteArrayList<KnobMoveListener> moveListeners = new CopyOnWriteArrayList<KnobMoveListener>();
	CopyOnWriteArrayList<KnobClickListener> clickListeners = new CopyOnWriteArrayList<KnobClickListener>();
	CopyOnWriteArrayList<KnobHoverListener> hoverListeners = new CopyOnWriteArrayList<KnobHoverListener>();
	
	public void addMoveListener(KnobMoveListener listener) {
		moveListeners.add(listener);
	}
	
	public void removeMoveListener(KnobMoveListener listener) {
		moveListeners.remove(listener);
	}

	public void addClickListener(KnobClickListener listener) {
		clickListeners.add(listener);
	}
	
	public void removeCLickListener(KnobClickListener listener) {
		clickListeners.remove(listener);
	}
	
	public void addHoverListener(KnobHoverListener listener) {
		hoverListeners.add(listener);
	}
	
	public void removeHoverListener(KnobHoverListener listener) {
		hoverListeners.remove(listener);
	}
	
	protected void fireKnobMoved(float cx, float cy) {
		for (KnobMoveListener listener : moveListeners)
			listener.moved(this, cx, cy);
	}
	
	protected void fireKnobClicked(float x, float y, int button, int mask) {
		for (KnobClickListener listener : clickListeners)
			listener.clicked(this, x, y, button, mask);
	}
	
	protected void fireKnobHovered(boolean state) {
		for (KnobHoverListener listener : hoverListeners)
			listener.hover(this, state);
	}
	
//==[ Mouse Handling ]==============================================================================
	
	float x0, y0;
	float l0, t0, r0, b0;
	
	boolean dragging;
	boolean hover;
	
	@Override protected boolean onMouseDown(float x, float y, int button, int modKeysAndButtons) {
		if (!shape.contains(x,y))
			return false;
		
		moveAbove(null);
		redraw();
		
		x0 = x; y0 = y;
		l0 = getLeft(); t0 = getTop(); r0 = getRight(); b0 = getBottom();
		
		dragging = true;
		
		fireKnobClicked(x, y, button, modKeysAndButtons);
		
		return true;
	}

	@Override protected boolean onMouseMove(float x, float y, int modKeysAndButtons) {

		if (!dragging)
			return false;
		
		if (modKeysAndButtons!=0) {
			float dx = x-x0;
			float dy = y-y0;
			setBounds(l0+dx, t0+dy, r0+dx, b0+dy);
			// translate(x-x0, y-y0);
		}
		
		return true;
	}
	
	@Override protected boolean onMouseUp(float x, float y, int button, int modKeysAndButtons) {
		dragging = false;
		return super.onMouseUp(x, y, button, modKeysAndButtons);
	}

	@Override protected boolean onMouseEnter() {
		hover = true;
		fireKnobHovered(true);
		redraw();
		return true;
	}
	
	@Override protected boolean onMouseExit() {
		hover = false;
		fireKnobHovered(false);
		redraw();
		return true;
	}
	
//==[ Rendering ]===================================================================================
	
	@Override protected void render(GC gc) {
		super.render(gc);
		
		Color fg = hover ? hoverForeground : foreground;
		Color bg = hover ? hoverBackground : background;
		
		gc.setAntialias(SWT.ON);
		// gc.setAlpha(228);
		
		double cx = shape.getX() + shape.getWidth()/2;
		double cy = shape.getY() + shape.getHeight()/2;
		
		PathShape path = new PathShape(gc.getDevice(), shape);
		gc.setBackground(bg);
		gc.fillPath(path);
		gc.drawPath(path);
		path.dispose();

		AffineTransform at = new AffineTransform();
		at.translate(+cx, +cy);
		at.scale(0.8, 0.8);
		at.translate(-cx, -cy);
		
		Shape inner = at.createTransformedShape(shape);
		
		PathShape innerPath = new PathShape(gc.getDevice(), inner);
		gc.setBackground(fg);
		gc.fillPath(innerPath);
		innerPath.dispose();
	}
	
//==[ Free Resources ]==============================================================================
	
	@Override public void dispose() {
		super.dispose();
		// free resources
	}
	
//==[ Test-Main ]===================================================================================
	
	public static void main(String[] args) {

		Application app = new Application();
		Shell sh = new Shell(app);

		sh.setLayout(new FillLayout());

		LayerCanvas dc = new LayerCanvas(sh, NONE) {
			final Random rng = new Random(1337);
			
//			@Override protected void renderBackground(Rectangle clip, Transform t, GC gc) {
//				Color random = new Color(getDisplay(), new RGB(rng.nextFloat()*360f,0.8f,0.9f));
//				gc.setBackground(random);
//				gc.fillRectangle(getBounds());
//				random.dispose();
//			}
			
		};

		dc.addListener(SWT.KeyDown, (e) -> {
			if (e.keyCode==SWT.ESC)
				sh.dispose();
		});
		
		
//		Layer d = new Layer(dc)
				// .setSize(100, 100);
				// .rotate(0.5)
//				.translate(100, 100).scale(.5, .5);

		LayerKnob k1 = new LayerKnob(dc);
		LayerKnob k2 = new LayerKnob(dc);
		LayerKnob k3 = new LayerKnob(dc);
		
		k1.addMoveListener((k, x, y) -> {
			System.out.println(k + ", " + x + ", " + y);
		});
		
		k2.addMoveListener((k, x, y) -> {
			System.out.println(k + ", " + x + ", " + y);
		});
		
		k1.setBounds(100, 100, 108, 108);
		k2.setBounds(200, 100, 208, 108);
		k3.setBounds(300, 100, 308, 108);
		
		
		sh.setBounds(1500, 150, 800, 600);
		sh.setVisible(true);
		app.loop(sh);
		
	}
	
}
