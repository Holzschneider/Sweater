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

import de.dualuse.swt.experiments.scratchy.video.VideoEditor;
import de.dualuse.swt.experiments.scratchy.video.VideoEditor.EditorListener;

public class Timeline extends Canvas implements EditorListener { // FrameListener, EditorListener {

	Display dsp;

	Color background;
	Color foreground;

	int totalFrames;
	int currentFrame;

	VideoEditor editor;

//==[ Constructor ]=================================================================================
	
	public Timeline(Composite parent, int style, VideoEditor editor) {
		super(parent, style);

		dsp = getDisplay();

		background = dsp.getSystemColor(SWT.COLOR_BLACK);
		foreground = dsp.getSystemColor(SWT.COLOR_GRAY);

		this.editor = editor;
		this.totalFrames = editor.getVideo().numFrames();
		
		editor.addEditorListener(this);

		addPaintListener(this::paintControl);

		addListener(SWT.MouseDown, this::down);
		addListener(SWT.MouseMove, this::move);
		addListener(SWT.MouseUp, this::up);
	}

	@Override public boolean setFocus() {
		return false;
	}
	
//==[ Controls ]====================================================================================
	
	private boolean pressed = false;
	private boolean dragged = false;
	
	private void down(Event event) {
		
//		double ratio = Double.valueOf(event.x) / getSize().x;
//		int frame = (int) Math.round(ratio * totalFrames);
		
		int frame = xToFrame(event.x);
		if (frame == currentFrame) return;
		editor.moveTo(frame);
		pressed = true;
		
	}
	
	private void up(Event e) {
		if (dragged)
			editor.moveTo(xToFrame(e.x));
		pressed = dragged = false;
	}
	
	private void move(Event e) {
		if (pressed) {
			dragged = true;
			editor.scratchTo(xToFrame(e.x));
		}
	}
	
	private int xToFrame(int x) {
		double ratio = Double.valueOf(x) / getSize().x;
		return (int) Math.round(ratio * totalFrames);
	}
	
//	private int frameToX(int frame) {
//		return -1;
//	}

//==[ Paint Event ]=================================================================================
	
	protected void paintControl(PaintEvent event) {
		GC gc = event.gc;

		gc.setAntialias(OFF);
		gc.setInterpolation(NONE);

		Point size = getSize();
		int width = size.x;
		int height = size.y;

		double ratio = Double.valueOf(currentFrame) / totalFrames;
		int x = (int) Math.round(ratio * width);

		gc.setBackground(background);
		gc.fillRectangle(0, 0, width, height);

		gc.setBackground(foreground);
		gc.fillRectangle(0, 0, x, height);
	}

//==[ EditorListener ]==============================================================================

	@Override public void scratchedTo(int from, int to) {
		updatePosition(to);
	}

	@Override public void movedTo(int from, int to) {
		updatePosition(to);
	}
	
	private void updatePosition(int to) {
		if (currentFrame == to) return;
		currentFrame = to;
		redraw();
	}
	
//==[ Layout ]======================================================================================
	
	@Override public Point computeSize(int wHint, int hHint, boolean flush) {
		// System.out.println("wHInt: " + wHint);
		return new Point(wHint, 24);
	}

}
