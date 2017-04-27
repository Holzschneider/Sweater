/*
package de.dualuse.swt.experiments;

import java.util.Arrays;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class MathExperiments {
	public static void main(String[] args) {
		// RealMatrix matrix = new RealMatrix();
		
		double alpha = Math.PI / 4;
		
		double a11 = 2 * Math.cos(alpha);
		double a21 = 2 * Math.sin(alpha);
		
//		double a12 = a11;
//		double a22 = a21;
		
		double a12 = 4 * -Math.sin(alpha);
		double a22 = 4 * Math.cos(alpha);
		
		double a31 = 0, a32 = 0;
		double a13 = 0, a23 = 0, a33 = 0;
		
		RealMatrix m = MatrixUtils.createRealMatrix(3,3);
		m = MatrixUtils.createRealIdentityMatrix(3);
		m.setSubMatrix(new double[][] {
			{ a11, a12, a13 },
			{ a21, a22, a23 },
			{ a31, a32, a33 }
		}, 0, 0);
		
//		m = m.scalarMultiply(3);
//		m = MatrixUtils.inverse(m);
		
//		m.multiplyEntry(0, 0, 0.5);
//		m.multiplyEntry(1, 1, 2);
//		m.multiplyEntry(2, 2, 3);
		
		RealVector v = MatrixUtils.createRealVector(
			new double[]{1.0, 1.0, 0.0}
		);
		RealVector u = m.preMultiply(v);
		
		System.out.println("m: " + m);
		System.out.println("v: " + v);
		System.out.println("u: " + u);
		
		SingularValueDecomposition svd = new SingularValueDecomposition(m);
		System.out.println("svd: " +Arrays.toString(svd.getSingularValues()));
		System.out.println("Rank: " + svd.getRank());
		
		RealMatrix U = svd.getU();
		RealMatrix S = svd.getS();
		RealMatrix V = svd.getV();
		
		System.out.println("det(U): " + new LUDecomposition(U).getDeterminant());
		System.out.println("det(V): " + new LUDecomposition(V).getDeterminant());
		
//		if (S.getEntry(0, 0)>0)
//			S.setEntry(0, 0, 1/S.getEntry(0, 0));
//		if (S.getEntry(1, 1)>0)
//			S.setEntry(1, 1, 1/S.getEntry(1, 1));
//		if (S.getEntry(2, 2)>0)
//			S.setEntry(2, 2, 1/S.getEntry(2, 2));
//		
//		U = U.transpose();
//		// S = S.transpose();
//		V = V.transpose();
//		
//		RealMatrix pseudoInv = V.multiply(S).multiply(U);
//		System.out.println("pseudo inv: " + S);
		
		RealMatrix M = U.multiply(S).multiply(V);
		System.out.println("M: " + M);
	}
}

*/