package de.dualuse.swt;

import static java.lang.Math.*;
import static org.eclipse.swt.SWT.*;

import java.util.*;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TimelineTest {
	static class Observation {
		TreeSet<Integer> keys = new TreeSet<>();
		int number = count++;
		static int count = 0;
	}
	
	
	TreeMap<Long, Observation> keyIdObjectMap = new TreeMap<Long, Observation>();
	

	Random r = new Random(1337);
	int n = 50;
	int minExtent = 0, maxExtent = 3000;
	int sigmaSpan = 50, µSpan = 150;
	int minKeys = 1, maxKeys = 8;


	public TimelineTest() { generate(); }
	
	public void generate() {
		keyIdObjectMap.clear();
		
		for (int i=0;i<n;i++) {
			Observation o = new Observation();
			
			int centerFrame = r.nextInt(maxExtent-minExtent)+minExtent;
			
			int radius = 1+(int) abs(µSpan+r.nextGaussian()*sigmaSpan);
			int numKeys = r.nextInt(maxKeys-minKeys)+minKeys;

			for (int j=0;j<numKeys;j++) {
				int key = max(minExtent, min(maxExtent, r.nextInt(radius*2)+centerFrame-radius));
				
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
		
		timelineCanvas.setSize(observationTimeline.maxExtent,observationTimeline.n*ROW_HEIGHT);
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		byte[] pixels = { 0,0,0, -1, -1, -1};
		ImageData borderData = new ImageData(2, 1, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF), 6, pixels);
		Image borderImage = new Image(dsp, borderData);
		
		
		Random rng = new Random(1337);
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
			
			
			///
			Color white = new Color(dsp, 255, 255, 255);
			Color lighter = new Color(dsp, 255, 255, 255, 80);
			Color dimmer = new Color(dsp, 0, 0, 0, 80);
			
			g.setBackground(white);
			
			g.setLineWidth(1);
			
			int A = ROW_HEIGHT/5, M = 2;
			for (Observation o: sorted) {
				int lowest = o.keys.first();
				int highest = o.keys.last();
				int top = o.number*ROW_HEIGHT+M;
				int height = ROW_HEIGHT-2*M; 
			
				rng.setSeed(System.identityHashCode(o));
				Color individualColor = new Color(dsp, new RGB(rng.nextFloat()*360, 0.7f, 0.6f));
				
				g.setBackground(individualColor);
				g.fillRoundRectangle(lowest, top, highest-lowest, height, A, A);
				
				g.setForeground(lighter);
				
				for (int i = lowest; i<highest; i=o.keys.higher(i))
					if (i!=lowest && i!=highest) {
//						g.setForeground(lighter);
//						g.setAlpha(lighter.getAlpha());
//						g.drawLine(i+1, top, i+1, top+height-1);
//						g.setForeground(dimmer);
//						g.setAlpha(dimmer.getAlpha());
//						g.drawLine(i, top, i, top+height-1);
						
						g.setAlpha(64);
						g.drawImage(borderImage, 0, 0, 2, 1, i, top, 2, height);
						
					}
				
//				g.drawRoundRectangle(lowest, o.number*ROW_HEIGHT+M, highest-lowest-1, ROW_HEIGHT-2*M-1, A, A);
				g.setAlpha(255);
				individualColor.dispose();
			}
			
			
			
			System.out.println(e.x+", "+e.y+", "+e.width+","+e.height);
			System.out.println();
		});
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		sc.setContent(timelineCanvas);
		
		timelineCanvas.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				timelineCanvas.redraw(e.x, e.y, 100, 100, true);				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		sh.setBounds(100,100,1200,500);
		sh.setVisible(true);
		
		while (!sh.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		
		dsp.dispose();
		
	}
}






