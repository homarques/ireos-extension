# IREOS (Internal, Relative Evaluation of Outlier Solutions)

Implementation by Henrique O. Marques < homarques@ualberta.ca >

Original paper:

```H. O. Marques, R. J. G. B. Campello, J. Sander, and A. Zimek.``` </br>
```Internal Evaluation of Unsupervised Outlier Detection. ACM Trans. Knowl. Discov. Data, Vol. 14, No. 4, Article 47, 2020.``` </br>
https://doi.org/10.1145/3394053

Included in this distribution is an example data set (WBC_withoutdupl_norm) which consists of 223 objects, each with 9 attributes, this data set is a modified version of the publicly available real world datasets from the UCI repository.
Also it is included an evaluation example performed in the file src/usp/icmc/lapad/Main.java. 


## Loading dataset

The dataset used in the evaluation must be loaded as ```SVMExamples``` object. An object of this type can be instantiated using the constructor below:</br>
```SVMExamples dataset = new SVMExamples(new BufferedReader(new FileReader("/path/to/csv")), sizeOfDataset, C, separator);```<br>
where the first parameter is the path to the CSV file, the second is the size of the dataset (number of line of the CSV file), the third is the value of the constant penalty cost for soft margin violations, and the fourth is the field separator character of the CSV file.<br>
As discussed in our previous paper [[1]](#references), the value of C just need to be large enough, for our experiments we used C = 100.

## <a name="references">References</a>
