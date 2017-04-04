package de.dualuse.swt.experiments;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.util.SWTTimer;
import de.dualuse.swt.util.SWTUtil;

public class ParentChild {
	
	public static void main(String[] args) {
		
		Shell parent = new Shell();
		parent.setSize(600, 480);
		parent.setText("Parent");
		
		parent.setLayout(new FillLayout());
		String wallOfText = "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae, ultricies eget, tempor sit amet, ante. Donec eu libero sit amet quam egestas semper. Aenean ultricies mi vitae est. Mauris placerat eleifend leo. Quisque sit amet est et sapien ullamcorper pharetra. Vestibulum erat wisi, condimentum sed, commodo vitae, ornare sit amet, wisi. Aenean fermentum, elit eget tincidunt condimentum, eros ipsum rutrum orci, sagittis tempus lacus enim ac dui. Donec non enim in turpis pulvinar facilisis. Ut felis. Praesent dapibus, neque id cursus faucibus, tortor neque egestas augue, eu vulputate magna eros eu erat. Aliquam erat volutpat. Nam dui mi, tincidunt quis, accumsan porttitor, facilisis luctus, metus";
		Label label = new Label(parent, SWT.WRAP);
		label.setText(wallOfText);
		
		SWTUtil.center(parent);
		
		parent.open();
		
		// Shell sub = new Shell(parent, SWT.SHELL_TRIM | SWT.PRIMARY_MODAL);
		final Shell sub = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		sub.setSize(480, 320);
		SWTUtil.center(parent, sub);
		sub.setAlpha(0);
		sub.open();
		
		SWTTimer timer = new SWTTimer(10, new SWTTimer.TimerJob() {
			int i = 0;
			@Override public void run(SWTTimer source) {
				
				// if (i%10 == 0) System.out.println(i);
				System.out.println(sub.getAlpha());
				
				sub.setAlpha(i); // not supported on Linux
				
				if (i==255) {
					System.out.println("Faded in (" + sub.getAlpha() + ")");
					source.stop();
					return;
				}
				
				i += 17;
			}
		});
		
		Listener listener = new Listener() {
			@Override public void handleEvent(Event e) {
				timer.start();
				sub.removeListener(SWT.Activate, this);
				
				System.out.println(parent.getBounds());
				
				SWTUtil.center(parent, sub);
			}
		};
		
		// Not called on osx? At least called more often on LXDE, even if window is just moved around
		// On LXDE called after subshell is moved
		sub.addListener(SWT.Activate, listener);
		
		// sub.addListener(SWT.Activate, (e) -> System.out.println("SubShell activated"));
		
		parent.addControlListener(new ControlListener() {

			// LXDE: only called on mouseReleased (not while dragging the window around)
			// OS X: called when no movement is detected for a certain amount of time
			// awesome: if window is floating, gets called normally
			@Override public void controlMoved(ControlEvent e) {
				System.out.println("Parent moved");
				SWTUtil.center(parent, sub);
			}

			@Override public void controlResized(ControlEvent e) {
				System.out.println("Parent resized");
			}
		
		});
		
		// Doesn't get mouse events due to SWT.PRIMARY_MODAL dialog
		parent.addMouseMoveListener(new MouseMoveListener() {
			@Override public void mouseMove(MouseEvent e) {
				System.out.println("mouse moved");
			}
		});
		
		Display dsp = parent.getDisplay();
		while(!(parent.isDisposed() || sub.isDisposed())) {
			if (!dsp.readAndDispatch())
				dsp.sleep();
		}
		
		dsp.dispose();
		
		System.out.println("Disposed");
	}
	
}
