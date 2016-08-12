package edu.asu.cse512;

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.SparkConf;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.ArrayList;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Coordinate;


public class ConvexHullOperation
{
	public static void geometryConvexHull(String inputLocation, String outputLocation) throws ClassNotFoundException
	{
		
		JavaSparkContext sc = convexHull.sparkContext;
		JavaRDD<String> lines = sc.textFile(inputLocation);
		JavaRDD<Coordinate> partitionMapping = lines.mapPartitions( new FlatMapFunction<Iterator<String>, Coordinate>() 
				{
					private static final long serialVersionUID = 1L;

					public Iterable<Coordinate> call(Iterator<String> coordinateIterator) throws Exception 
					{
						ArrayList<Coordinate> currentCoordinateList = new ArrayList<Coordinate>();
						for(;coordinateIterator.hasNext();)
						{
							String strTemp = coordinateIterator.next();
							String[] tempCoordArray = strTemp.split(",");
							
							Double coor_x,coor_y = 0.0;
							Coordinate coord = null;
							if(ConvexHullOperation.isNumber(tempCoordArray[0]) && ConvexHullOperation.isNumber(tempCoordArray[1]))
							{
								coor_x = Double.parseDouble(tempCoordArray[0]);
								coor_y = Double.parseDouble(tempCoordArray[1]);
								coord = new Coordinate(coor_x,coor_y);
							}
							if(coord != null)
								currentCoordinateList.add(coord);
								
						}
						GeometryFactory geometryFactor = new GeometryFactory();
						ConvexHull convexHull = new ConvexHull(currentCoordinateList.toArray(new Coordinate[currentCoordinateList.size()]), geometryFactor);
						Coordinate[] coordinateListArray  = convexHull.getConvexHull().getCoordinates();
						List<Coordinate> finalCoordinateList = new ArrayList<Coordinate>();
						for(Coordinate coor : coordinateListArray)
						{
							if(finalCoordinateList.contains(coor) == false)
								finalCoordinateList.add(coor);
						}
						return finalCoordinateList;
					}
				});
		
		//partitionMapping.saveAsTextFile(outputLocation);
		
		JavaRDD<Coordinate> partialReducedList = partitionMapping.repartition(1); //check for more number of partitions
		
		JavaRDD<Coordinate> finalReducedRRD = partialReducedList.mapPartitions( new FlatMapFunction<Iterator<Coordinate>, Coordinate>()
		{
			private static final long serialVersionUID = 1L;
			
			public Iterable<Coordinate> call(Iterator<Coordinate> allReducedListCoordinates)
			{
				ArrayList<Coordinate> intermediateCoordinateList = new ArrayList<Coordinate>();
				
				for(;allReducedListCoordinates.hasNext();)
				{
					Coordinate currentProcessingCoordinate = allReducedListCoordinates.next(); 
						//if(intermediateCoordinateList.contains(currentProcessingCoordinate) == false)
					intermediateCoordinateList.add(currentProcessingCoordinate);
				}
				
				GeometryFactory geom = new GeometryFactory();
				ConvexHull convexHull = new ConvexHull(intermediateCoordinateList.toArray(new Coordinate[intermediateCoordinateList.size()]), geom);
				Coordinate[] coordinateListArray = convexHull.getConvexHull().getCoordinates();
				List<Coordinate> finalCoordinateList = new ArrayList<Coordinate>();
				for(Coordinate coor : coordinateListArray)
				{
					if(finalCoordinateList.contains(coor) == false)
						finalCoordinateList.add(coor);
				}
				return finalCoordinateList;
			}
		});
		
		JavaRDD<String> finalSortedDataRDD = (JavaRDD<String>) finalReducedRRD.mapPartitions(new FlatMapFunction<Iterator<Coordinate>, String>()
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
		
		
		int index=outputLocation.lastIndexOf("/");
		String hadoopPath=outputLocation.substring(0, index);
		String intermediateResultpath=hadoopPath+"/IntermediateHullResult";

		FileToCsv.deleteExistingFile(intermediateResultpath);
		finalSortedDataRDD.saveAsTextFile(intermediateResultpath);
		
		FileToCsv.fileToCsv(intermediateResultpath, outputLocation);
		sc.close();
		

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