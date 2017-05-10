package de.dualuse.swt.experiments;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.dualuse.swt.graphics.PathShape;

public class ContourExperiment {
	
	double[] p = new double[8];
	
	int n;
	
	public int add(double px, double py) {
		allocate();
		int index = n;
		p[2*n] = px;
		p[2*n+1] = py;
		n = n + 1;
		return index;
	}

	public void insert(int i, double px, double py) {
		allocate();
		System.arraycopy(p, 2*i, p, 2*(i+1), 2*(n-i));
		p[2*i  ] = px;
		p[2*i+1] = py;
		n = n + 1;
	}

	public void remove(int i) {
		int from = 2*(i+1);
		int to = 2*i;
		if (from<2*n)
			System.arraycopy(p, from, p, to, 2*(n-i-1));
		n = n - 1;
	}
	
	public void removeLast() {
		if (n>0)
			remove(n-1);
	}
	
	private void allocate() {
		if (2*n==p.length)
			p = Arrays.copyOf(p, p.length*2);
	}
	
	public double getX(int i) {
		return p[2*i];
		// return x[i];
	}

	public double getY(int i) {
		return p[2*i+1];
		// return y[i];
	}

	public void set(int i, double x, double y) {
		if (i<0 || i>=n) throw new IllegalArgumentException("Index out of range.");
		p[2*i  ] = x;
		p[2*i+1] = y;
	}
	
	public int size() {
		return n;
	}
	
//	public void add(Point2D point) {}	
//	public Point2D get(int index) { return null; }
//	public int size() { return 0; }
//	
//	public Iterator<Point2D> iterator() {
//		return null;
//	}
//	
//	public void add(Contour b) {
//		
//	}
//	
//	double x1, y1, x2, y2, x3, y3, x4, y4;
//	public void sub(Contour b) {
//		Line2D line = null;
//		Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4);
//		
//	}

