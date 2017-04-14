package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.internal.cocoa.NSBitmapImageRep;
import org.eclipse.swt.internal.cocoa.NSSize;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.app.Application;
//Essentials for a CachedImage like "VolatileImage" 

class NSImage extends org.eclipse.swt.internal.cocoa.NSImage {
	private long sel_recache = OS.sel_registerName("recache");
	
	public void recache() {
		OS.objc_msgSend(this.id, sel_recache);
	}
	
}

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
				pixels[offset+0] = -1;
				pixels[offset+1] = (byte)(y+T);
				pixels[offset+2] = 
//				pixels[offset+1] = (byte)(Math.sin((y+t)/123f*PI)*Math.sin((x)/(180f*(1+0.9*Math.sin(t/200)))*PI)*100f+127); 
//				pixels[offset+2] = (byte)(Math.sin((x)/180f*PI)*100f+127); 
//						(byte)(Math.sin((x+T)/10f)*100f+127);
				pixels[offset+3] = (byte)(((x&y)>0)?0:-1);
			}
	}
	
	public static void main(String[] args) {
		
		int W = 800, H = 600;
		
		Application app = new Application();
		
		Shell sh = new Shell(app);
		sh.setLayout(new FillLayout());
		
		byte[] pixels = new byte[W*H*4];
		fill(W,H,pixels,0,W);
		
		
		ImageData id = new ImageData(W, H, 32, new PaletteData(0x00FF0000, 0x0000FF00, 0x000000FF), W*4, pixels);
		Image im = new Image(app,id);
		
		////////////
		
		final NSImage handle = (NSImage)new NSImage().alloc();
		NSSize size = new NSSize();
		size.width = W;
		size.height = H;
		handle.initWithSize(size);
		
		final NSBitmapImageRep rep = (NSBitmapImageRep)new NSBitmapImageRep().alloc();
		
//		OS.NSAlphaFirstBitmapFormat
//		rep = rep.initWithBitmapDataPlanes(0, W, H, 8, hasAlpha ? 4 : 3, hasAlpha, false, OS.NSDeviceRGBColorSpace, OS.NSAlphaNonpremultipliedBitmapFormat, W*4, 32);
//		rep.initWithBitmapDataPlanes(0, W, H, 8, 4, true, false, OS.NSDeviceRGBColorSpace , /*OS.NSAlphaFirstBitmapFormat |/**/ OS.NSAlphaNonpremultipliedBitmapFormat, W*4, 32);
//		rep.initWithBitmapDataPlanes(0, W, H, 8, 3, false, false, OS.NSDeviceRGBColorSpace , OS.NSAlphaFirstBitmapFormat |/**/ OS.NSAlphaNonpremultipliedBitmapFormat, W*4, 32);
		rep.initWithBitmapDataPlanes(0, W, H, 8, 4, true, false, OS.NSDeviceRGBColorSpace , OS.NSAlphaFirstBitmapFormat |/**/ OS.NSAlphaNonpremultipliedBitmapFormat, W*4, 32);
		
		handle.setCacheMode(OS.NSImageCacheNever);
		
		OS.memmove(rep.bitmapData(), pixels, W*H*4);
		
		handle.addRepresentation(rep);
		rep.release();
		
//		handle.setCacheMode(1);
		handle.setCacheMode(OS.NSImageCacheNever);	
		
		im.handle = handle;

		//////////////
		
		

		AtomicInteger mode = new AtomicInteger(0);
		Canvas c = new Canvas(sh, NONE);
		
		c.addPaintListener( (e) -> {

			long start = System.nanoTime();

			fill(800,600,pixels,0,800);
			
			long mid = System.nanoTime();
			String name = "";
			switch (mode.get()%3) {
			case 0: 
				Image jm = new Image(app, id);
				e.gc.drawImage(jm, 0, 0);
				jm.dispose();
				name = "new Image(dsp, id);";
				break;
				
			case 1:
				OS.memmove(rep.bitmapData(), pixels, W*H*4);
				handle.recache();
				e.gc.drawImage(im, 0, 0);
				name = "NSImage.recache();";
				break;
				
			case 2: 
				e.gc.drawImage(im, 0, 0);
				name = "No Image Updating";
				break;
				
			}
			
			long end = System.nanoTime();
			
			sh.setText( $((mid-start)/1e6)+"ms + "+$((end-mid)/1e6)+"ms = "+$((end-start)/1e6)+"ms"+ " [ "+name+" ]");
			
			c.redraw();
		});
		
		c.addListener(MouseDown, (e)-> {
			mode.incrementAndGet();
		});
		
		
		
		sh.setBounds(100, 100, 800, 500);
		sh.setVisible(true);
		
		app.loop(sh);
		
	}
}
