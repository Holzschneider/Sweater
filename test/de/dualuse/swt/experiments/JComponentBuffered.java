package de.dualuse.swt.experiments;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JComponent;

public class JComponentBuffered extends JComponent {
	private static final long serialVersionUID = 1L;

	JComponent child;

	Image buffer;
	Graphics bg;
	
	public JComponentBuffered(JComponent child) {
		this.child = child;
		add(child);
	}
	
	@Override public void doLayout() {
		child.setBounds(getBounds());
	}
	
	@Override public void paint(Graphics g) {
		
		if (buffer==null || buffer.getWidth(null)!=getWidth() || buffer.getHeight(null)!=getHeight()) {
			
			if (bg != null) {
				bg.dispose();
				bg = null;
			}
			
			if (buffer!=null) {
				buffer.flush();
				buffer=null;
				// System.gc();
			}
			
			buffer = createImage(getWidth(), getHeight());
			bg = buffer.getGraphics();
			
		}
		
		Graphics2D bg2 = (Graphics2D) bg;
		Graphics2D g2 = (Graphics2D) g;
		
		bg2.setTransform(g2.getTransform());
		bg2.setClip(g2.getClip());
//		
		super.paint(bg);
		
		g.drawImage(buffer, 0, 0, null);
	}
}
