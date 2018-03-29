package br.usp.icmc.lapad.ireos;

public class IREOSSolution implements Comparable<IREOSSolution>{
	/* Observations labeled as outliers in this solution */
	public IREOSExample examples[];
	/* Gamma maximum */
	private double gammaMax;
	double tp;
	double tc;

	/* IREOS index */
	private double indexIREOS = -1;
	/* Area under the separability curve */
	private double auc = -1;
	private int[] sorted_rank;
	private int index;

	/**
	 * Constructor class
	 * 
	 * @param examples
	 *            Observations labeled as outliers in this solution
	 * @param rank 
	 */
	public IREOSSolution(IREOSExample[] examples, double gammaMax, int[] sorted_rank, int index) {
		this.examples = examples;
		this.gammaMax = gammaMax;
		this.sorted_rank = sorted_rank;
		this.index = index;
		
		float sumW = 0;
		for(int i = 0; i < examples.length; i++) {
			sumW += this.examples[i].getWeight();
		}
		
		for(int i = 0; i < examples.length; i++) {
			this.examples[i].setWeight(this.examples[i].getWeight()/sumW);
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Compute and get IREOS index for this solution
	 * 
	 */
	public double getIREOS() {
		if (indexIREOS == -1)
			indexIREOS = getAUC(examples.length) / gammaMax;
		return indexIREOS;
	}

	/**
	 * Get area under the separability curve of this solution
	 * 
	 */
	public double getAUC(int cutoff) {
		auc = 0;
		for (int i = 0; i <= cutoff; i++) {
				auc += examples[sorted_rank[i]].getWeight() * examples[sorted_rank[i]].getAuc();
		}

		return auc;
	}
	
	public double getTp() {
		return tp;
	}

	public void setTp(double tp) {
		this.tp = tp;
	}

	public double getTc() {
		return tc;
	}

	public void setTc(double tc) {
		this.tc = tc;
	}
	
	@Override
	public int compareTo(IREOSSolution o)
	{
        return new Double(tc).compareTo(o.tc);

	}

}
