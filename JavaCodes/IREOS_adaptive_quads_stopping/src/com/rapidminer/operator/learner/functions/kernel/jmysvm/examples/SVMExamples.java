/* Modified by Henrique O. Marques on May 2015 to use cost weights for data instances and also not use operators*/

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
package com.rapidminer.operator.learner.functions.kernel.jmysvm.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a sparse example set which can be used for learning. This
 * data structure is also used as SVM model.
 * 
 * @author Stefan Rueping, Ingo Mierswa
 */
public class SVMExamples implements Serializable {

	private static final long serialVersionUID = 7204578592570791663L;

	/** The dimension of the example set. */
	private int dim;

	/** The number of examples. */
	private int train_size;

	// sparse representation of examples. public for avoiding invocation of a
	// method (slower)
	/** The known attribute values for each example. */
	public double[][] atts;

	/**
	 * The corresponding indices for the known attribute values for each
	 * example.
	 */
	public int[][] index;

	/** The ids of all examples. */
	public String[] ids;

	/** The SVM alpha values. Will be filled by learning. */
	private double[] alphas;

	/**
	 * The labels of the examples if known. -1 and +1 for classification or the
	 * real value for regression tasks. Will be filled by prediction.
	 */
	private double[] ys;

	/**
	 * The cost weights of the examples.
	 */
	private double[] cs;

	/**
	 * The maximum cost weights.
	 */
	private double c = -1;

	/** The hyperplane offset. */
	private double b;

	/**
	 * This example will be once constructed and delivered with the asked
	 * values.
	 */
	private SVMExample x;

	/** Creates an empty example set of the given size. */
	public SVMExamples(int size, double b) {
		this.train_size = size;
		this.b = b;

		atts = new double[train_size][];
		index = new int[train_size][];
		ys = new double[train_size];
		cs = new double[train_size];
		alphas = new double[train_size];

		ids = new String[size];

		x = new SVMExample();
	}

	public SVMExamples(SVMExamples a) {
		this(a.train_size, 0.0d);
		dim = a.get_dim();
		for (int i = 0; i < train_size; i++) {
			ids[i] = a.ids[i];
			ys[i] = -1;
			cs[i] = a.cs[i];
			atts[i] = new double[a.atts[i].length];
			index[i] = new int[a.index[i].length];

			for (int j = 0; j < a.atts[i].length; j++) {
				atts[i][j] = a.atts[i][j];
				index[i][j] = a.index[i][j];
			}
		}
	}

	/**
	 * Creates a fresh example set of the given size from the RapidMiner example
	 * reader. The alpha values and b are zero, the label will be set if it is
	 * known.
	 * 
	 * @throws IOException
	 */
	public SVMExamples(BufferedReader reader, int size, double cs[])
			throws IOException {
		this(size, 0.0d);

		int exampleCounter = 0;
		String current = null;
		while ((current = reader.readLine()) != null) {
			Map<Integer, Double> attributeMap = new LinkedHashMap<Integer, Double>();
			int a = 0;
			for (String attribute : current.split(" ")) {
				double value = Double.parseDouble(attribute);
				attributeMap.put(a, value);
				if ((a + 1) > dim)
					dim = (a + 1);
				a++;
			}
			atts[exampleCounter] = new double[attributeMap.size()];
			index[exampleCounter] = new int[attributeMap.size()];
			Iterator<Map.Entry<Integer, Double>> i = attributeMap.entrySet()
					.iterator();
			int attributeCounter = 0;
			while (i.hasNext()) {
				Map.Entry<Integer, Double> e = i.next();
				Integer indexValue = e.getKey();
				Double attributeValue = e.getValue();
				index[exampleCounter][attributeCounter] = indexValue.intValue();
				double value = attributeValue.doubleValue();
				atts[exampleCounter][attributeCounter] = value;
				attributeCounter++;
			}
			ys[exampleCounter] = -1;
			this.cs[exampleCounter] = cs[exampleCounter];
			ids[exampleCounter] = exampleCounter + "";

			exampleCounter++;
		}
	}

