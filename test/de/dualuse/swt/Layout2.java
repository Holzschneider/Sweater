package de.dualuse.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.util.SWTTimer;
import de.dualuse.swt.util.SWTUtil;

// RowLayout Test

public class Layout2 {
	
	private static void addImage(Shell shell, String imageName) {
		Label label = new Label(shell, SWT.NONE);
		label.setImage(new Image(shell.getDisplay(), Layout2.class.getResourceAsStream(imageName)));
	}
	
	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell shell = new Shell(dsp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		shell = new Shell();
		
		SWTUtil.center(shell, 800, 600);
		SWTUtil.exitOnClose(shell);
		
		RowLayout layout = new RowLayout();
		layout.type = SWT.HORIZONTAL;
		layout.marginWidth = 16;
		layout.marginHeight = 16;
		layout.spacing = 8;
		
		shell.setLayout(layout);

		String[] images = {
			"arbeitsstelle.png",
			// "generic-cat.jpeg",
			"frame.jpg",
			"hoechstgeschwindigkeit_sm.png",
			"stop_sm.png",
			"transparency-demonstration.png",
			"umleitung_sm.png",
			"vorfahrt_sm.png"
		};
		
		for (String image : images)
			addImage(shell, image);
		
		shell.open();
		
		SWTUtil.eventLoop();
	}
}
