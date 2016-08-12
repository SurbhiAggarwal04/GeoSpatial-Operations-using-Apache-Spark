package edu.asu.cse512;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;

//import akka.japi.Function;
import scala.Tuple2;

public class RangeQueryImpl
{
    static List<String> abc = new ArrayList<String>();
    public static void spatialRangeQuery(String inputLocation1,String inputLocation2,String outputLocation)
    {
       
        JavaSparkContext context =RangeQuery.sparkContext;
        JavaRDD<String> cordinates = context.textFile(inputLocation1);
             
        //load the rectangle query window in JavaRDD
        JavaRDD<String> window = context.textFile(inputLocation2);          
        String windowList=window.first();
        System.out.println(windowList);
        String[] windowlist= windowList.split(",");
        final Double[] windowX = new Double[2];
        final Double[] windowY = new Double[2];
        int k=0;
        int l=0;
        for(int i=0; i<4; i++)
        {
            if(i%2==0){
                windowX[k] = Double.parseDouble(windowlist[i]);
                k++;
            }
            else{
                windowY[l]= Double.parseDouble(windowlist[i]);
                l++;
               }         
        }
        Arrays.sort(windowX);
        Arrays.sort(windowY);
        System.out.println("before");
       
        JavaPairRDD<String,String> enclosed= cordinates.mapToPair(new PairFunction<String, String, String>()
        {
            //String output="";
            private static final long serialVersionUID = 1L;
                public Tuple2<String, String> call(String arg0) throws Exception {
                    String parts[] = arg0.split(",");
                    String tempD0 = "-1";
                    double tempD1 = Double.parseDouble(parts[1]);
                       double tempD2 = Double.parseDouble(parts[2]);
                       if(tempD1 > windowX[0] && tempD1 < windowX[1] && tempD2 > windowY[0] && tempD2 < windowY[1])
                       {
                           tempD0 = parts[0];
                          
                       }
                       return new Tuple2<String,String>(tempD0,"");
                }
      
           });

        JavaRDD<String> outputData = (JavaRDD<String>) enclosed.map(new Function<Tuple2<String,String>, String>(){
            private static final long serialVersionUID = 1L;
            public String call(Tuple2<String, String> data) {
                String result ="";
                try
                {
                result = data._1();               
                return result;
                }
                catch(Exception e)
                {
                    return result;
                }
            } //repartition(1)
        }).repartition(1).sortBy(new Function<String, String>() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			//private static final long serialVersionUID = 1L;
  
            public String call(String str) throws Exception {
                return str;
            }
        }, true, 1);
       
        JavaRDD<String> OutputFiltered = outputData.filter(new Function<String, Boolean>() {

            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Boolean call(String v1) throws Exception {
                // TODO Auto-generated method stub
                if(v1.equals("-1"))
                {
                    return false;
                }
               
                return true;
            }
           
        });
     
        List<String> finalOutput = OutputFiltered.collect();
        List<Integer> finalvalues=new ArrayList<Integer>();
        for(String tmp:finalOutput){
            finalvalues.add(Integer.parseInt(tmp));
        }
       
        Collections.sort(finalvalues);
        System.out.println(finalvalues);
       
        JavaRDD<Integer> final_result = context.parallelize(finalvalues).repartition(1);
       
           
    	int index=outputLocation.lastIndexOf("/");
		String hadoopPath=outputLocation.substring(0, index);
		String intermediateResultpath=hadoopPath+"/IntermediateHullResult";

		FileToCsv.deleteExistingFile(intermediateResultpath);
		final_result.saveAsTextFile(intermediateResultpath);
		
		FileToCsv.fileToCsv(intermediateResultpath, outputLocation);
		context.close();
    }
}




