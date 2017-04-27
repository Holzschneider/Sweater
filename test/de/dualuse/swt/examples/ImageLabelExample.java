package de.dualuse.swt.examples;

import java.io.IOException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.widgets.ImageLabel;

public class ImageLabelExample {
	// ==[ Test-Main
	// ]===================================================================================

	public static void main(String[] args) throws IOException {

		Shell shell = new Shell();
		FillLayout layout = new FillLayout();
		layout.marginWidth = 16;
		layout.marginHeight = 16;
		shell.setLayout(layout);

		String wallOfText = 
				"Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. "
				+ "Vestibulum tortor quam, feugiat vitae, ultricies eget, tempor sit amet, ante. Donec eu libero "
				+ "sit amet quam egestas semper. Aenean ultricies mi vitae est. Mauris placerat eleifend leo. "
				+ "Quisque sit amet est et sapien ullamcorper pharetra. Vestibulum erat wisi, condimentum sed, "
				+ "commodo vitae, ornare sit amet, wisi. Aenean fermentum, elit eget tincidunt condimentum, eros "
				+ "ipsum rutrum orci, sagittis tempus lacus enim ac dui. Donec non enim in turpis pulvinar "
				+ "facilisis. Ut felis. Praesent dapibus, neque id cursus faucibus, tortor neque egestas augue, "
				+ "eu vulputate magna eros eu erat. Aliquam erat volutpat. Nam dui mi, tincidunt quis, accumsan "
				+ "porttitor, facilisis luctus, metus";

		ImageLabel imgLabel = new ImageLabel(shell, SWT.NONE);

		URL url = ImageLabelExample.class.getResource("logo.png");
		Image image = null;

		try {

			image = new Image(Display.getCurrent(), url.openConnection().getInputStream());
			imgLabel.setImage(image);

		} catch (IOException io) {
			io.printStackTrace();
		}

		imgLabel.setText(wallOfText);
		imgLabel.setVerticalAlignment(SWT.CENTER);

		// imgLabel.textPanel.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		imgLabel.text.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW));

		// Image image = new Image(Display.getCurrent(), new
		// URL("http://i.imgur.com/wPIsO6u.png").openConnection().getInputStream());
		// Label label = new Label(shell, SWT.WRAP | SWT.CENTER);
		// label.setText(wallOfText);
		// label.setImage(image);

		shell.setSize(640, 480);
		shell.open();

		Display dsp = Display.getCurrent();
		while (!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();

		if (image != null)
			image.dispose();
	}

}
