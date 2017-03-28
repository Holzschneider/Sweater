package de.dualuse.swt.experiments;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BrowserTest {
	
	public static void main(String[] args) {
		
		Display dsp = new Display();
		
		Shell sh = new Shell(dsp);
		sh.setAlpha(200);
		sh.setLayout(new FillLayout());
		
		Browser br = new Browser(sh, 0);
		br.setJavascriptEnabled(true);
		sh.setText( br.getBrowserType() );
		
		br.setUrl("http://www.spiegel.de");
//		br.setUrl("http://dualuse.de");
//		br.setUrl("http://k9.github.io/globe-viewer/");
		
		
		
		
		sh.setBounds(100, 100, 800, 800);
		sh.setVisible(true);
		
		while (!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
	}

}
