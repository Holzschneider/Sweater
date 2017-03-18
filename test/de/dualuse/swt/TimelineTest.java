package de.dualuse.swt;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.util.*;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TimelineTest {
	static class Observation {
		TreeSet<Integer> keys = new TreeSet<>();
	}
	
	
	TreeMap<Long, Observation> keyIdObjectMap = new TreeMap<Long, Observation>();
	

	Random r = new Random(1337);
	int n = 50;
	int minSpan = 0, maxSpan = 3000;
	int sigmaSpan = 100;
	int minKeys = 1, maxKeys = 8;


	public TimelineTest() { generate(); }
	
	public void generate() {
		keyIdObjectMap.clear();
		
		for (int i=0;i<n;i++) {
			Observation o = new Observation();
			
			int centerFrame = r.nextInt(maxSpan-minSpan)+minSpan;
			
			int radius = 1+(int) abs(r.nextGaussian()*sigmaSpan);
			int numKeys = r.nextInt(maxKeys-minKeys)+minKeys;

			for (int j=0;j<numKeys;j++) {
				int key = max(minSpan, min(maxSpan, r.nextInt(radius*2)+centerFrame-radius));
				
				o.keys.add(key);
				
				
				long lid = (((long)System.identityHashCode(o))&0xFFFFFFFFL);
				long lkey =  (((long)key)<<32);
				
				long keyId = lkey|lid;
				
				keyIdObjectMap.put( keyId, o); 
			}
		}
		
	}
	
	
	
	
	final static int ROW_HEIGHT = 24;
	
	
	
	public static void main(String[] args) {
		
		Display dsp = new Display();
		Shell sh = new Shell();
		sh.setLayout(new FillLayout());
		
		
		TimelineTest observationTimeline = new TimelineTest();
		
		
		ScrolledComposite sc = new ScrolledComposite(sh,  V_SCROLL|H_SCROLL|NO_BACKGROUND);
		Canvas timelineCanvas  = new Canvas(sc, NONE);
		
		timelineCanvas.setSize(observationTimeline.maxSpan,observationTimeline.n*ROW_HEIGHT);
		
		timelineCanvas.addPaintListener( (e) -> {
			
			TimelineTest ot = observationTimeline;
			GC g = e.gc;
			Point s = timelineCanvas.getSize();
			
			Color grey = new Color( dsp, 0x70, 0x70, 0x70 );
			Color dark = new Color( dsp, 0x40, 0x40, 0x40 );
			Color darker = new Color( dsp, 0x30, 0x30, 0x30 );
			
			for (int i=0;i<ot.n;i++) {
				g.setBackground(i%2==0?dark:darker);
				g.fillRectangle(e.x, i*ROW_HEIGHT, e.width, ROW_HEIGHT);
			}
			
			g.setLineWidth(1);
			g.setForeground(grey);
			for (int i=0;i<s.x;i+=240) 
				if (e.x<i && i<e.x+e.width)
					g.drawLine(i, e.y, i, e.y+e.height);
			
			grey.dispose();
			dark.dispose();
			darker.dispose();
			
			

			HashSet<Observation> visible = new HashSet<Observation>();
			Long left = ot.keyIdObjectMap.ceilingKey(((long)e.x)<<32);
			Long right = ot.keyIdObjectMap.floorKey(((long)(e.x+e.width))<<32);
			
			for (;left<right; left = ot.keyIdObjectMap.higherKey(left))
				visible.add( ot.keyIdObjectMap.get(left) );
			
			ArrayList<Observation> sorted = new ArrayList<Observation>(visible);
			sorted.sort((a,b) ->  Integer.compare(a.keys.first(),b.keys.first()) );
			
			
			
			System.out.println(e.x+", "+e.y+", "+e.width+","+e.height);
			System.out.println();
		});
		
		sc.setContent(timelineCanvas);
		
		
		
		sh.setBounds(100,100,1200,500);
		sh.setVisible(true);
		
		while (!sh.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		
		dsp.dispose();
		
	}
}






