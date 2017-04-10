package de.dualuse.swt.widgets;

import java.io.IOException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.events.LayoutDelegate;
import de.dualuse.swt.layout.BorderLayout;
import de.dualuse.swt.layout.CenterLayout;

public class ImageLabel extends Composite {
		
	Image image;
	Composite textPanel;
	Label imageLabel;
	Label textLabel;
	int alignment = SWT.TOP;
	
//==[ Constructor ]=================================================================================
	
	public ImageLabel(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new BorderLayout());
		
		// Children
		imageLabel = new Label(this, SWT.NONE);
		imageLabel.setLayoutData(BorderLayout.WEST);
		
		textPanel = new Composite(this, SWT.NONE);
		textPanel.setLayout(new CenterLayout(SWT.FILL, SWT.CENTER));
		textPanel.setLayoutData(BorderLayout.CENTER);
		
		textLabel = new Label(textPanel, SWT.WRAP);
		
		// Resources
		addListener(SWT.Dispose, (e) -> { if (image!=null) image.dispose(); });
	}

//==[ Setter ]======================================================================================
	
	public void setImage(URL url) {
		checkWidget();
		try {
			Image newImage = new Image(Display.getCurrent(), url.openConnection().getInputStream());
			
			if (image != null)
				image.dispose();
			
			imageLabel.setImage(image = newImage);
		} catch (IOException io) {
			io.printStackTrace();
		}
		
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
		textPanel.requestLayout();
	}
	
//==[ Layout ]======================================================================================
	
	/*
	private void layoutChildren() {
		System.out.println("layoutChildren()");
		
		Rectangle bounds = getBounds();
		
		if (image!=null) {
			imageLabel.setLocation(0, 0);
			imageLabel.setSize(imageLabel.computeSize(image.getImageData().width, bounds.height));
		} else {
			imageLabel.setLocation(0, 0);
			imageLabel.setSize(0, 0);
		}
		Rectangle imageBounds = imageLabel.getBounds();
		
		Point hints = new Point(bounds.x - imageLabel.getSize().x, bounds.y);
		System.out.println("Hints: " + hints);
		
		Point textSize = textLabel.computeSize(bounds.x - imageLabel.getSize().x, bounds.y);
		System.out.println("TextSize: " + textSize);
		
		textLabel.setSize(textSize.x, textSize.y);
		
		if (alignment == SWT.TOP) {
			System.out.println("Top Alignment");
			textLabel.setLocation(imageBounds.width, 0);
		} else if (alignment == SWT.CENTER) {
			System.out.println("Center Alignment");
			textLabel.setLocation(imageBounds.width, (bounds.y - textSize.y)/2);
		} else if (alignment == SWT.BOTTOM) {
			System.out.println("Bottom Alignment");
			textLabel.setLocation(imageBounds.width, bounds.y - textSize.y);
		}		
		// textLabel.setSize(bounds.width - imageBounds.width, bounds.height - textLabel.getLocation().y);
	}
	
	@Override public Point computeSize(int wHint, int hHint, boolean changed) {
		System.out.println("computeSize");
		Point imageSize = imageLabel.computeSize(SWT.DEFAULT, hHint, changed);
		
		if (wHint != SWT.DEFAULT)
			wHint = Math.max(0, wHint - imageSize.x);
		
		System.out.println("wHint: " + wHint);
		
		Point labelSize = textLabel.computeSize(wHint, hHint, changed);
		Point result = new Point(imageSize.x + labelSize.x, Math.max(imageSize.y, labelSize.y));
		
		return result;
	}
	*/
	
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
		imgLabel.setImage(imgURL);
		imgLabel.setText(wallOfText);
		
		imgLabel.textPanel.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
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
	}
}
