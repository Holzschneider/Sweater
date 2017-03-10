package de.dualuse.swt;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class TreeTestVirtual {
	public static void main(String[] args) {
		Display dsp = new Display();
		
		Shell shell = new Shell(dsp);
		shell.setSize(800, 600);

		shell.setText("Tree");
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		
		shell.addShellListener(new ShellListener() {
			@Override public void shellIconified(ShellEvent arg0) {}
			@Override public void shellDeiconified(ShellEvent arg0) {}
			@Override public void shellDeactivated(ShellEvent arg0) {}
			@Override public void shellActivated(ShellEvent arg0) {}

			@Override public void shellClosed(ShellEvent arg0) {
				dsp.dispose();
			}
		});
		
		System.out.println("Hi");
		
		final Tree tree = new Tree(shell, SWT.VIRTUAL | SWT.BORDER);
		final File baseDir = new File("/");
		
		// tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		
		File[] entries = baseDir.listFiles();
		int count = 0;
		if (entries!=null) count = entries.length;
		
		tree.setItemCount(count);
//		tree.setTopItem(tree.getItem(5));
		// tree.setInsertMark(tree.getItem(5), true);
		
		tree.addTreeListener(new TreeListener() {
			@Override public void treeCollapsed(TreeEvent event) {
				System.out.println("treeCollapsed(" + event + ")");
			}
			@Override public void treeExpanded(TreeEvent event) {
				System.out.println("treeExpanded(" + event + ")");
			}
		});
		
		tree.addTraverseListener(new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent event) {
				System.out.println("keyTraversed( " + event + ")");
			}
		});
		
		tree.addSelectionListener(new SelectionListener() {
			@Override public void widgetDefaultSelected(SelectionEvent event) {
				System.out.println("widgetDefaultSelected(" + event + ")");
			}

			@Override public void widgetSelected(SelectionEvent event) {
				System.out.println("widgetSelected(" + event + ")");
			}
		});
		
		tree.addListener(SWT.SetData, new Listener() {
			@Override public void handleEvent(Event event) {
				
				TreeItem item = (TreeItem)event.item;
				TreeItem parentItem = item.getParentItem();
				
//				System.out.println("Item  : " + item);
//				System.out.println("Parent: " + parentItem);
				
				File dir = parentItem!=null ? (File)parentItem.getData() : baseDir;
				File[] files = dir.listFiles();
				
				String text = null;
				int index = 0;
				if (parentItem == null) {
					index = tree.indexOf(item);
					// text = "node " + tree.indexOf(item);
				} else {
					index = parentItem.indexOf(item);
					// text = parentItem.getText() + " - " + parentItem.indexOf(item);
				}
//				item.setText(text);
				File itemFile = files[index];
				
				item.setData(itemFile);
				
//				System.out.println(text);
				int count = 0;
				if (itemFile.isDirectory()) {
					System.out.println(itemFile.getAbsolutePath());
					File[] subFiles = itemFile.listFiles();
					count = subFiles==null ? 0 : subFiles.length;
					item.setItemCount(count);
				}
				
				String lengthString = itemFile.isDirectory() ? "" : "" + itemFile.length();
				String countString = itemFile.isDirectory() ? "" + count : "";
				
				item.setText(new String[] { itemFile.getName(), lengthString, countString });
				item.setText(itemFile.getName());
			}
		});
		
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT, 0);
		column1.setText("Name");
		column1.setWidth(200);
		
		TreeColumn column2 = new TreeColumn(tree, SWT.LEFT, 1);
		column2.setText("Size");
		column2.setWidth(200);
		
		TreeColumn column3 = new TreeColumn(tree, SWT.CENTER, 2);
		column3.setText("Count");
		column3.setWidth(200);
		
		tree.setBackground(new Color(dsp, new RGB(164,164,228)));
		
		shell.setVisible(true);
			
//		tree.addListener(SWT.PaintItem, new Listener() {
//			@Override
//			public void handleEvent(Event event) {
//				System.out.println(event);
//				event.magnification = 0.25;
//				event.gc.drawLine(0, 0, 100, 100);
//			}
//			
//		});
			
//		tree.addListener(SWT.MeasureItem, new Listener() {
//			@Override public void handleEvent(Event event) {
//				System.out.println("Measuring item (" + event.item  + ", " + event.width + ")");
//				event.width = 4;
//				event.height = 0;
//			}
//		});
		
		while(!dsp.isDisposed()) {
			
			if (!dsp.readAndDispatch())
				dsp.sleep();
		}
		
		dsp.dispose();
		
		System.out.println("Shutting down");
	}
}