	public double[] add(ContourExperiment b) {
		if (n<3 || b.n<3)
			throw new IllegalArgumentException("Contour needs at least 3 points");
		
		double[] tmp = new double[3];
		double[] result = new double[4];
		int[] indices = new int[4];
		
		double[] p1 = new double[2];
		int p1i = -1;
		int p1j = -1;
		
		double[] p2 = new double[2];
		int p2i = -1; // p2i < p1i possible (if loop-closure in between)
		int p2j = -1;
		
		int intersections = 0;
		
		// Find first intersection
		outer:
		for (int j=0, J=b.n; j<J; j++) {
			
			int first = j;
			int second = (j+1) % b.n; // treat as open / no loop closure?
			
			double x1 = b.p[2*first  ]; 
			double y1 = b.p[2*first+1];
			
			double x2 = b.p[2*second  ];
			double y2 = b.p[2*second+1];
			
			for (int i=0, I=n; i<I; i++) {

				first  = i;
				second = (i+1) % n;
				
				if (second<first)
					System.out.println("Debug me");
				
				// Current edge of other contour
				double x3 = p[2*first  ];
				double y3 = p[2*first+1];
				
				double x4 = p[2*second  ];
				double y4 = p[2*second+1];
				
				System.out.println("Intersection?");
				System.out.println("\t(" + x1 + "," + y1 + ") - (" + x2 + "," + y2 + ")");
				System.out.println("\t(" + x3 + "," + y3 + ") - (" + x4 + "," + y4 + ")");
				
				double[] p = ContourExperiment.intersect(x1, y1, x2, y2, x3, y3, x4, y4, tmp);
				if (p==null) {
					System.out.println("No intersection between (" + i + " x " + j + ")");
					continue; // no intersection
				}
				
				if (Double.isInfinite(p[0]) || Double.isNaN(p[0])) {					
					System.out.println("Debug me");
					p = ContourExperiment.intersect(x1, y1, x2, y2, x3, y3, x4, y4, tmp);
				}
				
				result[2*intersections  ] = p[0];
				result[2*intersections+1] = p[1];
				
				indices[2*intersections  ] = i;
				indices[2*intersections+1] = j;
				
				intersections++;
				if (intersections==2) break outer;
				
				// First intersection found at edge i of contour a and edge j of contour b
				// new edges:
				//		(x1, y1, p.x, p.y) (replaces edge i of a)
				//		(p.x, p.y, x4, y4) (replaces edge j of b)
				
			}
		}

		p1[0] = result[0];
		p1[1] = result[1];
		p2[0] = result[2];
		p2[1] = result[3];
		p1i = indices[0];
		p1j = indices[1];
		p2i = indices[2];
		p2j = indices[3];
		
		// Edge Case:
		// possible to enter/leave in the same edge if
		//		close together
		//		loop around
		// ( in one case no vertices would be removed, only new vertices would be added )
		// ( in the other case all vertices would be removed ... )
		
		System.out.println("First intersection: ");
		System.out.println("\ti1: " + p1i);
		System.out.println("\tj1: " + p1j);
		System.out.println("\tp: (" + p1[0] + "," + p1[1] + ")");

		System.out.println("Second intersection: ");
		System.out.println("\ti2: " + p2i);
		System.out.println("\tj2: " + p2j);
		System.out.println("\tp: (" + p2[0] + "," + p2[1] + ")");
		
		/*
		// No first intersection found
		if (p1i==-1 || p1j==-1)
			return null;
		
		// Find second intersection
		outer:
		for (int j=p1j+1, J=b.n; j<J; j++) {
			
			int first = j;
			int second = (j+1) % b.n;
			
			// Current edge
			double x1 = b.p[2*first  ];
			double y1 = b.p[2*first+1];
			
			double x2 = b.p[2*second  ];
			double y2 = b.p[2*second+1];
			
			for (int i=0, I=n; i<I; i++) {
				
				first = i;
				second = (i+1) % n;
				
				// Current edge of other contour
				double x3 = p[2*first  ];
				double y3 = p[2*first+1]; 
				
				double x4 = p[2*second  ];
				double y4 = p[2*second+1];
				
				double[] p = Contour.intersect(x1, y1, x2, y2, x3, y3, x4, y4, tmp2);
				if (p==null) continue; // no intersection
				
				p2 = p;
				p2i = i;
				p2j = j;

				// First intersection found at edge i of contour a and edge j of contour b
				// new edges:
				//		(x1, y1, p.x, p.y) (replaces edge i of a)
				//		(p.x, p.y, x4, y4) (replaces edge j of b)
				
				break outer;
				
			}
		}

		// No second intersection found
		if (p2i==-1 || p2j==-1)
			return null;

		System.out.println("Second intersection: ");
		System.out.println("\ti2: " + p2i);
		System.out.println("\tj2: " + p2j);
		System.out.println("\tp: (" + p2[0] + "," + p2[1] + ")");
		
		System.out.println();
		
		System.out.println("p1: " + p1[0] + ", " + p1[1]);
		System.out.println("p2: " + p2[0] + ", " + p2[1]);
		*/
		
		return new double[] {
			p1[0], p1[1],
			p2[0], p2[1]
		};
		
		/*
		// Replace edges (p1i,p2i) from Contour a with edges (p1j,p2j) from Contour b
		// (consider case p2i<p1i and p2i==p1i)
		// XXX
		
		// edge i: vertices i, i+1
		int beginning = p1i;
		int verticesToRemove = p2i - p1i;
		int verticesToInsert = p2j - p1j;
		int end = n - p2i;
		
		int newVertices = n - verticesToRemove + verticesToInsert + 2;
		
		double[] np = new double[2*newVertices];
		System.arraycopy(p, 0, np, 0, 2*(p1i+1));

		int cur = 2*(p1i+1);
		np[cur++] = p1[0];
		np[cur++] = p1[1];
		
		System.arraycopy(b.p, 2*(p1j+1), np, cur, 2*verticesToInsert);
		
		cur += 2*verticesToInsert;
		np[cur++] = p2[0];
		np[cur++] = p2[1];
		
		System.arraycopy(p, 2*(p2i+1), np, cur, 2*end);
		
		p = np;
		n = p.length/2;
		*/
		
		// # vertices = # edges + 1
		
		
		// 0..p1i // keep first vertex of last edge
		// insert intersection point
		// start with second vertex of edge j1 until first vertex of last edge j2
		// insert second insertion point
		// start with second vertex of edge i2 until last edge
	
		// start procedure over starting from p1j until all of Contour b has been processed
		
		// return this;
	}
	
	public static ContourExperiment add(ContourExperiment a, ContourExperiment b) {
		return null;
	}
	
	public void toPath(Path path) {
		
		path.moveTo((float)p[0], (float)p[1]);
		for (int i=1; i<n; i++) {
			
			float x = (float)p[2*i];
			float y = (float)p[2*i+1];
			path.lineTo(x, y);
			
		}
		
//		if (closed)
//			path.close();
		
	}
	
