package br.usp.icmc.lapad.ireos;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.logistic.KLR;

public class MaximumMarginClassifier extends Thread {
	/* KLR model used to evaluate the separability */
	private SVMExamples model;
	/* Gamma used to evaluated the separability of observation */
	private double gamma;
	/* Index in the dataset of the observation that being evaluated */
	private int outlierIndex;
	/* Probability of observation be an outlier according to KLR */
	private double p;

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
	public MaximumMarginClassifier(SVMExamples model, double gamma,
			int outlierIndex) {
		if (model != null)
			this.model = new SVMExamples(model);
		else
			this.model = null;
		if (outlierIndex > -1) {
			this.model.set_y(outlierIndex, 1);
			this.model.set_cs(outlierIndex, model.getC());
		}
		this.gamma = gamma;
		this.outlierIndex = outlierIndex;
		this.p = -1;
	}

	/**
	 * Evaluate the observation separability
	 */
	@Override
	public void run() {
		/*
		 * Initialize a new model, set the observation that being evaluated as
		 * outlier (1) and copy this observation to be evaluated according to
		 * the KLR model trained
		 */
		SVMExamples new_model = new SVMExamples(model);
		new_model.set_y(outlierIndex, 1);
		SVMExample sv = new SVMExample(new_model.get_example(outlierIndex));

		/* Initialize a new radial kernel */
		KernelRadial radial = new KernelRadial();
		radial.setGamma(gamma);
		radial.init(model, 1000);

		/*
		 * Initialize a new KLR model, train and get the observation probability
		 * to be outlier
		 */
		KLR klr = new KLR(0.0095, Integer.MAX_VALUE);
		klr.init(radial, model);
		klr.train();
		p = klr.predict(sv);
	}

	/**
	 * Get the gamma used by KLR to evaluate the separability
	 * 
	 */
	public double getGamma() {
		return gamma;
	}

	/**
	 * Get the index in the dataset of the observation that being evaluated
	 */
	public int getOutlierIndex() {
		return outlierIndex;
	}

	/**
	 * Get the probability of observation be an outlier according to KLR
	 * 
	 */
	public double getP() {
		return p;
	}
}
