package de.dualuse.swt.opengl;

import org.eclipse.swt.opengl.GLData;

public class GLDefaultData extends GLData {
	
	public GLDefaultData() {
		doubleBuffer = true;

		switch (System.getProperty("os.name")) {
		case "Mac OS X":
			this.samples = 8;
			break;
			
		}
	}

}
