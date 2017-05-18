package de.dualuse.swt.experiments;

import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.NONE;

import java.awt.geom.Rectangle2D;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.graphics.PathShape;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;
import de.dualuse.swt.widgets.LayerContainer;

public class LayerCanvasTest2 {

	static class Frame extends Layer {
		
		Random rng = new Random();
		RGB col = new RGB(rng.nextFloat()*360, 0.8f, 0.9f);

		static int counter = 0;
		String name;
		
		public Frame(LayerContainer parent) {
			super(parent);
			setExtents(-50, -50, 50, 50);
			name = "Frame " + (++counter);
		}
		
		@Override public void onPaint(Event e) {
			GC gc = e.gc;

			Color rc = new Color(getRoot().getDisplay(), col);
			gc.setBackground(rc);
			Rectangle2D rect = new Rectangle2D.Double(left, top, right-left, bottom-top);
			Path path = new PathShape(gc.getDevice(), rect);
			gc.fillPath(path);
			path.dispose();
			rc.dispose();
		}
		
		
		float x0, y0;
		boolean drag;
		
		@Override public void onMouseDown(float x, float y, Event event) {
//			System.out.println(this + ".onMouseDown()");
			if (event.button!=1) return;
			moveTop();
			x0 = x;
			y0 = y;
			drag = true;
			event.doit = false;
		}
		
		@Override public void onMouseUp(float x, float y, Event event) {
//			System.out.println(this + ".onMouseUp()");
			if (event.button!=1) return;
			drag = false;
			event.doit = false;
		}
		
		@Override public void onMouseMove(float x, float y, Event event) {
//			System.out.println(this + ".onMouseMove()");
			if (drag) {
				translate(x-x0, y-y0);
			}
			event.doit = false;
		}
		
		@Override public void onMouseWheel(float x, float y, Event event) {
			int tickCount = event.count;
			
			moveTop();
			
			if (event.button==0) 
				scale( pow(1.0337, tickCount), x,y );
			else
				rotate( tickCount/10f, x,y );
			
			redraw();
		}
		
		@Override public String toString() { return name; }
	}
	
	
	public static void main(String[] args) {

		Application app = new Application();
		Shell sh = new Shell(app);

		sh.setLayout(new FillLayout());

		LayerCanvas dc = new LayerCanvas(sh, NONE);
		
		dc.addListener(SWT.KeyDown, (e) -> {
			if (e.keyCode == SWT.ESC)
				sh.dispose();
		});


		Layer d = new Layer(dc)
				// .setSize(100, 100);
				// .rotate(0.5)
				.translate(100, 100).scale(.5, .5);
//		d.addListener(SWT.MouseMove, (e) -> { e.doit = true; });
		
		new Frame(d);
		new Frame(d);
		new Frame(d);

		Frame frame1 = (Frame)new Frame(dc).translate(320, 50);
		Frame frame2 = (Frame)new Frame(dc).translate(50,  320);
		
		sh.setBounds(1500, 150, 800, 600);
		sh.setVisible(true);
		app.loop(sh);
	}
}