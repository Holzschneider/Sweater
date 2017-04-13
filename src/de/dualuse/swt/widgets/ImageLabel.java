package de.dualuse.swt.widgets;

import java.io.IOException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.events.LayoutDelegate;
import de.dualuse.swt.layout.Layouter;

public class ImageLabel extends Composite implements Layouter {
	
	static final int hgap = 16;
	
	Image image;
	
	Label imageLabel;
	Label textLabel;
	int alignment = SWT.TOP;
	
//==[ Constructor ]=================================================================================
	
	public ImageLabel(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new LayoutDelegate(this));
		
		// Children
		imageLabel = new Label(this, SWT.NONE);
		textLabel = new Label(this, SWT.WRAP);
		
		// Resources
		// addListener(SWT.Dispose, (e) -> { if (image!=null) image.dispose(); }); // externally managed
		
//		addControlListener(new ControlListener() {
//			
//			@Override
//			public void controlResized(ControlEvent e) {
//				System.out.println("ImageLabel.resized(" + getBounds().width + "," + getBounds().height +")");
//			}
//			
//			@Override
//			public void controlMoved(ControlEvent e) {
//			}
//		});
		
	}

//==[ Layout ]======================================================================================

	@Override public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
//		System.out.println("imageLabel.computeSize(" + wHint + "," + hHint + ")");
		Point imgPrefSize = imageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//		System.out.println("\tImage Width: " + imgPrefSize.x);
		
		if (wHint != SWT.DEFAULT)
			wHint = Math.max(0, wHint - imgPrefSize.x - hgap);
		
//		System.out.println("\tReduced wHint: " + wHint);
		
		Point textPrefSize = textLabel.computeSize(wHint, hHint);

//		System.out.println("\tImage Height: " + imgPrefSize.y);
//		System.out.println("\tText Height: " + textPrefSize.y);
		
		int height = Math.max(imgPrefSize.y, textPrefSize.y);
		int width = imgPrefSize.x + hgap + textPrefSize.x;
		
//		System.out.println("\t-> Result: " + width + "," + height);
		
		return new Point(width, height);
	}

	@Override public void layout(Composite composite, boolean flushCache) {
		
		Rectangle bounds = composite.getBounds();
		
		Point imgPrefSize = imageLabel.computeSize(SWT.DEFAULT, bounds.height);
		Point textPrefSize = textLabel.computeSize(bounds.width - imgPrefSize.x, SWT.DEFAULT);
		
		int y = 0;
		if (alignment == SWT.CENTER)
			y = (bounds.height-textPrefSize.y)/2;
		else if (alignment == SWT.BOTTOM)
			y = bounds.height - textPrefSize.y;
		
		imageLabel.setBounds(0, 0, imgPrefSize.x, imgPrefSize.y);
		textLabel.setBounds(imgPrefSize.x + hgap, y, textPrefSize.x, textPrefSize.y);
		
	}
	
	@Override public boolean flushCache(Control control) {
		return false;
	}
	
//==[ Getter & Setter ]=============================================================================
	
	public Label getImageLabel() {
		return imageLabel;
	}
	
	public Label getTextLabel() {
		return textLabel;
	}
	
	public void setImage(Image newImage) {
		checkWidget();
		imageLabel.setImage(image = newImage);
	}
	
	public void setText(String text) {
		checkWidget();
		textLabel.setText(text);
	}
	
	public void setVerticalAlignment(int alignment) {
		checkWidget();
		if (alignment!=SWT.TOP && alignment!=SWT.CENTER && alignment!=SWT.BOTTOM)
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		this.alignment = alignment;
		requestLayout();
	}
	
//==[ Test-Main ]===================================================================================
	
	public static void main(String[] args) throws IOException {

		Shell shell = new Shell();
		FillLayout layout = new FillLayout();
		layout.marginWidth = 16;
		layout.marginHeight = 16;
		shell.setLayout(layout);
		
		URL imgURL = ImageLabel.class.getResource("logo.png");
		String wallOfText = "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae, ultricies eget, tempor sit amet, ante. Donec eu libero sit amet quam egestas semper. Aenean ultricies mi vitae est. Mauris placerat eleifend leo. Quisque sit amet est et sapien ullamcorper pharetra. Vestibulum erat wisi, condimentum sed, commodo vitae, ornare sit amet, wisi. Aenean fermentum, elit eget tincidunt condimentum, eros ipsum rutrum orci, sagittis tempus lacus enim ac dui. Donec non enim in turpis pulvinar facilisis. Ut felis. Praesent dapibus, neque id cursus faucibus, tortor neque egestas augue, eu vulputate magna eros eu erat. Aliquam erat volutpat. Nam dui mi, tincidunt quis, accumsan porttitor, facilisis luctus, metus";
		
		ImageLabel imgLabel = new ImageLabel(shell, SWT.NONE);
		
		URL url = ImageLabel.class.getResource("logo.png");
		Image image = null;
		
		try {
			
			image = new Image(Display.getCurrent(), url.openConnection().getInputStream());
			imgLabel.setImage(image);
			
		} catch (IOException io) {
			io.printStackTrace();
		}

		imgLabel.setText(wallOfText);
		imgLabel.setVerticalAlignment(SWT.CENTER);
		
//		imgLabel.textPanel.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		imgLabel.textLabel.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));
		
//			Image image = new Image(Display.getCurrent(), new URL("http://i.imgur.com/wPIsO6u.png").openConnection().getInputStream());
//			Label label = new Label(shell, SWT.WRAP | SWT.CENTER);
//			label.setText(wallOfText);
//			label.setImage(image);
		
		shell.setSize(640, 480);
		shell.open();
		
		Display dsp = Display.getCurrent();
		while(!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		if (image != null)
			image.dispose();
	}

}
