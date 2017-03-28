package de.dualuse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.dualuse.swt.events.Action;

public class ActionMenuItem extends MenuItem {
	
	private Action action;
	
	@Override protected void checkSubclass() {}
	
	public ActionMenuItem(Menu parent, int style, Action action) {
		super(parent, style);
		this.action = action;
		
		action.addChangeListener(changeListener);
		
		this.addListener(SWT.Selection, (e) -> {
			action.performAction();
		});
	}
	
	Action.ChangeListener changeListener = new Action.ChangeListener() {
		@Override public void actionChanged(Action a) {
			setEnabled(a.isEnabled());
			setText(a.getText());
			setImage(a.getImage());
			setAccelerator(a.getAccelerator());
		}
	};
	
	@Override public void dispose() {
		action.removeChangeListener(changeListener);
		super.dispose();
	}
}
