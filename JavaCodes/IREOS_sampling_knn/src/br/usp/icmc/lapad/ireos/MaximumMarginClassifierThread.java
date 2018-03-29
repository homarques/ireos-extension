package br.usp.icmc.lapad.ireos;

import java.util.Date;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.logistic.KLR;

public class MaximumMarginClassifierThread extends Thread {
	/* KLR model used to evaluate the separability */
	private SVMExamples model;
	/* Gamma used to evaluated the separability of observation */
	private double[] gammas;
	/* Index in the dataset of the observation that being evaluated */
	private int outlierIndex;
	private long[] runtime;
	private double separability[];
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
	public MaximumMarginClassifierThread(SVMExamples model, double[] gammas, int outlierIndex, int[] knn) {
		if (model != null) {
			this.model = new SVMExamples(model, knn, outlierIndex);
			this.internalOutlierIndex = knn.length;
		} else
			this.model = null;

		this.gammas = gammas;
		this.outlierIndex = outlierIndex;
		separability = new double[gammas.length];
		runtime = new long[gammas.length];
	}

	/**
	 * Evaluate the observation separability
	 */
	@Override
	public void run() {

		for (int l = 0; l < gammas.length; l++) {
			long elapsedTime = 0L;
			long startTime = System.currentTimeMillis();
			SVMExamples new_model = new SVMExamples(model);
			new_model.set_y(internalOutlierIndex, 1);
			SVMExample sv = new SVMExample(new_model.get_example(internalOutlierIndex));
			KernelRadial radial = new KernelRadial();
			KLR klr = new KLR(0.0095, Integer.MAX_VALUE);

			/* Initialize a new radial kernel */
			radial.setGamma(gammas[l]);
			radial.init(model, 1000);

			/*
			 * Initialize a new KLR model, train and get the observation probability to be
			 * outlier
			 */
			klr.init(radial, model);
			klr.train();
			separability[l] = klr.predict(sv);
		    elapsedTime = (new Date()).getTime() - startTime;
		    runtime[l] = elapsedTime;
		}

	}

	/**
	 * Get the gamma used by KLR to evaluate the separability
	 * 
	 */
	public double[] getGamma() {
		return gammas;
	}

	public double[] getSeparability() {
		return separability;
	}

	/**
	 * Get the index in the dataset of the observation that being evaluated
	 */
	public int getOutlierIndex() {
		return outlierIndex;
	}
	
	public long[] getRuntime() {
		return runtime;
	}

	public void setRuntime(long[] runtime) {
		this.runtime = runtime;
	}

}
