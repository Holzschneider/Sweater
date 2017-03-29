package de.dualuse.swt.app;

import static org.eclipse.swt.SWT.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.List;

import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import de.dualuse.swt.app.AutoMenuBar.AutoMenu;
import de.dualuse.swt.app.AutoMenuBar.AutoMenuItem;
import de.dualuse.swt.app.AutoMenuBar.MenuScope;
import de.dualuse.swt.app.MenuNode.*;

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
					if (intersect(scope, ami.scope())) {
						MenuNode node = root.find(ami.path().length>0?ami.path():ami.value());
						
						node.setAttributes(clazz,mth,ami);
						node.delegates.add(new DelegateMethod(w,mth));
						if (node.parent!=null)
							Collections.sort(node.parent.children);
					}
				}
		
		for (Class<?> clazz: hierarchy)
			for (Field fld: clazz.getDeclaredFields()) {
				if (fld.isAnnotationPresent(AutoMenuItem.class)) {
					AutoMenuItem ami = fld.getAnnotation(AutoMenuItem.class);
					if (intersect(scope, ami.scope())) { 
						MenuNode node = root.find(ami.path().length>0?ami.path():ami.value());
						node.setAttributes(clazz,fld,ami);
						
						Class<?> type = fld.getType();
						
						List<MenuItemDelegate> del = node.delegates;
						if (type.isAssignableFrom(MenuItem.class)) node.outlets.add(new Outlet(w, fld));
						else if (type.equals(Listener.class)) del.add(new DelegateListenerField(w, fld));
						else if (type.equals(SelectionListener.class)) del.add(new DelegateSelectionListenerField(w,fld));
						else throw new RuntimeException("Invalid AutoMenuItem target: "+fld);
						
						if (node.parent!=null)
							Collections.sort(node.parent.children);
					}
				}
				
				if (fld.isAnnotationPresent(AutoMenu.class)) {
					AutoMenu am = fld.getAnnotation(AutoMenu.class);
					
					if (intersect(scope, am.scope())) {
						MenuNode node = root.find(am.path().length>0?am.path():am.value());
						node.setAttributes(clazz,fld,am);
						
						Class<?> type = fld.getType();
						if (type.isAssignableFrom(Menu.class)) node.outlets.add(new Outlet(w, fld));
						else throw new RuntimeException("Invalid AutoMenu target: "+fld);
						
						if (node.parent!=null)
							Collections.sort(node.parent.children);
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
				MenuItem mi = null;
				for (MenuItem ei: m.getItems())
					if (ei.getText().matches(mc.label))
//					if (ei.getText().equals(mc.label))
						mi = ei;
					
				if (mi==null) {
					if (mc.splitBefore) new MenuItem(m, SEPARATOR);
					if (mc.index==null)
						mi = new MenuItem(m, CASCADE);
					else
						mi = new MenuItem(m, CASCADE, mc.index);

					mi.setText(mc.label);
					if (mc.splitAfter) new MenuItem(m, SEPARATOR);
				} else
					if (mi.getStyle()!=CASCADE)
						throw new RuntimeException("Unable to create Item "+mc.toPath()+": Present item incompatible");
				
				
				mi.setEnabled(mc.enabled);

				Menu sub = new Menu(mi);

				//XXX dirty hack: allows multiple MenuItem instances scattered over window menues to alias one outlet  
				sub.addMenuListener(new MenuAdapter() {
					public void menuShown(MenuEvent e) {
						for (MenuNode nc: mc.children)
							for (Outlet o: nc.outlets)
								if (o.value!=null)
									o.set(o.value);
					}
				});
				
				mi.setMenu(sub);
				buildMenu(sub, mc);

				for (Outlet o: mc.outlets)
					o.set(sub);
			} else {
				MenuItem mi = null;
				for (MenuItem ei: m.getItems())
					if (ei.getText().matches(mc.label))
//					if (ei.getText().equals(mc.label))
						mi = ei;

				int style = mc.check?mc.grouped?RADIO:CHECK:PUSH;
				
				if (mi==null) {
					if (mc.splitBefore) new MenuItem(m, SEPARATOR);
					if (mc.index==null)
						mi = new MenuItem(m, style);
					else
						mi = new MenuItem(m, style, mc.index);

					mi.setText(mc.label);
					if (mc.splitAfter) new MenuItem(m, SEPARATOR);
				} else
					if (mi.getStyle()!=style)
						throw new RuntimeException("Unable to create Item "+mc.toPath()+": Present item incompatible");
				
				
				mi.setEnabled(mc.enabled);
				mi.setSelection(mc.checked);
				
				if (mc.accelerator!=null && (mc.accelerator&COMMAND)!=0) //XXX fix, since MOD1 cannot be specified in Annotation attributes
					mc.accelerator= mc.accelerator&~COMMAND | MOD1;
				
				if (mc.accelerator!=null)
					mi.setAccelerator(mc.accelerator);

				URL iconURL = null;
				if (mc.icon!=null) 
					for (String iconIdentifier: mc.icon.path)
						if ((iconURL = mc.icon.loader.getResource(iconIdentifier))!=null)
							break;

				if (iconURL!=null) try (InputStream iconStream = iconURL.openStream()) { //XXX cache images here 
					mi.setImage(new Image(m.getDisplay(), iconStream));
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				
				if (iconURL==null && mc.systemIcon!=null)
					mi.setImage(m.getDisplay().getSystemImage(mc.systemIcon));

				
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
	
	
	

	
}
