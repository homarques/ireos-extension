package br.usp.icmc.lapad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import br.usp.icmc.lapad.ireos.IREOS;
import br.usp.icmc.lapad.ireos.IREOSSolution;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

public class MainExample2 {
	private static final String DATA = "/home/hom/FullData/data/";
	private static final String WEIGHTS = "/home/hom/ireos_extension/Datasets/Real/weight/normalized_scores_median/";
	private static final String GAMMA = "/home/hom/FullData/GM/";
	public static String DB = "/home/hom/FullData/DB/expfive/";
	private static final String KNN = "/home/hom/FullData/KNN/";

	/*private static final String DATA = "/home/henrique/ireos_extension/Datasets/Real/data/";
	private static final String WEIGHTS = "/home/henrique/ireos_extension/Datasets/Real/weight/normalized_scores_median/";
	private static final String GAMMA = "/home/henrique/FullData/GM/";
	public static String DB = "/home/henrique/DB/expthree/";*/

	public static void main(String[] args) throws Exception {
		//String args [] = {"WBC_withoutdupl_norm_v10", "1", "1", "1"};
		String data = args[0];
		System.out.println(data);
		File theDir = new File( DB + args[0]);
		if (!theDir.exists())
		    theDir.mkdir();
		DB = DB + args[0] + "/" + args[3];

		/* Count the number of observations in the dataset */
		BufferedReader weightReader;
		String line = null;
		weightReader = new BufferedReader(new FileReader(new File(WEIGHTS + data + "/" + args[3])));
		List<Float> weights = new ArrayList<Float>();
		int datasetSize = 0;
		while ((line = weightReader.readLine()) != null) {
			weights.add(Float.parseFloat(line));
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
		ireos.setmCl(Integer.parseInt(args[1]));
		
		/* Evaluate the solutions */
		List<IREOSSolution> evaluatedSolutions = ireos.evaluateSolutions();
		/* Print the results */
		for (int i = 0; i < evaluatedSolutions.size(); i++) {
			/* Set the IREOS statistics to the solution */
			System.out.println("Solution: " + args[3]);
			System.out.println("IREOS: " + evaluatedSolutions.get(i).getIREOS());
		}
	}
}