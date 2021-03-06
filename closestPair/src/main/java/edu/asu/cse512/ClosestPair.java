package edu.asu.cse512;

import java.io.FileNotFoundException;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class ClosestPair {
	public static JavaSparkContext sparkContext;
	
	public static void main(String[] args) {
		
		try
		{
		SparkConf sparkConf = new SparkConf().setAppName("Group10").set("spark.driver.allowMultipleContexts", "true");
		sparkContext = new JavaSparkContext(sparkConf);
		ClosestPairImpl.geometryClosestPair(args[0], args[1]);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Arguments missing");
		}
        catch(FileNotFoundException e)
		{
        	System.out.println("Input file not found");
		}
	
		
	}

}
