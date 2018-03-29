package ca.ualberta.cs.hdbscanstar;

import java.io.IOException;
import java.util.ArrayList;

import ca.ualberta.cs.distance.CosineSimilarity;
import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.distance.ManhattanDistance;
import ca.ualberta.cs.distance.PearsonCorrelation;
import ca.ualberta.cs.distance.SupremumDistance;

/**
 * Entry point for the HDBSCAN* algorithm.
 * @author zjullion
 */
public class HDBSCANStarRunner {
	
	private static final String FILE_FLAG = "file=";
	private static final String CONSTRAINTS_FLAG = "constraints=";
	private static final String MIN_PTS_FLAG = "minPts=";
	private static final String MIN_CL_SIZE_FLAG = "minClSize=";
	private static final String COMPACT_FLAG = "compact=";
	private static final String DISTANCE_FUNCTION_FLAG = "dist_function=";

	private static final String EUCLIDEAN_DISTANCE = "euclidean";
	private static final String COSINE_SIMILARITY = "cosine";
	private static final String PEARSON_CORRELATION = "pearson";
	private static final String MANHATTAN_DISTANCE = "manhattan";
	private static final String SUPREMUM_DISTANCE = "supremum";

	/**
	 * Runs the HDBSCAN* algorithm given an input data set file and a value for minPoints and
	 * minClusterSize.  Note that the input file must be a comma-separated value (CSV) file, and
	 * that all of the output files will be CSV files as well.  The flags "file=", "minPts=",
	 * "minClSize=", "constraints=", and "distance_function=" should be used to specify the input 
	 * data set file, value for minPoints, value for minClusterSize, input constraints file, and 
	 * the distance function to use, respectively.
	 * @param args The input arguments for the algorithm
	 */
	public static void main(String[] args) {

		long overallStartTime = System.currentTimeMillis();
		
		//Parse input parameters from program arguments:
		HDBSCANStarParameters parameters = checkInputParameters(args);
		
		System.out.println("Running HDBSCAN* on " + parameters.inputFile + " with minPts=" + parameters.minPoints + 
				", minClSize=" + parameters.minClusterSize + ", constraints=" + parameters.constraintsFile + 
				", compact=" + parameters.compactHierarchy + ", dist_function=" + parameters.distanceFunction.getName());
		
		//Read in input file:
		double[][] dataSet = null;
		try {
			dataSet = HDBSCANStar.readInDataSet(parameters.inputFile, ",");		
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}
		int numPoints = dataSet.length;
		
		//Read in constraints:
		ArrayList<Constraint> constraints = null;
		if (parameters.constraintsFile != null) {
			try {
				constraints = HDBSCANStar.readInConstraints(parameters.constraintsFile, ",");
			}
			catch (IOException e) {
				System.err.println("Error reading constraints file.");
				System.exit(-1);
			}
		}

		//Compute core distances:
		long startTime = System.currentTimeMillis();
		double[] coreDistances = HDBSCANStar.calculateCoreDistances(dataSet, parameters.minPoints, parameters.distanceFunction);
		System.out.println("Time to compute core distances (ms): " + (System.currentTimeMillis() - startTime));

		//Calculate minimum spanning tree:
		startTime = System.currentTimeMillis();
		UndirectedGraph mst = HDBSCANStar.constructMST(dataSet, coreDistances, true, parameters.distanceFunction);
		mst.quicksortByEdgeWeight();
		System.out.println("Time to calculate MST (ms): " + (System.currentTimeMillis() - startTime));

		//Remove references to unneeded objects:
		dataSet = null;
		
		double[] pointNoiseLevels = new double[numPoints];
		int[] pointLastClusters = new int[numPoints];

		//Compute hierarchy and cluster tree:
		ArrayList<Cluster> clusters = null;
		try {
			startTime = System.currentTimeMillis();
			clusters = HDBSCANStar.computeHierarchyAndClusterTree(mst, parameters.minClusterSize, 
					parameters.compactHierarchy, constraints, parameters.hierarchyFile, 
					parameters.clusterTreeFile, ",", pointNoiseLevels, pointLastClusters);
			System.out.println("Time to compute hierarchy and cluster tree (ms): " + (System.currentTimeMillis() - startTime));
		}
		catch (IOException ioe) {
			System.err.println("Error writing to hierarchy file or cluster tree file.");
			System.exit(-1);
		}

		//Remove references to unneeded objects:
		mst = null;
		
		//Propagate clusters:
		boolean infiniteStability = HDBSCANStar.propagateTree(clusters);

		//Compute final flat partitioning:
		try {
			startTime = System.currentTimeMillis();
			HDBSCANStar.findProminentClusters(clusters, parameters.hierarchyFile, parameters.partitionFile, 
					",", numPoints, infiniteStability);
			System.out.println("Time to find flat result (ms): " + (System.currentTimeMillis() - startTime));
		}
		catch (IOException ioe) {
			System.err.println("Error writing to partitioning file.");
			System.exit(-1);
		}
		
		//Compute outlier scores for each point:
		try {
			startTime = System.currentTimeMillis();
			HDBSCANStar.calculateOutlierScores(clusters, pointNoiseLevels, pointLastClusters, 
					coreDistances, parameters.outlierScoreFile, ",", infiniteStability);
			System.out.println("Time to compute outlier scores (ms): " + (System.currentTimeMillis() - startTime));
		}
		catch (IOException ioe) {
			System.err.println("Error writing to outlier score file.");
			System.exit(-1);
		}
		
		System.out.println("Overall runtime (ms): " + (System.currentTimeMillis() - overallStartTime));
	}


