package edu.asu.cse512;


import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import scala.collection.Seq;

public class convexHull {
	public static JavaSparkContext sparkContext;

	public static void main(String[] args) {
		try {
			SparkConf sparkConf = new SparkConf().setAppName("Group10")
					.set("spark.driver.allowMultipleContexts", "true");
			sparkContext = new JavaSparkContext(sparkConf);
				ConvexHullOperation.geometryConvexHull(args[0], args[1]);
		
		} catch (ArrayIndexOutOfBoundsException e) 
		{
			System.out.println("Arguments missing");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}