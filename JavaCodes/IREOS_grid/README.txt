IREOS (Internal, Relative Evaluation of Outlier Solutions)

Implementation by Henrique O. Marques < hom@icmc.usp.br >

Original paper:
H.O. Marques, R.J.G.B. Campello, A. Zimek and J. Sander. On the Internal Evaluation of Unsupervised Outlier Detection. In Proceedings of the 27th International Conference on Scientific and Statistical Database Management (SSDBM), San Diego, CA, 2015.

Included in this distribution is an example data set (WBC_withoutdupl_norm) which consists of 223 objects, each with 9 attributes, this data set is a publicly available real world datasets from the UCI repository modified following the procedure described in the paper.
Also included are 11 outlier solutions of the data set (folder solutions) which the name of the file is the number of outliers correctly labeled according to the ground truth.


LICENSE:
========

	RapidMiner 5 License:

	This work uses pieces of RapidMiner code and part of them were modified. RapidMiner is a free software distribute under the terms of the GNU Affero General Public License, and the modifications were made under the terms of license. Below are the RapidMiner classes used and the modifications performed:  
	
	Unmodified classes:
		-com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample.java
		-com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.Kernel.java
		-com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.KernelRadial.java
		-com.rapidminer.operator.learner.functions.kernel.jmysvm.util.Cache.java

	Modified classes:
		-com.rapidminer.parameter.value.ParameterValueGrid.java - This class was modified on May 2015 in order not to work with operators.
		-com.rapidminer.operator.learner.functions.kernel.logistic.KLR.java - This class was modified on May 2015 in order to work with individual cost weights for each data instances and to not work with operators.
		-com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples.java - This class was modified on May 2015 in order to work with individual cost weights for each data instances, removed unused methods which may have dependencies from other RapidMiner classes and created a method to read a dataset from the given plain text file which the attributes are separated by single spaces.


	Apache Commons Math 3.5 License:

	This work uses Apache Commons Math in order to perform statistical tests. Apache Commons Math is a free software distribute under the terms of the Apache License 2.0 and the binary without modification is included in the folder "libs".


Usage:
======

-Create dataset model using dataset file (plain text format, like dataset example provided), the dataset size and cost weight used by KLR:
	-SVMExamples dataset = new SVMExamples(BufferedReader reader, int size, double c);

-Create the list of solutions to be evaluated, each list element must be a vector that represent if the correspondent dataset element is an outlier (1) or an inlier (-1):
	-List<int[]> ireosSolutions

-Initialize IREOS using the dataset and the list of solutions to be evaluated:
	-IREOS ireos = new IREOS(dataset, ireosSolutions);

-Set Gamma Maximum:
	-ireos.setGammaMax(double gammaMax);

-Or find a Gamma Maximum:
	-ireos.findGammaMax();

-Set the number of values that Gamma will be discretized:
	-ireos.setnGamma(int nGamma);

-Discretize Gamma from 0 to Gamma Maximum into the number of values established, the discretization can be 0 (linear), 1 (quadratic), 2 (logarithmic) or 3 (logarithmic (legacy):
	-ireos.setGammas(int scale);

-Set the maximum clump size:
	-ireos.setmCl(int mCl)

-Evaluate the solutions:
	-List<IREOSSolution> evaluatedSolutions = ireos.evaluateSolutions();

-Return the IREOS index:
	-evaluatedSolutions.get(i).getIREOS();


-Optionally:
============

-Compute IREOS statistics to the dataset in order to perform statistical tests and to adjust the index for chance:
	-IREOSStatistics stats = ireos.getStatistics();

-Set the IREOS statistics to the solution
	-evaluatedSolutions.get(i).setStatistics(stats);

-Return the Adjusted IREOS index:
	-evaluatedSolutions.get(i).getAdjustedIREOS()

-Return the z-test:
	-evaluatedSolutions.get(i).zTest()

-Return the t-test:
	-evaluatedSolutions.get(i).tTest()