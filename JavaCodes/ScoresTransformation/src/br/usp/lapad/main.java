package br.usp.lapad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

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
import de.lmu.ifi.dbs.elki.result.outlier.InvertedOutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierScoreMeta;
import de.lmu.ifi.dbs.elki.result.outlier.ProbabilisticOutlierScore;
import de.lmu.ifi.dbs.elki.result.outlier.QuotientOutlierScoreMeta;
import de.lmu.ifi.dbs.elki.utilities.scaling.outlier.OutlierGammaScaling;
import de.lmu.ifi.dbs.elki.utilities.scaling.outlier.OutlierLinearScaling;
import de.lmu.ifi.dbs.elki.utilities.scaling.outlier.OutlierScalingFunction;
import de.lmu.ifi.dbs.elki.utilities.scaling.outlier.StandardDeviationScaling;

public class main {

	public static void main(String[] args) throws IOException {
		String filein = args[0];
		String fileout = args[1];
		String algorithm = args[2];
		double min = Double.parseDouble(args[3]);
		double max = Double.POSITIVE_INFINITY;
		try {
			max = Double.parseDouble(args[4]);
		} catch (Exception e) {
			System.out.println(args[4] + " replaced for: " + Double.POSITIVE_INFINITY);
			max = Double.POSITIVE_INFINITY;
		}
		int k = Integer.parseInt(args[5]);
		int size = Integer.parseInt(args[6]);

		OutlierScoreMeta scoreMeta = null;
		switch (algorithm) {
		case "KNN":
			scoreMeta = new BasicOutlierScoreMeta(min, max, 0., Double.POSITIVE_INFINITY, 0.);
			break;
		case "KNNW":
			scoreMeta = new BasicOutlierScoreMeta(min, max, 0.0, Double.POSITIVE_INFINITY);
			break;
		case "LOF":
			scoreMeta = new QuotientOutlierScoreMeta(min, max, 0.0, Double.POSITIVE_INFINITY, 1.0);
			break;
		case "SimplifiedLOF":
			scoreMeta = new QuotientOutlierScoreMeta(min, max, 0., Double.POSITIVE_INFINITY, 1.);
			break;
		case "LoOP":
			scoreMeta = new ProbabilisticOutlierScore(min, max, 0.);
			break;
		case "LDOF":
			double LDOF_BASELINE = 0.5;
			scoreMeta = new QuotientOutlierScoreMeta(min, max, 0.0, Double.POSITIVE_INFINITY, LDOF_BASELINE);
			break;
		case "ODIN":
			double inc = 1. / (k - 1);
			scoreMeta = new InvertedOutlierScoreMeta(min, max, 0., inc * (size - 1), 1);
			break;
		case "FastABOD":
			scoreMeta = new InvertedOutlierScoreMeta(min, max, 0.0, Double.POSITIVE_INFINITY);
			break;
		case "KDEOS":
			scoreMeta = new ProbabilisticOutlierScore(min, max);
			break;
		case "LDF":
			double c = 0.1;
			scoreMeta = new BasicOutlierScoreMeta(min, max, 0.0, 1. / c, 1 / (1 + c));
			break;
		case "INFLO":
			scoreMeta = new QuotientOutlierScoreMeta(min, max, 0., Double.POSITIVE_INFINITY, 1.);
			break;
		case "COF":
			scoreMeta = new QuotientOutlierScoreMeta(min, max, 0.0, Double.POSITIVE_INFINITY, 1.0);
			break;
		default:
			System.err.println("Not find!");
			break;
		}

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
			try {
				if (algorithm.equals("ODIN") || algorithm.equals("FastABOD")) {
					data[i][0] = -Math.log(Double.parseDouble(line));
					s[i] = -Math.log(Double.parseDouble(line));
				} else {
					data[i][0] = Double.parseDouble(line);
					s[i] = Double.parseDouble(line);
				}
			} catch (Exception e) {
				System.out.println(e);
				System.out.println(line + " replaced for: " + Double.POSITIVE_INFINITY);
				data[i][0] = Double.POSITIVE_INFINITY;
			}
			scores.putDouble(iditer, s[i]);
			iditer.advance();
			i++;
		}
		reader.close();
		//Arrays.sort(s);

		//double median = .5 * (s[size >> 1] + s[(size + 1) >> 1]);

		DoubleRelation scoreResult = new MaterializedDoubleRelation(algorithm, algorithm, scores, rel.getDBIDs());
		OutlierResult result = new OutlierResult(scoreMeta, scoreResult);

		OutlierScalingFunction dist;
		if (algorithm.equals("LoOP") || algorithm.equals("KDEOS")) {
			dist = new OutlierLinearScaling(0.0, 1.0, true, false);
		} else {
			dist = new StandardDeviationScaling();
		}
			
		/*if (algorithm.equals("LDF") || algorithm.equals("COF")) {
			dist = new OutlierGammaScaling(false);
		} else {
			dist = new StandardDeviationScaling(median, 1);
		}*/
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
