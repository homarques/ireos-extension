# IREOS (Internal, Relative Evaluation of Outlier Solutions)

Implementation by Henrique O. Marques < homarques@ualberta.ca >

Original paper:

```latex
H. O. Marques, R. J. G. B. Campello, J. Sander, and A. Zimek.
Internal Evaluation of Unsupervised Outlier Detection. ACM Trans. Knowl. Discov. Data, Vol. 14, No. 4, Article 47, 2020.
```

https://doi.org/10.1145/3394053

Included in this distribution is an example data set (WBC_withoutdupl_norm) which consists of 223 objects, each of which has 9 attributes. This data set is a publicly available real world dataset from the UCI repository, modified following the procedure described in [[1]](#references). <br>
We are also including an example of how to evaluate outlier detection solutions in the file ```src/usp/icmc/lapad/Main.java```. <br>
Below, we explain the steps in this file.

## Loading dataset

The dataset used in the evaluation must be loaded as ```SVMExamples``` object. An object of this type can be instantiated using the constructor below:</br>

```java
SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader("/path/to/csv")), sizeOfDataset, C, separator);
```
where the first parameter is the path to the CSV file, the second is the size of the dataset (number of line of the CSV file), the third is the value of the constant penalty cost for soft margin violations, and the fourth is the field separator character of the CSV file.<br>
As discussed in our previous paper [[1]](#references), the value of C just needs to be large enough. For our experiments, we used C = 100.

## Generating outlier detection solutions

We are going to use ELKI [[2]](#references) to generate the outlier detection solutions to be evaluated.<br>
First, we need to initialize the dataset in the structure/object used by ELKI. It can be done as follow:<br>
```java
DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(dataset.atts);
Database db = new StaticArrayDatabase(dbc, null);
db.initialize();
```

where ```dataset.atts``` is the ```double[][]``` where our data is stored inside the ```SVMExamples``` loaded in the last section.

Now, we can run any outlier detection algorithm available on ELKI. In our example, we are going to use LOF [[3]](#references), but any other could be used.

```java
ListParameterization params = new ListParameterization();
params.addParameter(LOF.Parameterizer.K_ID, k);
Algorithm alg = ClassGenericsUtil.parameterizeOrAbort(LOF.class, params);
OutlierResult result = (OutlierResult) alg.run(db);
```

The three first line of code is to configure the parameter -- the number of neighbors (k) -- and the last line to run the algorithm.

##  Outlier scoring normalization

After having the outlier scorings, we need to normalize them before using IREOS. To normalize the outlier scorings, we use the framework proposed by Kriegel et al. [[4]](#references). Luckily for us, it is also available on ELKI [[2]](#references).
```java
List<double[]> scorings = new ArrayList<double[]>();
double[] scoring = new double[size];
for (DBIDIter iter = result.getScores().iterDBIDs(); iter.valid(); iter.advance())
    scoring[iter.internalGetIndex() - 1] = result.getOutlierMeta().normalizeScore(result.getScores().doubleValue(iter));
scorings.add(scoring);
 ```
 The outlier detection solutions to be evaluated must be stored in a ```List<double[]>``` where each element of the list is an outlier detection solution to be evaluated.
 
## Initializing IREOS
After loading the dataset and generating the outlier detection solutions, we can initialize IREOS passing as parameters the ```SVMExamples``` dataset and the solutions to be evaluated. 
 ```java
IREOS ireos = new IREOS(dataset, scorings);
```

## Setting gamma max (gamma_max)
In a traditional classification problem the parameter gamma controls the compromise between the performance of the classifier on the training data versus on test data (overfitting vs. underfitting, bias-variance trade-off). For unsupervised outlier evaluation, however, we are not interested at all in the classifier itself or its performance on new, unseen data. We use a classifier merely to measure the degree of difficulty when trying to discriminate between one individual candidate outlier and the other data objects.

We make use of the fact that separability of outliers is expected to be larger than the separability of inliers irrespective of gamma, whereby we avoid a particular choice of gamma by taking the integral over a range of meaningful values, from zero up to gamma_max. In this range, the value of gamma just needs to become big enough to discriminate each individual candidate outlier from the other data objects, so we use a single gamma_max for which this will be true for all the candidate outliers. Note that gamma_max could be further increased, the exact value is not important, but the higher the value after this point (where the candidate outliers become separable), the greater the loss of contrast in the area under the curve (because the curve will already have flattened), and there is also an extra and unnecessary computational effort involved. However, the index should not be severely affected by different values of gamma_max. For relative comparison, higher values for gamma_max lead to smaller contrast between good and bad solutions. For absolute (statistical) comparison, the expected value removed from the index (adjustment for chance) fixes the contrast between the solutions.

In our experiments, we use gamma_max as the value required by the classifier to separate from the other objects every object labeled as outlier in all solutions, i.e., objects with outlier probability > 50%. This value can be found using the following function:

 ```java
double gammaMax = Utils.findGammaMax(dataset, scorings);
ireos.setGammaMax(gammaMax);
```
The above approach can lead to excessively high values in case a very dense object is considered outlier by some candidate solution. Or, excessively low values in cases where only objects with more or less the same degree of outlierness are considered outliers by the candidate solutions. <br>
Again, higher values for gamma_max lead to smaller contrast between good and bad solutions, while lower values for gamma_max lead to smaller IREOS values, even for good solutions. We do not, however, expect inversions between good and bad solutions.

To avoid the solution-based approach to choosing gamma, a dataset-based approach can be used. For example, the gamma value can be chosen based on pairwise distance, as it is commonly used in literature to select useful gamma values, as in [[5]](#references).<br>
The above function uses [```DistanceQuantileSampler```](http://elki.dbs.ifi.lmu.de/releases/release0.7.5/doc/de/lmu/ifi/dbs/elki/algorithm/statistics/DistanceQuantileSampler.html) ELKI function to compute a quantile of a distance sample to be used as gamma_max.

 ```java
double gammaMax = Utils.findGammaMaxbyDistances(db, sampling, quantile);
ireos.setGammaMax(gammaMax);
```
  
## Setting maximum clump size (mCl)
Clumps, or particles, are subsets of objects lying in the same region of the data space, relatively closer to each other than they are from other objects, but too small to be deemed a cluster. Unfortunately, what is a clump can depend on both the application domain and a user's expectation on what they judge to be a set of similar objects that is "too small" to be interpreted as a cluster. Therefore, we provide the users with an optional control mechanism to adjust their expectations about what a maximum clump size (mCl) should be.

The use of mCl is optional (and as such, it does not need to be chosen, it can be chosen at the user's discretion). <br>
The choices in which mCl > 1 (which correspond to modelling clumps explicitly) exhibited, on average, the best results in our experiments. In the absence of further domain information, we recommend our rule-of-thumb heuristic mCl = √(5%sizeOfDataset), as this setup presented, on average, the best results in our experiments.
 ```java
ireos.setmCl(mCl);
```

## Setting tolerance error
By using the adaptive quadrature to compute separability curves the user has to specify an arbitrary tolerance error. This error represents a clear trade-off between the index error and computational cost.
 ```java
ireos.setTol(0.005);
```

## Evaluating solutions
To evaluate the solutions, we just need to use the method ```evaluateSolutions()```, as below.
 ```java
List<IREOSSolution> evaluatedSolutions = ireos.evaluateSolutions();
```

## Approximate IREOS
Alternatively, the index can be approximated using only k nearest neighbors of the object to compute its separability. For that, we just need to pass as parameter nearest neighbors matrix in the method ```evaluateSolutions()```, as below.

 ```java
int[][] knn = Utils.knn(db, k);
List<IREOSSolution> evaluatedSolutionsApprox = ireos.evaluateSolutions(knn);
```

## <a name="references">References</a>
[1] [H. O. Marques, R. J. G. B. Campello, A. Zimek and J. Sander. On the Internal Evaluation of Unsupervised Outlier Detection. In Proceedings of the 27th International Conference on Scientific and Statistical Database Management (SSDBM), 2015.](https://doi.org/10.1145/2791347.2791352)<br>
[2] [E. Schubert and A. Zimek. ELKI: A large open-source library for data analysis. ELKI Release 0.7.5 "Heidelberg".](https://elki-project.github.io/)<br>
[3] [M. M. Breunig, H.-P. Kriegel, R. T. Ng and J. Sander. LOF: Identifying Density-based Local Outliers. In Proceedings of the 2000 ACM SIGMOD International Conference on Management of Data (SIGMOD), 2000.](https://doi.org/10.1145/335191.335388)<br>
[4] [H.-P. Kriegel, P. Kröger, E. Schubert and A. Zimek. Interpreting and unifying outlier scores. In Proceedings of the 11th SIAM International Conference on Data Mining (SDM), 2011.](https://www.dbs.ifi.lmu.de/~zimek/publications/SDM2011/SDM11-outlier-preprint.pdf)
[5] [F. T. Liu, K. M. Ting and Z.-H. Zhou. Isolation-Based Anomaly Detection. Transactions on Knowledge Discovery from Data, 2012](https://doi.org/10.1145/2133360.2133363)
