package edu.asu.cse512;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
public class Join {


public static JavaSparkContext sparkContext;

public static void main(String[] args) {
	try
	{
	SparkConf sparkConf = new SparkConf().setAppName("Group10").set("spark.driver.allowMultipleContexts", "true");
	sparkContext = new JavaSparkContext(sparkConf);
	
	SpatialJoin.spatialJoinQuery(args[0],args[1],args[2],args[3]);
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