	boolean closed=false;
	
	public void close() {
		closed=true;
	}
	
//	
//	public static Contour sub(Contour a, Contour b) {
//		return null;
//	}

//==[ Geom ]========================================================================================
	
	static double[] tmp_p1 = new double[3], tmp_p2 = new double[3], tmp_p3 = new double[3], tmp_p4 = new double[3];
	static double[] tmp_l1 = new double[3], tmp_l2 = new double[3];
	static double[] tmp_n = new double[3], tmp_p = new double[3];
	
	public static double[] intersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double[] res) {
		Line2D l1 = new Line2D.Double(x1, y1, x2, y2);
		Line2D l2 = new Line2D.Double(x3, y3, x4, y4);
		Point2D p = intersect(l1,l2);
		if (p==null) return null;
		return new double[] { p.getX(), p.getY() };
	}
	
	/*
	public static double[] intersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double[] res) {
		
		double[] l11 = fromPoint(x1, y1, tmp_p1);
		double[] l12 = fromPoint(x2, y2, tmp_p2);
		double[] l1 = cross(l11, l12, tmp_l1);
		
		double[] l21 = fromPoint(x3, y3, tmp_p3);
		double[] l22 = fromPoint(x4, y4, tmp_p4);
		double[] l2 = cross(l21, l22, tmp_l2);
		
		res = cross(l1, l2, res);

		if (dot(l1, res)!=0 || dot(l2, res)!=0)
			return null; // parallel lines
		
		normalize(l11);
		normalize(l12);
		normalize(res);

		double[] n = relative(l11, l12, tmp_n);
		double[] p = relative(l11, res, tmp_p);
		
		double len = normalizeLength(n);
		
		double s = dot(n, p);
		
		if (s < 0 || s > len)
			return null; // not in line segment
		
		return res;
	}
	*/
	
	public static Point2D intersect(Line2D line1, Line2D line2) {
		double[] l11 = fromPoint2D(line1.getP1());
		double[] l12 = fromPoint2D(line1.getP2());
		double[] l1 = cross(l11, l12);
		
		double[] l21 = fromPoint2D(line2.getP1());
		double[] l22 = fromPoint2D(line2.getP2());
		double[] l2 = cross(l21, l22);
		
		double[] res = cross(l1, l2);
		
		if (res[2]==0)
			return null; // only meet at infinity? should be parallel, but not sure
		
		if (dot(l1, res)!=0 || dot(l2, res)!=0)
			return null; // parallel lines
		
		normalize(l11);
		normalize(l12);
		normalize(res);

//		double[] n1 = relative(l11, l12);
//		double[] p1 = relative(l11, res);		
//		double len = normalizeLength(n1);		
//		double s = dot(n1, p1);		
//		if (s < 0 || s > len)
//			return null; // not in line segment 1
		
		if (!onLine(l11, l12, res)) return null;
		if (!onLine(l21, l22, res)) return null;
		
		return new Point2D.Double(res[0]/res[2], res[1]/res[2]);
	}
	
	public static boolean onLine(double[] l11, double[] l12, double[] v) {
		double[] n = relative(l11, l12);
		double[] p = relative(l11, v);		
		double len = normalizeLength(n);		
		double s = dot(n, p);
		return (s >= 0 && s <= len);
	}
	
