package br.usp.icmc.lapad.ireos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.parameter.value.ParameterValueGrid;

public class IREOS {
	public static final int C = 100;

	/* Maximum clump size */
	private int mCl = 1;
	private int n;
	/* Number of values that gamma will be discretized */
	private int nGamma = 10;
	/* Maximum value of gamma */
	private double gammaMax = 1;
	/* Set of gammas from 0 to gammaMax */
	private double[] gammas;
	/* Dataset */
	private SVMExamples dataset;

	private IREOSkNN distances[];

	private int strategy;

	/*
	 * List of solutions to be evaluated, only the labels: 1 (outlier) and -1
	 * (inlier)
	 */
	private List<int[]> rankings = new ArrayList<int[]>();
	private List<int[]> solutions = new ArrayList<int[]>();
	private List<float[]> weights = new ArrayList<float[]>();

	/* Number of threads to be used */
	private int number_of_threads = 1; // Runtime.getRuntime().availableProcessors()

	/** Constructor receive the dataset and list of solutions to be evaluated */
	public IREOS(SVMExamples dataset, List<int[]> solutions, List<float[]> weights, List<int[]> rankings,
			int strategy) {
		this.dataset = dataset;
		this.rankings = rankings;
		this.solutions = solutions;
		this.weights = weights;
		this.strategy = strategy;
	}

	public IREOS(SVMExamples dataset, List<int[]> solutions) {
		this.dataset = dataset;
		this.solutions = solutions;
	}

