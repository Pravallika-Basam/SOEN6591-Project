package soen6591.handlers;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class MetricCsvGenerator {
	public void createCSV(String outputFileName, Map<String, Integer> metricItems) throws URISyntaxException {
		try {

			String filePath = "C:\\Users\\srikr\\Desktop\\Metrics\\" + outputFileName.trim() + ".csv";

			FileWriter fileWriter = new FileWriter(filePath);

			CSVWriter csvWriter = new CSVWriter(fileWriter);
			for (Map.Entry<String, Integer> item : metricItems.entrySet()) {
				String fileName = item.getKey();
				int value = item.getValue();

				csvWriter.writeNext(new String[] { fileName, value + "" });
			}
			csvWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
