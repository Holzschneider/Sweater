package de.dualuse.swt.experiments;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.experiments.SimpleLayout.Layouter;


class SimpleLayout extends Layout {
	interface Layouter {
		void layout(Composite composite);
	}
	
	
	final public Layouter l; 
	public SimpleLayout(int w, int h, Layouter l) {
		this.l = l;
	}
	
	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		return null;
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		this.l.layout(composite);
	}
	
}



public class LayoutTest {
	
	public static void main(String[] args) {
		
		Shell sh = new Shell();
		
		Button but = new Button(sh, 0);
		but.setText("hallo");
		
		sh.setLayout(new SimpleLayout(100, 100, (c) -> but.setBounds(100, 100, 200, 50) ));
		
//		sh.setLayout(new SimpleLayout(100, 100, new Layouter() {
//			
//			@Override
//			public void layout(Composite composite) {
//				but.setBounds(100, 100, 200, 50);				
//			}
//		} ) );
		
		
//		sh.setLayout(new FillLayout());
		
//		sh.setLayout(new Layout() {
//			@Override
//			protected void layout(Composite composite, boolean flushCache) {
//				
//				new Throwable().printStackTrace();
//				
//				Rectangle r = composite.getClientArea();
//				for (Control c: composite.getChildren())
//					c.setBounds(r.width-100, r.height-100, 80, 20);
//			}
//			
//			@Override
//			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
//				return new Point(100, 100);
//			}
//		});
		
		
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);
		
		
		
		
		while (!sh.isDisposed())
			if (!sh.getDisplay().readAndDispatch())
				sh.getDisplay().sleep();
		
		
	}
	
}