	/**
	 * Creates a fresh example set of the given size from the RapidMiner example
	 * reader. The alpha values and b are zero, the label will be set if it is
	 * known. Also, all the examples have the same cost
	 * 
	 * @throws IOException
	 */
	public SVMExamples(BufferedReader reader, int size, double c)
			throws IOException {
		this(size, 0.0d);

		int exampleCounter = 0;
		String current = null;
		while ((current = reader.readLine()) != null) {
			Map<Integer, Double> attributeMap = new LinkedHashMap<Integer, Double>();
			int a = 0;
			for (String attribute : current.split(" ")) {
				double value = Double.parseDouble(attribute);
				attributeMap.put(a, value);
				if ((a + 1) > dim)
					dim = (a + 1);
				a++;
			}
			atts[exampleCounter] = new double[attributeMap.size()];
			index[exampleCounter] = new int[attributeMap.size()];
			Iterator<Map.Entry<Integer, Double>> i = attributeMap.entrySet()
					.iterator();
			int attributeCounter = 0;
			while (i.hasNext()) {
				Map.Entry<Integer, Double> e = i.next();
				Integer indexValue = e.getKey();
				Double attributeValue = e.getValue();
				index[exampleCounter][attributeCounter] = indexValue.intValue();
				double value = attributeValue.doubleValue();
				atts[exampleCounter][attributeCounter] = value;
				attributeCounter++;
			}
			ys[exampleCounter] = -1;
			cs[exampleCounter] = c;
			ids[exampleCounter] = exampleCounter + "";

			exampleCounter++;
		}
	}

	/** Reads an example set from the given input stream. */
	public SVMExamples(ObjectInputStream in) throws IOException {
		this(in.readInt(), in.readDouble());
		this.dim = in.readInt();
		for (int e = 0; e < this.train_size; e++) {
			index[e] = new int[in.readInt()];
			atts[e] = new double[index[e].length];
			for (int a = 0; a < index[e].length; a++) {
				index[e][a] = in.readInt();
				atts[e][a] = in.readDouble();
			}
			alphas[e] = in.readDouble();
			cs[e] = in.readDouble();
			ys[e] = in.readDouble();
		}
	}

	public int getNumberOfSupportVectors() {
		int result = 0;
		for (int i = 0; i < alphas.length; i++)
			if (alphas[i] != 0.0d)
				result++;
		return result;
	}

	/** Writes the example set into the given output stream. */
	public void writeSupportVectors(ObjectOutputStream out) throws IOException {
		out.writeInt(getNumberOfSupportVectors());
		out.writeDouble(b);
		out.writeInt(dim);

		for (int e = 0; e < train_size; e++) {
			if (alphas[e] != 0.0d) {
				out.writeInt(atts[e].length);
				for (int a = 0; a < atts[e].length; a++) {
					out.writeInt(index[e][a]);
					out.writeDouble(atts[e][a]);
				}
				out.writeDouble(alphas[e]);
				out.writeDouble(cs[e]);
				out.writeDouble(ys[e]);
			}
		}
	}

	public void remove(List<Integer> positions) {
		if (!positions.isEmpty()) {
			train_size = train_size - positions.size();

			double new_atts[][] = new double[train_size][dim];
			int new_index[][] = new int[train_size][dim];
			double new_ys[] = new double[train_size];
			double new_cs[] = new double[train_size];
			double new_alphas[] = new double[train_size];
			String new_ids[] = new String[train_size];

			int j = 0;
			for (int i = 0; i < ids.length; i++) {
				if (!positions.contains(i)) {
					for (int l = 0; l < dim; l++) {
						new_atts[j][l] = atts[i][l];
						new_index[j][l] = index[i][l];
					}
					new_cs[j] = cs[i];
					new_ids[j] = j + "";
					new_ys[j] = ys[i];
					new_alphas[j] = alphas[i];
					j++;

				}
			}

			this.ys = new_ys;
			this.cs = new_cs;
			this.alphas = new_alphas;
			this.ids = new_ids;
			this.atts = new_atts;
			this.index = new_index;
		}
	}

