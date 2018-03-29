package br.usp.icmc.lapad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import br.usp.icmc.lapad.ireos.IREOS;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

public class MainExample2 {
	private static final String DATA = "/home/hom/ireos_extension/Datasets/Real/data/";
	private static final String WEIGHTS = "/home/hom/ireos_extension/Datasets/Real/weight/normalized_scores_median/";
	private static final String GAMMA = "/home/hom/ireos_extension/Datasets/Real/GM/";
	private static final String SAVE = "/home/hom/ireos_extension/Datasets/Real/results/stopping/";
	
	/*private static final String DATA = "/home/henrique/ireos_extension/Datasets/Real/data/";
	private static final String WEIGHTS = "/home/henrique/ireos_extension/Datasets/Real/weight/normalized_scores_median/";
	private static final String GAMMA = "/home/henrique/ireos_extension/Datasets/Real/GM/";
	private static final String SAVE = "/home/henrique/";*/
	
	/*private static final String DATA = "/home/henrique/ireos_extension/Paper/acmart-master/data/";
	private static final String WEIGHTS = "/home/henrique/ireos_extension/Paper/acmart-master/data/t/";
	private static final String SAVE = "/home/henrique/";*/

	private static final char clumps = '1';

	public static void main(String[] args) throws Exception {
	//	String args [] = {"Parkinson_withoutdupl_norm_05_v02", "1", "10"}; //dataset n thread
		String data = args[0];
		//int n = Integer.parseInt(args[1]);
		System.out.println(data);

		/* Count the number of observations in the dataset */
		BufferedReader weightReader;
		String line = null;
		weightReader = new BufferedReader(new FileReader(new File(WEIGHTS + data + "/1")));
		List<float[]> weights = new ArrayList<float[]>();
		int datasetSize = 0;
		while ((line = weightReader.readLine()) != null) {
			datasetSize++;
			
		}
		weightReader.close();

		for (int i = 1; i <= 10; i++) {
			weightReader = new BufferedReader(new FileReader(new File(WEIGHTS + data + "/" + i)));
			float weight[] = new float[datasetSize];
			int outlierID = 0;
			while ((line = weightReader.readLine()) != null) {
				weight[outlierID] = Float.parseFloat(line);
				outlierID++;
			}
			weights.add(weight);
			weightReader.close();
		}
				
		/* Create dataset model */
		SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader(DATA + data)), datasetSize, IREOS.C);

		/*
		 * Initialize IREOS using the dataset and the solutions to be evaluated
		 */
		IREOS ireos = new IREOS(dataset, weights);
		ireos.setNumber_of_threads(10);
		/* Find the gamma maximum */
		BufferedReader readerMax = new BufferedReader(new FileReader(GAMMA + data));
		double max = Double.parseDouble(readerMax.readLine());
		readerMax.close();
		ireos.setGammaMax(max);
		
		//if (clumps == '1')
		ireos.setmCl(1);

		//if (clumps == 'n')
		//	ireos.setmCl(n);
		File theDir = new File(SAVE + args[0]);
		theDir.mkdir();
		ireos.setSave(SAVE + args[0]);
		/* Evaluate the solutions */
		ireos.evaluateSolutionsbyRow();

	}
}