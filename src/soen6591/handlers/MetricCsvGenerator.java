package soen6591.handlers;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class MetricCsvGenerator {
	/*
	 * MetricCsvGenerator class is responsible for generating CSV files containing the metric values for each 
	 * anti-pattern detection analysis.
	 * 
	 * It has a public method createCSV that receives the name of the CSV file and a map with 
	 * the metric items and values to be included in the file.
	 * 
	 * The method then creates the CSV file in the specified directory and writes the 
	 * metric items and their values in the file.
	 * 
	 * If there is any error during the creation of the CSV file, it throws an exception 
	 * that is caught in the caller method.
	*/
	public void createCSV(String outputFileName, Map<String, Integer> metricItems) throws URISyntaxException {
		try {

			String filePath = "C:\\Users\\usr_name\\Desktop\\Metrics\\" + outputFileName.trim() + ".csv";

			FileWriter fileWriter = new FileWriter(filePath);

			CSVWriter csvWriter = new CSVWriter(fileWriter);
			for (Map.Entry<String, Integer> item : metricItems.entrySet()) {
				String fileName = item.getKey();
				int value = item.getValue();

				csvWriter.writeNext(new String[] { fileName, value + "" });
			}
			csvWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
