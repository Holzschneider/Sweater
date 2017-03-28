package de.dualuse.swt.graphics;

import static org.eclipse.swt.SWT.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;



public class ResourcePool implements Closeable, DisposeListener, Listener {
	public static final int RESOURCE_OVERCONSUMPTION_WARNING_THRESHOLD = 10000;
	
	final public Device device; 

	/**
	 *  IDEA: Cross Connected ResourcePools:
	 *  
	 *  ResourcePools may share static caches (per Device!)
	 *  
	 *  private static HashMap<Device, HashMap<Object, RefCountedResource>> globalCache = ...;
	 *  
	 *  
	 *  ResourcePool () {
	 *   //...
	 *   ownCache = globalCache.getOrDefault(device);
	 *   globalCache.put(device,ownCache)
	 * }
	 * 
	 * 
	 * ...
	 * createColor() {
	 *   int id = ...;
	 *   Color c = (Color)ownCache.get(id);
	 *   if (c ==null)
	 *      ownCache.put(id, this.registerRefCounted(c = new Color(...) ).incRefCountAndReturn());
	 *      
	 *   return c; 
	 * }
	 * 
	 *  for (RefCountedResource rcr: registered)
	 *     if(rcr.decRefCount()==0)
	 *     		rcr.value.dispose();
	 *  
	 *  
	 *  ok sieht geilaus, brauchen wir -> Philipp bildet sich ein, dass ein LocalResourcePool kein großen gewinn
	 *  bringt. ggf. LocalResourcePool extends ResourcePool plus überschreiben der Globalen Zugriffs-Methoden 
	 *  Refcounters (die Register Methode muss einfach nicht refcounten und der Construktor Lookup guckt einfach
	 *  nicht im shared static Pool nach, sondern baut sich eine eigene HashMap)
	 *  
	 */

	public ResourcePool() { device = Display.getCurrent(); }
	public ResourcePool(Device d) { this.device = d; }
	public ResourcePool(Display d) { this.device = d; d.addListener(Dispose, this); }
	public ResourcePool(Widget w) { this.device = w.getDisplay(); w.addDisposeListener(this); }
	
	

	
	//////
	public void dispose() {
		if (registered == null) return;
			
		for (Resource r: registered)
			r.dispose();
		
		registered = null;
	}
	
	@Override
	public void handleEvent(Event event) {
		if (event.type==SWT.Dispose)
			dispose();
	}
	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		dispose();
	}
	
	@Override
	public void close() {
		dispose();
	}

	protected void warn() {
		System.err.print("Resource Overconsumption Warning: "+registered.size()+" objects allocated.");
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private ArrayList<Resource> registered = new ArrayList<Resource>(); 
	
	public<T extends Resource> T register(T resource) {
		if (registered==null)
			throw new RuntimeException("ResourcePool has been already disposed.");
			
		if ((registered.size()+1)%RESOURCE_OVERCONSUMPTION_WARNING_THRESHOLD==0) 
			warn();
		
		registered.add(resource);
		return resource;
	}

	public<T extends Resource> T unregister(T resource) {
		registered.remove(resource);
		return resource;
	}
	
	////////////// COLORS /////////////////
	
	final private HashMap<Integer,Color> colorCache = new HashMap<Integer,Color>();
	public Color createColor(RGB rgb) { return createColor(rgb.red,rgb.green,rgb.blue); }
	public Color createColor(int red, int green, int blue) { 
		int code = ((red&0xFF)<<16)|((green&0xFF)<<8)|(blue&0xFF);
		Color cached = colorCache.get(code);
		
		if (cached==null)
			colorCache.put(code,register(cached = new Color(device, red, green, blue)));
		
		return cached;
	}
	
	public Color createColor(RGBA rgba) { return createColor(rgba.rgb.red,rgba.rgb.green,rgba.rgb.blue,rgba.alpha); }
	public Color createColor(int r, int g, int b, int a) { 
		int code = ((a&0xFF)<<24)|((r&0xFF)<<16)|((g&0xFF)<<8)|(b&0xFF);
		Color cached = colorCache.get(code);
		
		if (cached==null)
			colorCache.put(code,register(cached = new Color(device, r, g, b, a)));
		
		return cached;
	}
	
	
	////////////// FONTS /////////////////
	
	final private HashMap<String,Font> fontCache = new HashMap<String,Font>(); 
	public Font createFont(String name, int height, int style) {
		String id = name+"-"+height+"-"+style;
		Font font = fontCache.get(id);
		
		if (font==null)
			fontCache.put(id,register(font = new Font(device,name,height,style)));
		
		return font;
	}
	
	public Font createFont(FontData fd) { 
		String id = fd.getName()+"-"+fd.getHeight()+"-"+fd.getStyle()+"-"+fd.getLocale();
		Font font = fontCache.get(id);
		
		if (font==null)
			fontCache.put(id,register(font = new Font(device,fd)));
		
		return font;
	}

	public Font createFont(FontData... fds) { 
		String id = "";
		for (FontData fd: fds)
			id += fd.getName()+"-"+fd.getHeight()+"-"+fd.getStyle()+"-"+fd.getLocale();
		
		Font font = fontCache.get(id);
		
		if (font==null)
			fontCache.put(id,register(font = new Font(device,fds)));
		
		return font;
	}
	
	////////////// IMAGES /////////////////
	
	HashMap<Object, Image> imageCache = new HashMap<Object,Image>();
	public Image createImage(String filename) {
		Image im = imageCache.get(filename);
		
		if (im==null)
			imageCache.put(filename, register(im = new Image(device, filename)));
		
		return im;
	}

	public Image createImage(URL imageUrl) throws IOException {
		Image im = imageCache.get(imageUrl);
		if (im==null)
			try (InputStream is = imageUrl.openStream()) {
				imageCache.put(imageUrl, register(im = new Image(device, is)));
			} 

		
		return im;
	}
	
	//XXX macht das Sinn? man kann ja ImageData ändern und das zurückgegebene Image wird immernoch gleich sein
//	public Image createImage(ImageData data) {
//		Image im = imageCache.get(data);
//		
//		if (im==null)
//			imageCache.put(filename, register(im = new Image(device, filename)));
//		
//		return im;
//	}
	

	////////////// TRANSFORMS /////////////////
	////////////// REGION /////////////////////
	////////////// CURSOR /////////////////////
	////////////// PATH ///////////////////////
	////////////// PATTERN ////////////////////
	////////////// TEXT-LAYOUT? ///////////////
	////////////// GC for Image? //////////////
	



	
//	undsoweiter

}






