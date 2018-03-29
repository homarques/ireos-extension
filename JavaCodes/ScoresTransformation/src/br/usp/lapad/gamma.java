package br.usp.lapad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDoubleDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedDoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.result.outlier.BasicOutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.utilities.scaling.outlier.OutlierScalingFunction;

public class gamma {

	public static void main(String[] args) throws IOException {
		//String[] args = {"/home/henrique/ireos_extension/Datasets/Synthetic//scorings/gaussian20dim_4clusters_nr1/10",
		//		"/home/henrique/ireos_extension/Datasets/Synthetic//weight/gaussian20dim_4clusters_nr1/10", "2.333239",
		//		"7.082012", "3453", "10"};
		String filein = args[0];
		String fileout = args[1];
		double min = Double.parseDouble(args[2])*Double.parseDouble(args[2]);
		double max = Double.parseDouble(args[3])*Double.parseDouble(args[3]);
		int size = Integer.parseInt(args[4]);
		Double dim = Double.parseDouble(args[5]);

		OutlierScoreMeta scoreMeta = null;
		scoreMeta = new BasicOutlierScoreMeta(min, max, 0., Double.POSITIVE_INFINITY, 0.);

		BufferedReader reader = new BufferedReader(new FileReader(filein));

		String line;
		double data[][] = new double[size][1];
		double s[] = new double[size];
		int i = 0;

		DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
		Database db = new StaticArrayDatabase(dbc, null);
		db.initialize();

		Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
		WritableDoubleDataStore scores = DataStoreUtil.makeDoubleStorage(rel.getDBIDs(), DataStoreFactory.HINT_STATIC);

		DBIDIter iditer = rel.getDBIDs().iter();
		while ((line = reader.readLine()) != null) {
			data[i][0] = Double.parseDouble(line)*Double.parseDouble(line);
			s[i] = Double.parseDouble(line)*Double.parseDouble(line);
			scores.putDouble(iditer, s[i]);
			iditer.advance();
			i++;
		}
		reader.close();

		DoubleRelation scoreResult = new MaterializedDoubleRelation("KNN", "KNN", scores, rel.getDBIDs());
		OutlierResult result = new OutlierResult(scoreMeta, scoreResult);

		OutlierScalingFunction dist;
		dist = new OutlierGammaScaling(false, dim, 2.0);
		dist.prepare(result);

		FileWriter writer = new FileWriter(fileout);
		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		for (int j = 0; j < size; j++) {
			bufferedWriter.write(dist.getScaled(s[j]) + "\n");
		}
		bufferedWriter.flush();
		bufferedWriter.close();
	}
}