	/**
	 * Parses out the input parameters from the program arguments.  Prints out a help message and
	 * exits the program if the parameters are incorrect.
	 * @param args The input arguments for the program
	 * @return Input parameters for HDBSCAN*
	 */
	private static HDBSCANStarParameters checkInputParameters(String[] args) {
		HDBSCANStarParameters parameters = new HDBSCANStarParameters();
		parameters.distanceFunction = new EuclideanDistance();
		parameters.compactHierarchy = false;

		//Read in the input arguments and assign them to variables:
		for (String argument : args) {

			//Assign input file:
			if (argument.startsWith(FILE_FLAG) && argument.length() > FILE_FLAG.length())
				parameters.inputFile = argument.substring(FILE_FLAG.length());
			
			//Assign constraints file:
			if (argument.startsWith(CONSTRAINTS_FLAG) && argument.length() > CONSTRAINTS_FLAG.length())
				parameters.constraintsFile = argument.substring(CONSTRAINTS_FLAG.length());

			//Assign minPoints:
			else if (argument.startsWith(MIN_PTS_FLAG) && argument.length() > MIN_PTS_FLAG.length()) {
				try {
					parameters.minPoints = Integer.parseInt(argument.substring(MIN_PTS_FLAG.length()));
				}
				catch (NumberFormatException nfe) {
					System.out.println("Illegal value for minPts.");
				}
			}

			//Assign minClusterSize:
			else if (argument.startsWith(MIN_CL_SIZE_FLAG) && argument.length() > MIN_CL_SIZE_FLAG.length()) {
				try {
					parameters.minClusterSize = Integer.parseInt(argument.substring(MIN_CL_SIZE_FLAG.length()));
				}
				catch (NumberFormatException nfe) {
					System.out.println("Illegal value for minClSize.");
				}
			}
			
			//Assign compact hierarchy:
			else if (argument.startsWith(COMPACT_FLAG) && argument.length() > COMPACT_FLAG.length()) {
				parameters.compactHierarchy = Boolean.parseBoolean(argument.substring(COMPACT_FLAG.length()));
			}

			//Assign distance function:
			else if (argument.startsWith(DISTANCE_FUNCTION_FLAG) && argument.length() > DISTANCE_FUNCTION_FLAG.length()) {
				String functionName = argument.substring(DISTANCE_FUNCTION_FLAG.length());
				
				if (functionName.equals(EUCLIDEAN_DISTANCE))
					parameters.distanceFunction = new EuclideanDistance();
				else if (functionName.equals(COSINE_SIMILARITY))
					parameters.distanceFunction = new CosineSimilarity();
				else if (functionName.equals(PEARSON_CORRELATION))
					parameters.distanceFunction = new PearsonCorrelation();
				else if (functionName.equals(MANHATTAN_DISTANCE))
					parameters.distanceFunction = new ManhattanDistance();
				else if (functionName.equals(SUPREMUM_DISTANCE))
					parameters.distanceFunction = new SupremumDistance();
				else
					parameters.distanceFunction = null;
			}
		}

		//Check that each input parameter has been assigned:
		if (parameters.inputFile == null) {
			System.out.println("Missing input file name.");
			printHelpMessageAndExit();
		}
		else if (parameters.minPoints == null) {
			System.out.println("Missing value for minPts.");
			printHelpMessageAndExit();
		}
		else if (parameters.minClusterSize == null) {
			System.out.println("Missing value for minClSize");
			printHelpMessageAndExit();
		}
		else if (parameters.distanceFunction == null) {
			System.out.println("Missing distance function.");
			printHelpMessageAndExit();
		}
		
		//Generate names for output files:
		String inputName = parameters.inputFile;
		if (parameters.inputFile.contains("."))
			inputName = parameters.inputFile.substring(0, parameters.inputFile.lastIndexOf("."));
		
		if (parameters.compactHierarchy)
			parameters.hierarchyFile = inputName + "_compact_hierarchy.csv";
		else
			parameters.hierarchyFile = inputName + "_hierarchy.csv";
		parameters.clusterTreeFile = inputName + "_tree.csv";
		parameters.partitionFile = inputName + "_partition.csv";
		parameters.outlierScoreFile = inputName + "_outlier_scores.csv";
		
		return parameters;
	}


