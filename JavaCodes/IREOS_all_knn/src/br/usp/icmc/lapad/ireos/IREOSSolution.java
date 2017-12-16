package br.usp.icmc.lapad.ireos;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;

public class IREOSSolution {
	/* Observations labeled as outliers in this solution */
	public IREOSExample examples[];
	/* IREOS statistics to this dataset */
	private IREOSStatistics statistics;
	/* Gamma maximum */
	private double gammaMax;
	/*
	 * The average of all observation separalities used to compute the expected
	 * value, each vector element represent the average of all the
	 * separabilities for the correspondent gamma
	 */
	private double averageSeparability[];
	/* IREOS index */
	private double indexIREOS = -1;
	/* Area under the separability curve */
	private double auc = -1;
	/* IREOS index adjusted for chance */
	private double adjustedIREOS = -1;
	/* Normal distribution from Apache Math to perform z-test */
	private NormalDistribution normal = new NormalDistribution();
	/* Student's t-distribution from Apache Math to perform t-test */
	private TDistribution t;
	private float weights[];

	private int n = -1;
	private int strategy;

	/**
	 * Constructor class
	 * 
	 * @param examples
	 *            Observations labeled as outliers in this solution
	 */
	public IREOSSolution(IREOSExample[] examples, int n, float[] weights, int strategy) {
		this.n = n;
		this.weights = weights;
		this.examples = examples;
		this.gammaMax = examples[0].getGammas()[(examples[0].getGammas().length - 1)];
		this.strategy = strategy;
		if (examples.length > 1)
			t = new TDistribution(examples.length - 1);
		else
			t = new TDistribution(1);
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
	 * Adjust IREOS index for chance
	 * 
	 * @throws Exception
	 *             The IREOS expected value for this dataset must be computed
	 *             before
	 */
	public double getAdjustedIREOS() throws Exception {
		if (statistics != null) {
			if (adjustedIREOS == -1)
				adjustedIREOS = (getAUC() - statistics.getExpectedValue()) / (gammaMax - statistics.getExpectedValue());
			return adjustedIREOS;
		} else
			throw new Exception("Variable statistics must be setted before by the method: setStatistics");
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
		if (auc == -1){
			switch (strategy) {
			case 0:
				auc = computeAUC(examples[0].getGammas(), getAverageSeparability());
				break;
			case 1:
				auc = computeTransformedAUC(examples[0].getGammas(), getAverageSeparability());
				break;

			default:
				System.err.println("Invalid Strategy.");
			}
			
		}
			return auc;
	}

	/**
	 * Compute z-test
	 * 
	 * @throws Exception
	 *             The IREOS expected value and variance for this dataset must
	 *             be computed before
	 */
	public double zTest() throws Exception {
		if (statistics != null) {
			double zscore = (getAUC() - statistics.getExpectedValue()) / Math.sqrt(statistics.getVariance(n));
			return normal.cumulativeProbability(1 - zscore);
		} else
			throw new Exception("Variable statistics must be setted before by the method: setStatistics");
	}

	/**
	 * Compute t-test
	 * 
	 * @throws Exception
	 *             The IREOS expected value and variance for this dataset must
	 *             be computed before
	 */
	public double tTest() throws Exception {
		if (statistics != null) {
			double tscore = (getAUC() - statistics.getExpectedValue()) / Math.sqrt(statistics.getVariance(n));
			return t.cumulativeProbability(1 - tscore);
		} else
			throw new Exception("Variable statistics must be setted before by the method: setStatistics");
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
			value +=  y[i];
		value /= y.length;
		return value;
	}

	/**
	 * Get IREOS statistics to this dataset
	 * 
	 */
	public IREOSStatistics getStatistics() {
		return statistics;
	}

	/**
	 * Set IREOS statistics to this dataset
	 * 
	 * @param statistics
	 *            The IREOS statistics to this dataset
	 */
	public void setStatistics(IREOSStatistics statistics) {
		this.statistics = statistics;
	}

	public int getn() {
		return n;
	}

	public void setn(int n) {
		this.n = n;
	}

}
