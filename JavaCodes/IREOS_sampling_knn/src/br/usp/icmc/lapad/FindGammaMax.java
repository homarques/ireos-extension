package br.usp.icmc.lapad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import br.usp.icmc.lapad.ireos.IREOS;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

public class FindGammaMax {
	private static final String DATA = "/home/hom/ireos_extension/Datasets/Real/data/";
	private static final String LABELS = "/home/hom/ireos_extension/Datasets/Real/weight/normalized_scores/";
	private static final String GAMMA = "/home/hom/ireos_extension/Datasets/Real/GM/";

	public static void main(String[] args) throws Exception {
		String dataset = DATA + args[0];
		File[] computed = (new File(GAMMA)).listFiles();
		ArrayList<String> comp = new ArrayList<String>();
		for (File file : computed) {
			comp.add(file.getName());
		}

		System.out.println(dataset);
		if (!comp.contains(dataset)) {
			/* List all the solutions in the folder */
			File[] solutions = (new File(LABELS + args[0])).listFiles();
			/* Count the number of observations in the dataset */
			BufferedReader reader = new BufferedReader(new FileReader(solutions[0]));
			int datasetSize = 0;
			while (reader.readLine() != null) {
				datasetSize++;
			}

			/* Read all the solutions from the files and add in the list of vector */
			List<Integer> outliers = new ArrayList<Integer>();
			int outlierID = 0;
			for (int i = 0; i < solutions.length; i++) {
				String line = null;
				reader = new BufferedReader(new FileReader(solutions[i]));
				outlierID = 0;
				while ((line = reader.readLine()) != null) {
					if (Double.parseDouble(line) >= 0.5) {
						if (!outliers.contains(outlierID))
							outliers.add(outlierID);
					}
					outlierID++;
				}
				reader.close();
			}

			/* Create dataset model */
			SVMExamples data = new SVMExamples(new BufferedReader(new FileReader(dataset)), datasetSize, 100);

			/* Initialize IREOS using the dataset and the solutions to be evaluated */
			IREOS ireos = new IREOS(data);

			/* Find the gamma maximum */
			ireos.findGammaMax(outliers);

			FileWriter writer = new FileWriter(GAMMA + args[0]);
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write(ireos.getGammaMax() + "");
			bufferedWriter.flush();
			bufferedWriter.close();
		}
	}
}