	/**
	 * Prints a help message that explains the usage of HDBSCANStarRunner, and then exits the program.
	 */
	private static void printHelpMessageAndExit() {
		System.out.println();
		
		System.out.println("Executes the HDBSCAN* algorithm, which produces a hierarchy, cluster tree, " +
				"flat partitioning, and outlier scores for an input data set.");
		System.out.println("Usage: java -jar HDBSCANStar.jar file=<input file> minPts=<minPts value> " + 
				"minClSize=<minClSize value> [constraints=<constraints file>] [compact={true,false}] " + 
				"[dist_function=<distance function>]");
		System.out.println("By default the hierarchy produced is non-compact (full), and euclidean distance is used.");
		System.out.println("Example usage: \"java -jar HDBSCANStar.jar file=input.csv minPts=4 minClSize=4\"");
		System.out.println("Example usage: \"java -jar HDBSCANStar.jar file=collection.csv minPts=6 minClSize=1 " + 
				"constraints=collection_constraints.csv dist_function=manhattan\"");
		System.out.println("Example usage: \"java -jar HDBSCANStar.jar file=data_set.csv minPts=8 minClSize=8 " + 
				"compact=true\"");
		System.out.println("In cases where the source is compiled, use the following: \"java HDBSCANStarRunner " +
				"file=data_set.csv minPts=8 minClSize=8 compact=true\"");
		System.out.println();
		
		System.out.println("The input data set file must be a comma-separated value (CSV) file, where each line " +
				"represents an object, with attributes separated by commas.");
		System.out.println("The algorithm will produce four files: the hierarchy, cluster tree, final " +
				"flat partitioning, and outlier scores.");
		System.out.println();

		System.out.println("The hierarchy file will be named <input>_hierarchy.csv for a non-compact " + 
				"(full) hierarchy, and <input>_compact_hierarchy.csv for a compact hierarchy.");
		System.out.println("The hierarchy file will have the following format on each line:");
		System.out.println("<hierarchy scale (epsilon radius)>,<label for object 1>,<label for object 2>,...,<label for object n>");
		System.out.println("Noise objects are labelled zero.");
		System.out.println();

		System.out.println("The cluster tree file will be named <input>_tree.csv");
		System.out.println("The cluster tree file will have the following format on each line:");
		System.out.println("<cluster label>,<birth level>,<death level>,<stability>,<gamma>," + 
				"<virtual child cluster gamma>,<character_offset>,<parent>");
		System.out.println("<character_offset> is the character offset of the line in the hierarchy " + 
				"file at which the cluster first appears.");
		System.out.println();

		System.out.println("The final flat partitioning file will be named <input>_partition.csv");
		System.out.println("The final flat partitioning file will have the following format on a single line:");
		System.out.println("<label for object 1>,<label for object 2>,...,<label for object n>");
		System.out.println();
		
		System.out.println("The outlier scores file will be named <input>_outlier_scores.csv");
		System.out.println("The outlier scores file will be sorted from 'most inlier' to 'most outlier', " + 
				"and will have the following format on each line:");
		System.out.println("<outlier score>,<object id>");
		System.out.println("<object id> is the zero-indexed line on which the object appeared in the input file.");
		System.out.println();
		
		System.out.println("The optional input constraints file can be used to provide constraints for " + 
				"the algorithm (semi-supervised flat partitioning extraction).");
		System.out.println("If this file is not given, only stability will be used to selected the " + 
				"most prominent clusters (unsupervised flat partitioning extraction).");
		System.out.println("This file must be a comma-separated value (CSV) file, where each line " +
				"represents a constraint, with the two zero-indexed objects and type of constaint " +
				"separated by commas.");
		System.out.println("Use 'ml' to specify a must-link constraint, and 'cl' to specify a cannot-link constraint.");
		System.out.println();
		
		System.out.println("The optional compact flag can be used to specify if the hierarchy saved to file " +
				"should be the full or the compact one (this does not affect the final partitioning or cluster tree).");
		System.out.println("The full hierarchy includes all levels where objects change clusters or " + 
				"become noise, while the compact hierarchy only includes levels where clusters are born or die.");
		System.out.println();

		System.out.println("Possible values for the optional dist_function flag are:");
		System.out.println("euclidean: Euclidean Distance, d = sqrt((x1-y1)^2 + (x2-y2)^2 + ... + (xn-yn)^2)");
		System.out.println("cosine: Cosine Similarity, d = 1 - ((X·Y) / (||X||*||Y||))");
		System.out.println("pearson: Pearson Correlation, d = 1 - (cov(X,Y) / (std_dev(X) * std_dev(Y)))");
		System.out.println("manhattan: Manhattan Distance, d = |x1-y1| + |x2-y2| + ... + |xn-yn|");
		System.out.println("supremum: Supremum Distance, d = max[(x1-y1), (x2-y2), ... ,(xn-yn)]");
		System.out.println();

		System.exit(0);
	}


	/**
	 * Simple class for storing input parameters.
	 */
	private static class HDBSCANStarParameters {
		public String inputFile;
		public String constraintsFile;
		public Integer minPoints;
		public Integer minClusterSize;
		public boolean compactHierarchy;
		public DistanceCalculator distanceFunction;
		
		public String hierarchyFile;
		public String clusterTreeFile;
		public String partitionFile;
		public String outlierScoreFile;
	}
}