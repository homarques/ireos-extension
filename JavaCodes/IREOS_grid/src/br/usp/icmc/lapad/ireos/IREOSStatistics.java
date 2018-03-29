package br.usp.icmc.lapad.ireos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.correlation.Covariance;

public class IREOSStatistics {
	/* Vector of all observations used to compute the statistics */
	public IREOSExample examples[];
	/*
	 * The average of all observation separalities used to compute the expected
	 * value, each vector element represent the average of all the
	 * separabilities for the correspondent gamma
	 */
	private double averageSeparability[];
	/* IREOS index expected value */
	private double expectedValue = -1;
	/* IREOS index variance */
	private double variance = -1;
	/* Number of outliers in the dataset */
	private int n = -1;
	/*
	 * List of all observation separalities used to compute the variance, each
	 * list element represent all the separabilities for the correspondent gamma
	 */
	private List<double[]> separabilitiesList;

	/**
	 * Constructor class
	 * 
	 * @param examples
	 *            Observations used to compute the statistics
	 * @param n
	 *            Number of outliers in the dataset
	 */
	public IREOSStatistics(IREOSExample[] examples, int n) {
		this.examples = examples;
		this.n = n;
		getSeparabilities();
	}

	/**
	 * Compute the average separability and build the separabilitiesList
	 * 
	 */
	public double[] getSeparabilities() {
		if (averageSeparability == null || separabilitiesList == null) {
			averageSeparability = new double[examples[0].getSeparability().length];
			separabilitiesList = new ArrayList<double[]>();
			for (int i = 0; i < examples[0].getSeparability().length; i++) {
				averageSeparability[i] = 0;
				double separability[] = new double[examples.length];
				for (int j = 0; j < examples.length; j++) {
					averageSeparability[i] += examples[j].getSeparability()[i];
					separability[j] = examples[j].getSeparability()[i];
				}
				separabilitiesList.add(separability);
				averageSeparability[i] /= examples.length;
			}
		}
		return averageSeparability;
	}

	/**
	 * Get and compute the IREOS index expected value
	 * 
	 */
	public double getExpectedValue() {
		if (expectedValue == -1)
			expectedValue = computeAUC(examples[0].getGammas(),
					getSeparabilities());
		return expectedValue;
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

		return value;
	}

	/**
	 * Get and compute the IREOS index variance
	 */
	public double getVariance() {
		if (variance == -1) {

			Covariance covariances = new Covariance();
			double[] x = examples[0].getGammas();
			List<double[]> y = separabilitiesList;
			variance = 0;
			for (int i = 1; i < x.length; i++) {
				for (int j = 1; j < x.length; j++) {
					variance += (x[i] - x[(i - 1)])
							* (x[j] - x[(j - 1)])
							* ((covariances.covariance(y.get(i), y.get(j)) / n)
									+ (covariances.covariance(y.get(i),
											y.get(j - 1)) / n)
									+ (covariances.covariance(y.get(i - 1),
											y.get(j)) / n) + (covariances
									.covariance(y.get(i - 1), y.get(j - 1)) / n));
				}
			}
			variance *= 0.25;
			/* Apply the finite population correction (fpc) factor */
			double fpc = (double) (examples.length - n) / (examples.length - 1);
			variance = fpc * variance;
		}

		return variance;
	}
}
