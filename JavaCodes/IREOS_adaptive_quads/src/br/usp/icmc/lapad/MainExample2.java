package br.usp.icmc.lapad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import br.usp.icmc.lapad.ireos.IREOS;
import br.usp.icmc.lapad.ireos.IREOSSolution;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

public class MainExample2 {
/*	private static final String DATA = "/home/hom/FullData/data/";
	private static final String WEIGHTS = "/home/hom/ireos_extension/Datasets/Real/weight/normalized_scores_median/";
	private static final String GAMMA = "/home/hom/FullData/GM/";
	public static String DB = "/home/hom/FullData/DB/expthree/";
	*/
	private static final String DATA = "/home/henrique/ireos_extension/Paper/acmart-master/data/";
	private static final String WEIGHTS = "/home/henrique/ireos_extension/Paper/acmart-master/data/t/";
	//private static final String GAMMA = "/home/henrique/FullData/GM/";
	public static String DB = "/home/henrique//";

	public static void main(String[] argsx) throws Exception {
		String args [] = {"ilx3", "1", "1", "10"};
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
				
		/* Create dataset model */
		SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader(DATA + data)), datasetSize, IREOS.C);

		/*
		 * Initialize IREOS using the dataset and the solutions to be evaluated
		 */
		IREOS ireos = new IREOS(dataset, weights);
		ireos.setNumber_of_threads(Integer.parseInt(args[2]));
		/* Find the gamma maximum */
		
		ireos.setGammaMax(8);
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