package br.usp.icmc.lapad.ireos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

import br.usp.icmc.lapad.MainExample2;

public class IREOS {
	public static final int C = 100;

	/* Maximum clump size */
	private int mCl = 1;
	/* Maximum value of gamma */
	private double gammaMax = 1;
	/* Dataset */
	private SVMExamples dataset;
	private HashMap<Double, Double> gammaSeparabilitybyPoint[];

	/*
	 * List of solutions to be evaluated, only the labels: 1 (outlier) and -1
	 * (inlier)
	 */
	private List<Float> weights;

	/* Number of threads to be used */
	private int number_of_threads = 50; // Runtime.getRuntime().availableProcessors()

	/** Constructor receive the dataset and list of solutions to be evaluated */
	public IREOS(SVMExamples dataset, List<Float> weights) {
		this.dataset = dataset;
		this.weights = weights;
		gammaSeparabilitybyPoint = new HashMap[weights.size()];
		for (int i = 0; i < weights.size(); i++) {
			gammaSeparabilitybyPoint[i] = new HashMap<>();
		}
	}

	public IREOS(SVMExamples dataset) {
		this.dataset = dataset;
	}

	public List<IREOSSolution> evaluateSolutions() throws InterruptedException, IOException {
		List<IREOSSolution> evaluatedSolutions = new ArrayList<IREOSSolution>();

		/* Setting cost weights to outliers */
		SVMExamples data = new SVMExamples(dataset);

		/* Evaluate solution & add to the list */
		IREOSSolution evaluatedSolution = new IREOSSolution(evaluateSolution(data, weights), gammaMax);
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
	 * @throws IOException
	 */
	public IREOSExample[] evaluateSolution(SVMExamples data, List<Float> weights)
			throws InterruptedException, IOException {
		/* Evaluating separability of each outlier */
		IREOSExample[] evaluatedExamples = new IREOSExample[weights.size()];
		double beta = (double) 1 / getmCl();
		double cs[] = data.get_cs();
		for (int j = 0; j < cs.length; j++) {
			cs[j] = cs[j] * (Math.pow(beta, weights.get(j)));
		}
		data.set_cs(cs);

		File f = new File(MainExample2.DB);
		if (f.exists() && !f.isDirectory()) {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line = "";
			while ((line = reader.readLine()) != null) {
				String[] splits = line.split(" ");
				try {
					if (Integer.parseInt(splits[0]) == getmCl()) {
						gammaSeparabilitybyPoint[Integer.parseInt(splits[1])].put(Double.parseDouble(splits[2]),
								Double.parseDouble(splits[3]));
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

		MaximumMarginClassifierThread threads[] = new MaximumMarginClassifierThread[number_of_threads];
		for (int i = 0; i < number_of_threads; i++) {
			threads[i] = new MaximumMarginClassifierThread(null, gammaMax, -1, -1, null);
		}
		int i = 0;
		for (; i < weights.size();) {
			for (int t = 0; t < number_of_threads; t++) {
				if (!threads[t].isAlive()) {
					System.out.println(i);
					if (threads[t].getOutlierIndex() != -1) {
						evaluatedExamples[threads[t].getOutlierIndex()].setAuc(threads[t].getAuc());
						evaluatedExamples[threads[t].getOutlierIndex()]
								.setGammaSeparability(threads[t].getGammaSeparability());
					}
					evaluatedExamples[i] = new IREOSExample(i, weights.get(i));
					threads[t] = new MaximumMarginClassifierThread(data, gammaMax, i, mCl, gammaSeparabilitybyPoint[i]);
					threads[t].start();
					i++;
					if (i >= weights.size())
						break;

				}
			}

		}

		for (MaximumMarginClassifierThread maximumMarginClassifierThread : threads) {
			maximumMarginClassifierThread.join();
			evaluatedExamples[maximumMarginClassifierThread.getOutlierIndex()]
					.setAuc(maximumMarginClassifierThread.getAuc());
			evaluatedExamples[maximumMarginClassifierThread.getOutlierIndex()]
					.setGammaSeparability(maximumMarginClassifierThread.getGammaSeparability());
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
			// System.out.println(record.getIndex());
			// System.out.println(record.getGammaSeparability().size());
			for (Double i : record.getGammaSeparability().keySet()) {
				// System.out.println(i);
				String line = record.getWeight() + " " + i + " " + record.getGammaSeparability().get(i) + "\n";
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

}
