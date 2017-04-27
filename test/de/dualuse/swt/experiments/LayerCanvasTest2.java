package de.dualuse.swt.experiments;

import static java.lang.Math.pow;
import static org.eclipse.swt.SWT.NONE;

import java.util.Random;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
import de.dualuse.swt.widgets.Layer;
import de.dualuse.swt.widgets.LayerCanvas;
import de.dualuse.swt.widgets.LayerContainer;

public class LayerCanvasTest2 {

	static class Frame extends Layer {

		public Frame(LayerContainer parent) {
			super(parent);
			setBounds(-50, -50, 50, 50);
		}
		
		Random rng = new Random();
		RGB col = new RGB(rng.nextFloat()*360, 0.8f, 0.9f);
		
		@Override
		protected void render(GC c) {
			Color rc = new Color(getRoot().getDisplay(), col);
			c.setBackground(rc);
			c.fillRectangle(getBounds());
			rc.dispose();
			
			c.drawRectangle(getBounds());
		}
		
		
		float x0, y0;
		
		@Override
		protected boolean onMouseDown(float x, float y, int button, int modKeysAndButtons) {
			moveAbove(null);
			getRoot().redraw();
			x0 = x;
			y0 = y;
			return true;
		}
		
		@Override
		protected boolean onMouseMove(float x, float y, int modKeysAndButtons) {
			if (modKeysAndButtons!=0)
				translate(x-x0, y-y0);

			getRoot().redraw();
			return true;
		}
		
		@Override
		protected boolean onMouseWheel(float x, float y, int tickCount, int modKeysAndButtons) {
			scale( pow(1.0337, tickCount), x,y );
			getRoot().redraw();
			
			return true;
		}
	}
	
	
	public static void main(String[] args) {

		Application app = new Application();
		Shell sh = new Shell(app);

		sh.setLayout(new FillLayout());

		LayerCanvas dc = new LayerCanvas(sh, NONE);
		Layer d = new Layer(dc)
				// .setSize(100, 100);
				// .rotate(0.5)
				.translate(100, 100).scale(.5, .5);

		
		Frame a = new Frame(d);
		Frame b = new Frame(d);
		Frame c = new Frame(d);
		
		
		sh.setBounds(1500, 150, 800, 600);
		sh.setVisible(true);
		app.loop(sh);

		// System.out.println(getVersion());
	}
}
