package br.usp.icmc.lapad.ireos;

import java.util.HashMap;

public class IREOSExample {

	/* Index of the observation in the dataset */
	private int index;
	private HashMap<Double, Double> gammaSeparability = new HashMap<>();
	private double auc;
	private double weight;

	/**
	 * Constructor class
	 * 
	 * @param index
	 *            Index of the observation in the dataset
	 * @param gammas
	 *            Set of gammas used to evaluated the observation separability
	 */
	public IREOSExample(int index, double weight, double auc) {
		this.index = index;
		this.weight = weight;
		this.auc = auc;
	}

	/**
	 * Get observation index in the dataset
	 * 
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Set observation index in the dataset
	 * 
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	public HashMap<Double, Double> getGammaSeparability() {
		return gammaSeparability;
	}

	public void setGammaSeparability(HashMap<Double, Double> gammaSeparability) {
		this.gammaSeparability = gammaSeparability;
	}

	public double getAuc() {
		return auc;
	}

	public void setAuc(double auc) {
		this.auc = auc;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

}
