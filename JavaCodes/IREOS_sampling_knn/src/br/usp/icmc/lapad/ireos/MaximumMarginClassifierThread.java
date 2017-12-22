package br.usp.icmc.lapad.ireos;

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
	public MaximumMarginClassifierThread(SVMExamples model, double[] gammas, int outlierIndex, int [] knn) {
		if (model != null) {
			this.model = new SVMExamples(model, knn, outlierIndex);
			this.internalOutlierIndex = knn.length;
		}else
			this.model = null;
		/*if (outlierIndex > -1) {
			this.model.set_y(outlierIndex, 1);
			this.model.set_cs(outlierIndex, model.getC());
			// System.out.println("."+ outlierIndex);
		}*/
		this.gammas = gammas;
		this.outlierIndex = outlierIndex;
		separability = new double[gammas.length];
	}

	/**
	 * Evaluate the observation separability
	 */
	@Override
	public void run() {
		/*
		 * Initialize a new model, set the observation that being evaluated as outlier
		 * (1) and copy this observation to be evaluated according to the KLR model
		 * trained
		 */

		int l;
		for (l = 0; l < gammas.length; l++) {
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
			//System.out.println("gamma: " + l);
			//for(double d : klr.getAlphas()) {
			//	System.out.println(d);
			//}
			separability[l] = klr.predict(sv);

			if (separability[l] == 1)
				break;
		}

		for (; l < gammas.length; l++)
			separability[l] = 1;

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

}
