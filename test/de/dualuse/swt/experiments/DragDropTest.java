package de.dualuse.swt.experiments;

import static org.eclipse.swt.SWT.*;

import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DragDropTest {
	
	
	public static void main(String[] args) {
		
		Display dsp = new Display();

		Shell sh = new Shell(dsp);
		sh.setLayout(new FillLayout());
		Label l = new Label(sh,NONE);

		l.setImage( new Image(dsp, ClipboardTest.class.getResourceAsStream("generic-cat.jpeg")));
		
		Transfer[] dropTypes = { TextTransfer.getInstance(), ImageTransfer.getInstance(), FileTransfer.getInstance() };
		DropTarget dt = new DropTarget(l, DND.DROP_LINK|DND.DROP_MOVE|DND.DROP_COPY);
		dt.setTransfer(dropTypes);
//		dt.setTransfer(new Transfer[] { ImageTransfer.getInstance() } );
//		dt.setTransfer(new Transfer[] { URLTransfer.getInstance() } );

		
		DragSource ds = new DragSource(l, DND.DROP_COPY);
		ds.setTransfer(new Transfer[] { ImageTransfer.getInstance() });
		
		dt.addDropListener(new DropTargetListener() {
			public void dropAccept(DropTargetEvent event) {
				System.out.println("accept");
				
			}
			
			public void drop(DropTargetEvent event) {
				System.out.println("syso "+event.data.getClass()+" "+event.data);
				if (event.data == null) 
					event.detail = DND.DROP_NONE;
				else
				if (event.data instanceof String[])  {
					l.setImage(new Image(dsp, ((String[])event.data)[0]));
					ds.setTransfer(new Transfer[] { ImageTransfer.getInstance() });
				}
				if (event.data instanceof String) { 
					l.setText(event.data.toString());
					ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
				}			
				else
				if (event.data instanceof ImageData) {
					l.setImage(new Image(dsp, (ImageData)event.data));
					ds.setTransfer(new Transfer[] { ImageTransfer.getInstance() });
				}
			}
			
			public void dragOver(DropTargetEvent event) {
//				System.out.println("over");
//				System.out.println(event.currentDataType);
			}
			public void dragOperationChanged(DropTargetEvent event) {
				System.out.println("changed");
			}
			public void dragLeave(DropTargetEvent event) {
				System.out.println("leave");
			}
			public void dragEnter(DropTargetEvent event) { 
				System.out.println("enter");
				event.detail = DND.DropAccept;   /// <-- super wichtig unter OS X
			}
		});
		

		ds.addDragListener(new DragSourceListener() {
			@Override
			public void dragStart(DragSourceEvent event) {
				event.doit = true;	
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					System.out.println("TEXT");
					event.data = l.getText();
				} else 
				if (ImageTransfer.getInstance().isSupportedType(event.dataType)) { 
					System.out.println("IMAGE");
					event.data = l.getImage().getImageData();
				} else
					event.data = null;
			}
			
			@Override
			public void dragFinished(DragSourceEvent event) {
				System.out.println("finished");
			}
		});
		
		
		sh.setBounds(600, 100, 800, 800);
		sh.setVisible(true);
		
		
		while (!dsp.isDisposed())
			if (!dsp.readAndDispatch())
				dsp.sleep();
		
		
	}
}
