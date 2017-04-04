package de.dualuse.swt.widgets;

import java.io.IOException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.events.LayoutDelegate;

public class ImageLabel extends Composite {
		
	Image image;
	Label imageLabel;
	Label textLabel;
	
//==[ Constructor ]=================================================================================
	
	public ImageLabel(Composite parent, int style) {
		super(parent, style);

		// Children
		imageLabel = new Label(this, SWT.NONE);
		textLabel = new Label(this, SWT.WRAP);

		// Layout
		LayoutDelegate layout = new LayoutDelegate();
		layout.layout((composite, flushCache) -> layoutChildren());
		setLayout(layout);
		
		// Resources
		addListener(SWT.Dispose, (e) -> { if (image!=null) image.dispose(); });
	}

//==[ Setter ]======================================================================================
	
	public void setImage(URL url) {
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
		textLabel.setText(text);
	}
	
//==[ Layout ]======================================================================================
	
	private void layoutChildren() {
		Rectangle bounds = getBounds();
		
		if (image!=null) {
			imageLabel.setLocation(0, 0);
			imageLabel.setSize(imageLabel.computeSize(image.getImageData().width, bounds.height));
		} else {
			imageLabel.setLocation(0, 0);
			imageLabel.setSize(0, 0);
		}
		Rectangle imageBounds = imageLabel.getBounds();
		
		textLabel.setLocation(imageBounds.width, 0);
		textLabel.setSize(bounds.width - imageBounds.width, bounds.height);
	}
	
//==[ Test-Main ]===================================================================================
	
	public static void main_(String[] args) throws IOException {

		Shell shell = new Shell();
		FillLayout layout = new FillLayout();
		layout.marginWidth = 16;
		layout.marginHeight = 16;
		shell.setLayout(layout);
		
		URL imgURL = new URL("http://i.imgur.com/wPIsO6u.png");
		String wallOfText = "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae, ultricies eget, tempor sit amet, ante. Donec eu libero sit amet quam egestas semper. Aenean ultricies mi vitae est. Mauris placerat eleifend leo. Quisque sit amet est et sapien ullamcorper pharetra. Vestibulum erat wisi, condimentum sed, commodo vitae, ornare sit amet, wisi. Aenean fermentum, elit eget tincidunt condimentum, eros ipsum rutrum orci, sagittis tempus lacus enim ac dui. Donec non enim in turpis pulvinar facilisis. Ut felis. Praesent dapibus, neque id cursus faucibus, tortor neque egestas augue, eu vulputate magna eros eu erat. Aliquam erat volutpat. Nam dui mi, tincidunt quis, accumsan porttitor, facilisis luctus, metus";
		
		ImageLabel imgLabel = new ImageLabel(shell, SWT.NONE);
		imgLabel.setImage(imgURL);
		imgLabel.setText(wallOfText);
		
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