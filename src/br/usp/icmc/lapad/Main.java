package br.usp.icmc.lapad;

import static de.lmu.ifi.dbs.elki.database.DatabaseUtil.precomputedKNNQuery;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

import br.usp.icmc.lapad.ireos.IREOS;
import br.usp.icmc.lapad.ireos.IREOSSolution;
import br.usp.icmc.lapad.ireos.Utils;
import de.lmu.ifi.dbs.elki.algorithm.Algorithm;
import de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LOF;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class Main {
	private static final String DATA = "WBC_withoutdupl_norm";

	public static void main(String[] args) throws Exception {
		/* Getting the size of the dataset */
		int size = 0;
		BufferedReader reader = new BufferedReader(new FileReader(DATA));
		while (reader.readLine() != null) {
			size++;
		}
		reader.close();

		/* Ground truth for WBC dataset, only the first 10 objects are outliers */
		int label[] = new int[size];
		for (int i = 0; i < size; i++) {
			if (i < 10)
				label[i] = 1;
			else
				label[i] = 0;
		}

		/* Create dataset model */
		SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader(DATA)), size, 100);

		/* Initializing the dataset for ELKI */
		DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(dataset.atts);
		Database db = new StaticArrayDatabase(dbc, null);
		db.initialize();

		/* Running the outlier detection algorithm using ELKI */
		List<double[]> scorings = new ArrayList<double[]>();

		// Running LOF varying k from 5 to 50, step 3
		for (int k = 5; k <= 50; k = k + 3) {
			double[] scoring = new double[size];
			ListParameterization params = new ListParameterization();
			params.addParameter(LOF.Parameterizer.K_ID, k);

			Algorithm alg = ClassGenericsUtil.parameterizeOrAbort(LOF.class, params);
			OutlierResult result = (OutlierResult) alg.run(db);

			// Outlier scoring normalization provided by ELKI
			for (DBIDIter iter = result.getScores().iterDBIDs(); iter.valid(); iter.advance())
				scoring[iter.internalGetIndex() - 1] = result.getOutlierMeta()
						.normalizeScore(result.getScores().doubleValue(iter));
			scorings.add(scoring);
		}

		int k = 200;
		int[][] knn = new int[dataset.getTrain_size()][k];
		Relation relation = db.getRelation(TypeUtil.array(TypeUtil.NUMBER_VECTOR_FIELD)[0]);
		KNNQuery<NumberVector> knnq = precomputedKNNQuery(db, relation, new EuclideanDistanceFunction(), k + 1);

		int i = 0;
		for (DBIDIter iter = relation.iterDBIDs(); iter.valid(); iter.advance()) {
			int j = 0;
			KNNList neighbors = knnq.getKNNForDBID(iter, k + 1);
			for (DoubleDBIDListIter neighbor = neighbors.iter(); neighbor.valid(); neighbor.advance()) {
				if (i == neighbor.internalGetIndex() - 1)
					continue;
				knn[i][j] = neighbor.internalGetIndex() - 1;
				j++;
				if (j >= k)
					break;
			}
			i++;
		}

		IREOS ireos = new IREOS(dataset, scorings);

		/*
		 * Finding Gamma Max can be found as the value required by the classifier to
		 * separate from the other objects every object labeled as outlier in all
		 * solutions, i.e., objects with outlier probability > 50%
		 */
		double gammaMax = Utils.findGammaMax(dataset, scorings);

		/*
		 * Alternatively, Gamma Max can be found as the 90th percentile of the pairwise
		 * distances
		 */
		// ListParameterization params = new ListParameterization();
		// params.addParameter(DistanceQuantileSampler.Parameterizer.QUANTILE_ID, 0.9);
		// if (size <= 1000)
		// params.addParameter(DistanceQuantileSampler.Parameterizer.SAMPLING_ID, size);
		// else
		// params.addParameter(DistanceQuantileSampler.Parameterizer.SAMPLING_ID, 1000);

		// DistanceQuantileSampler<?> distanceQuantile =
		// ClassGenericsUtil.parameterizeOrAbort(DistanceQuantileSampler.class, params);
		// CollectionResult<double[]> result = distanceQuantile.run(db);
		// gammaMax = ((Iterator<double[]>) result.iterator()).next()[0];

		ireos.setGammaMax(gammaMax);

		/* Set the maximum clump size */
		ireos.setmCl(1);

		/* Evaluate the solutions */
		List<IREOSSolution> evaluatedSolutions = ireos.evaluateSolutions();
		List<IREOSSolution> evaluatedSolutionsApprox = ireos.evaluateSolutions(knn);

		/* Print the results */
		for (i = 0; i < evaluatedSolutions.size(); i++) {
			System.out.println("------------------------------------");
			System.out.println("Solution: " + i);
			System.out.println("IREOS: " + evaluatedSolutions.get(i).getIREOS());
			System.out.println("Approximate IREOS: " + evaluatedSolutionsApprox.get(i).getIREOS());
			System.out.println("ROC AUC: " + Utils.roc(label, scorings.get(i)));
			System.out.println("------------------------------------");
		}

	}
}