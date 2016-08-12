package edu.asu.cse512;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import com.vividsolutions.jts.geom.Coordinate;

public class FarthestPoint {

	public static void geometryFarthestPair(String inputLocation,String outputLocation) throws ClassNotFoundException

	{
		JavaSparkContext sparkContext = FarthestPair.sparkContext;
		int index=outputLocation.lastIndexOf("/");
		String hadoopPath=outputLocation.substring(0, index);
		String intermediateResultpath=hadoopPath+"/IntermediateHullResult";

		JavaRDD<Coordinate> finalReducedRRD=FarthestPairConvexHullOperation.geometryConvexHull(inputLocation, intermediateResultpath);

		List<Coordinate> final_array = finalReducedRRD.toArray();

		Coordinate t1, t2, final1 = null, final2 = null;

		double max_distance = 0;

		for (int i = 0;i < final_array.size();i++)
		{
			t1 = final_array.get(i);
			double x1 = t1.x;
			double y1 = t1.y;

			for (int j = 0;j < final_array.size();j++)
			{
				t2 = final_array.get(j);
				double x2 = t2.x;
				double y2 = t2.y;

				double xcor_diff = x1 - x2;
				double xcor_square = Math.pow(xcor_diff, 2);

				double ycor_diff = y1 - y2;
				double ycor_square = Math.pow(ycor_diff, 2);

				double distance = Math.sqrt(xcor_square + ycor_square);

				if (distance >= max_distance)
				{
					final1 = t1;
					final2 = t2;
					max_distance = distance;
				}
				else
					continue;
			}
		}

		List<Coordinate> final_list = new ArrayList<Coordinate>();
		final_list.add(final1);
		final_list.add(final2);
		JavaRDD<Coordinate> final_output= sparkContext.parallelize(final_list).repartition(1);
		
		JavaRDD<String> finalSortedDataRDD = (JavaRDD<String>) final_output.mapPartitions(new FlatMapFunction<Iterator<Coordinate>, String>()
		{			
			private static final long serialVersionUID = 1L;
			ArrayList<String> finalSortedCoordinateList = new ArrayList<String>();
			Map<Double, Double>sortedCoordinates = new TreeMap<Double, Double>();
			
			public Iterable<String> call(Iterator<Coordinate> allReducedListCoordinates) throws Exception
			{
				for(;allReducedListCoordinates.hasNext();)
				{
					Coordinate a = allReducedListCoordinates.next();
					sortedCoordinates.put(a.x, a.y);
				}
				for(Map.Entry<Double,Double> entry : sortedCoordinates.entrySet()) {
					  Double key = entry.getKey();
					  Double value = entry.getValue();
					  finalSortedCoordinateList.add(key + "," + value);
				}
				return finalSortedCoordinateList;
			}
		});
		

		FileToCsv.deleteExistingFile(intermediateResultpath);

		index=outputLocation.lastIndexOf("/");
		hadoopPath=outputLocation.substring(0, index);
		intermediateResultpath=hadoopPath+"/IntermediateFarthestResult";


		FileToCsv.deleteExistingFile(intermediateResultpath);
		finalSortedDataRDD.saveAsTextFile(intermediateResultpath);

		FileToCsv.fileToCsv(intermediateResultpath, outputLocation);
		sparkContext.close();
	}

	public static boolean isNumber(String s) 
	{
		try { 
			Double.parseDouble(s); 
		} catch(NumberFormatException e) { 
			return false; 
		}
		return true;
	}

}