	/**
	 * Counts the positive training examples
	 * 
	 * @return Number of positive examples
	 */
	public int count_pos_examples() {
		int result = 0;
		for (int i = 0; i < train_size; i++) {
			if (ys[i] > 0) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Gets the dimension of the examples
	 * 
	 * @return dim
	 */
	public int get_dim() {
		return dim;
	}

	public void set_dim(int d) {
		dim = d;
	}

	/**
	 * Gets an example.
	 * 
	 * This method is not thread safe. It always returns the same instance.
	 * 
	 * @param pos
	 *            Number of example
	 * @return Array of example attributes in their default order
	 */
	public SVMExample get_example(int pos) {
		x.att = atts[pos];
		x.index = index[pos];

		return x;
	}

	/**
	 * Gets an y-value.
	 * 
	 * @param pos
	 *            Number of example
	 * @return y
	 */
	public double get_y(int pos) {
		return ys[pos];
	}

	/** Sets the label value for the specified example. */
	public void set_y(int pos, double y) {
		ys[pos] = y;
	}

	/**
	 * Gets the y array
	 * 
	 * @return y
	 */
	public double[] get_ys() {
		return ys;
	}

	public void set_ys(double[] ys) {
		this.ys = ys;
	}

	/**
	 * Gets an c-value.
	 * 
	 * @param pos
	 *            Number of example
	 * @return c
	 */
	public double get_cs(int pos) {
		return cs[pos];
	}

	/** Sets the c value for the specified example. */
	public void set_cs(int pos, double c) {
		if (c > this.c)
			this.c = c;
		cs[pos] = c;
	}

	/**
	 * Gets the c array
	 * 
	 * @return c
	 */
	public double[] get_cs() {
		return cs;
	}

	/**
	 * Sets the c array
	 */
	public void set_cs(double[] cs) {
		this.c = -1;
		this.cs = cs;
	}

	/**
	 * Gets an alpha-value. Please note that the alpha values are already
	 * multiplied by the corresponding y-value.
	 * 
	 * @param pos
	 *            Number of example
	 * @return alpha
	 */
	public double get_alpha(int pos) {
		return alphas[pos];
	}

	/**
	 * Gets the alpha array. Please note that the alpha values are already
	 * multiplied by the corresponding y-value.
	 * 
	 * @return alpha
	 */
	public double[] get_alphas() {
		return alphas;
	}

	/**
	 * swap two training examples
	 * 
	 * @param pos1
	 * @param pos2
	 */
	public void swap(int pos1, int pos2) {
		double[] dummyA = atts[pos1];
		atts[pos1] = atts[pos2];
		atts[pos2] = dummyA;
		int[] dummyI = index[pos1];
		index[pos1] = index[pos2];
		index[pos2] = dummyI;
		double dummyd = alphas[pos1];
		alphas[pos1] = alphas[pos2];
		alphas[pos2] = dummyd;
		dummyd = ys[pos1];
		ys[pos1] = ys[pos2];
		ys[pos2] = dummyd;
		dummyd = cs[pos1];
		cs[pos1] = cs[pos2];
		cs[pos2] = dummyd;
	}

	/**
	 * get b
	 * 
	 * @return b
	 */
	public double get_b() {
		return b;
	}

	/**
	 * set b
	 * 
	 * @param new_b
	 */
	public void set_b(double new_b) {
		b = new_b;
	}
	
	/**
	 * Counts the training examples.
	 * 
	 * @return Number of examples
	 */
	public int count_examples() {
		return train_size;
	}

	/**
	 * sets an alpha value.
	 * 
	 * @param pos
	 *            Number of example
	 * @param alpha
	 *            New value
	 */
	public void set_alpha(int pos, double alpha) {
		alphas[pos] = alpha;
	}

	public void clearAlphas() {
		for (int i = 0; i < alphas.length; i++)
			alphas[i] = 0.0d;
	}

	public int getTrain_size() {
		return train_size;
	}

	public void setTrain_size(int train_size) {
		this.train_size = train_size;
	}

	public double getB() {
		return b;
	}

	public void setB(double b) {
		this.b = b;
	}

	public double getC() {
		if (c == -1) {
			for (double i : cs) {
				if (i > c)
					c = i;
			}
		}
		return c;
	}

	public void setC(double c) {
		this.c = c;
	}

	// ================================================================================

	public String getId(int index) {
		return ids[index];
	}
};
