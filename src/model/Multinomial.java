package model;

import java.util.Random;

import util.MyArray;

public class Multinomial {
	//x,y == P(x given y)
	int x,y;
	public double[][] count;
	
	public Multinomial(int x, int y) {
		this.x = x; this.y = y;
		count = new double[x][y];
	}
	
	public void initializeRandom(Random r) {
		double small = 1e-100;
		for(int i=0; i<y; i++) {
			double sum = 0;
			for(int j=0; j<x; j++) {
				count[j][i] = r.nextDouble() + small;
				sum += count[j][i];
			}
			//normalize
			for(int j=0; j<x; j++) {
				count[j][i] = count[j][i] / sum;
			}
		}
	}
	
	public double get(int x, int y) {
		return count[x][y];
	}
	
	public void addToCounts(int x, int y, double value) {
		count[x][y] += value;
	}
	
	public void checkDistribution() {
		double tolerance = 1e-4;
		
		for(int i=0; i<y; i++) {
			double sum = 0;
			for(int j=0; j<x; j++) {
				sum += count[j][i];
			}
			if(Math.abs(1 - sum) > tolerance) {
				//System.err.println("Distribution sums to : " + sum);
				throw new RuntimeException("Distribution sums to : " + sum);
			}
		}
	}
	
	public void printDistribution() {
		MyArray.printTable(count);
	}
}
