package br.usp.icmc.lapad.ireos;

public class IREOSExample {

	/* Index of the observation in the dataset */
	private int index;
	/* Observation separability for each gamma */
	private double separability[];
	/* Set of gammas used to evaluated the observation separability */
	private double gammas[];
	private boolean eval[];

	/**
	 * Constructor class
	 * 
	 * @param index
	 *            Index of the observation in the dataset
	 * @param gammas
	 *            Set of gammas used to evaluated the observation separability
	 */
	public IREOSExample(int index, double[] gammas) {
		this.index = index;
		this.setGammas(gammas);
		this.separability = new double[gammas.length];
		eval = new boolean[gammas.length];
		for (int i = 0; i < gammas.length; i++) {
			eval[0] = false;
		}
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

	/**
	 * Get the observation separability for each gamma
	 * 
	 */
	public double[] getSeparability() {
		return separability;
	}

	/**
	 * Set the observation separability to a determined gamma
	 * 
	 * @param separability
	 *            The observation separability for that gamma
	 * @param index
	 *            The gamma index in the vector gammas
	 */
	public void setSeparability(double separability, int index) {
		this.separability[index] = separability;
		eval[index] = true;
	}

	public void setSeparability(double[] separability) {
		this.separability = separability;
		for (int i = 0; i < gammas.length; i++) {
			eval[0] = true;
		}
	}

	/**
	 * Get the set of gammas used to evaluated the observation separability
	 *
	 */
	public double[] getGammas() {
		return gammas;
	}

	/**
	 * Set the set of gammas used to evaluated the observation separability
	 *
	 */
	public void setGammas(double gammas[]) {
		this.gammas = gammas;
	}

	public boolean isEval(int index) {
		return eval[index];
	}
}
