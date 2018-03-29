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
import de.lmu.ifi.dbs.elki.math.MathUtil;
import de.lmu.ifi.dbs.elki.math.Mean;
import de.lmu.ifi.dbs.elki.math.MeanVariance;
import de.lmu.ifi.dbs.elki.math.statistics.distribution.NormalDistribution;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.NumberArrayAdapter;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;
import de.lmu.ifi.dbs.elki.utilities.scaling.outlier.OutlierScalingFunction;

/**
 * Scaling that can map arbitrary values to a probability in the range of [0:1].
 * 
 * Transformation is done using the formula max(0, erf(lambda * (x - mean) /
 * (stddev * sqrt(2))))
 * 
 * Where mean can be fixed to a given value, and stddev is then computed against
 * this mean.
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
public class StandardDeviationScaling implements OutlierScalingFunction {
  /**
   * Field storing the fixed mean to use
   */
  protected Double fixedmean = null;

  /**
   * Field storing the lambda value
   */
  protected double lambda;

  /**
   * Mean to use
   */
  double mean;

  /**
   * Scaling factor to use (usually: Lambda * Stddev * Sqrt(2))
   */
  double factor;

  /**
   * Constructor.
   * 
   * @param fixedmean Fixed mean
   * @param lambda Scaling factor lambda
   */
  public StandardDeviationScaling(Double fixedmean, double lambda) {
    super();
    this.fixedmean = fixedmean;
    this.lambda = lambda;
  }

  /**
   * Constructor.
   */
  public StandardDeviationScaling() {
    this(null, 1.0);
  }

  @Override
  public double getScaled(double value) {
    assert (factor != 0) : "prepare() was not run prior to using the scaling function.";
    if (value <= mean) {
      return 0;
    }
    return Math.max(0, NormalDistribution.erf((value - mean) / factor));
  }

  @Override
  public void prepare(OutlierResult or) {
    if (fixedmean == null) {
      MeanVariance mv = new MeanVariance();
      DoubleRelation scores = or.getScores();
      for (DBIDIter id = scores.iterDBIDs(); id.valid(); id.advance()) {
        double val = scores.doubleValue(id);
        if (!Double.isNaN(val) && !Double.isInfinite(val)) {
          mv.put(val);
        }
      }
      mean = mv.getMean();
      factor = lambda * mv.getSampleStddev() * MathUtil.SQRT2;
      if (factor == 0.0) {
        factor = Double.MIN_NORMAL;
      }
    } else {
      mean = fixedmean;
      Mean sqsum = new Mean();
      DoubleRelation scores = or.getScores();
      for (DBIDIter id = scores.iterDBIDs(); id.valid(); id.advance()) {
        double val = scores.doubleValue(id);
        if (!Double.isNaN(val) && !Double.isInfinite(val)) {
          sqsum.put((val - mean) * (val - mean));
        }
      }
      factor = lambda * Math.sqrt(sqsum.getMean()) * MathUtil.SQRT2;
      if (factor == 0.0) {
        factor = Double.MIN_NORMAL;
      }
    }
  }

  @Override
  public <A> void prepare(A array, NumberArrayAdapter<?, A> adapter) {
    if (fixedmean == null) {
      MeanVariance mv = new MeanVariance();
      final int size = adapter.size(array);
      for (int i = 0; i < size; i++) {
        double val = adapter.getDouble(array, i);
        if (!Double.isInfinite(val)) {
          mv.put(val);
        }
      }
      mean = mv.getMean();
      factor = lambda * mv.getSampleStddev() * MathUtil.SQRT2;
      if (factor == 0.0) {
        factor = Double.MIN_NORMAL;
      }
    } else {
      mean = fixedmean;
      Mean sqsum = new Mean();
      final int size = adapter.size(array);
      for (int i = 0; i < size; i++) {
        double val = adapter.getDouble(array, i);
        if (!Double.isInfinite(val)) {
          sqsum.put((val - mean) * (val - mean));
        }
      }
      factor = lambda * Math.sqrt(sqsum.getMean()) * MathUtil.SQRT2;
      if (factor == 0.0) {
        factor = Double.MIN_NORMAL;
      }
    }
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
    /**
     * Parameter to specify a fixed mean to use.
     * <p>
     * Key: {@code -stddevscale.mean}
     * </p>
     */
    public static final OptionID MEAN_ID = new OptionID("stddevscale.mean", "Fixed mean to use in standard deviation scaling.");

    /**
     * Parameter to specify the lambda value
     * <p>
     * Key: {@code -stddevscale.lambda}
     * </p>
     */
    public static final OptionID LAMBDA_ID = new OptionID("stddevscale.lambda", "Significance level to use for error function.");

    protected Double fixedmean = null;

    protected double lambda;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      DoubleParameter meanP = new DoubleParameter(MEAN_ID);
      meanP.setOptional(true);
      if (config.grab(meanP)) {
        fixedmean = meanP.getValue();
      }
      DoubleParameter lambdaP = new DoubleParameter(LAMBDA_ID, 3.0);
      if (config.grab(lambdaP)) {
        lambda = lambdaP.getValue();
      }
    }

    @Override
    protected StandardDeviationScaling makeInstance() {
      return new StandardDeviationScaling(fixedmean, lambda);
    }
  }
}
