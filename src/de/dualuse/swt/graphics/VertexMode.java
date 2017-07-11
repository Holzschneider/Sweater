package de.dualuse.swt.graphics;

public enum VertexMode {
	POINTS(1),
	LINES(2),
	TRIANGLES(3),
	QUADS(4),
	TRIANGLE_FAN(5),
	LINE_STRIP(10000-1),
	LINE_LOOP(10000);
	
	private VertexMode(int code) {
		this.code = code;
	}
	
	
	final int code;
}
