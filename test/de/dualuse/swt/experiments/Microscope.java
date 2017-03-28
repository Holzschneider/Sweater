package de.dualuse.swt.experiments;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import de.dualuse.swt.util.SWTUtil;

public abstract class Microscope extends Composite {

	private boolean relative = true;
	private boolean widthPinned = true;

	private boolean normalizeStrokeSize = false;
	private boolean flipY = false;
	
	private Transform canvasTransform;
	
	private Rectangle lastSize = null;
	
//==[ Constructors ]================================================================================
	
	public Microscope(Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);
		
		canvasTransform = new Transform(getDisplay());
		
		// XXX with scale/transformation smoother than without?
		canvasTransform.scale(1.01f, 1.01f);
		
		this.addControlListener(resizeListener);
		this.addMouseListener(viewportController);
		this.addMouseMoveListener(viewportController);
		this.addMouseWheelListener(viewportController);
		this.addPaintListener(paintListener);
	}

//==[ Setter/Getter ]===============================================================================
	
	public void setRelative(boolean relative) {
		this.relative = relative;
	}
	public boolean isRelative() {
		return relative;
	}
	
	public void setWidthPinned(boolean widthPinned) {
		this.widthPinned = widthPinned;
	}
	
	public boolean isWidthPinned() {
		return widthPinned;
	}
	
//==[ Canvas Transform ]============================================================================

	// Internal Work Variables
	private float[] elements = new float[6];
	private Transform inverseTransform = new Transform(getDisplay());
	
	final public float[] getPointOnCanvas(float[] onComponent) {
		float[] onCanvas = new float[2];
		onCanvas[0] = onComponent[0];
		onCanvas[1] = onComponent[1];
		inverseTransform(onCanvas);
		return onCanvas;
	}
	
	private float[] inverseTransform(float[] p) {
		try {
			canvasTransform.getElements(elements);
			inverseTransform.setElements(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5]);
			inverseTransform.invert();
			inverseTransform.transform(p);
		} catch (SWTException ex) {}
		return p;
	}

//==[ ChangeListener Management ]===================================================================
	
	public interface ChangeListener extends SWTEventListener {
		void stateChanged(Widget widget);
	}
	
	private CopyOnWriteArrayList<ChangeListener> cls = new CopyOnWriteArrayList<ChangeListener>();
	public void addChangeListener(ChangeListener cl) { cls.add(cl); }
	public void removeChangeListener(ChangeListener cl) { cls.remove(cl); }
	protected void fireStateChanged() { for (ChangeListener cl : cls) cl.stateChanged(this); }
	
//==[ Abstract Interface ]==========================================================================
	
	abstract void paintCanvas(GC gc);
	
//==[ Resize-Listener ]=============================================================================
	
	ControlAdapter resizeListener = new ControlAdapter() {
		@Override public void controlResized(ControlEvent e) {
			Rectangle currentSize = Microscope.this.getBounds();
			
			if (lastSize!=null && isRelative()) {
				float dx = (currentSize.width - lastSize.width)/2f;
				float dy = (currentSize.height - lastSize.height)/2f;
				float dsw = currentSize.width*1f / lastSize.width;
				float dsh = currentSize.height*1f / lastSize.height;
				
				Transform at = new Transform(getDisplay());
				float cx = getBounds().width/2;
				float cy = getBounds().height/2;
				
				if (isWidthPinned()) {
					at.translate(0, cy + dy*dsw);
					at.scale(dsw, dsw);
					at.translate(0,  -cy);
				} else {
					at.translate(cx + dx + dsh, 0);
					at.scale(dsh,  dsh);
					at.translate(-cx, 0);
				}
				
				canvasTransform.multiply(at);
				fireStateChanged();
			}
			
			lastSize = currentSize;
		}
	};
	
//==[ Viewport Controller ]=========================================================================
	
	class ViewportController extends MouseAdapter implements MouseMoveListener, MouseWheelListener {
		
		float[] p = new float[2];
		float[] q = new float[2];
		
		boolean dragActive = false;
		
		@Override public void mouseMove(MouseEvent e) {
			if (dragActive)
				mouseDragged(e);
			else
				setLocation(p, e);
		}

		@Override public void mouseDown(MouseEvent e) {
			dragActive = true;
			setLocation(p, e);
		}
		
		@Override public void mouseUp(MouseEvent e) {
			dragActive = false;
		}
		
		private void mouseDragged(MouseEvent e) {
//			System.out.println("Dragged (" + e.button + ")");
//			if (e.button != 2)
//				return;
			
			setLocation(q, e);
			
			canvasTransform.getElements(elements);
			float scx = elements[0];
			float scy = elements[3];
			float shy = elements[1];
			float shx = elements[2];
			
			canvasTransform.translate(
				((q[0]-p[0]) / (float)Math.hypot(scx, shy)),
				((q[1]-p[1]) / (float)Math.hypot(scy, shx))
			);
			
			fireStateChanged();
			
			setLocation(p, q);
			redraw();
		}

		@Override public void mouseScrolled(MouseEvent e) {
			setLocation(q, e);
			inverseTransform(q);
			
			canvasTransform.translate(q[0], q[1]);
						
			float s = (float)Math.pow(1.04, e.count);
			canvasTransform.scale(s, s);
			
			canvasTransform.translate(-q[0],  -q[1]);
			
			fireStateChanged();
			
			redraw();
		}
		
		private void setLocation(float[] arr, MouseEvent src) {
			arr[0] = src.x;
			arr[1] = src.y;
		}
		
		private void setLocation(float[] to, float[] from) {
			to[0] = from[0];
			to[1] = from[1];
		}
	}
	
	ViewportController viewportController = new ViewportController();
	
//==[ Paint-Listener ]==============================================================================
	
	PaintListener paintListener = new PaintListener() {
		@Override public void paintControl(PaintEvent e) {
			GC gc = e.gc;
			
			Transform at = new Transform(getDisplay());
			gc.getTransform(at);
			at.multiply(canvasTransform);
			
			if (flipY) {
				at.translate(0, getBounds().height);
				at.scale(1,  -1);
			}
			
			gc.setTransform(at);
			
			// XXX stroke size? only int, no subpixel precision...
			if (normalizeStrokeSize) {
				canvasTransform.getElements(elements);
				float scaleX = elements[0];
				gc.setLineWidth((int)(1f/scaleX));
			}
				
			paintCanvas(gc);
		}
	};

	
//==[ Test-Main ]===================================================================================

	public static void main(String[] args) {
		Shell shell = new Shell();
		shell.setText("SWTMicroscope");
		shell.setLayout(new FillLayout());
		
		Image image = new Image(Display.getCurrent(), Microscope.class.getResourceAsStream("generic-cat.jpeg"));
		
		
		new Microscope(shell, SWT.NONE) {
			@Override void paintCanvas(GC gc) {
				gc.setAntialias(SWT.OFF);
				gc.setInterpolation(SWT.NONE);
				gc.setLineWidth(1);
				gc.drawImage(image, 0, 0);
				gc.drawLine(0, 0, 100, 100);
			}
		};
		
		new Browser(shell,  SWT.NONE).setUrl("http://news.ycombinator.com");
		
		SWTUtil.exitOnClose(shell);
		SWTUtil.center(shell);
		shell.open();
		SWTUtil.eventLoop();
	}
	
}
