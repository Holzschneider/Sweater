package de.dualuse.swt.experiments;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;

public class ImageUpdateTest {
	static private double $(double s) {
		return ((int)(s*1000))/1000.0;
	}
	
	static private void fill(int w, int h, byte pixels[], int off, int scan) {
		double t = (System.nanoTime()/1e9)*100;
		int T = (int)t;
		for (int y=0,Y=h;y<Y;y++)
			for (int x=0,X=w;x<X;x++) {
				int offset = off+(x+y*scan)*4;
				pixels[offset+0] = 0;
				pixels[offset+1] = (byte)(y+T);
				pixels[offset+2] = 
//				pixels[offset+1] = (byte)(Math.sin((y+t)/123f*PI)*Math.sin((x)/(180f*(1+0.9*Math.sin(t/200)))*PI)*100f+127); 
//				pixels[offset+2] = (byte)(Math.sin((x)/180f*PI)*100f+127); 
//						(byte)(Math.sin((x+T)/10f)*100f+127);
				pixels[offset+3] = (byte)(((x&y)>0)?0:-1);
			}
	}
	
	public static void main(String[] args) {
		
		Application app = new Application();
		
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		byte[] pixels = new byte[800*600*4];
		fill(800,600,pixels,0,800);
		
		
		ImageData id = new ImageData(800, 600, 32, new PaletteData(0x00FF0000, 0x0000FF00, 0x000000FF), 800*4, pixels);
		Image im = new Image(app,id);
		
		Canvas c = new Canvas(sh, NONE);
		
		c.addPaintListener( (e) -> {

			long start = System.nanoTime();

			fill(800,600,pixels,0,800);
			
			long mid = System.nanoTime();
			e.gc.drawImage(im, 0, 0);
//			Image jm = new Image(app, id);
//			e.gc.drawImage(jm, 0, 0);
//			jm.dispose();
			
			long end = System.nanoTime();
			
			sh.setText( $((mid-start)/1e6)+"ms + "+$((end-mid)/1e6)+"ms = "+$((end-start)/1e6)+"ms");
			
			c.redraw();
		});
		
		
		
		
		sh.setBounds(100, 100, 800, 500);
		sh.setVisible(true);
		
		app.loop(sh);
		
	}
}
