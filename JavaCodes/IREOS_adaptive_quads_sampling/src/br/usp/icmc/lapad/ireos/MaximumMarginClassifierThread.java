package br.usp.icmc.lapad.ireos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.logistic.KLR;

import br.usp.icmc.lapad.MainExample2;

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
	private int cl;
	private int internalOutlierIndex;

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
	public MaximumMarginClassifierThread(SVMExamples model, double gammaMax, int outlierIndex, int cl,
			HashMap<Double, Double> gammaSeparability, int[] knn) {
		if (model != null) {
			this.model = new SVMExamples(model, knn, outlierIndex);
			this.internalOutlierIndex = knn.length;
		} else {
			this.model = null;
		}
		this.gammaMax = gammaMax;
		this.tol = gammaMax - (gammaMax * 0.9999);
		System.out.println(tol);
		this.outlierIndex = outlierIndex;
		this.cl = cl;
		this.gammaSeparability = gammaSeparability;

	}

	/**
	 * Evaluate the observation separability
	 */
	@Override
	public void run() {
		// long startTime = System.currentTimeMillis();
		auc = adaptiveQuads(0, gammaMax, tol);
		System.out.println(auc);
		// String text = new SimpleDateFormat("mm:ss").format((new Date()).getTime() -
		// startTime);
		// System.out.println(text);
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
		double gammas[] = { a, (a + b) / 2, b };
		for (int l = 0; l < gammas.length; l++) {
			if (!gammaSeparability.containsKey(gammas[l])) {
				long elapsedTime = 0L;
				long startTime = System.currentTimeMillis();

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

				gammaSeparability.put(gammas[l], klr.predict(sv));
				elapsedTime = (new Date()).getTime() - startTime;

				FileWriter writer;
				try {
					writer = new FileWriter(new File(MainExample2.DB), true);
					BufferedWriter bufferedWriter = new BufferedWriter(writer);
					bufferedWriter.write(cl + " " + outlierIndex + " " + gammas[l] + " "
							+ gammaSeparability.get(gammas[l]) + " " + elapsedTime + "\n");
					bufferedWriter.flush();
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		double sum = 0;
		sum = ((b - a) / 6)
				* (gammaSeparability.get(a) + 4 * gammaSeparability.get((a + b) / 2) + gammaSeparability.get(b));
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
