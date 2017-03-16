package de.dualuse.swt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.util.SWTUtil;

public class SwingTest {
	public static void main(String[] args) throws Exception {

		// XXX to reduce Flicker
		try {
		      System.setProperty("sun.awt.noerasebackground", "true");
		} catch (NoSuchMethodError error) {
			error.printStackTrace();
		}
		
		final BufferedImage bi = ImageIO.read(JMicroscope.class.getResourceAsStream("generic-cat.jpeg"));
		
		final Shell shell = new Shell();
//		shell.setLayout(null);
//		shell.setLayout(new FillLayout());

		final Composite composite = new Composite(shell, SWT.EMBEDDED | SWT.NO_BACKGROUND); // XXX SWT.EMBEDDED for embedding AWT/Swing // XXX SWT.NO_BACKGROUND to reduce flicker
		
		
//		composite.setBackground(new org.eclipse.swt.graphics.Color(Display.getCurrent(), new RGB(0, 0, 255)));
		
//		composite.setBounds(20, 20, 1000, 2000);
//		composite.setLayout(new RowLayout());
//		composite.setLayout(new FillLayout());
		
		/*
		shell.addControlListener(new ControlListener() {
			@Override public void controlMoved(ControlEvent e) {}
			@Override public void controlResized(ControlEvent e) {
				composite.setBounds(shell.getClientArea());
//				System.out.println(composite.getBounds());
//				shell.layout();
//				shell.redraw();
			}
		});
		*/
		
		java.awt.Frame frame = SWT_AWT.new_Frame(composite);
		frame.setBackground(null);
//		frame.setBackground(Color.RED);
		
		Panel jpanel = new Panel(new BorderLayout()) { // XXX Flicker with JPanel, must be heavyweight Panel? // XXX also a few seconds delay with JPanel
//		 JPanel jpanel = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;
			
			/*
			Image buffer;
			Graphics bg;
			
			@Override public void update(java.awt.Graphics g) { // XXX Flickr without overriding
				
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
				
				// System.out.println("p: " + System.identityHashCode(bg));
				// Do not erase the background
				// paint(g);
				paint(bg);
				
				g.drawImage(buffer, 0, 0, null);
			}
			*/
			
			@Override public void update(Graphics g) {
				paint(g);
			}
		};
//		jpanel.setBackground(Color.RED);
		jpanel.setBackground(Color.GREEN);
//		jpanel.setBackground(null);
		frame.add(jpanel);

		JMicroscope microscope = new JMicroscope() {
			private static final long serialVersionUID = 1L;
			
			@Override public void paintCanvas(Graphics g) {
//				System.out.println("WTF (" + getBounds() + ")");
				
//				Graphics2D g2 = (Graphics2D) g;
//				
//				Shape clip = g.getClip();
//				Area clipArea = new Area(clip);
//				Area imageArea = new Area(new Rectangle2D.Double(0, 0, bi.getWidth(), bi.getHeight()));
//				clipArea.subtract(imageArea);
//				g.setClip(clipArea);
				
				java.awt.Rectangle bounds = g.getClipBounds();
				g.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);
				
//				g.setColor(Color.BLUE);
//				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

//				g.setClip(clip);
				
				g.drawImage(bi,0,0,null);
				g.drawLine(0, 0, bi.getWidth(), bi.getHeight());
			}
		};
		microscope.setBackground(Color.ORANGE);
		
		jpanel.add(new JComponentBuffered(microscope));
		
		
//		JLabel jlabel = new JLabel();
//		jlabel.setText("Hello World");
//		jpanel.add(jlabel);
		
//		java.awt.Panel panel = new java.awt.Panel();
//		frame.add(panel);
//		
//		java.awt.Label label = new java.awt.Label();
//		label.setText("Hello World!");
//		panel.add(label);
		
		shell.setText("Double Buffered Swing Component in SWT");
		
		/*
		Button button = new Button(shell, SWT.NONE);
		button.setText("Click Me");

		shell.addListener(SWT.Activate, (e) -> {

			System.out.println("Laying out");
			
			Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Rectangle clientArea = shell.getClientArea();
			// composite.setBounds();
			composite.setBounds(clientArea.x, clientArea.y, clientArea.width, clientArea.height - size.y);
			button.setBounds(clientArea.width - size.x, clientArea.height - size.y, size.x, size.y);
			
		});
		*/
		
		
//		composite.addListener(SWT.Resize, (e) -> {
//			System.out.println(composite == e.widget);
//			System.out.println(composite.getClientArea());
//		});
		
		Browser browser = new Browser(shell, SWT.NONE);
		browser.setUrl("http://news.ycombinator.com");
		

		shell.setLayout(new Layout() {
			@Override protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				return new Point(wHint, hHint);
			}

			@Override protected void layout(Composite parent, boolean flushCache) {
				Rectangle area = parent.getClientArea();
				composite.setBounds(0, 0, area.width/2, area.height);
				browser.setBounds(area.width/2, 0, area.width/2, area.height);
			}
		});
		
		SWTUtil.exitOnClose(shell);
		shell.open();
		
		SWTUtil.eventLoop();
	}
}
