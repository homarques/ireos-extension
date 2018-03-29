package br.usp.icmc.lapad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import br.usp.icmc.lapad.ireos.MaximumMarginClassifier;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

public class Main2 {
	private static final String DATA = "/home/henrique/ireos_extension/Paper/acmart-master/data/";

	public static void main(String[] argsx) throws Exception {
		String args[] = { "oilx3", "1", "1" }; //data mcl threads

		/* List all the solutions in the folder */
		File solutions = new File(DATA + args[0]);

		/* Count the number of observations in the dataset */
		BufferedReader reader = new BufferedReader(new FileReader(solutions));
		int datasetSize = 0;
		while (reader.readLine() != null) {
			datasetSize++;
		}
		reader.close();

		/* Create dataset model */
		SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader(DATA + args[0])), datasetSize, 10000000);

		/* Initialize IREOS using the dataset and the solutions to be evaluated */
		MaximumMarginClassifier a = new MaximumMarginClassifier(dataset, 100, 100);
		a.run();
	}
}
