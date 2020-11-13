package br.usp.icmc.lapad.ireos;

public class IREOSSolution {
	public IREOSExample examples[];

	/* Gamma maximum */
	private double gammaMax;
	/* IREOS index */
	private double indexIREOS = -1;
	private double weights[];

	/**
	 * Constructor class
	 * 
	 * @param examples Observations labeled as outliers in this solution
	 */
	public IREOSSolution(IREOSExample[] examples, double[] weights, double gammaMax) {
		this.weights = weights;
		this.examples = examples;
		this.gammaMax = gammaMax;
	}

	public double[] getWeights() {
		return weights;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}

	/**
	 * Compute and get IREOS index for this solution
	 * 
	 */
	public double getIREOS() {
		if (indexIREOS == -1) {
			double sumWeight = 0;
			indexIREOS = 0;
			for (IREOSExample example : examples) {
				indexIREOS += weights[example.getIndex()] * example.getAuc();
				sumWeight += weights[example.getIndex()];
			}
			indexIREOS /= sumWeight;
			indexIREOS /= gammaMax;
		}
		return indexIREOS;
	}
}
