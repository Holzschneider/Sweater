package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ClipboardTest {
	public static void main(String[] args) {
		
		Display dsp = new Display();
		Shell sh = new Shell(dsp);
		
		sh.setLayout(new FillLayout());
		Label l = new Label(sh, NONE);

		l.setImage( new Image(dsp, ClipboardTest.class.getResourceAsStream("generic-cat.jpeg")));
		
		Clipboard cb = new Clipboard(dsp);
		
		l.addListener(MouseDown, (e) -> {
			Object text = cb.getContents(TextTransfer.getInstance());
			if (text!=null) {
				l.setImage(null);
				l.setText(text.toString());
			}
			
			Object o = cb.getContents(ImageTransfer.getInstance());
			if (o!=null) {
				ImageData id = (ImageData)o;
				l.setImage(new Image(dsp, id));
			}
		});
		
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);;
		
		while(!dsp.isDisposed())
			if(!dsp.readAndDispatch())
				dsp.sleep();
		
	}
}
