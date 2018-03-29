/* Modified by Henrique O. Marques on May 2015 to not use operators */

/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.rapidminer.parameter.value;

/**
 * A grid of numerical parameter values.
 * 
 * @author Tobias Malbrecht
 */
public class ParameterValueGrid{

	public static final int SCALE_LINEAR = 0;
	
	public static final int SCALE_QUADRATIC = 1;
	
	public static final int SCALE_LOGARITHMIC = 2;

	public static final int SCALE_LOGARITHMIC_LEGACY = 3;
	
	public static final String[] SCALES = { "linear", "quadratic", "logarithmic", "logarithmic (legacy)" };
	
	public static final int DEFAULT_STEPS = 10;
	
	public static final int DEFAULT_SCALE = SCALE_LINEAR;
	
	private String min;
	
	private String max;
	
	private String steps;
	
	private String stepSize;
	
	private int scale;


	public ParameterValueGrid(String min, String max, String steps, int scale) {
		this.min = min;
		this.max = max;
		this.steps = steps;
		this.scale = scale;
	}

	public void setMin(String min) {
		this.min = min;
	}
	
	public String getMin() {
		return min;
	}
	
	public void setMax(String max) {
		this.max = max;
	}
	
	public String getMax() {
		return max;
	}
	
	public void setSteps(String steps) {
		this.steps = steps;
	}
	
	public String getSteps() {
		return steps;
	}
	
	public void setScale(int scale) {
		this.scale = scale;
	}
	
	public int getScale() {
		return scale;
	}

	public double[] getValues() {
		double[] values = null;
		if (stepSize != null && steps == null) {
			steps = Integer.toString((int) (Double.valueOf(max) - Double.valueOf(min)) / Integer.parseInt(stepSize));
		}
		switch (scale) {
        case SCALE_LINEAR:
        	values = scalePolynomial(Integer.parseInt(steps), 1);
        	break;
        case SCALE_QUADRATIC:
        	values = scalePolynomial(Integer.parseInt(steps), 2);
        	break;
        case SCALE_LOGARITHMIC:
        	values = scaleLogarithmic(Integer.parseInt(steps));
        	break;
        case SCALE_LOGARITHMIC_LEGACY:
        	values = scaleLogarithmicLegacy(Integer.parseInt(steps));
        	break;
        default:
        	values = scalePolynomial(Integer.parseInt(steps), 1);
        }

		return values;
	}

	private double[] scalePolynomial(int steps, double power) {
		double[] values = new double[steps + 1];
		double minValue = Double.parseDouble(min);
		double maxValue = Double.parseDouble(max);
		for (int i = 0; i < steps + 1; i++) {
			values[i] = minValue + Math.pow((double) i / (double) steps, power) * (maxValue - minValue);
		}
		return values;
	}
	
	private double[] scaleLogarithmic(int steps) {
		double minValue = Double.parseDouble(min);
		double maxValue = Double.parseDouble(max);
		double[] values = new double[steps + 1];
		for (int i = 0; i < steps + 1; i++) {
			values[i] = Math.pow(maxValue / minValue, (double) i / (double) steps) * minValue;
		}
		return values;
	}

	private double[] scaleLogarithmicLegacy(int steps) {
		double minValue = Double.parseDouble(min);
		double maxValue = Double.parseDouble(max);
		double[] values = new double[steps + 1];
		double offset = 1 - minValue;
		for (int i = 0; i < steps + 1; i++) {
			values[i] = Math.pow(maxValue + offset, (double) i / (double) steps) - offset;
		}
		return values;
	}
	
	public int getNumberOfValues() {
		return Integer.parseInt(steps) + 1;
	}
	
	public String getValuesString() {
		return "[" + min +
		       ";" + max +
		       ";" + steps + 
		       ";" + SCALES[scale] + "]";
	}
	
	@Override
	public String toString() {
		return "grid: " + min + " - " + max + " (" + steps + ", " + SCALES[scale] + ")";
	}
}
