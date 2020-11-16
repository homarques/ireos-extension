package br.usp.icmc.lapad.ireos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

public class IREOS {
	/* Maximum clump size */
	private int mCl = 1;

	/* Maximum value of gamma */
	private double gammaMax = 1;

	/* Dataset */
	private SVMExamples dataset;

	/* List of solutions to be evaluated, i.e., outlier scorings */
	private List<double[]> solutions;

	private double tol = 0.05;

	/* Number of threads to be used */
	private int number_of_threads = Runtime.getRuntime().availableProcessors();

	/** Constructor receive the dataset and list of solutions to be evaluated */
	public IREOS(SVMExamples dataset, List<double[]> solutions) {
		this.dataset = dataset;
		this.solutions = solutions;
	}

	/** Constructor receive the dataset and list of solutions to be evaluated */
	public IREOS(SVMExamples dataset, List<double[]> solutions, double tol) {
		this.dataset = dataset;
		this.solutions = solutions;
		this.tol = tol;
	}

	public List<IREOSSolution> evaluateSolutions() throws NumberFormatException, InterruptedException, IOException {
		List<IREOSSolution> evaluatedSolutions = new ArrayList<IREOSSolution>();

		/* Evaluating each solution */
		for (double[] solution : solutions) {
			/* Setting cost weights to outliers */
			SVMExamples data = new SVMExamples(dataset);
			double beta = (double) 1 / getmCl();
			double cs[] = data.get_cs();
			for (int j = 0; j < cs.length; j++) {
				cs[j] = cs[j] * (Math.pow(beta, solution[j]));
			}
			data.set_cs(cs);

			/* Evaluate solution & add to the list */
			IREOSSolution evaluatedSolution = new IREOSSolution(evaluateSolution(data, solution), solution, gammaMax);
			evaluatedSolutions.add(evaluatedSolution);
		}

		return evaluatedSolutions;
	}

	public List<IREOSSolution> evaluateSolutions(int[][] knn)
			throws NumberFormatException, InterruptedException, IOException {
		List<IREOSSolution> evaluatedSolutions = new ArrayList<IREOSSolution>();

		/* Evaluating each solution */
		for (double[] solution : solutions) {
			/* Setting cost weights to outliers */
			SVMExamples data = new SVMExamples(dataset);
			double beta = (double) 1 / getmCl();
			double cs[] = data.get_cs();
			for (int j = 0; j < cs.length; j++) {
				cs[j] = cs[j] * (Math.pow(beta, solution[j]));
			}
			data.set_cs(cs);

			/* Evaluate solution & add to the list */
			IREOSSolution evaluatedSolution = new IREOSSolution(evaluateSolution(data, solution, knn), solution, gammaMax);
			evaluatedSolutions.add(evaluatedSolution);
		}

		return evaluatedSolutions;
	}

	/**
	 * Evaluate a single solution
	 * 
	 * @param data     The dataset
	 * 
	 * @param outliers List of outliers indexes in the dataset
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public IREOSExample[] evaluateSolution(SVMExamples data, double[] solution, int[][] knn) {
		/* Initializing threads */
		FastMaximumMarginClassifier[] threads = new FastMaximumMarginClassifier[number_of_threads];
		for (int i = 0; i < number_of_threads; i++)
			threads[i] = new FastMaximumMarginClassifier(null, -1, -1, -1);

		/* Evaluating separability of each outlier */
		IREOSExample[] evaluatedExamples = new IREOSExample[data.getTrain_size()];
		int i = 0;
		outloop: while (true) {
			/* Evaluating separability of each outlier for each gamma */
			for (int j = 0; j < number_of_threads; j++) {
				evaluatedExamples[i] = new IREOSExample(i);

				if (solution[i] > 0) {
					threads[j] = new FastMaximumMarginClassifier(data, gammaMax, i, tol, knn[i]);
					threads[j].start();
				} else {
					evaluatedExamples[i].setAuc(0);
					j--;
				}

				if (i < data.getTrain_size() - 1)
					i++;
				else
					break outloop;
			}

			/* Waiting for threads to be finalized */
			for (FastMaximumMarginClassifier t : threads) {
				try {
					t.join();
					evaluatedExamples[t.getOutlierIndex()].setAuc(t.getAuc());
					evaluatedExamples[t.getOutlierIndex()].setSeparabilities(t.getSeparabilities());
				} catch (InterruptedException e) {
					System.out.println("Error while waiting for threads that were evaluating the solutions: " + e);
				}
			}

		}
		/* Waiting for threads to be finalized */
		for (FastMaximumMarginClassifier t : threads) {
			try {
				t.join();
				evaluatedExamples[t.getOutlierIndex()].setAuc(t.getAuc());
				evaluatedExamples[t.getOutlierIndex()].setSeparabilities(t.getSeparabilities());
			} catch (InterruptedException e) {
				System.out.println("Error while waiting for threads that were evaluating the solutions: " + e);
			}
		}