//	public static double[] intersect(double[] l11, double[] l12, double[] l21, double[] l22) {
//		
//		double[] l1 = cross(l11, l12);
//		
//		System.out.println("Check 11: " + dot(l1, l11));
//		System.out.println("Check 12: " + dot(l1, l12));
//		
//		double[] l2 = cross(l21, l22);
//		
//		System.out.println("Check 21: " + dot(l2, l21));
//		System.out.println("Check 22: " + dot(l2, l22));
//		
//		double[] res = cross(l1, l2);
//		
//		System.out.println("x~ = " + res[0]);
//		System.out.println("y~ = " + res[1]);
//		System.out.println("w~ = " + res[2]);
//		
//		return res;
//	}
	
	public static double[] relative(double[] p1, double[] p2) {
		return relative(p1, p2, new double[2]);
	}
	
	public static double[] relative(double[] p1, double[] p2, double[] res) {
		res[0] = p2[0] - p1[0];
		res[1] = p2[1] - p1[1];
		return res;
	}
	
	public static double length(double[] p) {
		return Math.sqrt(p[0]*p[0]+p[1]*p[1]);
	}
	
	public static double[] fromPoint(double x, double y, double[] res) {
		res[0] = x;
		res[1] = y;
		res[2] = 1;
		return res;
	}
	
	public static double[] fromPoint2D(Point2D p) {
		return fromPoint2D(p, new double[3]);
	}
	
	public static double[] fromPoint2D(Point2D p, double[] res) {
		res[0] = p.getX();
		res[1] = p.getY();
		res[2] = 1;
		return res;
	}
	
	public static double normalizeLength(double[] p) {
		double len = length(p);
		for (int i=0, I=p.length; i<I; i++)
			p[i] /= len;
		return len;
	}
	
	public static double[] normalize(double[] p) {
		p[0] /= p[2];
		p[1] /= p[2];
		p[2] = 1;
		return p;
	}
	
	public static double dot(double[] p1, double[] p2) {
		if (p2.length<p1.length)
			throw new IllegalArgumentException("p2.length should at least match p1.length");
		
		double acc = 0;
		for (int i=0,I=p1.length; i<I; i++)
			acc += p1[i]*p2[i];
		
		return acc;
	}
	
	public static double[] cross(double[] p1, double[] p2) {
		return cross(p1, p2, new double[3]);
	}

	public static double[] cross(double[] p1, double[] p2, double[] res) {
		
		res[0] = p1[1]*p2[2] - p1[2]*p2[1];
		res[1] = p1[2]*p2[0] - p1[0]*p2[2];
		res[2] = p1[0]*p2[1] - p1[1]*p2[0];
		
		return res;
	}
	
//==[ Test-Main ]===================================================================================
	
