HDBSCAN* code released ________.
Compiled using JRE 7.
The source code is provided as an eclipse project.

Implemented by Zachary Jullion (zjullion@ualberta.ca)
Original paper:
_________________REFERENCE TO PAPER__________________________

Included in this distribution is an example data set (example_data_set.csv) which consists of 500 objects, each with 2 attributes.
Also included is an example constraints file (example_constraints.csv) which has 10 constraints for the example data set given above.
This constraints file is an optional input for the algorithm.

DISCLAIMER: For any type of performance evaluation, the user must set the "compact" flag to true.

------------------------------------------------
Program help:

Executes the HDBSCAN* algorithm, which produces a hierarchy, cluster tree, flat partitioning, and outlier scores for an input data set.
Usage: java -jar HDBSCANStar.jar file=<input file> minPts=<minPts value> minClSize=<minClSize value> [constraints=<constraints file>] [compact={true,false}] [dist_function=<distance function>]
By default the hierarchy produced is non-compact (full), and euclidean distance is used.
Example usage: "java -jar HDBSCANStar.jar file=input.csv minPts=4 minClSize=4"
Example usage: "java -jar HDBSCANStar.jar file=collection.csv minPts=6 minClSize=1 constraints=collection_constraints.csv dist_function=manhattan"
Example usage: "java -jar HDBSCANStar.jar file=data_set.csv minPts=8 minClSize=8 compact=true"
In cases where the source is compiled, use the following: "java HDBSCANStarRunner file=data_set.csv minPts=8 minClSize=8 compact=true"

The input data set file must be a comma-separated value (CSV) file, where each line represents an object, with attributes separated by commas.
The algorithm will produce four files: the hierarchy, cluster tree, final flat partitioning, and outlier scores.

The hierarchy file will be named <input>_hierarchy.csv for a non-compact (full) hierarchy, and <input>_compact_hierarchy.csv for a compact hierarchy.
The hierarchy file will have the following format on each line:
<hierarchy scale (epsilon radius)>,<label for object 1>,<label for object 2>,...,<label for object n>
Noise objects are labelled zero.

The cluster tree file will be named <input>_tree.csv
The cluster tree file will have the following format on each line:
<cluster label>,<birth level>,<death level>,<stability>,<gamma>,<virtual child cluster gamma>,<character_offset>,<parent>
<character_offset> is the character offset of the line in the hierarchy file at which the cluster first appears.

The final flat partitioning file will be named <input>_partition.csv
The final flat partitioning file will have the following format on a single line:
<label for object 1>,<label for object 2>,...,<label for object n>

The outlier scores file will be named <input>_outlier_scores.csv
The outlier scores file will be sorted from 'most inlier' to 'most outlier', and will have the following format on each line:
<outlier score>,<object id>
<object id> is the zero-indexed line on which the object appeared in the input file.

The optional input constraints file can be used to provide constraints for the algorithm (semi-supervised flat partitioning extraction).
If this file is not given, only stability will be used to selected the most prominent clusters (unsupervised flat partitioning extraction).
This file must be a comma-separated value (CSV) file, where each line represents a constraint, with the two zero-indexed objects and type of constaint separated by commas.
Use 'ml' to specify a must-link constraint, and 'cl' to specify a cannot-link constraint.

The optional compact flag can be used to specify if the hierarchy saved to file should be the full or the compact one (this does not affect the final partitioning or cluster tree).
The full hierarchy includes all levels where objects change clusters or become noise, while the compact hierarchy only includes levels where clusters are born or die.

Possible values for the optional dist_function flag are:
euclidean: Euclidean Distance, d = sqrt((x1-y1)^2 + (x2-y2)^2 + ... + (xn-yn)^2)
cosine: Cosine Similarity, d = 1 - ((X·Y) / (||X||*||Y||))
pearson: Pearson Correlation, d = 1 - (cov(X,Y) / (std_dev(X) * std_dev(Y)))
manhattan: Manhattan Distance, d = |x1-y1| + |x2-y2| + ... + |xn-yn|
supremum: Supremum Distance, d = max[(x1-y1), (x2-y2), ... ,(xn-yn)]