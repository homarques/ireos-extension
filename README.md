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

## Loading dataset

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

where ```dataset.atts``` is a ```double[][]``` where our data is stored inside the ```SVMExamples``` object loaded in the last section.





## <a name="references">References</a>
[1] [H. O. Marques, R. J. G. B. Campello, A. Zimek and J. Sander. On the Internal Evaluation of Unsupervised Outlier Detection. In Proceedings of the 27th International Conference on Scientific and Statistical Database Management (SSDBM), San Diego, CA, 2015.](https://doi.org/10.1145/2791347.2791352)<br>
[2] [E. Schubert and A. Zimek. ELKI: A large open-source library for data analysis. ELKI Release 0.7.5 "Heidelberg".](https://elki-project.github.io/)<br>
[3] [M. M. Breunig, H.-P. Kriegel, R. T. Ng and J. Sander. LOF: Identifying Density-based Local Outliers. In Proceedings of the 2000 ACM SIGMOD International Conference on Management of Data (SIGMOD), 2000.](https://doi.org/10.1145/335191.335388)<br>

