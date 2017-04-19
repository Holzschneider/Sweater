package de.dualuse.swt.experiments.scratchy.view;

import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.OFF;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import de.dualuse.swt.experiments.scratchy.view.MainView.FrameListener;

public class Timeline extends Canvas implements FrameListener {
	
	Display dsp;
	
	Color background;
	Color foreground;
	
	int totalFrames;
	int currentFrame;
	
	MainView main;
	
	public Timeline(Composite parent, int style, MainView main, int totalFrames) {
		super(parent, style);
		
		dsp = getDisplay();
		
		background = dsp.getSystemColor(SWT.COLOR_DARK_GRAY);
		foreground = dsp.getSystemColor(SWT.COLOR_GRAY);
		
		this.totalFrames = totalFrames;
		this.main = main;
		
		addPaintListener(this::paintControl);
		addListener(SWT.MouseDown, this::down);
	}
	
	void down(Event event) {
		double ratio = Double.valueOf(event.x) / getSize().x;
		int frame = (int)Math.round(ratio * totalFrames);
		main.gotoFrame(frame);
	}
	
	protected void paintControl(PaintEvent event) {
		GC gc = event.gc;

		gc.setAntialias(OFF);
		gc.setInterpolation(NONE);
		
		Point size = getSize();
		int width = size.x;
		int height = size.y;
		
		double ratio = Double.valueOf(currentFrame) / totalFrames;
		int x = (int)Math.round(ratio * width);
		
		gc.setBackground(background);
		gc.fillRectangle(0, 0, width, height);
		
		gc.setBackground(foreground);
		gc.fillRectangle(0, 0, x, height);
		
		System.out.println("Painting timeline (" + width + ")");
	}

	public void setCurrentFrame(int currentFrame) {
		if (this.currentFrame == currentFrame) return;
		
		this.currentFrame = currentFrame;
		redraw();
	}

	@Override public void currentFrame(int last, int current) {
		if (currentFrame == current) return;
		currentFrame = current;
		System.out.println("current: " + current);
		redraw();
	}
	
	@Override public Point computeSize(int wHint, int hHint, boolean flush) {
		System.out.println("wHInt: " + wHint);
		return new Point(wHint, 24);
	}
}
