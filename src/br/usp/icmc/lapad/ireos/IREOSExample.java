package br.usp.icmc.lapad.ireos;

import java.util.HashMap;

public class IREOSExample {

	/* Index of the observation in the dataset */
	private int index;

	private HashMap<Double, Double> separabilities;
	private double auc;

	/**
	 * Constructor class
	 * 
	 * @param index  Index of the observation in the dataset
	 * @param gammas Set of gammas used to evaluated the observation separability
	 */
	public IREOSExample(int index) {
		this.index = index;
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
	
	public HashMap<Double, Double> getSeparabilities() {
		return separabilities;
	}

	public void setSeparabilities(HashMap<Double, Double> separabilities) {
		this.separabilities = separabilities;
	}

	public double getAuc() {
		return auc;
	}

	public void setAuc(double auc) {
		this.auc = auc;
	}



}
