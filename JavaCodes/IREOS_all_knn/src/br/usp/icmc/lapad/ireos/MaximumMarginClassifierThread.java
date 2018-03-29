package br.usp.icmc.lapad.ireos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial;
import com.rapidminer.operator.learner.functions.kernel.logistic.KLR;

import br.usp.icmc.lapad.MainExample2;;

public class MaximumMarginClassifierThread extends Thread {
	/* KLR model used to evaluate the separability */
	private SVMExamples model;
	/* Gamma used to evaluated the separability of observation */
	private double[] gammas;
	/* Index in the dataset of the observation that being evaluated */
	private int outlierIndex;
	/* Probability of observation be an outlier according to KLR */
	private double separability[];
	int i;
	private int cl;

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
	public MaximumMarginClassifierThread(SVMExamples model, double[] gammas, int outlierIndex, int i, int cl) {
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
		separability = new double[gammas.length];
		this.i = i;
		this.cl = cl;
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
			if (!IREOS.evaluatedExamples[this.i].isEval(l)) {
				long elapsedTime = 0L;
				long startTime = System.currentTimeMillis();

				SVMExamples new_model = new SVMExamples(model);
				new_model.set_y(outlierIndex, 1);
				SVMExample sv = new SVMExample(new_model.get_example(outlierIndex));

				/* Initialize a new radial kernel */
				KernelRadial radial = new KernelRadial();
				radial.setGamma(gammas[l]);
				radial.init(model, 2500);

				/*
				 * Initialize a new KLR model, train and get the observation probability to be
				 * outlier
				 */
				KLR klr = new KLR(0.0095, Integer.MAX_VALUE);
				klr.init(radial, model);
				klr.train();
				IREOS.evaluatedExamples[this.i].setSeparability(klr.predict(sv), l);
			    elapsedTime = (new Date()).getTime() - startTime;


				FileWriter writer;
				try {
					writer = new FileWriter(new File(MainExample2.DB), true);
					BufferedWriter bufferedWriter = new BufferedWriter(writer);
					bufferedWriter.write(cl + " " + outlierIndex + " " + gammas[l] + " "
								+ IREOS.evaluatedExamples[this.i].getSeparability()[l] + " " + elapsedTime + "\n");
					bufferedWriter.flush();
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Get the gamma used by KLR to evaluate the separability
	 * 
	 */
	public double[] getGammas() {
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

	public int getI() {
		return i;
	}
}