//	static ArrayList<Path2D> shapes = new ArrayList<>();
//	static Path2D path;
	
	static ArrayList<ContourExperiment> shapes = new ArrayList<>();
	static ContourExperiment path;
	
	static ArrayList<Point2D> points = new ArrayList<>();
	
	static Point2D p1, p2, p3, p4;
	static Line2D l1, l2;
	
	public static void main(String[] args) {
		
		Display dsp = Display.getDefault();
		Shell shell = new Shell(dsp, SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());
		Canvas canvas = new Canvas(shell, SWT.NO_BACKGROUND);
		
		canvas.addListener(SWT.KeyDown, (e) -> {
			System.out.println("Keypressed");
			if (e.keyCode==27) shell.dispose();
		});
		
//		canvas.addListener(SWT.Paint, (e) -> {
//			System.out.println("Painting");
//			GC gc = e.gc;
//			gc.setForeground(dsp.getSystemColor(SWT.COLOR_WHITE));
//			gc.fillRectangle(0, 0, e.width, e.height);
//		});
		
		canvas.addPaintListener((e) -> {
			System.out.println("Painting");
			GC gc = e.gc;
			
			gc.setBackground(dsp.getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(0, 0, e.width, e.height);
			
			for (ContourExperiment c : shapes) {
				gc.setBackground(dsp.getSystemColor(SWT.COLOR_DARK_GRAY));
				Path path = new Path(dsp);
				c.toPath(path);
				gc.fillPath(path);
				path.dispose();
			}

			if (path != null) {
				gc.setForeground(dsp.getSystemColor(SWT.COLOR_BLACK));
				Path swtpath = new Path(dsp);
				path.toPath(swtpath);
				gc.drawPath(swtpath);
				swtpath.dispose();
			}
			
			for (Point2D p : points) {
				gc.setBackground(dsp.getSystemColor(SWT.COLOR_GREEN));
				Ellipse2D ellipse = new Ellipse2D.Double(p.getX()-1.5, p.getY()-1.5, 3, 3);
				Path pathShape = new PathShape(dsp, ellipse);
				gc.fillPath(pathShape);
				pathShape.dispose();
			}
			
//			for (Path2D p : shapes) {
//				gc.setBackground(dsp.getSystemColor(SWT.COLOR_DARK_GRAY));
//				PathShape shape = new PathShape(dsp, p);
//				gc.fillPath(shape);
//				shape.dispose();
//			}
			
//			if (path != null) {
//				gc.setForeground(dsp.getSystemColor(SWT.COLOR_BLACK));
//				PathShape shape = new PathShape(dsp, path);
//				gc.drawPath(shape);
//				shape.dispose();
//			}
			
			/*
			if (l1 != null)
				gc.drawLine((int)l1.getX1(), (int)l1.getY1(), (int)l1.getX2(), (int)l1.getY2());
			
			if (l2 != null)
				gc.drawLine((int)l2.getX1(), (int)l2.getY1(), (int)l2.getX2(), (int)l2.getY2());
			
			if (l1 != null && l2 != null) {
				gc.setForeground(dsp.getSystemColor(SWT.COLOR_RED));
				
				System.out.println(l1.getP1() + " - " + l1.getP2());
				System.out.println(l2.getP1() + " - " + l2.getP2());
				
				Point2D p = intersect(l1, l2);
				if (p != null) {
					Ellipse2D ep = new Ellipse2D.Double(p.getX()-2.5, p.getY()-2.5, 5, 5);
					PathShape path = new PathShape(dsp, ep);
					
					System.out.println(p);
					
					// gc.drawArc((int)p.getX()-3, (int)p.getY()-3, 6, 6, 0, 360);
					gc.drawPath(path);
					
					path.dispose();
				}
			}
			*/
		});
		
		canvas.addListener(SWT.MouseDown, (e) -> {
			
			if (e.button==1) {
				if (path==null) {
					
					System.out.println("new contour");
					
					path = new ContourExperiment();
					path.p[0] = e.x;
					path.p[1] = e.y;
					path.n = 1;
					canvas.redraw();
				} else {
					path.add(e.x, e.y);
					canvas.redraw();
				}
			} else if (e.button==3) {
				if (path!=null) path.removeLast();
				canvas.redraw();
			}
			
//			path = new Path2D.Double();
//			path.moveTo(e.x, e.y);
			
			/*
			if (p4!=null) {
				p1=p2=p3=p4=null;
				l1=l2=null;
				canvas.redraw();
			}
			
			if (p1==null) { p1 = new Point2D.Double(e.x, e.y); return; }
			if (p2==null) {
				p2 = new Point2D.Double(e.x, e.y);
				l1 = new Line2D.Double(p1, p2);
				canvas.redraw();
				return;
			}
			
			if (p3==null) { p3 = new Point2D.Double(e.x, e.y); return; }
			if (p4==null) {
				p4 = new Point2D.Double(e.x, e.y);
				l2 = new Line2D.Double(p3, p4);
				canvas.redraw();
				return;
			}
			*/
		});
		
		canvas.addListener(SWT.MouseMove, (e) -> {
			
//			if (path != null) {
//				path.add(e.x, e.y);
//				canvas.redraw();
//			}
			
//			if (path != null) {
//				path.lineTo(e.x, e.y);
//				canvas.redraw();
//			}
			
		});
		
		canvas.addListener(SWT.MouseUp, (e) -> {
			
			/*
			if (path != null) {
				
				boolean ctrlPressed = (e.stateMask & SWT.CTRL) != 0;
				boolean shiftPressed = (e.stateMask & SWT.SHIFT) != 0;
				
				if (!shapes.isEmpty() && (shiftPressed || ctrlPressed)) {
					
					if (!shapes.isEmpty() && shiftPressed) {
						
						Contour a = shapes.get(0);
						Contour b = path;
						double[] intersections = a.add(b);
						
						if (intersections!=null) {
							Point2D p1 = new Point2D.Double(intersections[0], intersections[1]);
							Point2D p2 = new Point2D.Double(intersections[2], intersections[3]);
							points.add(p1);
							points.add(p2);
						}
						
					}
					
				} else {
					
					path.close();
					shapes.add(path);
					
				}
				
				path = null;
				canvas.redraw();
			}
			*/
			
//			if (path != null) {
//				path.closePath();
//				
//				boolean ctrlPressed = (e.stateMask & SWT.CTRL) != 0;
//				boolean shiftPressed = (e.stateMask & SWT.SHIFT) != 0;
//				
//				if (!shapes.isEmpty() && (shiftPressed || ctrlPressed)) {
//					
//					Area area1 = new Area(shapes.get(0));
//					Area area2 = new Area(path);
//					
//					if (shiftPressed)
//						area1.add(area2);
//					if (ctrlPressed)
//						area1.subtract(area2);
//					
//					shapes.set(0, new Path2D.Double(area1));
//					
//				} else {
//				
//					shapes.add(path);
//					
//				}
//				
//				path = null;
//				canvas.redraw();
//			}
		});
		
		shell.open();
		
		while(!shell.isDisposed())
			while(!dsp.readAndDispatch())
				dsp.sleep();
		
		dsp.dispose();
		
	}
	
}
