# IREOS (Internal, Relative Evaluation of Outlier Solutions)

Implementation by Henrique O. Marques < homarques@ualberta.ca >

Original paper:

```latex
H. O. Marques, R. J. G. B. Campello, J. Sander, and A. Zimek.
Internal Evaluation of Unsupervised Outlier Detection. ACM Trans. Knowl. Discov. Data, Vol. 14, No. 4, Article 47, 2020.
```


https://doi.org/10.1145/3394053

Included in this distribution is an example data set (WBC_withoutdupl_norm) which consists of 223 objects, each with 9 attributes, this data set is a modified version of the publicly available real world datasets from the UCI repository.
Also it is included an evaluation example performed in the file src/usp/icmc/lapad/Main.java. 

## <a name="references">Loading dataset</a>

The dataset used in the evaluation must be loaded as ```SVMExamples``` object. An object of this type can be instantiated using the constructor below:</br>

```java
SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader("/path/to/csv")), sizeOfDataset, C, separator);
```

where the first parameter is the path to the CSV file, the second is the size of the dataset (number of line of the CSV file), the third is the value of the constant penalty cost for soft margin violations, and the fourth is the field separator character of the CSV file.<br>
As discussed in our previous paper [[1]](#references), the value of C just need to be large enough. For our experiments, we used C = 100.

## Generating outlier detection solutions

We are going to use ELKI [[2]](#references) to generate the outlier detection solutions to be evaluated.<br>
First, we need to initialize the dataset in the structure/object used by ELKI. It can be done as follow:<br>
```java
DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(dataset.atts);
Database db = new StaticArrayDatabase(dbc, null);
db.initialize();
```

where ```dataset.atts``` is the ```double[][]``` where our data is stored inside the ```SVMExamples``` object loaded in the last section.

Now, we can run any outlier detection algorithm available on ELKI. In our example, we are going to use LOF [[3]](#references).

```java
ListParameterization params = new ListParameterization();
params.addParameter(LOF.Parameterizer.K_ID, k);
Algorithm alg = ClassGenericsUtil.parameterizeOrAbort(LOF.class, params);
OutlierResult result = (OutlierResult) alg.run(db);
```

The three first line of code is to configure the parameter -- the number of neighbors (k) -- and the last line to run the algorithm.

##  Outlier scoring normalization

After having the outlier scorings, we need to normalize them before using IREOS. To normalize the outlier scorings we use the framework proposed by Kriegel et al. [[4]](#references). For our luck, it is also available on ELKI [[2]](#references).
```java
List<double[]> scorings = new ArrayList<double[]>();
double[] scoring = new double[size];
for (DBIDIter iter = result.getScores().iterDBIDs(); iter.valid(); iter.advance())
    scoring[iter.internalGetIndex() - 1] = result.getOutlierMeta().normalizeScore(result.getScores().doubleValue(iter));
scorings.add(scoring);
 ```
 
 The outlier detection solution to be evaluated must be stored in a ```List<double[]>``` where each element of the list is an outlier detection solution to be evaluated.
 
## Initializing IREOS
 To initialize IREOS
 
 ```java
IREOS ireos = new IREOS(dataset, scorings);
```
 
## Gamma max

 ```java
double gammaMax = Utils.findGammaMax(dataset, scorings);
ireos.setGammaMax(gammaMax);
```
  
## Maximum clump size
 ```java
ireos.setmCl(1);
```

## Tolerance
 ```java
ireos.setTol(0.005);
```

## Evaluating solutions
 ```java
List<IREOSSolution> evaluatedSolutions = ireos.evaluateSolutions();
```

## Approximate IREOS
 ```java
		int k = 100;
		int[][] knn = new int[dataset.getTrain_size()][k];
		Relation<NumberVector> relation = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
		KNNQuery<NumberVector> knnq = precomputedKNNQuery(db, relation, new EuclideanDistanceFunction(), k + 1);

		int i = 0;
		for (DBIDIter iter = relation.iterDBIDs(); iter.valid(); iter.advance()) {
			int j = 0;
			KNNList neighbors = knnq.getKNNForDBID(iter, k + 1);
			for (DoubleDBIDListIter neighbor = neighbors.iter(); neighbor.valid(); neighbor.advance()) {
				if (i == neighbor.internalGetIndex() - 1)
					continue;
				knn[i][j] = neighbor.internalGetIndex() - 1;
				j++;
				if (j >= k)
					break;
			}
			i++;
		}
```

 ```java
List<IREOSSolution> evaluatedSolutionsApprox = ireos.evaluateSolutions(knn);
```
 


## <a name="references">References</a>
[1] [H. O. Marques, R. J. G. B. Campello, A. Zimek and J. Sander. On the Internal Evaluation of Unsupervised Outlier Detection. In Proceedings of the 27th International Conference on Scientific and Statistical Database Management (SSDBM), San Diego, CA, 2015.](https://doi.org/10.1145/2791347.2791352)<br>
[2] [E. Schubert and A. Zimek. ELKI: A large open-source library for data analysis. ELKI Release 0.7.5 "Heidelberg".](https://elki-project.github.io/)<br>
[3] [M. M. Breunig, H.-P. Kriegel, R. T. Ng and J. Sander. LOF: Identifying Density-based Local Outliers. In Proceedings of the 2000 ACM SIGMOD International Conference on Management of Data (SIGMOD), 2000.](https://doi.org/10.1145/335191.335388)<br>
[4] [H.-P. Kriegel, P. Kröger, E. Schubert, and A. Zimek. Interpreting and unifying outlier scores. In Proceedings of the 11th SIAM International Conference on Data Mining (SDM), Mesa, AZ, 2011.](https://www.dbs.ifi.lmu.de/~zimek/publications/SDM2011/SDM11-outlier-preprint.pdf)

