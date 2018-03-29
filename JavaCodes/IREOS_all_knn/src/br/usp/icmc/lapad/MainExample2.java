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
	/*
	 * private static final String DATA =
	 * "/home/hom/ireos_extension/Datasets/Real/data/"; private static final String
	 * WEIGHTS =
	 * "/home/hom/ireos_extension/Datasets/Real/weight/normalized_scores/"; private
	 * static final String GAMMA = "/home/hom/ireos_extension/Datasets/Real/GM/";
	 * private static final String SAVE =
	 * "/home/hom/ireos_extension/Datasets/Real/results/cl1/";
	 */

	/*
	 * private static final String DATA =
	 * "/home/henrique/ireos_extension/Paper/acmart-master/data/"; private static
	 * final String WEIGHTS =
	 * "/home/henrique/ireos_extension/Paper/acmart-master/data/t/"; //private
	 * static final String GAMMA = "/home/hom/ireos_extension/Datasets/Real/GM/";
	 * private static final String SAVE = "/home/henrique/";
	 */
	
	private static final String DATA = "/home/hom/FullData/data/";
	private static final String WEIGHTS = "/home/hom/ireos_extension/Datasets/Real/weight/normalized_scores_median/";
	private static final String GAMMA = "/home/hom/FullData/GM/";
	public static String DB = "/home/hom/FullData/DB/exptwo/";
	/*
	private static final String DATA = "/home/hom/ireos_extension/Datasets/Synthetic/data/";
	private static final String WEIGHTS = "/home/hom/ireos_extension/Datasets/Synthetic/weight/";
	private static final String GAMMA = "/home/hom/ireos_extension/Datasets/Synthetic/GM/";
	public static String DB = "/home/hom/FullData/DB/exptwo/";

	/* 0 - trapezoide; 1 - media */

	/* 7 - log2; 8 - logn; 9 - log10 */
	private static final int discretization = ParameterValueGrid.SCALE_LOGARITHMIC_LEGACY;

	public static void main(String[] args) throws Exception {
		// String args [] = {"ilx3", "10", "1", "1"};//dataset n threads sol
		//String args[] = { "Arrhythmia_withoutdupl_norm_05_v01", "1", "2", "10" };
		String data = args[0];
		System.out.println(data);
		File theDir = new File( DB + args[0]);
		if (!theDir.exists())
		    theDir.mkdir();

		DB = DB + args[0] + "/" + args[3];

		/* Count the number of observations in the dataset */
		BufferedReader weightReader;
		weightReader = new BufferedReader(new FileReader(new File(WEIGHTS + data + "/" + args[3])));

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

		weightReader = new BufferedReader(new FileReader(new File(WEIGHTS + data + "/" + args[3])));
		float weights[] = new float[datasetSize];

		outlierID = 0;
		while ((line = weightReader.readLine()) != null) {
			weights[outlierID] = Float.parseFloat(line);
			outlierID++;
		}
		weightReader.close();

		/* Create dataset model */
		SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader(DATA + data)), datasetSize, IREOS.C);

		/*
		 * Initialize IREOS using the dataset and the solutions to be evaluated
		 */
<<<<<<< HEAD
		IREOS ireos = new IREOS(dataset, weights);
		ireos.setNumber_of_threads(Integer.parseInt(args[2]));
=======
		IREOS ireos = new IREOS(dataset, detection, weights, STRATEGY);

>>>>>>> parent of bf87337... merry xmas
		/* Find the gamma maximum */
		BufferedReader readerMax = new BufferedReader(new FileReader(GAMMA + data));
		double max = Double.parseDouble(readerMax.readLine());
		readerMax.close();
		// ireos.setGammaMax(7.5);
		ireos.setGammaMax(max);

		/* Set the number of values that gamma will be discretized */
		ireos.setnGamma(100);

		ireos.setGammas(discretization);

		ireos.setmCl(Integer.parseInt(args[1]));

		// double[] gammas = {0.3827862, 1.883045, 4.08, 6.276955, 7.777214};
		// ireos.setGammas(gammas);

		/* Evaluate the solutions */
		List<IREOSSolution> evaluatedSolutions = ireos.evaluateSolutions();
		/* Print the results */
		for (int i = 0; i < evaluatedSolutions.size(); i++) {
			/* Set the IREOS statistics to the solution */
			System.out.println("Solution: " + args[3]);
			System.out.println("IREOS: " + evaluatedSolutions.get(i).getIREOS());
			//File theDir = new File(SAVE + args[0]);
			//theDir.mkdir();

			//ireos.saveResult(SAVE + args[0] + "/" + args[1], evaluatedSolutions.get(i));
		}
	}
}