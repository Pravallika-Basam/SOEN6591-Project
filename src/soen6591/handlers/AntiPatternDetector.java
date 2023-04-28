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

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("Inside AntiPatternDetector");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();

		Map<String, Integer> metricTrySizeLOC = new HashMap<String, Integer>();
		Map<String, Integer> metricTryBlockCount = new HashMap<String, Integer>();
		Map<String, Integer> metricTryBlockSLOC = new HashMap<String, Integer>();
		Map<String, Integer> metricCatchBlockSLOC = new HashMap<String, Integer>();

		Map<String, Integer> metricFlowHandlingActionsCount = new HashMap<String, Integer>();
		Map<String, Integer> metricCatchBlockCount = new HashMap<String, Integer>();
		Map<String, Integer> metricCatchBlockLOC = new HashMap<String, Integer>();
		Map<String, Integer> metricCatchReturnNullCount = new HashMap<String, Integer>();

		Map<String, Integer> metricIncompleteImplementationCount = new HashMap<String, Integer>();
		Map<String, Integer> metricInvokedMethodsCount = new HashMap<String, Integer>();
		Map<String, Integer> metricCatchAndDoNothing = new HashMap<String, Integer>();
		Map<String, Integer> metricDummyCatch = new HashMap<String, Integer>();
		Map<String, Integer> metricLogAndThrow = new HashMap<String, Integer>();
		Map<String, Integer> metricOverCatch = new HashMap<String, Integer>();
		Map<String, Integer> metricThrowKitchenSink = new HashMap<String, Integer>();
		Map<String, Integer> metricTryScope = new HashMap<String, Integer>();
		Map<String, Integer> metricFlowTypePrevalance = new HashMap<String, Integer>();
		Map<String, Integer> metricFlowQuantity = new HashMap<String, Integer>();
		Map<String, Integer> metricExceptionHandlingStrategy = new HashMap<String, Integer>();
		Map<String, Integer> metricFlowSourceDeclared = new HashMap<String, Integer>();
		Map<String, Integer> metricCatchRecoverability = new HashMap<String, Integer>();

		for (IProject project : projects) {
			System.out.println("DETECTING IN: " + project.getName());
			ExceptionAndMetricFinder exceptionFinder = new ExceptionAndMetricFinder();

			try {
				// find the exceptions and print the methods that contain the exceptions
				exceptionFinder.findExceptions(project);
				metricTrySizeLOC = extractMetricTrySizeLOC(exceptionFinder);
				metricTryBlockCount = extractMetricTryBlockCount(exceptionFinder);
				metricTryBlockSLOC = extractMetricTryBlockSLOC(exceptionFinder);
				metricCatchBlockSLOC = extractMetricCatchBlockSLOC(exceptionFinder);
				metricFlowHandlingActionsCount = extractMetricFlowHandlingActionsCount(exceptionFinder);
				metricCatchBlockCount = extractMetricCatchBlockCount(exceptionFinder);
				metricCatchBlockLOC = extractMetricCatchBlockLOC(exceptionFinder);
				metricCatchReturnNullCount = extractMetricCatchReturnNullCount(exceptionFinder);
				metricIncompleteImplementationCount = extractMetricIncompleteImplementationCount(exceptionFinder);
				metricCatchAndDoNothing = extractMetricCatchAndDoNothing(exceptionFinder);
				metricDummyCatch = extractMetricDummyCatch(exceptionFinder);
				metricLogAndThrow = extractMetricLogAndThrow(exceptionFinder);
				metricInvokedMethodsCount = extractMetricInvokedMethodsCount(exceptionFinder);
				metricOverCatch = extractMetricOverCatch(exceptionFinder);
				metricThrowKitchenSink = extractMetricThrowKitchenSink(exceptionFinder);
				metricTryScope = extractMetricTryScope(exceptionFinder);
				metricFlowTypePrevalance = extractMetricFlowTypePrevalance(exceptionFinder);
				metricFlowQuantity = extractMetricFlowQuantity(exceptionFinder);
				metricExceptionHandlingStrategy = extractMetricExceptionHandlingStrategy(exceptionFinder);
				metricFlowSourceDeclared = extractMetricFlowSourceDeclared(exceptionFinder);
				metricCatchRecoverability = extractMetricCatchRecoverability(exceptionFinder);
				exceptionFinder.printExceptions();

			} catch (JavaModelException | URISyntaxException e) {
				e.printStackTrace();
			}

		}

		System.out.println("metricTrySizeLOC sizeeeee:" + metricTrySizeLOC.size());
		System.out.println("metricTryBlockCount sizeeeee:" + metricTryBlockCount.size());
		System.out.println("Metric TrySLOC sizeeeee:" + metricTryBlockSLOC.size());
		System.out.println("Metric CatchSLOC sizeeeee:" + metricCatchBlockSLOC.size());

		generateMetricsInCSV("TrySizeLOC", metricTrySizeLOC);
		generateMetricsInCSV("TryQuantity", metricTryBlockCount);
		generateMetricsInCSV("TrySLOC", metricTryBlockSLOC);
		generateMetricsInCSV("CatchSizeSLOC", metricCatchBlockSLOC);
		generateMetricsInCSV("FlowHandlingActions", metricFlowHandlingActionsCount);
		generateMetricsInCSV("CatchQuantity", metricCatchBlockCount);
		generateMetricsInCSV("CatchSizeLOC", metricCatchBlockLOC);
		generateMetricsInCSV("CatchAndReturnNull_AntiPattern", metricCatchReturnNullCount);
		generateMetricsInCSV("CatchAndDoNothing_AntiPattern", metricCatchAndDoNothing);
		generateMetricsInCSV("DummyCatch_AntiPattern", metricDummyCatch);
		generateMetricsInCSV("LogAndThrow_AntiPattern", metricLogAndThrow);
		generateMetricsInCSV("OverCatch_AntiPattern", metricOverCatch);
		generateMetricsInCSV("ThrowKitchenSink_AntiPattern", metricThrowKitchenSink);
		generateMetricsInCSV("TryScope", metricTryScope);
		generateMetricsInCSV("FlowTypePrevalance", metricFlowTypePrevalance);
		generateMetricsInCSV("FlowQuantity", metricFlowQuantity);
		generateMetricsInCSV("ExceptionHandlingStrategy_AntiPattern", metricExceptionHandlingStrategy);
		generateMetricsInCSV("FlowSourceDeclared", metricFlowSourceDeclared);
		generateMetricsInCSV("CatchRecoverability_AntiPattern", metricCatchRecoverability);

		System.out.println("DONE DETECTING!!");
		AntiPatternDetectorHandler.printMessage("DONE DETECTING");
		return null;
	}

	private Map<String, Integer> extractMetricOverCatch(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_OverCatch();
	}

	private Map<String, Integer> extractMetricCatchRecoverability(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchRecoverability();
	}

	private Map<String, Integer> extractMetricFlowSourceDeclared(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_FlowSourceDeclared();
	}

	private Map<String, Integer> extractMetricExceptionHandlingStrategy(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_ExceptionHandlingStrategy();
	}

	private Map<String, Integer> extractMetricFlowQuantity(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_FlowQuantity();
	}

	private Map<String, Integer> extractMetricFlowTypePrevalance(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_FlowTypePrevalance();
	}

	private Map<String, Integer> extractMetricTryScope(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_TryScope();
	}

	private Map<String, Integer> extractMetricThrowKitchenSink(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_ThrowKitchenSink();
	}

	private Map<String, Integer> extractMetricInvokedMethodsCount(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_InvokedMethodsCount();
	}

	private Map<String, Integer> extractMetricLogAndThrow(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_LogAndThrow();
	}

	private Map<String, Integer> extractMetricDummyCatch(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_DummyCatch();
	}

	private Map<String, Integer> extractMetricCatchAndDoNothing(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchAndDoNothing();
	}

	private Map<String, Integer> extractMetricIncompleteImplementationCount(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_IncompleteImplementationCount();
	}

	private Map<String, Integer> extractMetricCatchReturnNullCount(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchReturnNullCount();
	}

	private Map<String, Integer> extractMetricCatchBlockLOC(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchBlockLOC();
	}

	private Map<String, Integer> extractMetricCatchBlockCount(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchBlockCount();
	}

	private Map<String, Integer> extractMetricFlowHandlingActionsCount(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_FlowHandlingActionsCount();
	}

	private Map<String, Integer> extractMetricCatchBlockSLOC(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_CatchBlockSLOC();
	}

	private Map<String, Integer> extractMetricTryBlockSLOC(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_TryBlockSLOC();
	}

	private Map<String, Integer> extractMetricTryBlockCount(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_TryBlockCount();
	}

	private Map<String, Integer> extractMetricTrySizeLOC(ExceptionAndMetricFinder exceptionFinder) {
		return exceptionFinder.getProject_Metric_TrySizeLOC();
	}

	public static void generateMetricsInCSV(String fileName, Map<String, Integer> value) {

		MetricCsvGenerator csvCreator = new MetricCsvGenerator();

		try {

			csvCreator.createCSV(fileName, value);

		} catch (URISyntaxException e) {

			AntiPatternDetectorHandler.printMessage("Error while generating the Metric CSV "+e.getMessage());
		}

	}
}