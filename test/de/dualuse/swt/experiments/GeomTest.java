package de.dualuse.swt.experiments;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GeomTest {
	
	float tick = 0f;
	
	public GeomTest() {
		
		Display dsp = Display.getDefault();
		Shell shell = new Shell(dsp);
		
//		float[] m = new float[] { 1,2,3,4,5,6 };
//		Transform t = new Transform(dsp);
//		t.setElements(m[0], m[1], m[2], m[3], m[4], m[5]);
//		t.getElements(m);
//		for (int i=0; i<m.length; i++)
//			System.out.println(m[i]);
		
		shell.addListener(SWT.Paint, (e) -> {
			
			GC gc = e.gc;
			gc.setAntialias(SWT.ON);
//			gc.setLineAttributes(new LineAttributes(1));

			gc.getGCData().state |= 1 << 14;
//			gc.setLineAttributes(new LineAttributes(2, SWT.CAP_FLAT, SWT.JOIN_MITER));
			gc.setLineAttributes(new LineAttributes(1));
			
			float theta = (float)(Math.PI/2);
			
			float sx = 1.0f;
			float shy = 1.0f;
			
			float shx = 0.0f;
			float sy = 1.0f;

			float tx = 600;
			float ty = 600;
			
//			sx = (float)Math.cos(theta);
//			shx = (float)Math.sin(theta);
//			shy = -(float)Math.sin(theta);
//			sy = (float)Math.cos(theta);
			
			Transform transform = new Transform(Display.getCurrent());
			transform.setElements(sx, shx, shy, sy, tx, ty);
			// transform.identity();
			
//			float[] elements = new float[6];
//			transform.getElements(elements);
//			print("pre", elements);

//			float scy = (float)Math.sin(tick = tick+0.01f);
//			System.out.println(scy);
			
//			transform.scale(1, -1.0000001f);
			transform.scale(1, -1f);
			// transform.scale(1, scy);
			
//			transform.getElements(elements);
//			print("post", elements);
			
			gc.setTransform(transform);

			// y red
			gc.setForeground(dsp.getSystemColor(SWT.COLOR_RED));
			gc.drawLine(0, -256, 0, +256);
			
			// x blue
			gc.setForeground(dsp.getSystemColor(SWT.COLOR_BLUE));
			gc.drawLine(-256, 0, 256, 0);
			
			transform.dispose();
			
//			shell.redraw();
			
		});
		
		shell.open();
		
		while(!shell.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
	}
	
	public static void main(String[] args) {
		new GeomTest();
	}
	
	static void print(String label, float[] elements) {
		System.out.println("===== ( " + label + " )");
		System.out.println(elements[0] + "\t" + elements[1] + "\t" + elements[4]);
		System.out.println(elements[2] + "\t" + elements[3] + "\t" + elements[5]);
		System.out.println("=====");
	}
}
