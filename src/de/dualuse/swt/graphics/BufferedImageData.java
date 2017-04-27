package de.dualuse.swt.graphics;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

public class BufferedImageData extends BufferedImage {
	final static private ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	final static private int[] nBits = {8, 8, 8};
	final static private int[] bOffs = {2, 1, 0};

	final public ImageData data;

	public BufferedImageData(int width, int height) {
		this ( new ImageData(width, height, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF)) );
	}
	
	private BufferedImageData(ImageData data) {
		this( 
				data,
				new ComponentColorModel(cs, nBits, false, false,
                                             Transparency.OPAQUE,
                                             DataBuffer.TYPE_BYTE),
				
				Raster.createInterleavedRaster(
	        		new DataBufferByte(data.data, data.width*data.height*3),
	        		data.width, data.height,
	                data.width*3, 3,
	                bOffs, null)
			);
	}
	
	private BufferedImageData(ImageData data, ComponentColorModel model, WritableRaster raster) {
		super(model,raster,false,null);
		this.data = data;
	}

}