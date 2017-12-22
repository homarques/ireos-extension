package br.usp.icmc.lapad.ireos;

import java.util.TreeMap;

public class IREOSkNN {
	
	int index;
	//index & distance
	TreeMap<Float, Integer> neighbourhood = new TreeMap<>();

	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public TreeMap<Float, Integer> getNeighbourhood() {
		return neighbourhood;
	}
	public void setNeighbourhood(TreeMap<Float, Integer> neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
	

}
