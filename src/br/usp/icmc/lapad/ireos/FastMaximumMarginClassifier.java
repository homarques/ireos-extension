package br.usp.icmc.lapad.ireos;

import java.util.HashMap;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.logistic.KLR;

public class FastMaximumMarginClassifier extends Thread {
	/* KLR model used to evaluate the separability */
	private SVMExamples model;

	/* Gamma used to evaluated the separability of observation */
	private double gammaMax;

	/* Index in the dataset of the observation that being evaluated */
	private int outlierIndex;

	private double tol;

	private HashMap<Double, Double> separabilities = new HashMap<>();

	private double auc;

	private int internalOutlierIndex;

	/**
	 * Exact IREOS
	 * 
	 * @param model        The KLR model used to evaluate the separability
	 * @param gammaMax        The gamma_max used by KLR to evaluate the separability
	 * @param outlierIndex The index of the observation that will be evaluated
	 * @param tol tolerance error
	 */
	public FastMaximumMarginClassifier(SVMExamples model, double gammaMax, int outlierIndex, double tol) {
		if (model != null)
			this.model = new SVMExamples(model);
		else
			this.model = null;
		if (outlierIndex > -1) {
			this.model.set_y(outlierIndex, 1);
			this.model.set_cs(outlierIndex, model.getC());
		}
		this.gammaMax = gammaMax;
		this.tol = tol;
		this.outlierIndex = outlierIndex;
		internalOutlierIndex = outlierIndex;
		separabilities = new HashMap<>();
	}

	/**
	 * Approximate IREOS
	 * 
	 * @param model        The KLR model used to evaluate the separability
	 * @param gammaMax        The gamma_max used by KLR to evaluate the separability
	 * @param outlierIndex The index of the observation that will be evaluated
	 * @param tol tolerance error
	 * @param knn The k nearest neighbors of the object
	 */
	public FastMaximumMarginClassifier(SVMExamples model, double gammaMax, int outlierIndex, double tol, int[] knn) {
		if (model != null) {
			this.model = new SVMExamples(model, knn, outlierIndex);
			internalOutlierIndex = knn.length;
		} else {
			this.model = null;
		}
		if (outlierIndex > -1) {
			this.model.set_y(internalOutlierIndex, 1);
			this.model.set_cs(internalOutlierIndex, model.getC());
		}

		this.gammaMax = gammaMax;
		this.tol = tol;
		this.outlierIndex = outlierIndex;
		separabilities = new HashMap<>();
	}

	/**
	 * Evaluate the observation separability
	 */
	@Override
	public void run() {
		auc = adaptiveQuads(0, gammaMax, tol);
	}

	public double adaptiveQuads(double a, double b, double tol) {
		double s1 = simpsonRule(a, b);
		double m = (a + b) / 2;
		double s2 = simpsonRule(a, m) + simpsonRule(m, b);
		double errest = Math.abs(s1 - s2) / 15;

		if (errest > tol)
			return adaptiveQuads(a, m, tol / 2) + adaptiveQuads(m, b, tol / 2);
		else
			return s2;
	}

	public double simpsonRule(double a, double b) {
		double h = (b - a) / 2;
		double gammas[] = { a, a + h, b };
		for (int l = 0; l < gammas.length; l++) {
			if (separabilities.containsKey(gammas[l]))
				continue;

			SVMExamples new_model = new SVMExamples(model);
			new_model.set_y(internalOutlierIndex, 1);
			SVMExample sv = new SVMExample(new_model.get_example(internalOutlierIndex));

			/* Initialize a new radial kernel */
			KernelRadial radial = new KernelRadial();
			radial.setGamma(gammas[l]);
			radial.init(model, 1000);

			/*
			 * Initialize a new KLR model, train and get the observation probability to be
			 * outlier
			 */
			KLR klr = new KLR(0.0095, Integer.MAX_VALUE);
			klr.init(radial, model);

			klr.train();

			separabilities.put(gammas[l], klr.predict(sv));
		}

		double sum = 0;
		sum = (h / 3) * (separabilities.get(a) + 4 * separabilities.get(a + h) + separabilities.get(b));
		return sum;
	}

	/**
	 * Get the index in the dataset of the observation that being evaluated
	 */
	public int getOutlierIndex() {
		return outlierIndex;
	}

	public HashMap<Double, Double> getSeparabilities() {
		return separabilities;
	}

	public void setSeparabilities(HashMap<Double, Double> separabilities) {
		this.separabilities = separabilities;
	}

	public double getAuc() {
		return auc;
	}

	public void setAuc(double auc) {
		this.auc = auc;
	}

}
