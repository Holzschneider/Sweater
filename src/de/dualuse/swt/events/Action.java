package de.dualuse.swt.events;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.graphics.Image;

public class Action {

	// Title
	// Description (short, long)
	// Icon
	
	// Hotkey
	
	// ChangeListener
	
	// also implement ActionMenuItem(Action) extends MenuItem?
	//		getAction()

	private boolean enabled = true;
	private String text;
	private String description;
	private int accelerator;
	private Image image; // XXX Resource Manager
	
//==[ Listener Interfaces ]=========================================================================
	
	public interface ChangeListener {
		void actionChanged(Action action);
	}
	
	public interface ActionListener {
		void actionPerformed(Action action);
	}
	
//==[ Constructor ]=================================================================================
	
	public Action(String text, int accelerator, ActionListener listener) {
		this(text, accelerator);
		addActionListener(listener);
	}

	public Action(String text, int accelerator) {
		setText(text);
		setAccelerator(accelerator);
	}
	
	public void performAction() {
		fireActionPerformed();
	}
	
	
//==[ Getter/Setter ]===============================================================================
	
	public void setText(String text) {
		if (this.text.equals(text)) return;
		this.text = text;
		fireChange();
	}
	
	public String getText() {
		return text;
	}
	
	public void setEnabled(boolean enabled) {
		if (this.enabled==enabled) return;
		this.enabled = enabled;
		fireChange();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setDescription(String description) {
		if (this.description.equals(description)) return;
		this.description = description;
		fireChange();
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setAccelerator(int accelerator) {
		if (this.accelerator==accelerator) return;
		this.accelerator = accelerator;
		fireChange();
	}
	
	public int getAccelerator() {
		return accelerator;
	}
	
	public void setImage(Image image) {
		if (this.image == image) return;
		this.image = image;
		fireChange();
	}
	
	public Image getImage() {
		return image;
	}
	
//==[ Listener Management ]=========================================================================
	
	CopyOnWriteArrayList<ChangeListener> changeListeners = new CopyOnWriteArrayList<>();
	CopyOnWriteArrayList<ActionListener> actionListeners = new CopyOnWriteArrayList<>();
	
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		changeListeners.remove(listener);
	}
	
	protected void fireChange() {
		for (ChangeListener listener : changeListeners)
			listener.actionChanged(this);
	}
	
	/////
	
	public void addActionListener(ActionListener listener) {
		actionListeners.add(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		actionListeners.remove(listener);
	}

	protected void fireActionPerformed() {
		for (ActionListener listener : actionListeners)
			listener.actionPerformed(this);
	}
}
