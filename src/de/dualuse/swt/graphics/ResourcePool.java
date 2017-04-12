package de.dualuse.swt.graphics;

import static org.eclipse.swt.SWT.Dispose;

import java.awt.Shape;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;



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
 * Color() {
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


public class ResourcePool implements Closeable, DisposeListener, Listener {
	static private class SharedResourceHolder {
		private Object key;
		private Resource reference;
		private int counter = 0;
		
		public SharedResourceHolder(Object key, Resource reference) {
			this.key = key;
			this.reference = reference;
		}
		
		@SuppressWarnings("unchecked")
		public<T extends Resource> T inc() {
			counter++;
			return (T) reference; 
		}
		
		public int dec() {
			return --counter;
		}
		
		public void dispose() {
			System.out.println("Disposing: "+reference);
			reference.dispose();
			reference = null;
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private final static int MAX_STACK_DEPTH = 32;	
	
	final static private HashMap<Device, HashMap<Object, SharedResourceHolder>> global =
				new HashMap<Device, HashMap<Object, SharedResourceHolder>> ();
	
	private Display device; 
	private HashMap<Object, SharedResourceHolder> pool;
	private ArrayList<SharedResourceHolder> shared;
	private ArrayList<Resource> owned;
	
	private int frame = 0;
	private int[] sharedBase = new int[MAX_STACK_DEPTH];
	private int[] ownedBase = new int[MAX_STACK_DEPTH];
	
	public ResourcePool push() {
		sharedBase[frame] = shared.size();
		ownedBase[frame] = owned.size();
		frame++;
		return this;
	}
	
	public ResourcePool pop() {
		frame--;
		if (frame<0) {
			System.err.println("Resource Pool: Stack Underflow. Resource leak hazard.");
			frame = 0;
		}
		
		for (int i=sharedBase[frame],I=shared.size()-1;i<=I;I--)
			if (shared.get(I).dec()==0) 
				pool.remove(shared.remove(I).key).dispose();
			else 
				shared.remove(I);
		
		sharedBase[frame] = shared.size();
		

		for (int i=ownedBase[frame],I=owned.size()-1;i<=I;I--)
			owned.remove(I).dispose();
		
		ownedBase[frame] = owned.size();
		
		return this;
	}
	
	
	public ResourcePool() { 
		this(Display.getCurrent()); 
	}
	
	public ResourcePool(Widget w) {
		this(w.getDisplay());
		w.addDisposeListener(this);
	}
	
	public ResourcePool(Display d) { 
		this.device = d; 
		d.addListener(Dispose, this);
		
		synchronized(global) {
			if (!global.containsKey(d))
				global.put(d, new HashMap<Object,SharedResourceHolder>());
			
			pool = global.get(d);
			
		}
		
		shared = new ArrayList<SharedResourceHolder>();
		owned = new ArrayList<Resource>();
		
		push();
	}
	
	//////
	public void dispose() {
		while (frame>0)
			pop();
		
		shared = null;
		device = null;
		pool = null;
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
	
	
	public void open() {
		push();
	}
	
	@Override
	public void close() {
		pop();
	}
	
	@Override
	protected void finalize() throws Throwable {
		device.asyncExec(this::dispose);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private boolean registered(Object key) {
		return pool.containsKey(key);
	}
	
	private<T extends Resource> T register(Object key, T r) {
		SharedResourceHolder srh = new SharedResourceHolder(key, r);
		pool.put(key, srh);
		return r;
	}
	
	
	private<T extends Resource> T refer(Object key) {
		SharedResourceHolder srh = pool.get(key);
		shared.add(srh);
		return srh.inc();
	}
	
	private<T extends Resource> T register(T r) {
		owned.add(r);
		return r;
	}
	
	////////////// COLORS /////////////////
	public Color color(int argb) {
		return color(
				(argb&0x00FF0000)>>>16,
				(argb&0x0000FF00)>>>8,
				(argb&0x000000FF)>>>0,
				(argb&0xFF000000)>>>24	);
	}
	
	public Color color(RGB rgb) { return color(rgb.red,rgb.green,rgb.blue); }
	public Color color(int red, int green, int blue) { 
		int code = ((red&0xFF)<<16)|((green&0xFF)<<8)|(blue&0xFF);

		if (!registered(code)) 
			register(code, new Color(device, red, green, blue));
		
		return refer(code);
	}
	
	public Color color(RGBA rgba) { return color(rgba.rgb.red,rgba.rgb.green,rgba.rgb.blue,rgba.alpha); }
	public Color color(int r, int g, int b, int a) { 
		int code = ((a&0xFF)<<24)|((r&0xFF)<<16)|((g&0xFF)<<8)|(b&0xFF);

		if (!registered(code)) 
			register(code, new Color(device, r, g, b, a));
		
		return refer(code);
	}
	
	////////////// PATTERNS //////////////
	public Pattern pattern(Image im) {
		return register(new Pattern(device, im));
	}

	public Pattern pattern(float x1, float y1, float x2, float y2, Color c1, Color c2) {
		String id = ""+x1+y1+x2+y2+c1+c2;
		
		if (!registered(id))
			register(id, new Pattern(device, x1, y1, x2, y2, c1, c2));
		
		return refer(id);
	}

	public Pattern pattern(float x1, float y1, float x2, float y2, Color c1, int a1, Color c2, int a2) {
		String id = ""+x1+y1+x2+y2+c1+c2;
		
		if (!registered(id))
			register(id, new Pattern(device, x1, y1, x2, y2, c1, a1, c2, a2));
		
		return refer(id);
	}
	
	
	////////////// FONTS /////////////////
	public Font font(String name, int height, int style) {
		String id = name+"-"+height+"-"+style;

		if (!registered(id))
			register(id, new Font(device,name,height,style));
		
		return refer(id);
	}
	
	public Font font(FontData fd) { 
		String id = fd.getName()+"-"+fd.getHeight()+"-"+fd.getStyle()+"-"+fd.getLocale();
		if (!registered(id))
			register(id, new Font(device,fd));
		
		return refer(id);
	}

	public Font font(FontData... fds) { 
		String id = "";
		for (FontData fd: fds)
			id += fd.getName()+"-"+fd.getHeight()+"-"+fd.getStyle()+"-"+fd.getLocale();
		
		if (!registered(id))
			register(id, new Font(device,fds));
		
		return refer(id);
	}

	
	////////////// IMAGES /////////////////
	public Image image(String filename) {
		if (!registered(filename))
			register(filename, new Image(device, filename));
		
		return refer(filename);
	}
	
	public Image image(URL imageUrl) throws IOException {
		if (!registered(imageUrl))
			try (InputStream is = imageUrl.openStream()) {
				register( imageUrl, new Image(device, is) );
			}
		
		
		return refer(imageUrl);
	}
	
	
	////////////// MUTABLE RESOURCES //////////
	
	public Region region() { return register(new Region(device)); }
	public Transform transform() { return register(new Transform(device)); }
	public Cursor cursor(int style) { return register(new Cursor(device, style)); }
	public Cursor cursor(ImageData source, int x, int y) { return register(new Cursor(device, source, x, y)); }
	public Cursor cursor(ImageData s, ImageData m, int x, int y) { return register(new Cursor(device, s, m, x, y)); }

	public Path path() { return register(new Path(device)); }
	public Path path(Shape s) { return register(new PathShape(device,s)); }
	public Path path(Path p, float flatness) { return register(new Path(device, p, flatness)); }

	public TextLayout textLayout() { return register(new TextLayout(device)); }
	
}






