package de.dualuse.swt.app;

import static org.eclipse.swt.SWT.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.*;

import de.dualuse.swt.app.AutoMenuBar.AutoMenu;
import de.dualuse.swt.app.AutoMenuBar.AutoMenuItem;
import de.dualuse.swt.app.AutoMenuBar.MenuScope;



public class AutoMenuBuilder {
	static public final String TOOLKIT_PACKAGE_BOUNDARY = "org.eclipse.swt.widgets";
	static public Map<Class<?>, List<Class<?>>> hierarchies = new HashMap<Class<?>, List<Class<?>>>();
	
	MenuNode root = new MenuNode();

	private List<Class<?>> hierarchy(Class<?> clazz) {
		synchronized (hierarchies) {
			if (hierarchies.containsKey(clazz))
				return hierarchies.get(clazz);
		}
		
		LinkedList<Class<?>> hierarchy = new LinkedList<Class<?>>();
		for(Class<?> c=clazz;c!=null && !c.getPackage().getName().equals(TOOLKIT_PACKAGE_BOUNDARY);c=c.getSuperclass())
			hierarchy.add(c);
		
		hierarchies.put(clazz,hierarchy);

		return hierarchy;
	}
	
	
	public AutoMenuBuilder add(Widget w, MenuScope... scopes) { return supply(w, scopes); }
	public AutoMenuBuilder add(Display d, MenuScope... scopes) { return supply(d, scopes); }
	
	private boolean intersect(MenuScope[] A, MenuScope[] B) {
		for (MenuScope a: A)
			for (MenuScope b: B)
				if (a.equals(b))
					return true;
		
		return false;
	}
	
	private AutoMenuBuilder supply(Object w, MenuScope... scope) {
		Collection<Class<?>> hierarchy = hierarchy(w.getClass());
		
		for (Class<?> clazz: hierarchy)
			for (Method mth: clazz.getDeclaredMethods()) 
				if (mth.isAnnotationPresent(AutoMenuItem.class)) {
					AutoMenuItem ami = mth.getAnnotation(AutoMenuItem.class);
					
					if (intersect(scope, ami.scope()))
						root.find(ami.path()).delegates.add(new DelegateMethod(w,mth));
				}

		for (Class<?> clazz: hierarchy)
			for (Field fld: clazz.getDeclaredFields()) {
				if (fld.isAnnotationPresent(AutoMenuItem.class)) {
					AutoMenuItem ami = fld.getAnnotation(AutoMenuItem.class);
					if (intersect(scope, ami.scope())) { 
	
						MenuNode node = root.find(ami.path());
						
						Class<?> type = fld.getType();
						
						List<MenuItemDelegate> del = node.delegates;
						if (type.isAssignableFrom(MenuItem.class)) node.outlets.add(new Outlet(w, fld));
						else if (type.equals(Listener.class)) del.add(new DelegateListenerField(w, fld));
						else if (type.equals(SelectionListener.class)) del.add(new DelegateSelectionListenerField(w,fld));
						else throw new RuntimeException("Invalid AutoMenuItem target: "+fld);
						
					}
				}
				
				if (fld.isAnnotationPresent(AutoMenu.class)) {
					AutoMenu am = fld.getAnnotation(AutoMenu.class);
					
					if (intersect(scope, am.scope())) {
						MenuNode node = root.find(am.value());
						
						Class<?> type = fld.getType();
						if (type.isAssignableFrom(Menu.class)) node.outlets.add(new Outlet(w, fld));
						else throw new RuntimeException("Invalid AutoMenu target: "+fld);
					}
				}
			}
		
		return this;
	}
	
	
	public Menu build(Menu m) {
		return buildMenu(m, root);
	};
	
	
	private Menu buildMenu(Menu m, MenuNode n) {
		for (MenuNode mc: n.children) {
			if (!mc.children.isEmpty()) {
				MenuItem mi = new MenuItem(m, CASCADE);
				mi.setText(mc.label);
				
				Menu sub = new Menu(mi);
				
				mi.setMenu(sub);
				buildMenu(sub, mc);

				for (Outlet o: mc.outlets)
					o.set(sub);

			} else {
				MenuItem mi = new MenuItem(m, PUSH);
				
				mi.setText(mc.label);
				
				for (Outlet o: mc.outlets)
					o.set(mi);
				
				for (MenuItemDelegate mid: mc.delegates)
					mid.attachTo(mi);
			}
			
		}
		return m;
	}
	
	public String toTreeString() {
		return root.toTreeString();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static private class Outlet {
		final Object target;
		final Field field;

		public void set(Object value) {
			try {
				field.set(target, value);
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
	
	static abstract private class MenuItemDelegate {
		final Object target;

		abstract void attachTo(MenuItem me); 
		
		public MenuItemDelegate(Object target) {
			this.target = target;
		}
	}
	
	static private class DelegateMethod extends MenuItemDelegate implements SelectionListener, Listener {
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
	
	static private class DelegateSelectionListenerField extends MenuItemDelegate implements SelectionListener {
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
	
	static private class DelegateListenerField extends MenuItemDelegate implements Listener {
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
	
	
	static private class MenuNode {
		final String label;
		MenuNode parent;
		LinkedList<MenuNode> children = new LinkedList<MenuNode>();
		
		public MenuNode() { this.label = null; }		
		public MenuNode(String label) { this.label = label; }
		
		List<MenuItemDelegate> delegates = new LinkedList<MenuItemDelegate>();
		List<Outlet> outlets = new LinkedList<Outlet>();
		
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
	}	
	
}
