package br.usp.icmc.lapad.ireos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.logistic.KLR;

public class MaximumMarginClassifier extends Thread {
	/* KLR model used to evaluate the separability */
	private SVMExamples model;
	private SVMExamples grid;
	/* Gamma used to evaluated the separability of observation */
	private double gammas;
	/* Index in the dataset of the observation that being evaluated */
	private int outlierIndex;
	/* Probability of observation be an outlier according to KLR */
	private double separability[];
	int gridsize = 90000;

	/**
	 * Constructor class
	 * 
	 * @param model
	 *            The KLR model used to evaluate the separability
	 * @param gamma
	 *            The gamma used by KLR to evaluate the separability
	 * @param outlierIndex
	 *            The index of the observation that will be evaluated
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public MaximumMarginClassifier(SVMExamples model, double gammas, int outlierIndex)
			throws FileNotFoundException, IOException {
		if (model != null)
			this.model = new SVMExamples(model);
		else
			this.model = null;
		if (outlierIndex > -1) {
			this.model.set_y(outlierIndex, 1);
			this.model.set_cs(outlierIndex, model.getC());
		}
		this.gammas = gammas;
		this.outlierIndex = outlierIndex;

		grid = new SVMExamples(new BufferedReader(new FileReader("/home/henrique/grid")), gridsize, 100);
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
		SVMExamples new_model = new SVMExamples(model);
		new_model.set_y(outlierIndex, 1);

		/* Initialize a new radial kernel */
		KernelRadial radial = new KernelRadial();
		radial.setGamma(gammas);
		radial.init(model, 1000);

		/*
		 * Initialize a new KLR model, train and get the observation probability to be
		 * outlier
		 */
		KLR klr = new KLR(0.0095, Integer.MAX_VALUE);
		klr.init(radial, model);
		klr.train();
		for (int i = 0; i < gridsize; i++) {
			if(klr.predict(grid.get_example(i)) > 0.5)
				System.out.println(-1);
			else
				System.out.println(1);
		}
	}

	/**
	 * Get the gamma used by KLR to evaluate the separability
	 * 
	 */
	public double getGammas() {
		return gammas;
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
	public double[] getSeparability() {
		return separability;
	}

}
