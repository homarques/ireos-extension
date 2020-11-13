package br.usp.icmc.lapad.ireos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.parameter.value.ParameterValueGrid;

public class IREOS {
	public static final int C = 100;

	/* Maximum clump size */
	private int mCl = 1;
	/* Number of values that gamma will be discretized */
	private int nGamma = 10;
	/* Maximum value of gamma */
	private double gammaMax = 1;
	/* Set of gammas from 0 to gammaMax */
	private double[] gammas;
	/* Dataset */
	private SVMExamples dataset;
	private int[][] knn;
	private float[] weights;

	/* Number of threads to be used */
	private int number_of_threads = 50; // Runtime.getRuntime().availableProcessors()

	/** Constructor receive the dataset and list of solutions to be evaluated */
	public IREOS(SVMExamples dataset, float[] weights, int[][] knn) {
		this.dataset = dataset;
		this.weights = weights;
		this.knn = knn;
	}

	public IREOS(SVMExamples dataset) {
		this.dataset = dataset;
	}

	public List<IREOSSolution> evaluateSolutions() throws InterruptedException {
		List<IREOSSolution> evaluatedSolutions = new ArrayList<IREOSSolution>();
		/* Getting outlier indexes */
		List<Integer> outliers = new ArrayList<Integer>();
		for (int i = 0; i < weights.length; i++) {
			outliers.add(i);
		}
		/* Setting cost weights to outliers */
		SVMExamples data = new SVMExamples(dataset);

		/* Evaluate solution & add to the list */
		IREOSSolution evaluatedSolution = new IREOSSolution(evaluateSolution(data, outliers, weights), weights);
		evaluatedSolutions.add(evaluatedSolution);

		return evaluatedSolutions;
	}

	/**
	 * Evaluate a single solution
	 * 
	 * @param data
	 *            The dataset
	 * 
	 * @param outliers
	 *            List of outliers indexes in the dataset
	 * @throws InterruptedException
	 */
	public IREOSExample[] evaluateSolution(SVMExamples data, List<Integer> outliers, float[] weights)
			throws InterruptedException {
		/* Evaluating separability of each outlier */
		IREOSExample[] evaluatedExamples = new IREOSExample[outliers.size()];
		double beta = (double) 1 / getmCl();
		double cs[] = data.get_cs();
		for (int j = 0; j < cs.length; j++) {
			cs[j] = cs[j] * (Math.pow(beta, weights[j]));
		}
		data.set_cs(cs);

		MaximumMarginClassifierThread threads[] = new MaximumMarginClassifierThread[number_of_threads];
		for (int i = 0; i < number_of_threads; i++) {
			threads[i] = new MaximumMarginClassifierThread(null, gammas, -1, null);
		}
		int i = 0;
		for (; i < outliers.size();) {
			for (int t = 0; t < number_of_threads; t++) {
				if (!threads[t].isAlive()) {
					if (threads[t].getOutlierIndex() != -1) {
						evaluatedExamples[threads[t].getOutlierIndex()].setSeparability(threads[t].getSeparability());
						evaluatedExamples[threads[t].getOutlierIndex()].setRuntime(threads[t].getRuntime());
					}
					System.out.println(i);
					int outlier = outliers.get(i);
					evaluatedExamples[i] = new IREOSExample(outlier, gammas);
					threads[t] = new MaximumMarginClassifierThread(data, gammas, outlier, knn[outlier]);
					threads[t].start();
					i++;
					if (i >= outliers.size())
						break;
				}
			}
		}

		for (MaximumMarginClassifierThread maximumMarginClassifierThread : threads) {
			maximumMarginClassifierThread.join();
			if (maximumMarginClassifierThread.getOutlierIndex() != -1) {
				evaluatedExamples[maximumMarginClassifierThread.getOutlierIndex()]
						.setSeparability(maximumMarginClassifierThread.getSeparability());
				evaluatedExamples[maximumMarginClassifierThread.getOutlierIndex()]
						.setRuntime(maximumMarginClassifierThread.getRuntime());
			}
		}

		return evaluatedExamples;
	}

	/**
	 * Save the solution, it includes the IREOS index, the separability of each
	 * outlier in the solution for each gamma and if the statistics are computed
	 * also store the Adjusted IREOS and the p-value to z-test and t-test
	 * 
	 * @param file
	 *            Path of the file where the result will be stored
	 * @param solution
	 *            Solution that will be stored
	 */
	public void saveResult(String file, IREOSSolution solution) throws Exception {
		FileWriter writer = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		bufferedWriter.write("# IREOS " + solution.getIREOS() + "\n");
		for (IREOSExample record : solution.examples) {
			bufferedWriter.write("# " + record.getIndex() + "\n");
			for (int i = 0; i < record.getGammas().length; i++) {
				String line = solution.getWeights()[record.getIndex()] + " " + record.getGammas()[i] + " "
						+ record.getSeparability()[i] + " " + record.getRuntime()[i] + "\n";
				bufferedWriter.write(line);
			}
		}
		bufferedWriter.flush();
		bufferedWriter.close();
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
	 * @param mCl
	 *            The maximum clump size
	 * @throws Exception
	 *             Maximum clump size must be higher than 0
	 */
	public void setmCl(int mCl) throws Exception {
		this.mCl = mCl;
	}

	/**
	 * Get the number of values that gamma will be discretized
	 */
	public int getnGamma() {
		return nGamma;
	}

	/**
	 * Set number of values that gamma will be discretized
	 * 
	 * @param nGamma
	 *            The number of values that gamma will be discretized
	 * @throws Exception
	 *             The of values that gamma will be discretized must be higher than
	 *             0
	 */
	public void setnGamma(int nGamma) throws Exception {
		if (nGamma > 0)
			this.nGamma = nGamma;
		else
			throw new Exception("Number of values to discretize gamma (nGamma) must be higher than 0");
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
	 * @param gammaMax
	 *            The maximum value of gamma
	 * @throws Exception
	 *             The maximum value of gamma must be higher than 0
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
	 * @param dataset
	 *            The dataset
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
	 * @param number_of_threads
	 *            The number of threads used by the program
	 */
	public void setNumber_of_threads(int number_of_threads) {
		this.number_of_threads = number_of_threads;
	}

	/**
	 * Get the set of gammas used to evaluate the separability
	 */
	public double[] getGammas() {
		return gammas;
	}

	/**
	 * Set the set of gammas used to evaluate the separability
	 * 
	 * @param gammas
	 *            The set of gammas from 0 to gammaMax
	 */
	public void setGammas(double[] gammas) {
		this.gammas = gammas;
	}

	/**
	 * Discretize gamma from 0 to gammaMax into nGamma values, the discretization
	 * can be linear, quadratic, logarithmic or logarithmic (legacy)
	 * 
	 * @param scale
	 *            The discretization to be used
	 */
	public void setGammas(int scale) throws Exception {
		ParameterValueGrid valueGridGammas = new ParameterValueGrid("0", gammaMax + "", (nGamma - 1) + "", scale);
		gammas = valueGridGammas.getValues();
	}

}