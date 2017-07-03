package de.dualuse.swt.app;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

import de.dualuse.swt.layout.LayoutDelegate;

public class Window extends Shell implements AutoMenuBar {
	
	public Window() {
		this(Display.getCurrent());
	}
	
	public Window(Display parent) {
		setMenuBar(
				new AutoMenuBuilder()
				.add(parent, MenuScope.WINDOW)
				.add(this, MenuScope.UNSPECIFIED, MenuScope.WINDOW)
				.build(new Menu(this,BAR))
		);
		
		setLayout(layoutDelegate);
	}
	
	
	@Override public void layout() {
		if (getLayout()!=layoutDelegate) 
			super.layout();
		else
			for (Control c: getChildren())
				c.setBounds(getClientArea());
	}
	
	private Layout layoutDelegate = new LayoutDelegate().computeSize(this::computeSize).layout(this::layout);
	private Point computeSize(Composite c, int wHint, int hHint, boolean flush) { return computeSize(wHint, hHint); }
	private void layout(Composite c, boolean flushCache) { this.layout(); }
	
	@Override
	protected void checkSubclass() { }
	
	
//    public static final int DO_NOTHING_ON_CLOSE = 0;
//    public static final int HIDE_ON_CLOSE = 1;
//    public static final int DISPOSE_ON_CLOSE = 2;
//    public static final int EXIT_ON_CLOSE = 3;
//
//	public void setDefaultCloseOperation(int operation) {
//		//...
//	}
	
	
	
	
}
