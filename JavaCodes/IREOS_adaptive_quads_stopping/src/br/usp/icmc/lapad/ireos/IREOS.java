package br.usp.icmc.lapad.ireos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

public class IREOS {
	public static final int C = 100;

	/* Maximum clump size */
	private int mCl = 1;
	/* Maximum value of gamma */
	private double gammaMax = 1;
	/* Dataset */
	private SVMExamples dataset;
	public List<IREOSSolution> evaluatedSolutions;
	private String save;

	/*
	 * List of solutions to be evaluated, only the labels: 1 (outlier) and -1
	 * (inlier)
	 */
	private List<float[]> weights;
	private List<int[]> sorted_rank = new ArrayList<int[]>();

	/* Number of threads to be used */
	private int number_of_threads = 10; // Runtime.getRuntime().availableProcessors()

	/** Constructor receive the dataset and list of solutions to be evaluated */
	public IREOS(SVMExamples dataset, List<float[]> weights) {
		this.dataset = dataset;
		this.weights = weights;

		for (int i = 0; i < weights.size(); i++) {
			LinkedHashMap<Integer, Float> temp = new LinkedHashMap<>();
			for (int j = 0; j < weights.get(i).length; j++) {
				temp.put(j, weights.get(i)[j]);
			}

			int[] st = sortByValues(temp);
			sorted_rank.add(st);
		}

	}

	private static int[] sortByValues(HashMap map) {
		List list = new LinkedList(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		int[] sortedHashMap = new int[list.size()];
		int i = 0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap[i] = (int) entry.getKey();
			i++;
		}

		return sortedHashMap;
	}

	public IREOS(SVMExamples dataset) {
		this.dataset = dataset;
	}

	public void evaluateSolutionsbyRow() throws Exception {
		evaluatedSolutions = new ArrayList<IREOSSolution>();
		SVMExamples data[] = new SVMExamples[weights.size()];
		double beta = (double) 1 / getmCl();
		for (int j = 0; j < weights.size(); j++) {
			/* Setting cost weights to outliers */
			data[j] = new SVMExamples(dataset);
			double cs[] = data[j].get_cs();
			for (int l = 0; l < cs.length; l++) {
				cs[l] = cs[l] * (Math.pow(beta, weights.get(j)[l]));
			}
			data[j].set_cs(cs);

			IREOSExample[] examples = new IREOSExample[dataset.getTrain_size()];
			for (int l = 0; l < dataset.getTrain_size(); l++) {
				examples[l] = new IREOSExample(l, weights.get(j)[l], gammaMax);
			}
			IREOSSolution evaluatedSolution = new IREOSSolution(examples, gammaMax, sorted_rank.get(j), j);
			evaluatedSolutions.add(evaluatedSolution);
		}

		int i = 0;
		while (evaluatedSolutions.size() > 1) {
			MaximumMarginClassifierThread[] threads = new MaximumMarginClassifierThread[number_of_threads];
			for (int l = 0; l < number_of_threads; l++) {
				threads[l] = new MaximumMarginClassifierThread(null, gammaMax, -1);
			}
			System.out.println(i);
			for (int t = 0; t < number_of_threads; t++) {
				// if (weights.get(evaluatedSolutions.get(t).getIndex())[sorted_rank.get(t)[i]]
				// > 0) {
				if (evaluatedSolutions.get(t).examples[sorted_rank.get(evaluatedSolutions.get(t).getIndex())[i]]
						.getWeight() > 0) {
					threads[t] = new MaximumMarginClassifierThread(data[(evaluatedSolutions.get(t).getIndex())],
							gammaMax, sorted_rank.get(evaluatedSolutions.get(t).getIndex())[i]);

					threads[t].start();
				}
			}

			for (int t = 0; t < number_of_threads; t++) {
				threads[t].join();
				evaluatedSolutions.get(t).examples[sorted_rank.get(evaluatedSolutions.get(t).getIndex())[i]]
						.setAuc(threads[t].getAuc());
				evaluatedSolutions.get(t).examples[sorted_rank.get(evaluatedSolutions.get(t).getIndex())[i]]
						.setGammaSeparability(threads[t].getGammaSeparability());
				evaluatedSolutions.get(t).setTp(evaluatedSolutions.get(t).getAUC(dataset.getTrain_size() - 1));
				evaluatedSolutions.get(t).setTc(evaluatedSolutions.get(t).getAUC(i));
			}

			Collections.sort(evaluatedSolutions);
			/*for (int t = 0; t < number_of_threads; t++)
				System.out.println((evaluatedSolutions.get(t).getIndex() + 1) + " " + evaluatedSolutions.get(t).getTc()
						+ " " + evaluatedSolutions.get(t).getTp());*/

			// Solucao nao pode ser ultrapassada e nem pode ultrapassar nenhuma outra
			// tp

			int j = 0;
			while (j < evaluatedSolutions.size()) {
				boolean flagA = true;
				boolean flagB = true;
				for (int l = 0; l < j; l++) {
					if (evaluatedSolutions.get(j).tc < evaluatedSolutions.get(l).tp) {
						flagA = false;
						break;
					}
				}

				for (int l = j + 1; l < evaluatedSolutions.size(); l++) {
					if (evaluatedSolutions.get(j).tp > evaluatedSolutions.get(l).tc) {
						flagB = false;
						break;
					}
				}
				if (flagA && flagB) {
					System.out.println("drop: " + i + " sol." + (evaluatedSolutions.get(j).getIndex() + 1) + " I: "
							+ evaluatedSolutions.get(j).getTp());
					saveResult(save + "/" + (evaluatedSolutions.get(j).getIndex() + 1), evaluatedSolutions.get(j));

					evaluatedSolutions.remove(j);
					number_of_threads--;
				} else {
					j++;
				}
			}

			for (j = 0; j < evaluatedSolutions.size(); j++) {
				evaluatedSolutions.get(j).setTc(evaluatedSolutions.get(j).getIndex());
			}
			Collections.sort(evaluatedSolutions);

			i++;
		}

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
	public IREOSExample[] evaluateSolution(SVMExamples data, List<Float> weights) throws InterruptedException {
		/* Evaluating separability of each outlier */
		IREOSExample[] evaluatedExamples = new IREOSExample[weights.size()];
		double beta = (double) 1 / getmCl();
		double cs[] = data.get_cs();
		for (int j = 0; j < cs.length; j++) {
			cs[j] = cs[j] * (Math.pow(beta, weights.get(j)));
		}
		data.set_cs(cs);

		MaximumMarginClassifierThread threads[] = new MaximumMarginClassifierThread[number_of_threads];
		for (int i = 0; i < number_of_threads; i++) {
			threads[i] = new MaximumMarginClassifierThread(null, gammaMax, -1);
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
					evaluatedExamples[i] = new IREOSExample(i, weights.get(i), gammaMax);
					threads[t] = new MaximumMarginClassifierThread(data, gammaMax, i);
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
		// bufferedWriter.write("# IREOS " + solution.getIREOS() + "\n");
		for (IREOSExample record : solution.examples) {
			if (!record.getGammaSeparability().isEmpty()) {
				bufferedWriter.write("# " + record.getIndex() + "\n");
				// System.out.println(record.getIndex());
				// System.out.println(record.getGammaSeparability().size());
				for (Double i : record.getGammaSeparability().keySet()) {
					// System.out.println(i);
					String line = record.getWeight() + " " + i + " " + record.getGammaSeparability().get(i) + "\n";
					bufferedWriter.write(line);
				}
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

	public String getSave() {
		return save;
	}

	public void setSave(String save) {
		this.save = save;
	}

}
