package edu.asu.cse512;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class RangeQuery {
	public static JavaSparkContext sparkContext;

	public static void main(String[] args) {
		try {
			SparkConf sparkConf = new SparkConf().setAppName("Group10")
					.set("spark.driver.allowMultipleContexts", "true");
			sparkContext = new JavaSparkContext(sparkConf);
				RangeQueryImpl.spatialRangeQuery(args[0], args[1], args[2]);
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
