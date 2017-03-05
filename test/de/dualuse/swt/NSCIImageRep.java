package de.dualuse.swt;

import org.eclipse.swt.internal.cocoa.NSImageRep;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.internal.cocoa.id;

public class NSCIImageRep extends NSImageRep {

	public NSCIImageRep() {
		super();
	}
	
	public NSCIImageRep(int id) {
		super(id);
	}
	
//	public CIImage CIImage() {
//		int result = OS.objc_msgSend(this.id, OS.sel_CIImage);
//		return result != 0 ? new CIImage(result) : null;
//	}
	
	public static id imageRepWithCIImage(CIImage image) {
		long result = OS.objc_msgSend(class_NSCIImageRep, sel_imageRepWithCIImage_1, image != null ? image.id : 0);
		return result != 0 ? new id(result) : null;
	}
	
	public NSCIImageRep initWithCIImage(CIImage image) {
		long result = OS.objc_msgSend(this.id, sel_initWithCIImage_1, image != null ? image.id : 0);
		return result != 0 ? this : null;
	}

	public static final long class_NSCIImageRep = OS.sel_registerName("NSCIImageRep");
	public static final long sel_imageRepWithCIImage_1 = OS.sel_registerName("imageRepWithCIImage:");
	public static final long sel_initWithCIImage_1 = OS.sel_registerName("initWithCIImage:");
		
//	public static final long /*int*/ class_WebPanelAuthenticationHandler = OS.objc_getClass("WebPanelAuthenticationHandler");


}