package br.usp.icmc.lapad.ireos;

import java.util.HashMap;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.logistic.KLR;

public class MaximumMarginClassifierThread extends Thread {
	/* KLR model used to evaluate the separability */
	private SVMExamples model;
	/* Gamma used to evaluated the separability of observation */
	/* Index in the dataset of the observation that being evaluated */
	private int outlierIndex;
	double gammaMax;
	double tol;
	private HashMap<Double, Double> gammaSeparability = new HashMap<>();
	double auc;

	/**
	 * Constructor class
	 * 
	 * @param model
	 *            The KLR model used to evaluate the separability
	 * @param gamma
	 *            The gamma used by KLR to evaluate the separability
	 * @param outlierIndex
	 *            The index of the observation that will be evaluated
	 */
	public MaximumMarginClassifierThread(SVMExamples model, double gammaMax, int outlierIndex) {
		if (model != null)
			this.model = new SVMExamples(model);
		else
			this.model = null;
		if (outlierIndex > -1) {
			this.model.set_y(outlierIndex, 1);
			this.model.set_cs(outlierIndex, model.getC());
			// System.out.println("."+ outlierIndex);
		}
		this.gammaMax = gammaMax;
		this.tol = 0.001;
		this.outlierIndex = outlierIndex;
	}

	/**
	 * Evaluate the observation separability
	 */
	@Override
	public void run() {
		try {
			auc = adaptiveQuads(0, gammaMax);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public double adaptiveQuads(double a, double b) throws InterruptedException {
		double s1 = simpsonRule(a, b);
		double m = (a + b) / 2;
		double s2 = simpsonRule(a, m) + simpsonRule(m, b);
		double errest = Math.abs(s1 - s2) / 10;

		if (errest > tol * (b - a))
			return adaptiveQuads(a, m) + adaptiveQuads(m, b);
		else
			return s2;
	}

	public double simpsonRule(double a, double b) throws InterruptedException {
		double gammas[] = { a, (a + b) / 2, b };
		Classifier threads[] = new Classifier[3];
		for (int l = 0; l < gammas.length; l++) {
			if (!gammaSeparability.containsKey(gammas[l])) {
				threads[l] = new Classifier(model, gammas[l], outlierIndex);
				threads[l].start();
			}
		}
		
		for (int l = 0; l < gammas.length; l++) {
			if (!gammaSeparability.containsKey(gammas[l])) {
				threads[l].join();
				gammaSeparability.put(gammas[l], threads[l].getP());
			}
		}
		
		double sum = 0;
		sum = ((b-a)/6) * (gammaSeparability.get(a) + 4*gammaSeparability.get((a + b) / 2) + gammaSeparability.get(b));
		return sum;
	}	

	/**
	 * Get the index in the dataset of the observation that being evaluated
	 */
	public int getOutlierIndex() {
		return outlierIndex;
	}

	public HashMap<Double, Double> getGammaSeparability() {
		return gammaSeparability;
	}

	public void setGammaSeparability(HashMap<Double, Double> gammaSeparability) {
		this.gammaSeparability = gammaSeparability;
	}

	public double getAuc() {
		return auc;
	}

	public void setAuc(double auc) {
		this.auc = auc;
	}
	
}
