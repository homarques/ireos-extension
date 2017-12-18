package br.usp.icmc.lapad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import br.usp.icmc.lapad.ireos.IREOS;
import br.usp.icmc.lapad.ireos.IREOSSolution;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.parameter.value.ParameterValueGrid;

public class MainExample2 {
	private static final String DATA = "/home/hom/ireos_extension/Datasets/Real/data/";
	private static final String WEIGHTS = "/home/hom/ireos_extension/Datasets/Real/weight/normalized_scores/";
	private static final String GAMMA = "/home/hom/ireos_extension/Datasets/Real/GM/";
	private static final String SAVE = "/home/hom/ireos_extension/Datasets/Real/results/cl1/";

	/* 0 - trapezoide; 1 - media */
	private static final int STRATEGY = 0;

	/* 7 - log2; 8 - logn; 9 - log10 */
	private static final int discretization = ParameterValueGrid.SCALE_LOGARITHMIC_LEGACY;

	private static final char clumps = '1';

	public static void main(String[] args) throws Exception {
		//String args [] = {"Vowel", "10", "10"};
		String data = args[0];
		int n = Integer.parseInt(args[2]);
		System.out.println(data);

		/* Count the number of observations in the dataset */
		BufferedReader weightReader;
		weightReader = new BufferedReader(new FileReader(new File(WEIGHTS + data + "/" + args[1])));

		int datasetSize = 0;
		while (weightReader.readLine() != null) {
			datasetSize++;
		}
		weightReader.close();
		
		/*
		 * Read all the solutions from the files and add in the list of vector
		 */
		int outlierID = 0;
		String line = null;

		weightReader = new BufferedReader(new FileReader(new File(WEIGHTS + data + "/" + args[1])));
		int detection[] = new int[datasetSize];
		float weights[] = new float[datasetSize];

		outlierID = 0;
		while ((line = weightReader.readLine()) != null) {
			weights[outlierID] = Float.parseFloat(line);
			if (Float.parseFloat(line) >= 0) {
				detection[outlierID] = 1;
			} else {
				detection[outlierID] = -1;
			}
			outlierID++;
		}
		weightReader.close();
		
		/* Create dataset model */
		SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader(DATA + data)), datasetSize, IREOS.C);

		/*
		 * Initialize IREOS using the dataset and the solutions to be evaluated
		 */
		IREOS ireos = new IREOS(dataset, detection, weights, STRATEGY);

		/* Find the gamma maximum */
		BufferedReader readerMax = new BufferedReader(new FileReader(GAMMA + data));
		double max = Double.parseDouble(readerMax.readLine());
		readerMax.close();
		ireos.setGammaMax(max);

		/* Set the number of values that gamma will be discretized */
		ireos.setnGamma(100);

		ireos.setGammas(discretization);

		if (clumps == '1')
			ireos.setmCl(1);

		if (clumps == 'n')
			ireos.setmCl(n);

		/* Evaluate the solutions */
		List<IREOSSolution> evaluatedSolutions = ireos.evaluateSolutions();
		/* Print the results */
		for (int i = 0; i < evaluatedSolutions.size(); i++) {
			/* Set the IREOS statistics to the solution */
			System.out.println("Solution: " + args[1]);
			System.out.println("IREOS: " + evaluatedSolutions.get(i).getIREOS());
			File theDir = new File(SAVE + args[0]);
			theDir.mkdir();

			ireos.saveResult(SAVE + args[0] + "/" + args[1], evaluatedSolutions.get(i));
		}
	}
}