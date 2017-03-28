package de.dualuse.swt.experiments;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeTestTracerSigns extends Composite {

	public final static String SIGN_STOP = "Stop";
	public final static String SIGN_VORFAHRT = "Vorfahrt";
	public final static String SIGN_HOECHSTGESCHWINDIGKEIT = "Speed Limit";
	public final static String SIGN_ARBEITSSTELLE = "Arbeitsstelle";
	public final static String SIGN_UMLEITUNG = "Umleitung";
	
	public final static Map<String,Image> images = new HashMap<>();
	
	public final static Image getImage(Display dsp, String identifier) {
		if (images.containsKey(identifier)) {
			return images.get(identifier);
		}
		
		String filename = null;
		switch(identifier) {
			case SIGN_STOP: filename = "stop_sm.png"; break;
			case SIGN_VORFAHRT: filename = "vorfahrt_sm.png"; break;
			case SIGN_HOECHSTGESCHWINDIGKEIT: filename = "hoechstgeschwindigkeit_sm.png"; break;
			case SIGN_ARBEITSSTELLE: filename = "arbeitsstelle_sm.png"; break;
			case SIGN_UMLEITUNG: filename = "umleitung_sm.png"; break;
		}
		
		Image image = filename!=null ? new Image(dsp, TreeTest.class.getResourceAsStream(filename)) : null;
		if (image!=null) images.put(identifier, image);
		
		return image;
	}
	
	private void createSignItem(TreeItem parent, String name, int count) {
		TreeItem signItem = new TreeItem(parent, SWT.NONE);
		signItem.setText(name + " (" + count + ")");
		signItem.setImage(getImage(getDisplay(), name));
		signItem.setData(name);
		createInstances(signItem, name, 5);
	}
	
	private void createInstances(TreeItem parent, String name, int count) {
		for (int i=0; i<count; i++) {
			TreeItem instanceItem = new TreeItem(parent, SWT.NONE);
			instanceItem.setText(name + " #" + (i+1));
		}
	}
	
	public TreeTestTracerSigns(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new FillLayout());
		
		Display dsp = this.getDisplay();
		
		final Tree tree = new Tree(this, SWT.BORDER);
		
		tree.setBackground(new Color(dsp, new RGB(164,164,228)));
		
		TreeItem item = new TreeItem(tree, SWT.NONE);
		item.setText("Traffic (22)");
		
			createSignItem(item, SIGN_STOP, 5);
			createSignItem(item, SIGN_VORFAHRT, 3);
			createSignItem(item, SIGN_HOECHSTGESCHWINDIGKEIT, 4);
			
		item.setExpanded(true); // must be called _after_ the subitems have been added
			
		item = new TreeItem(tree, SWT.NONE);
		item.setText("Construction (22)");
		
			createSignItem(item, SIGN_ARBEITSSTELLE, 2);
			createSignItem(item, SIGN_UMLEITUNG, 3);
			
		item.setExpanded(true);  // must be called _after_ the subitems have been added
		
		DragSource dragSource = new DragSource(tree, DND.DROP_COPY);
		dragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		
		dragSource.addDragListener(new DragSourceListener() {
			TreeItem item;
			
			@Override public void dragFinished(DragSourceEvent event) {
				System.out.println("dragFinished");
			}

			@Override public void dragSetData(DragSourceEvent event) {
				System.out.println("dragSetData");
				
				if (item!=null && TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = item.getData();
				}
			}
			@Override public void dragStart(DragSourceEvent event) {
				item = tree.getItem(new Point(event.x,event.y));
				
				Object data = item.getData();
				event.doit = false;
				if (data==SIGN_STOP || data==SIGN_VORFAHRT || data==SIGN_HOECHSTGESCHWINDIGKEIT || data==SIGN_ARBEITSSTELLE || data==SIGN_UMLEITUNG)
					event.doit = true;
				
				if (event.doit)
					System.out.println("Drag start (" + item.getData() + ")");
			}
			
		});
	}

}