		return evaluatedExamples;
	}

	/**
	 * Evaluate a single solution
	 * 
	 * @param data     The dataset
	 * 
	 * @param outliers List of outliers indexes in the dataset
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public IREOSExample[] evaluateSolution(SVMExamples data, double[] solution) {
		/* Initializing threads */
		FastMaximumMarginClassifier[] threads = new FastMaximumMarginClassifier[number_of_threads];
		for (int i = 0; i < number_of_threads; i++)
			threads[i] = new FastMaximumMarginClassifier(null, -1, -1, -1);

		/* Evaluating separability of each outlier */
		IREOSExample[] evaluatedExamples = new IREOSExample[data.getTrain_size()];
		int i = 0;
		outloop: while (true) {
			/* Evaluating separability of each outlier for each gamma */
			for (int j = 0; j < number_of_threads; j++) {
				evaluatedExamples[i] = new IREOSExample(i);

				if (solution[i] > 0) {
					threads[j] = new FastMaximumMarginClassifier(data, gammaMax, i, tol);
					threads[j].start();
				} else {
					evaluatedExamples[i].setAuc(0);
					j--;
				}

				if (i < data.getTrain_size() - 1)
					i++;
				else
					break outloop;
			}

			/* Waiting for threads to be finalized */
			for (FastMaximumMarginClassifier t : threads) {
				try {
					t.join();
					evaluatedExamples[t.getOutlierIndex()].setAuc(t.getAuc());
					evaluatedExamples[t.getOutlierIndex()].setSeparabilities(t.getSeparabilities());
				} catch (InterruptedException e) {
					System.out.println("Error while waiting for threads that were evaluating the solutions: " + e);
				}
			}

		}
		/* Waiting for threads to be finalized */
		for (FastMaximumMarginClassifier t : threads) {
			try {
				t.join();
				evaluatedExamples[t.getOutlierIndex()].setAuc(t.getAuc());
				evaluatedExamples[t.getOutlierIndex()].setSeparabilities(t.getSeparabilities());
			} catch (InterruptedException e) {
				System.out.println("Error while waiting for threads that were evaluating the solutions: " + e);
			}
		}

		return evaluatedExamples;
	}
	
	public double getTol() {
		return tol;
	}

	public void setTol(double tol) {
		this.tol = tol;
	}

	/**
	 * Get the maximum clump size
	 * 
	 */
	public int getmCl() {
		return mCl;
	}

	/**
	 * Set the Maximum clump size
	 * 
	 * @param mCl The maximum clump size
	 * @throws Exception Maximum clump size must be higher than 0
	 */
	public void setmCl(int mCl) throws Exception {
		if (mCl < 1)
			throw new Exception("The maximum clump size (mCl) must be higher than 0");
		else
			this.mCl = mCl;
	}

	/**
	 * Get the maximum value of gamma
	 */
	public double getGammaMax() {
		return gammaMax;
	}

	/**
	 * Set the maximum value of gamma
	 * 
	 * @param gammaMax The maximum value of gamma
	 * @throws Exception The maximum value of gamma must be higher than 0
	 */
	public void setGammaMax(double gammaMax) throws Exception {
		if (gammaMax > 0)
			this.gammaMax = gammaMax;
		else
			throw new Exception("The maximum value of gamma (gammaMax) must be higher than 0");
	}

	/**
	 * Get the dataset
	 */
	public SVMExamples getDataset() {
		return dataset;
	}

	/**
	 * Set the dataset
	 * 
	 * @param dataset The dataset
	 */
	public void setDataset(SVMExamples dataset) {
		this.dataset = dataset;
	}

	/**
	 * Get the number of threads used by the program
	 * 
	 */
	public int getNumber_of_threads() {
		return number_of_threads;
	}

	/**
	 * Set the number of threads used by the program
	 * 
	 * @param number_of_threads The number of threads used by the program
	 */
	public void setNumber_of_threads(int number_of_threads) {
		this.number_of_threads = number_of_threads;
	}

}
