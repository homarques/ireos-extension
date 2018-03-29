package br.usp.icmc.lapad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Scanner;

import br.usp.icmc.lapad.ireos.IREOS;
import br.usp.icmc.lapad.ireos.IREOSSolution;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.parameter.value.ParameterValueGrid;

public class MainExample2 {
	private static final String DATA = "/home/hom/FullData/data/";
	private static final String WEIGHTS = "/home/hom/ireos_extension/Datasets/Real/weight/normalized_scores/";
	private static final String GAMMA = "/home/hom/FullData/GM/";
	private static final String SAVE = "/home/hom/FullData/DB/expfour/";
	private static final String KNN = "/home/hom/FullData/KNN/";

	/*
 	private static final String DATA = "/home/henrique/ireos_extension/Datasets/_Real/data/";
	private static final String WEIGHTS = "/home/henrique/ireos_extension/Datasets/_Real/weight/normalized_scores/";
	private static final String GAMMA = "/home/henrique/ireos_extension/Datasets/_Real/GM/";
	private static final String SAVE = "/home/henrique/ireos_extension/Datasets/_Real/results/knn/";
	private static final String KNN = "/home/henrique/ireos_extension/Datasets/_Real/KNN/";
	*/

	private static final int discretization = ParameterValueGrid.SCALE_LOGARITHMIC_LEGACY;

	public static void main(String[] args) throws Exception {
		//String args[] = { "Vowel", "1", "1", "1", "99" };
		String data = args[0];
		System.out.println(data);

		/* Count the number of observations in the dataset */
		BufferedReader weightReader;
		weightReader = new BufferedReader(new FileReader(new File(WEIGHTS + data + "/" + args[3])));

		int datasetSize = 0;
		while (weightReader.readLine() != null) {
			datasetSize++;
		}
		weightReader.close();

		BufferedReader knnReader = new BufferedReader(new FileReader(new File(KNN + args[0])));
		Scanner input; // pre-read in the number of rows/columns
		input = new Scanner(knnReader.readLine());
		int columns = 0;
		while(input.hasNextInt()) {
			input.nextInt();
			columns++;
		}
		knnReader.close();
		input.close();

		int rows = datasetSize;
		if(columns > Integer.parseInt(args[4]))
			columns = Integer.parseInt(args[4]);
		System.out.println(columns);
		int[][] a = new int[rows][columns];
		knnReader = new BufferedReader(new FileReader(new File(KNN + args[0])));
		for (int i = 0; i < rows; ++i) {
			input = new Scanner(knnReader.readLine());
			for (int j = 0; j < columns; ++j) {
				if (input.hasNextInt()) {
					a[i][j] = input.nextInt();
				}
			}
			input.close();
		}
		knnReader.close();

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
		IREOS ireos = new IREOS(dataset, weights, a);
		ireos.setNumber_of_threads(Integer.parseInt(args[2]));
		/* Find the gamma maximum */
		BufferedReader readerMax = new BufferedReader(new FileReader(GAMMA + data));
		double max = Double.parseDouble(readerMax.readLine());
		readerMax.close();
		ireos.setGammaMax(max);

		/* Set the number of values that gamma will be discretized */
		ireos.setnGamma(100);

		ireos.setGammas(discretization);

		ireos.setmCl(Integer.parseInt(args[1]));

		/* Evaluate the solutions */
		List<IREOSSolution> evaluatedSolutions = ireos.evaluateSolutions();
		/* Print the results */
		for (int i = 0; i < evaluatedSolutions.size(); i++) {
			/* Set the IREOS statistics to the solution */
			System.out.println("Solution: " + args[3]);
			System.out.println("IREOS: " + evaluatedSolutions.get(i).getIREOS());
			File theDir = new File(SAVE + "/" + args[4]);
			theDir.mkdir();
			
			theDir = new File(SAVE + "/" + args[4] + "/" + args[0]);
			theDir.mkdir();

			ireos.saveResult(SAVE + "/" + args[4] + "/" + args[0] + "/" + args[3], evaluatedSolutions.get(i));
		}
	}
}