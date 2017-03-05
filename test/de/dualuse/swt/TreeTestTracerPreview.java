package de.dualuse.swt;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class TreeTestTracerPreview extends Composite {

	static class Sign {
		public Image img;
		public Point2D p;
		public Sign(Image img, Point2D p) {
			this.img = img;
			this.p = p;
		}
	}
	
	List<Sign> signs = new ArrayList<Sign>();
	
	int dx, dy, dw, dh;
	
	private Point2D convert(Point p) {
		
		int x_ = p.x - dx;
		int y_ = p.y - dy;
		
		return new Point2D.Double(x_*1.0/dw, y_*1.0/dh);
	}
	
	public TreeTestTracerPreview(Composite parent, int style) {
		super(parent, style);
//		super(parent, style | SWT.DOUBLE_BUFFERED);
		
		setLayout(new FillLayout());
		Canvas canvas = new Canvas(this, style | SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
		
		canvas.setBackground(new Color(getDisplay(), new RGB(228, 128, 128)));
		
		final Image frame = new Image(getDisplay(), TreeTestTracerPreview.class.getResourceAsStream("frame.jpg"));
		
		canvas.addPaintListener(new PaintListener() {
			@Override public void paintControl(PaintEvent event) {
				
				GC gc = event.gc;
				
				int WIDTH = canvas.getBounds().width;
				int HEIGHT = canvas.getBounds().height;
				gc.fillRectangle(0, 0, WIDTH, HEIGHT);
				
				int width = frame.getBounds().width;
				int height=  frame.getBounds().height;
				
				double scale = 1;
				
				// System.out.println(WIDTH + " x " + HEIGHT);
				if (width*HEIGHT >= WIDTH*height) {
					// fit width
					dx = 0; dw = WIDTH;
					int _height = (height*WIDTH) / width;
					dy = (HEIGHT - _height)/2; dh = _height;
					
					scale = _height * 1.0 / height;
					
				} else {
					// fit height
					dy = 0; dh = HEIGHT;
					int _width = (width*HEIGHT) / height;
					dx = (WIDTH - _width)/2; dw = _width;
					
					scale = _width * 1.0 / width;
				}
				
				gc.drawImage(frame, 0, 0, width, height, dx, dy, dw, dh);
				
				for (Sign sign : signs) {
					if (sign.img==null) continue;
					int px = (int)Math.round(dx + sign.p.getX() * dw);
					int py = (int)Math.round(dy + sign.p.getY() * dh);
					
					int sw = sign.img.getBounds().width;
					int sh = sign.img.getBounds().height;
					
					int _sw = (int)Math.round(scale*sw);
					int _sh = (int)Math.round(scale*sh);
					
					// gc.drawImage(sign.img, sign.p.x , sign.p.y );
					// gc.drawImage(sign.img, px - sign.img.getBounds().width/2, py - sign.img.getBounds().height/2);

					// gc.drawImage(sign.img, px - sw/2, py - sh/2);
					
					gc.drawImage(sign.img, 0, 0, sw, sh, px - _sw/2, py - _sh/2, _sw, _sh);
					
				}
				
			}
		});
		
		DropTarget dt = new DropTarget(canvas, DND.DROP_COPY);
		dt.setTransfer(new Transfer[] {
			TextTransfer.getInstance()
		});
		
		dt.addDropListener(new DropTargetListener() {
			
			@Override public void dragEnter(DropTargetEvent event) {
//				System.out.println("dragEnter");
//				if (event.detail == DND.DROP_DEFAULT) {
//					if ((event.operations & DND.DROP_COPY) != 0) {
//						event.detail = DND.DROP_COPY;
//					} else {
//						event.detail = DND.DROP_NONE;
//					}
//				}
				
				TextTransfer textType = TextTransfer.getInstance();
				for (TransferData dataType : event.dataTypes) {
					if (textType.isSupportedType(dataType)) {
						System.out.println("\tAccept drop");
						event.detail = DND.DROP_COPY;
						return;
					}
				}
				
			}

			@Override public void dragLeave(DropTargetEvent event) {
				System.out.println("dragLeave");
			}

			@Override public void dragOperationChanged(DropTargetEvent event) {
				System.out.println("dragOperationChanged");
			}

			boolean onlyFirst = false;
			@Override public void dragOver(DropTargetEvent event) {
				if (onlyFirst) return;
				System.out.println("DragOver");
				onlyFirst = true;
				
			}

			@Override public void drop(DropTargetEvent event) {
				System.out.println("Drop");
				
				if (event.data == null) { // no data to copy, indicate failure in event.detail
					event.detail = DND.DROP_NONE;
					return;
				}
				
				String identifier = (String)event.data;
				System.out.println(" => " + identifier);
				
				Image img = TreeTestTracerSigns.getImage(getDisplay(), identifier);
				Point p = canvas.toControl(new Point(event.x, event.y));
				
				Sign sign = new Sign(img, convert(p));
				signs.add(sign);
				
				canvas.redraw();
			}

			@Override public void dropAccept(DropTargetEvent event) {
				System.out.println("dropAccept");
				// event.detail = DND.DROP_NONE; // last chance to cancel drop
			}
			
		});
		
	}
	
}
