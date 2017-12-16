package br.usp.icmc.lapad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import br.usp.icmc.lapad.ireos.IREOS;
import br.usp.icmc.lapad.ireos.IREOSSolution;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.parameter.value.ParameterValueGrid;

public class MainExample2 {
	private static final String DATA = "/home/hom/ireos_extension/Datasets/Real/data/";
	private static final String LABELS = "/home/hom/ireos_extension/Datasets/Real/solutions/";
	private static final String GAMMA = "/home/hom/ireos_extension/Datasets/Real/GM/";
	private static final String SAVE = "/home/hom/ireos_extension/Datasets/Real/results/cl1/";

	/* 0 - trapezoide; 1 - media */
	private static final int STRATEGY = 0;

	/* 7 - log2; 8 - logn; 9 - log10 */
	private static final int discretization = ParameterValueGrid.SCALE_LOGARITHMIC_LEGACY;

	private static final char clumps = '1';

	public static void main(String[] args) throws Exception {
		String data = args[0];
		System.out.println(data);
		List<int[]> ireosSolutions = new ArrayList<>();
		List<int[]> ireosnoSolutions = new ArrayList<>();

		List<int[]> rankings = new ArrayList<>();
		List<float[]> weights = new ArrayList<>();

		/* List all the solutions in the folder */
		File solution = new File(LABELS + data + "/" + args[1]);

		/* Count the number of observations in the dataset */
		BufferedReader reader = new BufferedReader(new FileReader(solution));
		// BufferedReader weightReader;

		int datasetSize = 0;
		while (reader.readLine() != null) {
			datasetSize++;
		}

		/*
		 * Read all the solutions from the files and add in the list of vector
		 */
		int outlierID = 0;
		String line = null;

		reader = new BufferedReader(new FileReader(solution));
		int detection[] = new int[datasetSize];
		int nodetection[] = new int[datasetSize];
		int ranking[] = new int[datasetSize];
		float weight[] = new float[datasetSize];

		outlierID = 0;
		while ((line = reader.readLine()) != null) {
			ranking[outlierID] = Integer.parseInt(line);
			weight[outlierID] = Float.parseFloat(line);
			nodetection[outlierID] = 1;

			if (Integer.parseInt(line) >= datasetSize) {
				detection[outlierID] = 1;
			} else {
				detection[outlierID] = -1;
			}
			outlierID++;
		}
		ireosnoSolutions.add(nodetection);
		ireosSolutions.add(detection);
		rankings.add(ranking);
		weights.add(weight);
		reader.close();

		/* Create dataset model */
		SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader(DATA + data)), datasetSize, IREOS.C);

		/*
		 * Initialize IREOS using the dataset and the solutions to be evaluated
		 */
		IREOS ireos = new IREOS(dataset, ireosSolutions, weights, rankings, STRATEGY);

		/* Find the gamma maximum */
		BufferedReader readerMax = new BufferedReader(new FileReader(GAMMA + data));
		double max = Double.parseDouble(readerMax.readLine());

		ireos.setGammaMax(max);

		/* Set the number of values that gamma will be discretized */
		ireos.setnGamma(100);
		ireos.setSolutions(ireosnoSolutions);

		ireos.setGammas(discretization);

		if (clumps == '1')
			ireos.setmCl(1);

		// if (clumps == 'n')
		// ireos.setmCl(n);

		/* Evaluate the solutions */
		List<IREOSSolution> evaluatedSolutions = ireos.evaluateSolutions();
		/* Print the results */
		for (int i = 0; i < evaluatedSolutions.size(); i++) {
			/* Set the IREOS statistics to the solution */
			System.out.println("Solution: " + solution);
			System.out.println("IREOS: " + evaluatedSolutions.get(i).getIREOS());
			File theDir = new File(SAVE + args[0]);
			theDir.mkdir();

			ireos.saveResult(SAVE + args[0] + "/" + args[1], evaluatedSolutions.get(i));
		}
	}
}