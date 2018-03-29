package br.usp.icmc.lapad.ireos;

import java.util.List;

public class IREOSSolution {
	/* Observations labeled as outliers in this solution */
	public IREOSExample examples[];
	/* Gamma maximum */
	private double gammaMax;

	/* IREOS index */
	private double indexIREOS = -1;
	/* Area under the separability curve */
	private double auc = -1;


	/**
	 * Constructor class
	 * 
	 * @param examples
	 *            Observations labeled as outliers in this solution
	 */
	public IREOSSolution(IREOSExample[] examples, double gammaMax) {
		this.examples = examples;
		this.gammaMax = gammaMax;
	}

	/**
	 * Compute and get IREOS index for this solution
	 * 
	 */
	public double getIREOS() {
		if (indexIREOS == -1)
			indexIREOS = getAUC()/gammaMax;
		return indexIREOS;
	}

	/**
	 * Get area under the separability curve of this solution
	 * 
	 */
	public double getAUC() {
		if (auc == -1){
			double sumWeight = 0;
			auc = 0;
			for (int i = 0; i < examples.length; i++) {
				auc += examples[i].getWeight() * examples[i].getAuc();
				sumWeight += examples[i].getWeight();
			}
			auc /= sumWeight;
		}
			return auc;
	}

}
