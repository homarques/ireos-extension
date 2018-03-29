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

import java.util.ArrayList;

import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.math.DoubleMinMax;
import de.lmu.ifi.dbs.elki.math.MeanVariance;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.NumberArrayAdapter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.GlobalParameterConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.OnlyOneIsAllowedToBeSetGlobalConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.Flag;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.Parameter;
import de.lmu.ifi.dbs.elki.utilities.scaling.outlier.OutlierScalingFunction;

/**
 * Scaling that can map arbitrary values to a value in the range of [0:1].
 * 
 * Transformation is done by linear mapping onto 0:1 using the minimum and
 * maximum values.
 * 
 * @author Erich Schubert
 * @since 0.3
 */
public class OutlierLinearScaling implements OutlierScalingFunction {
  /**
   * Parameter to specify a fixed minimum to use.
   * <p>
   * Key: {@code -linearscale.min}
   * </p>
   */
  public static final OptionID MIN_ID = new OptionID("linearscale.min", "Fixed minimum to use in linear scaling.");

  /**
   * Parameter to specify the maximum value.
   * <p>
   * Key: {@code -linearscale.max}
   * </p>
   */
  public static final OptionID MAX_ID = new OptionID("linearscale.max", "Fixed maximum to use in linear scaling.");

  /**
   * Flag to use the mean as minimum for scaling.
   * 
   * <p>
   * Key: {@code -linearscale.usemean}
   * </p>
   */
  public static final OptionID MEAN_ID = new OptionID("linearscale.usemean", "Use the mean as minimum for scaling.");

  /**
   * Flag to use ignore zeros when computing the min and max.
   * 
   * <p>
   * Key: {@code -linearscale.ignorezero}
   * </p>
   */
  public static final OptionID NOZEROS_ID = new OptionID("linearscale.ignorezero", "Ignore zero entries when computing the minimum and maximum.");

  /**
   * Field storing the Minimum to use
   */
  protected Double min = null;

  /**
   * Field storing the Maximum value
   */
  protected Double max = null;
  protected Double median = null;

  /**
   * Scaling factor to use (1/ max - min)
   */
  double factor;

  /**
   * Use the mean for scaling
   */
  boolean usemean = false;
  boolean usemedian = false;

  /**
   * Ignore zero values
   */
  boolean nozeros = false;

  /**
   * Constructor.
   */
  public OutlierLinearScaling() {
    this(null, null, false, false);
  }

  /**
   * Constructor.
   * 
   * @param min
   * @param max
   * @param usemean
   * @param nozeros
   */
  public OutlierLinearScaling(Double min, Double max, boolean usemean, boolean nozeros) {
    super();
    this.min = min;
    this.max = max;
    this.usemean = usemean;
    this.nozeros = nozeros;
    if (min != null && max != null) {
      this.factor = (max - min);
    }
  }

  @Override
  public double getScaled(double value) {
    assert (factor != 0) : "prepare() was not run prior to using the scaling function.";
    if (value <= min) {
      return 0;
    }
    return Math.min(1, ((value - min) / factor));
  }

  @Override
  public void prepare(OutlierResult or) {
    if (usemean) {
      MeanVariance mv = new MeanVariance();
      DoubleMinMax mm = (max == null) ? new DoubleMinMax() : null;
      boolean skippedzeros = false;
      DoubleRelation scores = or.getScores();
      for (DBIDIter id = scores.iterDBIDs(); id.valid(); id.advance()) {
        double val = scores.doubleValue(id);
        if (nozeros && val == 0.0) {
          skippedzeros = true;
          continue;
        }
        if (!Double.isNaN(val) && !Double.isInfinite(val)) {
          mv.put(val);
        }
        if (max == null) {
          mm.put(val);
        }
      }
      if (skippedzeros && mm.getMin() == mm.getMax()) {
        mm.put(0.0);
        mv.put(0.0);
      }
      min = mv.getMean();
      if (max == null) {
        max = mm.getMax();
      }
    } else {
      if (min == null || max == null) {
        boolean skippedzeros = false;
        DoubleMinMax mm = new DoubleMinMax();
        DoubleRelation scores = or.getScores();
        for (DBIDIter id = scores.iterDBIDs(); id.valid(); id.advance()) {
          double val = scores.doubleValue(id);
          if (nozeros && val == 0.0) {
            skippedzeros = true;
            continue;
          }
          mm.put(val);
        }
        if (skippedzeros && mm.getMin() == mm.getMax()) {
          mm.put(0.0);
        }
        if (min == null) {
          min = mm.getMin();
        }
        if (max == null) {
          max = mm.getMax();
        }
      }
    }
    if(usemedian) {
    	min = median;
    }
    factor = (max - min);
  }

