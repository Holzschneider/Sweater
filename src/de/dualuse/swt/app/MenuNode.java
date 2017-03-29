package de.dualuse.swt.app;

import static org.eclipse.swt.SWT.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import de.dualuse.swt.app.AutoMenuBar.AutoMenu;
import de.dualuse.swt.app.AutoMenuBar.AutoMenuItem;
import de.dualuse.swt.app.AutoMenuBar.MenuScope;

class MenuNode implements Comparable<MenuNode> {
	final String label;
	MenuNode parent;
	LinkedList<MenuNode> children = new LinkedList<MenuNode>();
	
	
	public MenuNode() { this.label = null; }		
	public MenuNode(String label) { this.label = label; }
	
	List<MenuItemDelegate> delegates = new LinkedList<MenuItemDelegate>();
	List<Outlet> outlets = new LinkedList<Outlet>();
	
	Integer index = null;
	int rank = 0;
	boolean splitAfter = false;
	boolean splitBefore = false;
	Integer accelerator = null;
	boolean check = false;
	boolean checked = false;
	boolean grouped = false;
	boolean enabled = true;
	IconReference icon = null;
	Integer systemIcon = null;

	public void setAttributes(Class<?> source, AnnotatedElement e, AutoMenu ami) {
		if (ami.enabled().length>0) this.enabled = ami.enabled()[0];
		if (ami.rank().length>0) this.rank = ami.rank()[0];
		if (ami.index().length>0) this.index = ami.index()[0];
		if (ami.splitAfter().length>0) this.splitAfter = ami.splitAfter()[0];
		if (ami.splitBefore().length>0) this.splitBefore = ami.splitBefore()[0];
		if (ami.icon().length>0) this.icon = new IconReference(source, ami.icon());
		if (ami.systemIcon().length>0) this.systemIcon = ami.systemIcon()[0];
	}

	public void setAttributes(Class<?> source, AnnotatedElement e, AutoMenuItem ami) {
		if (ami.checkbox().length>0) this.check = ami.checkbox()[0];
		if (ami.grouped().length>0) this.grouped = ami.grouped()[0];
		if (ami.accelerator().length>0) this.accelerator = ami.accelerator()[0];
		if (ami.enabled().length>0) this.enabled = ami.enabled()[0];
		if (ami.splitAfter().length>0) this.splitAfter = ami.splitAfter()[0];
		if (ami.splitBefore().length>0) this.splitBefore = ami.splitBefore()[0];
		if (ami.index().length>0) this.index = ami.index()[0];
		if (ami.rank().length>0) this.rank = ami.rank()[0];
		if (ami.icon().length>0) this.icon = new IconReference(source, ami.icon());
		if (ami.systemIcon().length>0) this.systemIcon = ami.systemIcon()[0];
	}

	
	@Override
	public int compareTo(MenuNode o) {
		return Integer.compare(this.rank, o.rank);
	}
	
	
	MenuNode lookup(String label) {
		for (MenuNode mn: children)
			if (mn.label.equals(label))
				return mn;
		
		MenuNode created = new MenuNode(label);
		created.parent = this;
		children.add(created);
		
		return created;
	}
	
	MenuNode find(String[] path) {
		MenuNode node = this;
		for (String step: path)
			node = node.lookup(step);
		return node;
	}
	
	private String toTargetString() {
		String target = "";
		
		String DELIMITER = ", ";
		
		if (outlets.size()>0)
			target += DELIMITER+"outlets: "+outlets;
		
		if (delegates.size()>0)
			target += DELIMITER+"delegates: "+delegates;
		
		target = target.replaceAll("^"+DELIMITER, "");
		
		
		return target.length()==0?"":"{ "+ target+ " }";
	}
	
	public String toPath() {
		return (parent==null?"":parent.toPath()+"->")+label;
	}
	
	private String toTreeString(String prefix) {
		String treeString = prefix+"|_ "+ toString();//+","+selectionListeners+","+itemOutlet+","+")";
		if (label ==null) treeString = "    *";
			
		boolean last = parent==null||parent.children.getLast()==this; 
		for (MenuNode child: children)
			treeString+="\n"+child.toTreeString(prefix+(last?"    ":"|   "));
			
		return treeString.substring(4);
	};
	
	public String toTreeString() {
		return toTreeString("");
	}
	
	@Override
	public String toString() {
		return label+toTargetString();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static class IconReference {
		Class<?> loader;
		String[] path;

		public IconReference(Class<?> loader, String[] path) {
			this.loader = loader;
			this.path = path;
		}
	}
	
	
	static class Outlet {
		final Object target;
		final Field field;
		
		Object value = null;
		
		public void set(Object value) {
			try {
				field.set(target, this.value = value);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		
		public Outlet(Object target, Field field) {
			this.target = target;
			this.field = field;
			this.field.setAccessible(true);
		}
		
		@Override
		public String toString() {
			return "Outlet("+field+")";
		}
	}
	
	static abstract class MenuItemDelegate {
		final Object target;

		abstract void attachTo(MenuItem me); 
		
		public MenuItemDelegate(Object target) {
			this.target = target;
		}
	}
	
	static class DelegateMethod extends MenuItemDelegate implements SelectionListener, Listener {
		final Method handler;
		
		public DelegateMethod(Object target, Method handler) {
			super(target);
			this.handler = handler;
			this.handler.setAccessible(true);
		}

		@Override
		void attachTo(MenuItem me) {
			if (handler.getParameterCount()==1 && handler.getParameterTypes()[0].isAssignableFrom(Event.class))
				me.addListener(Selection,this);
			else 
				me.addSelectionListener(this);
		}
		
		////////////////
		
		@Override
		public void handleEvent(Event event) {
			try {
				handler.invoke(target, event);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		
		/////
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				if (handler.getParameterCount()==0)
					handler.invoke(target);
				else
				if (handler.getParameterTypes()[0].isAssignableFrom(SelectionEvent.class))
					handler.invoke(target, e);
				else
					handler.invoke(target, e.widget);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) { }
		
		@Override
		public String toString() {
			return "DelegateMethod("+handler+")";
		}
	}
	
	static class DelegateSelectionListenerField extends MenuItemDelegate implements SelectionListener {
		final Field selectionListenerField;
		
		public DelegateSelectionListenerField(Object target, Field selectionListenerField) {
			super(target);
			this.selectionListenerField = selectionListenerField;
			this.selectionListenerField.setAccessible(true);
		}

		@Override
		void attachTo(MenuItem me) {
			me.addSelectionListener(this);
		}
		
		////////////////
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				((SelectionListener)selectionListenerField.get(target)).widgetSelected(e);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			try {
				((SelectionListener)selectionListenerField.get(target)).widgetDefaultSelected(e);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			}			
		}
		
		@Override
		public String toString() {
			return "DelegateSelectionListenerField("+selectionListenerField+")";
		}
	}
	
	static class DelegateListenerField extends MenuItemDelegate implements Listener {
		final Field listenerField;
		
		public DelegateListenerField(Object target, Field listenerField) {
			super(target);
			this.listenerField = listenerField;
			this.listenerField.setAccessible(true);
		}

		@Override
		void attachTo(MenuItem me) {
			me.addListener(Selection, this);
		}

		@Override
		public void handleEvent(Event event) {
			try {
				((Listener)listenerField.get(target)).handleEvent(event);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			}			
		}
		
		@Override
		public String toString() {
			return "DelegateListenerField("+listenerField+")";
		}
	}	
}