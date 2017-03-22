package de.dualuse.swt;

import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.ImageData;

public class BufferedImageData extends BufferedImage {

	final public ImageData data = null;

	public BufferedImageData(int width, int height) {
		super(width, height, BufferedImage.TYPE_3BYTE_BGR); //oderso
	}

}
