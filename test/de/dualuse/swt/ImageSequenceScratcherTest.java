package de.dualuse.swt;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.io.File;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;


/**
 * 
 * Anforderungen
 * 
 * funktioniert
 *  - fucking fast load / rendering
 *  - nice video scratch-experience 
 * 
 * funktioniert noch nicht:
 * - resource-recycling (nur 100 Images oderso, dann disposen (siehe LinkedHashMap mit removeEldestEntry)
 * - resource disposal (z.B. internes Handling von Images, und async zulieferung von ImageData!)
 * - async display vom Closest loaded + nachträgliches Liefern vom neuesten geladenen
 * (wannimmer repainted wird, soll ein Frame, das nah genug am "ziel-Frame" ist gerendert werden können, sofern 
 * das Zielframe noch nicht fertig geladen wurde. Sobald das Zielframe da ist noch mal ein Repaint, ohne ruckler!)
 *  - HD/SD Frame Supply
 *  (ggf Lösung vom Problem: Mouse-Up triggered HD Frame Load + Repaint, Mouse-Drag operates sloppy on SD Frames)
 *  - achtung! mit einem missmatch von "Current Frame" zu "displayed frame" sollte man umgehen 
 *  (overlay objekte sollten ihre Position am Displayed-Frame anlehnen statt am Current frame!)  
 * 
 * @author holzschneider
 *
 */

public abstract class ImageSequenceScratcherTest extends Canvas {
	
	abstract int frames();
	abstract Image frame(int frameNumber);
		
	public ImageSequenceScratcherTest(Composite parent, int style) {
		super(parent, style| NO_BACKGROUND);
		super.addPaintListener(this::paintControl);
		
		super.addListener(MouseDown, this::down);
		super.addListener(MouseUp, this::up);
		super.addListener(MouseMove, this::move);
	}
	
	
	//////////////////
	
	int frame = 0;
	
	Transform t = new Transform(getDisplay());
	protected void paintControl(PaintEvent e) {
		e.gc.setAntialias(OFF);
		e.gc.setInterpolation(NONE);
		e.gc.drawString("Frame number: "+frame, 100, 100);

		Point p = getSize();
		Image i = frame(frame);
		
		if (i==null)
			System.out.println("NULL?");
		
		if (i!=null) {
			ImageData id = i.getImageData();
			
			float scale = max(p.x*1f/id.width,p.y*1f/id.height);
			e.gc.getTransform(t);
			t.scale(scale, scale);
			e.gc.setTransform(t);
			e.gc.drawImage(i, 0, 0);
		}
		
		
		e.gc.dispose();
	}
	
	////////
	
	private boolean pressed = false;
	private Event l = null;
	
	private void down(Event e) {
		pressed = true;
		l = e;
	}

	private void up(Event e) {
		pressed = false;
	}
	
	private void move(Event e) {
		if (pressed) {
			int last = frame, next = frame+(e.x-l.x);
			
			if (last!=next)
				this.redraw();

			frame = max(0,min(frames()-1,next));
			l = e;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		Display dsp = new Display();
		Shell sh = new Shell(dsp, SHELL_TRIM|NO_BACKGROUND);
		sh.setLayout(new FillLayout());
		

		new ImageSequenceScratcherTest(sh, NONE) {
			
			//XXX Lösung ausdenken für diese "resource dependency" (z.B. Sub-Modules? maven build-Scripts? oder?)
			File root = new File("/Users/holzschneider/Archive/Geenee/Geenee Strips/bigbangtheory-clip1.mov.strip");
			File frames[] = root.listFiles((f) -> f.getName().endsWith(".jpg"));
			
//			Map<Integer,Image> cache = /*Collections.synchronizedMap*/(new LinkedHashMap<Integer,Image>() {
//				private static final long serialVersionUID = 1L;
//				
//				@Override
//				protected boolean removeEldestEntry(java.util.Map.Entry<Integer, Image> eldest) {
//					if (size()<100) 
//						return false;
//					
//					eldest.getValue().dispose();
//					return true;
//				}				
//			});
			
			NavigableMap<Integer,Image> cache = new TreeMap<Integer,Image>();
			Set<Integer> loading = new HashSet<Integer>();
			
			int lastIndex = -1; 
			

			ImageLoader loader = new ImageLoader();
			ExecutorService worker = Executors.newSingleThreadExecutor();
			
			int redrawCounter = 0;
			boolean specialRedraw = true;
			
			
			int paintControlCounter = 0;
			@Override
			protected void paintControl(PaintEvent e) {
				sh.setText("Paint: "+paintControlCounter+++" Load: "+loadCounter+" Cache: "+cacheCounter);
				
				super.paintControl(e);
//				if (specialRedraw)
//					new Throwable().printStackTrace();
//				specialRedraw=false;
				
			}
			
			int loadCounter = 0;
			int cacheCounter = 0;
			
			@Override
			Image frame(int currentIndex) {
				if (!cache.containsKey(currentIndex) && !loading.contains(currentIndex)) {
					loading.add(currentIndex);
					worker.execute( () -> { 
						ImageData id = loader.load(frames[currentIndex].getPath())[0];
						loadCounter++;
						
						dsp.syncExec(() -> {
							cache.put(currentIndex, new Image(dsp, id) );
							loading.remove(currentIndex);
							cacheCounter++;
							
//							if (currentIndex == lastIndex) {
//								System.out.println("redraw "+redrawCounter++);
//								redraw();
//							}
						});
												
					} );
				}
//					cache.put(i, new Image(dsp, frames[i].getPath()) );
//				else
//					cache.put(i, cache.get(i));
				
//				requestedImage = i;
//				return cache.get(i);

//				return lastIndex<currentIndex ?
//						cache.ceilingEntry(lastIndex=currentIndex).getValue() : 
//						cache.floorEntry(lastIndex=currentIndex).getValue();
				
				Entry<Integer,Image> entry = lastIndex<currentIndex ?
					cache.floorEntry(lastIndex=currentIndex) :
					cache.ceilingEntry(lastIndex=currentIndex);
				
				return entry != null ? entry.getValue() : null;
				
			}
			
			@Override
			int frames() {
				return frames.length;
			}
			
		};
		
		sh.setBounds(100, 100, 1200, 800);
		sh.setVisible(true);
		
		
		
		
		while(!sh.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
		
		System.out.println("Disposed");
		System.exit(0);
	}
}
