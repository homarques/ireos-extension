package br.usp.icmc.lapad.ireos;

import static de.lmu.ifi.dbs.elki.database.DatabaseUtil.precomputedKNNQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;

import de.lmu.ifi.dbs.elki.algorithm.statistics.DistanceQuantileSampler;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.result.CollectionResult;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import smile.sort.QuickSort;

public class Utils {

	/**
	 * Look for the maximum gamma by increasing gradually gamma until all the
	 * outliers are separable using the default increasing
	 */
	public static double findGammaMax(SVMExamples dataset, List<double[]> solutions) throws Exception {
		return findGammaMax(dataset, solutions, 1.1f);
	}

	/**
	 * Look for the maximum gamma by increasing gradually gamma until all the
	 * outliers are separable
	 * 
	 * @param rateofIncreaseGammaMax Rate that gamma will be gradually increased
	 * @throws Exception Rate of increasing must be higher than 1
	 */
	public static double findGammaMax(SVMExamples dataset, List<double[]> solutions, double rateofIncreaseGammaMax)
			throws Exception {
		if (rateofIncreaseGammaMax > 1) {
			/* Add all the outliers in a list */
			List<Integer> outliers = new ArrayList<Integer>();
			for (double[] solution : solutions) {
				for (int i = 0; i < solution.length; i++) {
					if (solution[i] >= 0.5) {
						if (!outliers.contains(i))
							outliers.add(i);
					}
				}
			}

			/* Initial gamma based on the data dimensionality */
			double gammaMax = (double) 1 / (dataset.get_dim() * 1000);

			/*
			 * Verify if the outlier is separable using the actual gamma maximum, if so, the
			 * outlier is removed from the list, otherwise the gamma maximum is increased,
			 * this procedure is repeated until the list is empty
			 */

			while (!outliers.isEmpty()) {
				/* Initialize a maximum margin classifier per thread */
				MaximumMarginClassifier classifier = new MaximumMarginClassifier(dataset, gammaMax, outliers.get(0));

				classifier.start();
				classifier.join();

				if (classifier.getP() > 0.5) {
					outliers.remove(0);
				} else {
					gammaMax = gammaMax * rateofIncreaseGammaMax;
				}

			}

			return gammaMax;

		} else
			throw new Exception(
					"The rate of increase the maximum value of gamma (rateofIncreaseGammaMax) must be higher than 1");

	}

	/**
	 * Look for the maximum gamma based on the pairwise distances
	 * @param db dataset structure used by ELKI
	 * @param k number of nearest neighbors
	 * @throws Exception k > number of examples in the dataset
	 */
	public static int[][] knn(Database db, int k) throws Exception {
		Relation<NumberVector> relation = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

		if (k >= relation.size()) {
			throw new IllegalArgumentException("k > number of examples in the dataset");
		}

		int[][] knn = new int[relation.size()][k];
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
		return knn;
	}

	/**
	 * 
	 * ROC AUC
	 * 
	 * @author Haifeng Li
	 *         https://github.com/haifengl/smile/blob/master/core/src/main/java/smile/validation/AUC.java
	 */
	public static double roc(int[] labels, double[] scorings) {
		if (labels.length != scorings.length) {
			throw new IllegalArgumentException(
					String.format("The vector sizes don't match: %d != %d.", labels.length, scorings.length));
		}

		// for large sample size, overflow may happen for pos * neg.
		// switch to double to prevent it.
		double pos = 0;
		double neg = 0;

		for (int i = 0; i < labels.length; i++) {
			if (labels[i] == 0) {
				neg++;
			} else if (labels[i] == 1) {
				pos++;
			} else {
				throw new IllegalArgumentException(
						"ROC AUC is only for binary classification. Invalid label: " + labels[i]);
			}
		}

		int[] label = labels.clone();
		double[] prediction = scorings.clone();

		QuickSort.sort(prediction, label);

		double[] rank = new double[label.length];
		for (int i = 0; i < prediction.length; i++) {
			if (i == prediction.length - 1 || prediction[i] != prediction[i + 1]) {
				rank[i] = i + 1;
			} else {
				int j = i + 1;
				for (; j < prediction.length && prediction[j] == prediction[i]; j++)
					;
				double r = (i + 1 + j) / 2.0;
				for (int k = i; k < j; k++)
					rank[k] = r;
				i = j - 1;
			}
		}

		double auc = 0.0;
		for (int i = 0; i < label.length; i++) {
			if (label[i] == 1)
				auc += rank[i];
		}

		auc = (auc - (pos * (pos + 1) / 2.0)) / (pos * neg);
		return auc;
	}
	
	/**
	 * Look for the maximum gamma based on the pairwise distances
	 * @param db dataset structure used by ELKI
	 * @param sampleSize
	 */
	public static double findGammaMaxbyDistances(Database db, int sampleSize) {
		return findGammaMaxbyDistances(db, sampleSize, 0.9);

	}

	/**
	 * Look for the maximum gamma based on the pairwise distances
	 * @param db dataset structure used by ELKI
	 * @param sampleSize
	 * @param quantile 
	 */
	public static double findGammaMaxbyDistances(Database db, int sampleSize, double quantile) {
		/* Finding Gamma Max as the 90th percentile of the pairwise distances */
		ListParameterization params = new ListParameterization();
		params.addParameter(DistanceQuantileSampler.Parameterizer.QUANTILE_ID, quantile);
		params.addParameter(DistanceQuantileSampler.Parameterizer.SAMPLING_ID, sampleSize);

		DistanceQuantileSampler<?> distanceQuantile = ClassGenericsUtil
				.parameterizeOrAbort(DistanceQuantileSampler.class, params);
		CollectionResult<double[]> result = distanceQuantile.run(db);
		return ((Iterator<double[]>) result.iterator()).next()[0];

	}

}
