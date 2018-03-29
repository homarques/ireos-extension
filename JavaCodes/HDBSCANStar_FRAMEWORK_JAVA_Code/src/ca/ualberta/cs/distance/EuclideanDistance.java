package ca.ualberta.cs.distance;

/**
 * Computes the euclidean distance between two points, d = sqrt((x1-y1)^2 + (x2-y2)^2 + ... + (xn-yn)^2).
 * @author zjullion
 */
public class EuclideanDistance implements DistanceCalculator {

	// ------------------------------ PRIVATE VARIABLES ------------------------------

	// ------------------------------ CONSTANTS ------------------------------

	// ------------------------------ CONSTRUCTORS ------------------------------
	
	public EuclideanDistance() {
	}

	// ------------------------------ PUBLIC METHODS ------------------------------
	
	public double computeDistance(double[] attributesOne, double[] attributesTwo) {
		double distance = 0;
		
		for (int i = 0; i < attributesOne.length && i < attributesTwo.length; i++) {
			distance+= ((attributesOne[i] - attributesTwo[i]) * (attributesOne[i] - attributesTwo[i]));
		}
		
		return Math.sqrt(distance);
	}
	
	
	public String getName() {
		return "euclidean";
	}

	// ------------------------------ PRIVATE METHODS ------------------------------

	// ------------------------------ GETTERS & SETTERS ------------------------------

}