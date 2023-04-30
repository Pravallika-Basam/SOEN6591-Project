package soen6591.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaModelException;

import soen6591.patterns.ExceptionAndMetricFinder;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class AntiPatternDetector extends AbstractHandler {
	
	/**
	This method is called when the command "Start Analysis is
	executed from the user interface, either by clicking the menu item.
	@param event the execution event containing information about the current state of the application
	@throws ExecutionException if an exception occurs during the execution of the command
	@return the result of the command execution, or null if no result is available
	*/
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IProject[] projects = workspaceRoot.getProjects();

		Map<String, Integer> trySizeLOCMetric = new HashMap<>();
		Map<String, Integer> tryBlockCountMetric = new HashMap<>();
		Map<String, Integer> tryBlockSLOCMetric = new HashMap<>();
		Map<String, Integer> catchBlockSLOCMetric = new HashMap<>();

		Map<String, Integer> flowHandlingActionsCountMetric = new HashMap<>();
		Map<String, Integer> catchBlockCountMetric = new HashMap<>();
		Map<String, Integer> catchBlockLOCMetric = new HashMap<>();
		Map<String, Integer> catchReturnNullCountMetric = new HashMap<>();

		Map<String, Integer> incompleteImplementationCountMetric = new HashMap<>();
		Map<String, Integer> invokedMethodsCountMetric = new HashMap<>();
		Map<String, Integer> catchAndDoNothingMetric = new HashMap<>();
		Map<String, Integer> dummyCatchMetric = new HashMap<>();
		Map<String, Integer> logAndThrowMetric = new HashMap<>();
		Map<String, Integer> overCatchMetric = new HashMap<>();
		Map<String, Integer> throwKitchenSinkMetric = new HashMap<>();
		Map<String, Integer> tryScopeMetric = new HashMap<>();
		Map<String, Integer> flowTypePrevalenceMetric = new HashMap<>();
		Map<String, Integer> flowQuantityMetric = new HashMap<>();
		Map<String, Integer> exceptionHandlingStrategyMetric = new HashMap<>();
		Map<String, Integer> flowSourceDeclaredMetric = new HashMap<>();
		Map<String, Integer> catchRecoverabilityMetric = new HashMap<>();

		for (IProject project : projects) {
			ExceptionAndMetricFinder exceptionFinder = new ExceptionAndMetricFinder();

			try {
				exceptionFinder.findExceptions(project);
				trySizeLOCMetric = extractTrySizeLOCMetric(exceptionFinder);
				tryBlockCountMetric = extractTryBlockCountMetric(exceptionFinder);
				tryBlockSLOCMetric = extractTryBlockSLOCMetric(exceptionFinder);
				catchBlockSLOCMetric = extractCatchBlockSLOCMetric(exceptionFinder);
				flowHandlingActionsCountMetric = extractFlowHandlingActionsCountMetric(exceptionFinder);
				catchBlockCountMetric = extractCatchBlockCountMetric(exceptionFinder);
				catchBlockLOCMetric = extractCatchBlockLOCMetric(exceptionFinder);
				catchReturnNullCountMetric = extractCatchReturnNullCountMetric(exceptionFinder);
				incompleteImplementationCountMetric = extractIncompleteImplementationCountMetric(exceptionFinder);
				catchAndDoNothingMetric = extractCatchAndDoNothingMetric(exceptionFinder);
				dummyCatchMetric = extractDummyCatchMetric(exceptionFinder);
				logAndThrowMetric = extractLogAndThrowMetric(exceptionFinder);
				invokedMethodsCountMetric = extractInvokedMethodsCountMetric(exceptionFinder);
				overCatchMetric = extractOverCatchMetric(exceptionFinder);
				throwKitchenSinkMetric = extractThrowKitchenSinkMetric(exceptionFinder);
				tryScopeMetric = extractTryScopeMetric(exceptionFinder);
				flowQuantityMetric = extractFlowQuantityMetric(exceptionFinder);
				exceptionHandlingStrategyMetric = extractExceptionHandlingStrategyMetric(exceptionFinder);
				flowSourceDeclaredMetric = extractFlowSourceDeclaredMetric(exceptionFinder);
				catchRecoverabilityMetric = extractCatchRecoverabilityMetric(exceptionFinder);
			} catch (JavaModelException | URISyntaxException e) {
				e.printStackTrace();
			}
		}

		/*
		 * This section of the code is responsible for generating the CSV files for each of 
		 * the extracted metrics. Each line calls the generateMetricsInCSV method with a metric 
		 * name and its corresponding value, which creates a CSV file with the given name and 
		 * writes the value to it.
		 */
		generateMetricsInCSV("TryLOC", trySizeLOCMetric);
		generateMetricsInCSV("TryQuantity", tryBlockCountMetric);
		generateMetricsInCSV("TrySLOC", tryBlockSLOCMetric);
		generateMetricsInCSV("CatchSLOC", catchBlockSLOCMetric);
		generateMetricsInCSV("FlowHandlingActions", flowHandlingActionsCountMetric);
		generateMetricsInCSV("CatchQuantity", catchBlockCountMetric);
		generateMetricsInCSV("CatchLOC", catchBlockLOCMetric);
		generateMetricsInCSV("CatchAndReturnNull", catchReturnNullCountMetric);
		generateMetricsInCSV("CatchAndDoNothing", catchAndDoNothingMetric);
		generateMetricsInCSV("DummyCatch", dummyCatchMetric);
		generateMetricsInCSV("LogAndThrow", logAndThrowMetric);
		generateMetricsInCSV("OverCatch", overCatchMetric);
		generateMetricsInCSV("ThrowKitchenSink", throwKitchenSinkMetric);
		generateMetricsInCSV("TryScope", tryScopeMetric);
		generateMetricsInCSV("FlowQuantity", flowQuantityMetric);
		generateMetricsInCSV("ExceptionHandlingStrategy", exceptionHandlingStrategyMetric);
		generateMetricsInCSV("FlowSourceDeclared", flowSourceDeclaredMetric);
		generateMetricsInCSV("CatchRecoverability", catchRecoverabilityMetric);

		return null;
	}
	
	/**
	Generates a CSV file for a given set of metrics.
	@param fileName the name of the CSV file to be created
	@param value the map containing the metrics to be included in the CSV file
	*/
	
	public static void generateMetricsInCSV(String fileName, Map<String, Integer> value) {

		MetricCsvGenerator csvCreator = new MetricCsvGenerator();

		try {

			csvCreator.createCSV(fileName, value);

		} catch (URISyntaxException e) {

			AntiPatternDetectorHandler.printMessage("Error while generating the Metric CSV " + e.getMessage());
		}

	}
		
	private Map<String, Integer> extractTrySizeLOCMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_TrySizeLOC();
	}

	private Map<String, Integer> extractTryBlockCountMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_TryBlockCount();
	}

	private Map<String, Integer> extractTryBlockSLOCMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_TryBlockSLOC();
	}

	private Map<String, Integer> extractCatchBlockSLOCMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchBlockSLOC();
	}

	private Map<String, Integer> extractFlowHandlingActionsCountMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_FlowHandlingActionsCount();
	}

	private Map<String, Integer> extractCatchBlockCountMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchBlockCount();
	}

	private Map<String, Integer> extractCatchBlockLOCMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchBlockLOC();
	}

	private Map<String, Integer> extractCatchReturnNullCountMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchReturnNullCount();
	}

	private Map<String, Integer> extractIncompleteImplementationCountMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_IncompleteImplementationCount();
	}

	private Map<String, Integer> extractCatchAndDoNothingMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchAndDoNothing();
	}

	private Map<String, Integer> extractDummyCatchMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_DummyCatch();
	}

	private Map<String, Integer> extractLogAndThrowMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_LogAndThrow();
	}
	
	private Map<String, Integer> extractThrowKitchenSinkMetric(ExceptionAndMetricFinder exceptionFinder) {
	    return exceptionFinder.getProject_Metric_ThrowKitchenSink();
	}

	private Map<String, Integer> extractInvokedMethodsCountMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_InvokedMethodsCount();
	}

	private Map<String, Integer> extractOverCatchMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_OverCatch();
	}

	private Map<String, Integer> extractCatchRecoverabilityMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchRecoverability();
	}

	private Map<String, Integer> extractFlowSourceDeclaredMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_FlowSourceDeclared();
	}

	private Map<String, Integer> extractExceptionHandlingStrategyMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_ExceptionHandlingStrategy();
	}

	private Map<String, Integer> extractFlowQuantityMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_FlowQuantity();
	}

	private Map<String, Integer> extractTryScopeMetric(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_TryScope();
	}

}