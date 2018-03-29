package ca.ualberta.cs.hdbscanstar;

import java.util.ArrayList;

/**
 * An undirected graph, with weights assigned to each edge.  Vertices in the graph are 0 indexed.
 * @author zjullion
 */
public class UndirectedGraph {

	// ------------------------------ PRIVATE VARIABLES ------------------------------
	
	private int numVertices;
	private int[] verticesA;
	private int[] verticesB;
	private double[] edgeWeights;
	private Object[] edges;		//Each Object in this array in an ArrayList<Integer>

	
	// ------------------------------ CONSTANTS ------------------------------

	// ------------------------------ CONSTRUCTORS ------------------------------
	
	/**
	 * Constructs a new UndirectedGraph, including creating an edge list for each vertex from the 
	 * vertex arrays.  For an index i, verticesA[i] and verticesB[i] share an edge with weight
	 * edgeWeights[i].
	 * @param numVertices The number of vertices in the graph (indexed 0 to numVertices-1)
	 * @param verticesA An array of vertices corresponding to the array of edges
	 * @param verticesB An array of vertices corresponding to the array of edges
	 * @param edgeWeights An array of edges corresponding to the arrays of vertices
	 */
	public UndirectedGraph(int numVertices, int[] verticesA, int[] verticesB, double edgeWeights[]) {
		this.numVertices = numVertices;
		this.verticesA = verticesA;
		this.verticesB = verticesB;
		this.edgeWeights = edgeWeights;
		
		this.edges = new Object[numVertices];
		for (int i = 0; i < this.edges.length; i++) {
			this.edges[i] = new ArrayList<Integer>(1 + edgeWeights.length/numVertices);
		}
		
		for (int i = 0; i < edgeWeights.length; i++) {
			int vertexOne = this.verticesA[i];
			int vertexTwo = this.verticesB[i];
			((ArrayList<Integer>)(this.edges[vertexOne])).add(vertexTwo);
			if (vertexOne != vertexTwo)
				((ArrayList<Integer>)(this.edges[vertexTwo])).add(vertexOne);
		}
	}

	
	// ------------------------------ PUBLIC METHODS ------------------------------
	
	/**
	 * Quicksorts the graph by edge weight in descending order.  This quicksort implementation is 
	 * iterative and in-place.
	 */
	public void quicksortByEdgeWeight() {
		if (this.edgeWeights.length <= 1)
			return;
		
		int[] startIndexStack = new int[this.edgeWeights.length/2];
		int[] endIndexStack = new int[this.edgeWeights.length/2];
		
		startIndexStack[0] = 0;
		endIndexStack[0] = this.edgeWeights.length-1;
		int stackTop = 0;
		
		while (stackTop >= 0) {
			int startIndex = startIndexStack[stackTop];
			int endIndex = endIndexStack[stackTop];
			stackTop--;
			
			int pivotIndex = this.selectPivotIndex(startIndex, endIndex);
			pivotIndex = this.partition(startIndex, endIndex, pivotIndex);
			
			if (pivotIndex > startIndex+1) {
				startIndexStack[stackTop+1] = startIndex;
				endIndexStack[stackTop+1] = pivotIndex-1;
				stackTop++;
			}
			
			if (pivotIndex < endIndex-1) {
				startIndexStack[stackTop+1] = pivotIndex+1;
				endIndexStack[stackTop+1] = endIndex;
				stackTop++;
			}
		}
	}
	

	// ------------------------------ PRIVATE METHODS ------------------------------
	
	/**
	 * Quicksorts the graph in the interval [startIndex, endIndex] by edge weight.
	 * @param startIndex The lowest index to be included in the sort
	 * @param endIndex The highest index to be included in the sort
	 */
	private void quicksort(int startIndex, int endIndex) {
		if (startIndex < endIndex) {
			int pivotIndex = this.selectPivotIndex(startIndex, endIndex);
			pivotIndex = this.partition(startIndex, endIndex, pivotIndex);
			this.quicksort(startIndex, pivotIndex-1);
			this.quicksort(pivotIndex+1, endIndex);
		}
	}
	
	
	/**
	 * Returns a pivot index by finding the median of edge weights between the startIndex, endIndex,
	 * and middle.
	 * @param startIndex The lowest index from which the pivot index should come
	 * @param endIndex The highest index from which the pivot index should come
	 * @return A pivot index
	 */
	private int selectPivotIndex(int startIndex, int endIndex) {
		if (startIndex - endIndex <= 1)
			return startIndex;
		
		double first = this.edgeWeights[startIndex];
		double middle = this.edgeWeights[startIndex + (endIndex-startIndex)/2];
		double last = this.edgeWeights[endIndex];
		
		if (first <= middle) {
			if (middle <= last)
				return startIndex + (endIndex-startIndex)/2;
			else if (last >= first)
				return endIndex;
			else
				return startIndex;
		}
		else {
			if (first <= last)
				return startIndex;
			else if (last >= middle)
				return endIndex;
			else
				return startIndex + (endIndex-startIndex)/2;
		}
	}

	
	/**
	 * Partitions the array in the interval [startIndex, endIndex] around the value at pivotIndex.
	 * @param startIndex The lowest index to  partition
	 * @param endIndex The highest index to partition
	 * @param pivotIndex The index of the edge weight to partition around
	 * @return The index position of the pivot edge weight after the partition
	 */
	private int partition(int startIndex, int endIndex, int pivotIndex) {
		double pivotValue = this.edgeWeights[pivotIndex];
		this.swapEdges(pivotIndex, endIndex);
		int lowIndex = startIndex;
		
		for (int i = startIndex; i < endIndex; i++) {
			if (this.edgeWeights[i] < pivotValue) {
				this.swapEdges(i, lowIndex);
				lowIndex++;
			}
		}
		
		this.swapEdges(lowIndex, endIndex);
		return lowIndex;
	}
	
	
	/**
	 * Swaps the vertices and edge weights between two index locations in the graph.
	 * @param indexOne The first index location
	 * @param indexTwo The second index location
	 */
	private void swapEdges(int indexOne, int indexTwo) {
		if (indexOne == indexTwo)
			return;
		
		int tempVertexA = this.verticesA[indexOne];
		int tempVertexB = this.verticesB[indexOne];
		double tempEdgeDistance = this.edgeWeights[indexOne];
		
		this.verticesA[indexOne] = this.verticesA[indexTwo];
		this.verticesB[indexOne] = this.verticesB[indexTwo];
		this.edgeWeights[indexOne] = this.edgeWeights[indexTwo];
		
		this.verticesA[indexTwo] = tempVertexA;
		this.verticesB[indexTwo] = tempVertexB;
		this.edgeWeights[indexTwo] = tempEdgeDistance;
	}

	
	// ------------------------------ GETTERS & SETTERS ------------------------------
	
	public int getNumVertices() {
		return this.numVertices;
	}
	
	public int getNumEdges() {
		return this.edgeWeights.length;
	}

	public int getFirstVertexAtIndex(int index) {
		return this.verticesA[index];
	}
	
	public int getSecondVertexAtIndex(int index) {
		return this.verticesB[index];
	}
	
	public double getEdgeWeightAtIndex(int index) {
		return this.edgeWeights[index];
	}
	
	public ArrayList<Integer> getEdgeListForVertex(int vertex) {
		return (ArrayList<Integer>)this.edges[vertex];
	}
}