	public List<IREOSSolution> evaluateSolutions() {
		List<IREOSSolution> evaluatedSolutions = new ArrayList<IREOSSolution>();
		/* Evaluating each solution */
		int l = 0;
		for (int[] solution : solutions) {
			/* Getting outlier indexes */
			List<Integer> outliers = new ArrayList<Integer>();
			for (int i = 0; i < solution.length; i++) {
				if (solution[i] > 0)
					outliers.add(i);
			}
			/* Setting cost weights to outliers */
			SVMExamples data = new SVMExamples(dataset);

			/* Evaluate solution & add to the list */
			IREOSSolution evaluatedSolution = new IREOSSolution(evaluateSolution(data, outliers, rankings.get(l)), n,
					weights.get(l), strategy);
			evaluatedSolutions.add(evaluatedSolution);
			l++;
		}
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
	 */
	public IREOSExample[] evaluateSolution(SVMExamples data, List<Integer> outliers, int[] ranking) {
		/* Evaluating separability of each outlier */
		IREOSExample[] evaluatedExamples = new IREOSExample[outliers.size()];
		double beta = (double) 1 / getmCl();
		double cs[] = data.get_cs();
		for (int j = 0; j < cs.length; j++) {
			cs[j] = cs[j] * (Math.pow(beta, 1 - (double) ranking[j] / cs.length));
		}
		data.set_cs(cs);

		for (int i = 0; i < outliers.size(); i++) {
			System.out.println(i);
			int outlier = outliers.get(i);
			int l;
			evaluatedExamples[i] = new IREOSExample(outlier, gammas);
			/* Evaluating separability of each outlier for each gamma */
			/* Initializing threads */
			MaximumMarginClassifier threads;

			for (l = 0; l < gammas.length; l++) {
				threads = new MaximumMarginClassifier(data, gammas[l], outlier);
				threads.run();
				evaluatedExamples[i].setSeparability(threads.getP(), l);

				if (threads.getP() == 1)
					break;
			}

			for (; l < gammas.length; l++)
				evaluatedExamples[i].setSeparability(1, Arrays.binarySearch(gammas, gammas[l]));
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
		if (solution.getStatistics() != null)
			bufferedWriter.write("# IREOS " + solution.getIREOS() + " AdjIREOS: " + solution.getAdjustedIREOS()
					+ " z-test: " + solution.zTest() + " t-test: " + solution.tTest() + " Mean: "
					+ solution.getStatistics().getExpectedValue() + " Var: "
					+ solution.getStatistics().getVariance(solution.getn()) + " n: " + solution.getn() + "\n");
		else
			bufferedWriter.write("# IREOS " + solution.getIREOS() + "\n");
		for (IREOSExample record : solution.examples) {
			bufferedWriter.write("# " + record.getIndex() + "\n");
			for (int i = 0; i < record.getGammas().length; i++) {
				String line = solution.getWeights()[record.getIndex()] + " " + record.getGammas()[i] + " "
						+ record.getSeparability()[i] + "\n";
				bufferedWriter.write(line);
			}
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}

	/**
	 * Save the solution, it includes the expected value, variance and the
	 * separability of each observation in the dataset for each gamma
	 * 
	 * @param file
	 *            Path of the file where the statistics will be stored
	 * @param statistics
	 *            Statistics that will be stored
	 */
	public void saveStatistics(String file, IREOSStatistics statistics, int n) throws Exception {
		FileWriter writer = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		bufferedWriter.write(
				"# Expected Value " + statistics.getExpectedValue() + " Variance: " + statistics.getVariance(n) + "\n");

		for (IREOSExample record : statistics.examples) {
			bufferedWriter.write("# " + record.getIndex() + "\n");
			for (int i = 0; i < record.getGammas().length; i++) {
				String line = record.getGammas()[i] + " " + record.getSeparability()[i] + "\n";
				bufferedWriter.write(line);
			}
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}
	
	/**
	 * Look for the maximum gamma by increasing gradually gamma until all the
	 * outliers are separable using the default increasing
	 */
	public void findGammaMax() throws Exception {
		findGammaMax(1.1f);
	}

	/**
	 * Look for the maximum gamma by increasing gradually gamma until all the
	 * outliers are separable
	 * 
	 * @param rateofIncreaseGammaMax
	 *            Rate that gamma will be gradually increased
	 * @throws Exception
	 *             Rate of increasing must be higher than 1
	 */
	public void findGammaMax(double rateofIncreaseGammaMax) throws Exception {
		if (rateofIncreaseGammaMax > 1) {
			/* Add all the outliers in a list */
			List<Integer> outliers = new ArrayList<Integer>();
			for (int[] solution : solutions) {
				for (int i = 0; i < solution.length; i++) {
					if (solution[i] > 0) {
						if (!outliers.contains(i))
							outliers.add(i);
					}
				}
			}
			/* Initial gamma based on the data dimensionality */
			gammaMax = (double) 1 / (dataset.get_dim() * 1000);
			/* Initialize a maximum margin classifier per thread */
			MaximumMarginClassifierThread[] threads = new MaximumMarginClassifierThread[number_of_threads];
			for (int i = 0; i < number_of_threads; i++)
				threads[i] = new MaximumMarginClassifierThread(null, -1, -1);
			/*
			 * Verify if the outlier is separable using the actual gamma
			 * maximum, if so, the outlier is removed from the list, otherwise
			 * the gamma maximum is increased, this procedure is repeated until
			 * the list is empty
			 */
			while (!outliers.isEmpty()) {
				int listOutlierIndex = 0;
				for (int i = 0; (i < number_of_threads)
						&& (i < outliers.size()); i++) {
					if (!threads[i].isAlive()) {
						if (threads[i].getP() > 0.5) {
							outliers.remove(new Integer(threads[i]
									.getOutlierIndex()));
							if (outliers.isEmpty())
								break;
						} else {
							if (threads[i].getGamma() == gammaMax)
								gammaMax = gammaMax * rateofIncreaseGammaMax;
						}
						threads[i] = new MaximumMarginClassifierThread(dataset,
								gammaMax, outliers.get(listOutlierIndex));
						threads[i].start();
						listOutlierIndex++;
					}
				}
			}
			/* Wait for until all threads are finalized */
			for (Thread t : threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					System.out
							.println("Error while waiting for threads that were looking for maximum gamma: "
									+ e);
				}
			}
		} else
			throw new Exception(
					"The rate of increase the maximum value of gamma (rateofIncreaseGammaMax) must be higher than 1");

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
		/*
		 * if (mCl <= 0) throw new Exception(
		 * "The maximum clump size (mCl) must be higher than 0"); else { if (mCl >
		 * getn()) System.err .println(
		 * "The maximum clump size (mCl) should not be higher than the number of outliers"
		 * );
		 */
		this.mCl = mCl;
		// }
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
	 * Get the list of solutions to be evaluated
	 * 
	 */
	public List<int[]> getSolutions() {
		return solutions;
	}

	/**
	 * Set the list of solutions to be evaluated, only the labels: 1 (outlier) and
	 * -1 (inlier)
	 * 
	 * @param solutions
	 *            The list of solutions to be evaluated, each list element must be a
	 *            vector that represent if the correspondent dataset element is an
	 *            outlier (1) or an inlier (-1)
	 */
	public void setSolutions(List<int[]> solutions) {
		this.solutions = solutions;
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
	 * Get the set of gammas used to evaluate the separability
	 */
	public int getn() {
		return n;
	}

	/**
	 * Set the set of gammas used to evaluate the separability
	 * 
	 * @param gammas
	 *            The set of gammas from 0 to gammaMax
	 */
	public void setn(int n) {
		this.n = n;
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
