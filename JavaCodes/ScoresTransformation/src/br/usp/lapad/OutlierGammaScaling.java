package br.usp.lapad;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2015
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.math.MeanVariance;
import de.lmu.ifi.dbs.elki.math.statistics.distribution.GammaDistribution;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.NumberArrayAdapter;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.Flag;
import de.lmu.ifi.dbs.elki.utilities.scaling.outlier.OutlierScalingFunction;

/**
 * Scaling that can map arbitrary values to a probability in the range of [0:1]
 * by assuming a Gamma distribution on the values.
 * 
 * Reference:
 * <p>
 * H.-P. Kriegel, P. Kröger, E. Schubert, A. Zimek<br />
 * Interpreting and Unifying Outlier Scores<br />
 * Proc. 11th SIAM International Conference on Data Mining (SDM), Mesa, AZ, 2011
 * </p>
 * 
 * @author Erich Schubert
 * @since 0.3
 */
@Reference(authors = "H.-P. Kriegel, P. Kröger, E. Schubert, A. Zimek", //
		title = "Interpreting and Unifying Outlier Scores", //
		booktitle = "Proc. 11th SIAM International Conference on Data Mining (SDM), Mesa, AZ, 2011", //
		url = "http://dx.doi.org/10.1137/1.9781611972818.2")
public class OutlierGammaScaling implements OutlierScalingFunction {
	/**
	 * Normalization flag.
	 * 
	 * <pre>
	 * -gammascale.normalize
	 * </pre>
	 */
	public static final OptionID NORMALIZE_ID = new OptionID("gammascale.normalize",
			"Regularize scores before using Gamma scaling.");

	/**
	 * Gamma parameter k
	 */
	Double k = null;

	/**
	 * Gamma parameter theta
	 */
	Double theta = null;

	/**
	 * Score at the mean, for cut-off.
	 */
	double atmean = 0.5;

	/**
	 * Store flag to Normalize data before curve fitting.
	 */
	boolean normalize = false;

	/**
	 * Keep a reference to the outlier score meta, for normalization.
	 */
	OutlierScoreMeta meta = null;

	/**
	 * Constructor.
	 * 
	 * @param normalize
	 *            Normalization flag
	 */
	public OutlierGammaScaling(boolean normalize, Double k, Double theta) {
		super();
		this.normalize = normalize;
		this.k = k;
		this.theta = theta;
	}

	@Override
	public double getScaled(double value) {
		assert (theta > 0) : "prepare() was not run prior to using the scaling function.";
		value = preScale(value);
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return 1.0;
		}
		return Math.max(0, (GammaDistribution.regularizedGammaP(k, value / theta) - atmean) / (1 - atmean));
	}

	@Override
	public void prepare(OutlierResult or) {
		meta = or.getOutlierMeta();
		MeanVariance mv = new MeanVariance();
		DoubleRelation scores = or.getScores();
		for (DBIDIter id = scores.iterDBIDs(); id.valid(); id.advance()) {
			double score = scores.doubleValue(id);
			score = preScale(score);
			if (!Double.isNaN(score) && !Double.isInfinite(score)) {
				mv.put(score);
			}
		}
		final double mean = mv.getMean();
		final double var = mv.getSampleVariance();
		if (k == null) {
			k = (mean * mean) / var;
		}
		if (theta == null) {
			theta = var / mean;
		}
		atmean = GammaDistribution.regularizedGammaP(k, mean / theta);
		// logger.warning("Mean:"+mean+" Var:"+var+" Theta: "+theta+" k: "+k+"
		// valatmean"+atmean);
	}

	@Override
	public <A> void prepare(A array, NumberArrayAdapter<?, A> adapter) {
		MeanVariance mv = new MeanVariance();
		final int size = adapter.size(array);
		for (int i = 0; i < size; i++) {
			double score = adapter.getDouble(array, i);
			score = preScale(score);
			if (!Double.isNaN(score) && !Double.isInfinite(score)) {
				mv.put(score);
			}
		}
		final double mean = mv.getMean();
		final double var = mv.getSampleVariance();
		k = (mean * mean) / var;
		theta = var / mean;
		atmean = GammaDistribution.regularizedGammaP(k, mean / theta);
		// logger.warning("Mean:"+mean+" Var:"+var+" Theta: "+theta+" k: "+k+"
		// valatmean"+atmean);
	}

	/**
	 * Normalize data if necessary.
	 * 
	 * Note: this is overridden by {@link MinusLogGammaScaling}!
	 * 
	 * @param score
	 *            Original score
	 * @return Normalized score.
	 */
	protected double preScale(double score) {
		if (normalize) {
			score = meta.normalizeScore(score);
		}
		return score;
	}

	@Override
	public double getMin() {
		return 0.0;
	}

	@Override
	public double getMax() {
		return 1.0;
	}

	/**
	 * Parameterization class.
	 * 
	 * @author Erich Schubert
	 * 
	 * @apiviz.exclude
	 */
	public static class Parameterizer extends AbstractParameterizer {
		protected boolean normalize = false;

		@Override
		protected void makeOptions(Parameterization config) {
			super.makeOptions(config);
			Flag normalizeF = new Flag(NORMALIZE_ID);
			if (config.grab(normalizeF)) {
				normalize = normalizeF.getValue();
			}
		}

		@Override
		protected OutlierGammaScaling makeInstance() {
			return new OutlierGammaScaling(normalize, null, null);
		}
	}
}
