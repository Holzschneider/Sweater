package de.dualuse.swt.experiments;

import org.eclipse.swt.internal.cocoa.NSBitmapImageRep;
import org.eclipse.swt.internal.cocoa.NSObject;
import org.eclipse.swt.internal.cocoa.OS;

public class CIImage extends NSObject {

	public CIImage() {
		super();
	}
	
	public CIImage(int id) {
		super(id);
	}

	// public void drawAtPoint(NSPoint point, NSRect fromRect, int op, float
	// delta) {
	// OS.objc_msgSend(this.id,
	// OS.sel_drawAtPoint_1fromRect_1operation_1fraction_1, point, fromRect, op,
	// delta);
	// }
	//
	// public void drawInRect(NSRect rect, NSRect fromRect, int op, float delta)
	// {
	// OS.objc_msgSend(this.id,
	// OS.sel_drawInRect_1fromRect_1operation_1fraction_1, rect, fromRect, op,
	// delta);
	// }

	public CIImage initWithBitmapImageRep(NSBitmapImageRep bitmapImageRep) {
		long result = OS.objc_msgSend(this.id, sel_initWithBitmapImageRep_1, bitmapImageRep != null ? bitmapImageRep.id : 0);
		return result != 0 ? this : null;
	}

	static long sel_initWithBitmapImageRep_1 = OS.sel_registerName("initWithBitmapImageRep:");

}