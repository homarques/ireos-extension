package br.usp.icmc.lapad.ireos;

public class IREOSSolution {
	/* Observations labeled as outliers in this solution */
	public IREOSExample examples[];
	/* Gamma maximum */
	private double gammaMax;
	/*
	 * The average of all observation separalities used to compute the expected
	 * value, each vector element represent the average of all the separabilities
	 * for the correspondent gamma
	 */
	private double averageSeparability[];
	/* IREOS index */
	private double indexIREOS = -1;
	/* Area under the separability curve */
	private double auc = -1;
	private float weights[];

	/**
	 * Constructor class
	 * 
	 * @param examples
	 *            Observations labeled as outliers in this solution
	 */
	public IREOSSolution(IREOSExample[] examples, float[] weights) {
		this.weights = weights;
		this.examples = examples;
		this.gammaMax = examples[0].getGammas()[(examples[0].getGammas().length - 1)];
	}

	/**
	 * Compute the average separability
	 * 
	 */
	public double[] getAverageSeparability() {
		if (averageSeparability == null) {
			double sumWeight;
			averageSeparability = new double[examples[0].getSeparability().length];
			for (int i = 0; i < examples[0].getSeparability().length; i++) {
				averageSeparability[i] = 0;
				sumWeight = 0;
				for (IREOSExample example : examples) {
					averageSeparability[i] += weights[example.getIndex()] * example.getSeparability()[i];
					sumWeight += weights[example.getIndex()];
				}
				averageSeparability[i] /= sumWeight;
			}
		}
		return averageSeparability;
	}

	public float[] getWeights() {
		return weights;
	}

	public void setWeights(float[] weights) {
		this.weights = weights;
	}

	/**
	 * Compute and get IREOS index for this solution
	 * 
	 */
	public double getIREOS() {
		if (indexIREOS == -1)
			indexIREOS = getAUC();
		return indexIREOS;
	}

	/**
	 * Get area under the separability curve of this solution
	 * 
	 */
	public double getAUC() {
		if (auc == -1) {
			auc = computeAUC(examples[0].getGammas(), getAverageSeparability());
		}
		return auc;
	}

	/**
	 * Compute AUC using the Trapezoidal Rule
	 * 
	 * @param x
	 *            x-axis
	 * @param y
	 *            y-axis
	 * @return AUC
	 */
	public double computeAUC(double[] x, double[] y) {
		double value = 0;
		for (int i = 1; i < x.length; i++)
			value += (x[i] - x[(i - 1)]) * (y[i - 1] + y[i]);
		value *= 0.5;
		value /= gammaMax;

		return value;
	}

	/**
	 * 
	 * Compute AUC using the transformation of discretization
	 * 
	 */
	public double computeTransformedAUC(double[] x, double[] y) {
		double value = 0;
		for (int i = 0; i < y.length; i++)
			value += y[i];
		value /= y.length;
		return value;
	}

}
