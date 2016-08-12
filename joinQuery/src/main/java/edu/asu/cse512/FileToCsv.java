package edu.asu.cse512;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class FileToCsv {
	public static Configuration confhadoop = new Configuration();
	public static FileSystem fileHadoop;
	
public static void fileToCsv(String hdfsInputPath,String hdfsOutputPath)
{
	setConfigurationProperties();
	
	String localFilePath=hdfsInputPath+"/part-00000";
	URI hdfsOutputPathUri = URI.create(hdfsOutputPath);
	Path pathOutput = new Path(hdfsOutputPathUri);
	URI hdfsInputPathUri = URI.create(localFilePath);
	Path pathlocal = new Path(hdfsInputPathUri);
	
	
	try {
		
		
		deleteExistingFile(hdfsOutputPath);	
		
		fileHadoop = FileSystem.get(hdfsInputPathUri,confhadoop);
		BufferedReader reader=new BufferedReader(new InputStreamReader(fileHadoop.open(pathlocal)));
		String line=null;
		
		deleteExistingFile(hdfsInputPath);
		
		fileHadoop = FileSystem.get(hdfsOutputPathUri,confhadoop);
		BufferedWriter writer =new BufferedWriter(new OutputStreamWriter(fileHadoop.create(pathOutput,true)));
		while((line = reader.readLine())!=null)
		{
			writer.write(line);
			writer.flush();
			writer.write("\n");
			writer.flush();
		}
		writer.close();
		reader.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
}

public static void deleteExistingFile(String path)
{
	setConfigurationProperties();
	URI hdfsPathUri = URI.create(path);
	Path uriPath = new Path(hdfsPathUri);
	try {
		FileSystem filehadoop = FileSystem.get(hdfsPathUri, confhadoop);
		if(filehadoop.exists(uriPath))
		filehadoop.delete(uriPath,true);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
public static void setConfigurationProperties()
{
	confhadoop.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
	confhadoop.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"));
}
}
