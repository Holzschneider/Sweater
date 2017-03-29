package de.dualuse.swt.app;


import static org.eclipse.swt.SWT.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

interface Test {
	static public final int bla = 10;
}

public interface AutoMenuBar {
	static public final String SEPARATOR = "_";
	
	static public final int CTRL = 1<<31;
	static public final int ALT = 1<<30;
	static public final int SHIFT = 1<<29;
	static public final int META = 1<<28;
	static public final int FUNCTION = 1<<27;
	
	///
	
	static public final char VK_BACK_SPACE     = '\b';
	static public final char DELETE         = 0x7F; /* ASCII DEL */
    static public final char CAPS_LOCK      = 0x14;
    static public final char ESCAPE         = 0x1B;
    static public final char SPACE          = 0x20;
    static public final char PAGE_UP        = 0x21;
    static public final char PAGE_DOWN      = 0x22;
    static public final char END            = 0x23;
    static public final char HOME           = 0x24;
    static public final char LEFT           = 0x25;
    static public final char UP             = 0x26;
    static public final char RIGHT          = 0x27;
    static public final char DOWN           = 0x28;

	
    
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	static public @interface AutoMenu {
		String[] value();
		int rank() default 0;
		int[] ranks() default 0;
		boolean enabled() default true;
		MenuScope scope() default MenuScope.INHERIT;
	}

	@Target({ElementType.FIELD,ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	static public @interface AutoMenuItem {
		String[] value();
		int[] ranks() default 0;
		int accelerator() default 0;
		boolean check() default false;
		String group() default "";
		boolean enabled() default true;
		String icon() default "";
		MenuScope scope() default MenuScope.INHERIT; 
	}
	
	static public enum MenuScope {
		INHERIT,
		WINDOW,
		SYSTEM,
		APPLICATION;
	}
	
	
	
	static class MenuNode {
		final String label;
		MenuNode parent;
		LinkedList<MenuNode> children = new LinkedList<MenuNode>();
		
		public MenuNode() { this.label = null; }		
		public MenuNode(String label) { this.label = label; }
		
		
		List<Method> handlerMethods = new LinkedList<Method>();
		List<Field> itemOutlets = new LinkedList<Field>();
		List<Field> menuOutlets = new LinkedList<Field>();
		List<Field> selectionEventListeners = new LinkedList<Field>();
		List<Field> selectionListeners = new LinkedList<Field>();
		
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
			
			if (handlerMethods.size()>0)
				target += DELIMITER+"handlerMethods: "+handlerMethods;
			
			if (itemOutlets.size()>0)
				target += DELIMITER+"itemOutlets: "+itemOutlets;

			if (menuOutlets.size()>0)
				target += DELIMITER+"menuOutlets: "+menuOutlets;
			
			if (selectionListeners.size()>0)
				target += DELIMITER+"selectionListeners: "+selectionListeners;
			
			if (selectionEventListeners.size()>0)
				target += DELIMITER+"selectionEventListeners: "+selectionEventListeners;
			
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

	
	static public final String TOOLKIT_PACKAGE = "org.eclipse.swt.widgets";
	
	/////maybe just needed once, da man von Shell und Display je eine MenuBar zum einhängen von Menus bekommt
	default public void build(Object thiz, Menu m, MenuScope... scope) {
		Class<?> baseClass = thiz.getClass(); //<- extract menustructure from here
		MenuNode root = new MenuNode();
		
		LinkedList<Class<?>> hierarchy = new LinkedList<Class<?>>();
		for(Class<?> c=baseClass;c!=null && !c.getPackage().getName().equals(TOOLKIT_PACKAGE);c=c.getSuperclass())
			hierarchy.add(c);
		
		for (Class<?> clazz: hierarchy)
			for (Method mth: clazz.getDeclaredMethods()) 
				if (mth.isAnnotationPresent(AutoMenuItem.class)) {
					AutoMenuItem ami = mth.getAnnotation(AutoMenuItem.class);
					root.find(ami.value()).handlerMethods.add(mth);
					mth.setAccessible(true);
				}
		
		for (Class<?> clazz: hierarchy)
			for (Field fld: clazz.getDeclaredFields()) {
				if (fld.isAnnotationPresent(AutoMenuItem.class)) {
					AutoMenuItem ami = fld.getAnnotation(AutoMenuItem.class);
					MenuNode node = root.find(ami.value());
					
					Class<?> type = fld.getType();
					if (type.isAssignableFrom(MenuItem.class))
						node.itemOutlets.add(fld);
					else
					if (type.equals(Listener.class))
						node.selectionEventListeners.add(fld);
					else
					if (type.equals(SelectionListener.class))
						node.selectionListeners.add(fld);
					else
						throw new RuntimeException("Invalid AutoMenuItem target: "+fld);
	
					fld.setAccessible(true);
				}
				
				if (fld.isAnnotationPresent(AutoMenu.class)) {
					AutoMenu am = fld.getAnnotation(AutoMenu.class);
					MenuNode node = root.find(am.value());
					
					Class<?> type = fld.getType();
					if (type.isAssignableFrom(Menu.class))
						node.menuOutlets.add(fld);
					else
						throw new RuntimeException("Invalid AutoMenuItem target: "+fld);
	
					fld.setAccessible(true);
				}
			}
		
		/////////////
		
		buildMenu(thiz, m, root);
		
	}
	
	default public void buildMenu(Object thiz, Menu m, MenuNode n) {
		for (MenuNode mc: n.children) {
			if (!mc.children.isEmpty()) {
				MenuItem mi = new MenuItem(m, CASCADE);
				mi.setText(mc.label);
				
				Menu sub = new Menu(mi);
				
				mi.setMenu(sub);
				buildMenu(thiz, sub, mc);
				
			} else {
				MenuItem mi = new MenuItem(m, PUSH);
				
				mi.setText(mc.label);
				
				for (Field outlet: mc.itemOutlets) try {
					outlet.set(thiz, mi);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				

				for (Method mth: mc.handlerMethods) 
					mi.addListener(Selection, new Listener() {
						public void handleEvent(Event event) {
							try {
								if (mth.getParameterCount()==0)
									mth.invoke(thiz);
								else
									mth.invoke(thiz, mi);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					});
					
				for (Field sl: mc.selectionListeners) 
					mi.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent e) {
							try {
								((SelectionListener)sl.get(thiz)).widgetSelected(e);
							} catch (Exception e1) {
								throw new RuntimeException(e1);
							}
						}
						
						public void widgetDefaultSelected(SelectionEvent e) {
							try {
								((SelectionListener)sl.get(thiz)).widgetDefaultSelected(e);
							} catch (Exception e1) {
								throw new RuntimeException(e1);
							}
						}
					});
				
				
				for (Field sl: mc.selectionEventListeners) 
					mi.addListener(Selection, new Listener() {
						@Override
						public void handleEvent(Event event) {
							try {
								((Listener)sl.get(thiz)).handleEvent(event);
							} catch (Exception e1) {
								throw new RuntimeException(e1);
							}
						}
					});
					
				
			}
			
		}
			
	}
	
}