  @Override
  public <A> void prepare(A array, NumberArrayAdapter<?, A> adapter) {
    if (usemean) {
      MeanVariance mv = new MeanVariance();
      DoubleMinMax mm = (max == null) ? new DoubleMinMax() : null;
      boolean skippedzeros = false;
      final int size = adapter.size(array);
      for (int i = 0; i < size; i++) {
        double val = adapter.getDouble(array, i);
        if (nozeros && val == 0.0) {
          skippedzeros = true;
          continue;
        }
        if (!Double.isNaN(val) && !Double.isInfinite(val)) {
          mv.put(val);
        }
        if (max == null) {
          mm.put(val);
        }
      }
      if (skippedzeros && mm.getMin() == mm.getMax()) {
        mm.put(0.0);
        mv.put(0.0);
      }
      min = mv.getMean();
      if (max == null) {
        max = mm.getMax();
      }
    } else {
      if (min == null || max == null) {
        boolean skippedzeros = false;
        DoubleMinMax mm = new DoubleMinMax();
        final int size = adapter.size(array);
        for (int i = 0; i < size; i++) {
          double val = adapter.getDouble(array, i);
          if (nozeros && val == 0.0) {
            skippedzeros = true;
            continue;
          }
          mm.put(val);
        }
        if (skippedzeros && mm.getMin() == mm.getMax()) {
          mm.put(0.0);
        }
        if (min == null) {
          min = mm.getMin();
        }
        if (max == null) {
          max = mm.getMax();
        }
      }
    }
    factor = (max - min);
  }

  @Override
  public double getMin() {
    return 0.0;
  }

  @Override
  public double getMax() {
    return 1.0;
  }

  public Double getMedian() {
	return median;
}

public void setMedian(Double median) {
	this.median = median;
}

public boolean isUsemedian() {
	return usemedian;
}

public void setUsemedian(boolean usemedian) {
	this.usemedian = usemedian;
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
     * Field storing the Minimum to use
     */
    protected Double min = null;

    /**
     * Field storing the Maximum value
     */
    protected Double max = null;

    /**
     * Use the mean for scaling
     */
    boolean usemean = false;

    /**
     * Ignore zero values
     */
    boolean nozeros = false;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      DoubleParameter minP = new DoubleParameter(MIN_ID);
      minP.setOptional(true);
      if (config.grab(minP)) {
        min = minP.getValue();
      }

      DoubleParameter maxP = new DoubleParameter(MAX_ID);
      maxP.setOptional(true);
      if (config.grab(maxP)) {
        max = maxP.getValue();
      }

      Flag meanF = new Flag(MEAN_ID);
      if (config.grab(meanF)) {
        usemean = meanF.getValue();
      }

      Flag nozerosF = new Flag(NOZEROS_ID);
      if (config.grab(nozerosF)) {
        nozeros = nozerosF.getValue();
      }

      // Use-Mean and Minimum value must not be set at the same time!
      ArrayList<Parameter<?>> minmean = new ArrayList<>();
      minmean.add(minP);
      minmean.add(meanF);
      GlobalParameterConstraint gpc = new OnlyOneIsAllowedToBeSetGlobalConstraint(minmean);
      config.checkConstraint(gpc);
    }

    @Override
    protected OutlierLinearScaling makeInstance() {
      return new OutlierLinearScaling(min, max, usemean, nozeros);
    }
  }
}
