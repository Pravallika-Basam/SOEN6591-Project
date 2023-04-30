package soen6591.patterns;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

import soen6591.handlers.MetricCsvGenerator;
import soen6591.handlers.AntiPatternDetectorHandler;
import soen6591.visitors.CatchClauseVisitor;
import soen6591.visitors.CatchRecoverabilityVisitor;
import soen6591.visitors.CommentVisitorTryAndCatch;
import soen6591.visitors.ExceptionHandlingStrategyVisitor;
import soen6591.visitors.FlowQuantityVisitor;
import soen6591.visitors.MethodInvocationVisitor;
import soen6591.visitors.MethodInvokeVisitor;
import soen6591.visitors.OverCatchVisitor;
import soen6591.visitors.ReturnStatementVisitor;
import soen6591.visitors.TryScopeVisitor;
import soen6591.visitors.ThrowClauseVisitor;
import soen6591.visitors.TryVisitor;

public class ExceptionAndMetricFinder {

	private HashMap<MethodDeclaration, String> emptyCatches = new HashMap<>();
	private HashMap<MethodDeclaration, String> dummyCatches = new HashMap<>();
	private HashMap<MethodDeclaration, String> suspectMethods = new HashMap<>();
	private HashMap<MethodDeclaration, String> throwMethods = new HashMap<>();
	private HashMap<MethodDeclaration, String> catchMethods = new HashMap<>();
	private HashMap<MethodDeclaration, String> kitchenSinkMethods = new HashMap<>();
	private HashMap<TryStatement, String> tryBlocks = new HashMap<>();
	private HashMap<MethodDeclaration, String> methodIvoke = new HashMap<>();
	private HashMap<MethodDeclaration, String> tryScope = new HashMap<>();
	private HashMap<MethodDeclaration, String> flowQuantity = new HashMap<>();
	private int tryBlockCount = 0;
	private int tryBlockLOC = 0;
	private int tryBlockSLOC = 0;
	private int catchBlockCount = 0;
	private int catchBlockSLOC = 0;
	private int catchBlockLOC = 0;
	private ArrayList<String> tryBlockLOCStatements = new ArrayList<String>();
	private ArrayList<String> catchBlockLOCStatements = new ArrayList<String>();
	private int flowHandlingActionsCount = 0;
	private int incompleteDPCount = 0;
	private int catchReturnNullCount = 0;
	private int invokedMethodsCount = 0;
	private int exceptionHandlingStrategyCount = 0;
	private int catchRecoverabilityCount = 0;

	private static Map<String, Integer> metricTrySizeLOC = new HashMap<String, Integer>();
	private static Map<String, Integer> metricTryBlockCount = new HashMap<String, Integer>();
	private static Map<String, Integer> metricTryBlockSLOC = new HashMap<String, Integer>();
	private static Map<String, Integer> metricCatchBlockSLOC = new HashMap<String, Integer>();
	private static Map<String, Integer> metricFlowHandlingActionsCount = new HashMap<String, Integer>();
	private static Map<String, Integer> metricCatchBlockCount = new HashMap<String, Integer>();
	private static Map<String, Integer> metricCatchBlockLOC = new HashMap<String, Integer>();
	private static Map<String, Integer> metricCatchReturnNullCount = new HashMap<String, Integer>();
	private static Map<String, Integer> metricIncompleteImplementationCount = new HashMap<String, Integer>();
	private static Map<String, Integer> metricInvokedMethodsCount = new HashMap<String, Integer>();
	private static Map<String, Integer> metricCatchAndDoNothing = new HashMap<String, Integer>();
	private static Map<String, Integer> metricDummyCatch = new HashMap<String, Integer>();
	private static Map<String, Integer> metricLogAndThrow = new HashMap<String, Integer>();
	private static Map<String, Integer> metricOverCatch = new HashMap<String, Integer>();
	private static Map<String, Integer> metricThrowKitchenSink = new HashMap<String, Integer>();
	private static Map<String, Integer> metricTryScope = new HashMap<String, Integer>();
	private static Map<String, Integer> metricFlowQuantity = new HashMap<String, Integer>();
	private static Map<String, Integer> metricExceptionHandlingStrategy = new HashMap<String, Integer>();
	private static Map<String, Integer> metricCatchRecoverability = new HashMap<String, Integer>();

	// This method receives a project as a parameter, and its purpose is to find
	// various exceptions in the project's source code
	public void findExceptions(IProject project) throws JavaModelException, URISyntaxException {
		// Get an array of all package fragments in the project
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		// Iterate through each package in the project
		for (IPackageFragment mypackage : packages) {
			// Iterate through each compilation unit in the package
			for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
				// Parse the current compilation unit to obtain the corresponding AST node
				CompilationUnit parsedCompilationUnit = parse(unit);
				// Perform analysis for various exception patterns and metrics using custom
				// visitors that traverse the AST
				// 1: log & throw, and Exception Metrics: Flow Handling Actions
				CatchClauseVisitor catchVisitor = new CatchClauseVisitor();
				parsedCompilationUnit.accept(catchVisitor);
				// Get the methods that contain target catch clauses and print relevant
				// information about them
				getMethodsWithTargetCatchClauses(catchVisitor);
				flowHandlingActionsCount = catchVisitor.getActionStatements().size();
				catchBlockCount = catchVisitor.getCatchBlockCount();
				catchBlockLOC = catchVisitor.getTryBlockLOC();
				catchBlockLOCStatements = catchVisitor.getCatchBlockLOCStatements();
				// 'Catch & Return Null' Anti-pattern
				catchReturnNullCount = catchVisitor.catchReturnNullCount();
				// 2: overcatch
				OverCatchVisitor overCatchVisitor = new OverCatchVisitor();
				parsedCompilationUnit.accept(overCatchVisitor);
				// Get the methods that contain target try clauses and print relevant
				// information about them
				getMethodsWithTargetTryClauses(overCatchVisitor);
				// 3 : Kitchen Sink
				ThrowClauseVisitor throwUncheckedException1 = new ThrowClauseVisitor();
				parsedCompilationUnit.accept(throwUncheckedException1);
				getMethodsWithTargetThrowClause(throwUncheckedException1);
				// Calculate exception metrics related to try-catch blocks
				// Get the count of try blocks and size of try blocks in LOC
				TryVisitor tryVisitor = new TryVisitor();
				parsedCompilationUnit.accept(tryVisitor);
				tryBlockCount = tryVisitor.getTryBlockCount();
				tryBlockLOC = tryVisitor.getTryBlockLOC();
				tryBlockLOCStatements = tryVisitor.getTryBlockLOCStatements();
				// Get the size of try and catch blocks in SLOC
				// CommentVisitorTryAndCatch is used to count comments in try-catch blocks
				CommentVisitorTryAndCatch CommentVisitor = new CommentVisitorTryAndCatch(parsedCompilationUnit,
						unit.getSource().split("\n"));
				CommentVisitor.setTree(parsedCompilationUnit);
				parsedCompilationUnit.accept(CommentVisitor);

				// Count comment lines in each try and catch block
				for (Comment comment : (List<Comment>) parsedCompilationUnit.getCommentList()) {
					comment.accept(CommentVisitor);
				}
				tryBlockSLOC = CommentVisitor.getCommentInTryCount();
				catchBlockSLOC = CommentVisitor.getCommentInCatchCount();

				// Count the number of 'TODO' and 'FIXME' comments
				// This is used to detect the 'Incomplete Implementation' anti-pattern
				incompleteDPCount = CommentVisitor.getToDoOrFixMeCommentsCount();

				// Method invocation count for each class
				MethodInvokeVisitor numberOfMethodInvoked = new MethodInvokeVisitor();
				parsedCompilationUnit.accept(numberOfMethodInvoked);
				getMethodsWithTargetInvoke(numberOfMethodInvoked);
				invokedMethodsCount = numberOfMethodInvoked.getNumberofMethodInvoke();

				// Try block scope count for each class
				TryScopeVisitor numberOfTryScope = new TryScopeVisitor();
				parsedCompilationUnit.accept(numberOfTryScope);
				getMethodsWithTargetTryScope(numberOfTryScope);

				// Flow handler count for each class
				FlowQuantityVisitor numberOfflowhandler = new FlowQuantityVisitor();
				parsedCompilationUnit.accept(numberOfflowhandler);
				getMethodsWithTargetFlowQuantity(numberOfflowhandler);


				// Exception Handling Strategy - Characteristics metric
				// Count the number of different exception handling strategies in each method
				ExceptionHandlingStrategyVisitor exceptionHandlingStrategyVisitor = new ExceptionHandlingStrategyVisitor();
				parsedCompilationUnit.accept(exceptionHandlingStrategyVisitor);
				exceptionHandlingStrategyCount = exceptionHandlingStrategyVisitor.ExceptionHandlingStrategyCount();

			}
		}
	}
	
	/**

	Parses the given ICompilationUnit and generates an AST for it using the ASTParser class.
	@param unit the ICompilationUnit to parse
	@return a CompilationUnit object representing the AST generated for the given ICompilationUnit
	*/
	public static CompilationUnit parse(ICompilationUnit unit) {
	@SuppressWarnings("deprecation")
	ASTParser parser = ASTParser.newParser(AST.JLS8); // Create a new ASTParser instance with JLS8 configuration
	parser.setKind(ASTParser.K_COMPILATION_UNIT); // Set the parser's kind to K_COMPILATION_UNIT
	parser.setSource(unit); // Set the source of the parser to the given ICompilationUnit
	parser.setResolveBindings(true); // Enable binding resolution
	parser.setBindingsRecovery(true); // Enable bindings recovery
	parser.setStatementsRecovery(true); // Enable statements recovery
	return (CompilationUnit) parser.createAST(null); // Parse and return the generated CompilationUnit object
	}

	/**
	 * Extracts metrics for a given compilation unit and stores them in various
	 * metric maps.
	 *
	 * @param unit                      the compilation unit to extract metrics for
	 * @param numberOfTryScope          visitor for counting the number of try
	 *                                  scopes in the code
	 * @param numberOfFlowQuantity      the total number of flow quantities in the
	 *                                  code
	 */
	private void extractMetrics(ICompilationUnit unit, TryScopeVisitor numberOfTryScope,
			int numberOfFlowQuantity) {
		// Store the extracted metrics for the given compilation unit in the various
		// metric maps.
		metricTrySizeLOC.put(unit.getElementName(), tryBlockLOC);
		metricTryBlockCount.put(unit.getElementName(), tryBlockCount);
		metricTryBlockSLOC.put(unit.getElementName(), tryBlockSLOC);

		metricCatchBlockSLOC.put(unit.getElementName(), catchBlockSLOC);
		metricFlowHandlingActionsCount.put(unit.getElementName(), flowHandlingActionsCount);
		metricCatchBlockCount.put(unit.getElementName(), catchBlockCount);
		metricCatchBlockLOC.put(unit.getElementName(), catchBlockLOC);
		metricCatchReturnNullCount.put(unit.getElementName(), catchReturnNullCount);

		metricIncompleteImplementationCount.put(unit.getElementName(), incompleteDPCount);
		metricInvokedMethodsCount.put(unit.getElementName(), invokedMethodsCount);
		metricCatchAndDoNothing.put(unit.getElementName(), emptyCatches.size());
		metricDummyCatch.put(unit.getElementName(), dummyCatches.size());
		metricLogAndThrow.put(unit.getElementName(), throwMethods.size());
		metricOverCatch.put(unit.getElementName(), catchMethods.size());
		metricThrowKitchenSink.put(unit.getElementName(), kitchenSinkMethods.size());
		metricTryScope.put(unit.getElementName(), numberOfTryScope.getNumberOfTryScopes());
		metricFlowQuantity.put(unit.getElementName(), numberOfFlowQuantity);
		metricExceptionHandlingStrategy.put(unit.getElementName(), exceptionHandlingStrategyCount);
		metricCatchRecoverability.put(unit.getElementName(), catchRecoverabilityCount);
	}

	public static Map<String, Integer> getProject_Metric_CatchRecoverability() {
		return metricCatchRecoverability;
	}

	public static Map<String, Integer> getProject_Metric_ExceptionHandlingStrategy() {
		return metricExceptionHandlingStrategy;
	}

	public static Map<String, Integer> getProject_Metric_ThrowKitchenSink() {
		return metricThrowKitchenSink;
	}

	public static Map<String, Integer> getProject_Metric_FlowQuantity() {
		return metricFlowQuantity;
	}

	public static Map<String, Integer> getProject_Metric_TryScope() {
		return metricTryScope;
	}

	public static Map<String, Integer> getProject_Metric_CatchAndDoNothing() {
		return metricCatchAndDoNothing;
	}

	public static Map<String, Integer> getProject_Metric_DummyCatch() {
		return metricDummyCatch;
	}

	public static Map<String, Integer> getProject_Metric_LogAndThrow() {
		return metricLogAndThrow;
	}

	public static Map<String, Integer> getProject_Metric_OverCatch() {
		return metricOverCatch;
	}

	public static Map<String, Integer> getProject_Metric_InvokedMethodsCount() {
		return metricInvokedMethodsCount;
	}

	public static Map<String, Integer> getProject_Metric_IncompleteImplementationCount() {
		return metricIncompleteImplementationCount;
	}

	public static Map<String, Integer> getProject_Metric_FlowHandlingActionsCount() {
		return metricFlowHandlingActionsCount;
	}

	public static Map<String, Integer> getProject_Metric_CatchBlockCount() {
		return metricCatchBlockCount;
	}

	public static Map<String, Integer> getProject_Metric_CatchBlockLOC() {
		return metricCatchBlockLOC;
	}

	public static Map<String, Integer> getProject_Metric_CatchReturnNullCount() {
		return metricCatchReturnNullCount;
	}

	public static Map<String, Integer> getProject_Metric_TrySizeLOC() {
		return metricTrySizeLOC;
	}

	public static Map<String, Integer> getProject_Metric_TryBlockCount() {
		return metricTryBlockCount;
	}

	public static Map<String, Integer> getProject_Metric_TryBlockSLOC() {
		return metricTryBlockSLOC;
	}

	public static Map<String, Integer> getProject_Metric_CatchBlockSLOC() {
		return metricCatchBlockSLOC;
	}

	private void getMethodsWithTargetThrowClause(ThrowClauseVisitor throwUncheckedException) {
		// TODO Auto-generated method stub
		for (MethodInvocation methodInvocationStatement : ThrowClauseVisitor.getmethodInvocationStatements()) {
			kitchenSinkMethods.put(findMethodForThrow1(methodInvocationStatement), "Throwing the Kitchen Sink");
		}
	}

	private void getMethodsWithTargetFlowQuantity(FlowQuantityVisitor throwUncheckedException) {
		// TODO Auto-generated method stub
		for (MethodInvocation methodInvocationStatement : FlowQuantityVisitor.getmethodInvocationStatements()) {
			flowQuantity.put(findMethodForThrow1(methodInvocationStatement), "Flow Quantity");
		}
	}

	private void getMethodsWithTargetInvoke(MethodInvokeVisitor numberOfMethodInvoked) {
		// TODO Auto-generated method stub
		for (TryStatement methodInvocationStatement : MethodInvokeVisitor.getTryStatements()) {
			methodIvoke.put(findMethodForInvoke(methodInvocationStatement), "Method Invoke");
		}
	}

	private void getMethodsWithTargetTryScope(TryScopeVisitor numberOfTryScope) {
		// TODO Auto-generated method stub
		for (TryStatement methodInvocationStatement : TryScopeVisitor.getTryStmts()) {
			tryScope.put(findMethodForTryScope(methodInvocationStatement), "Try Scope");
		}
	}

	private void getMethodsWithTargetCatchClauses(CatchClauseVisitor catchClauseVisitor) {

		for (CatchClause emptyCatch : catchClauseVisitor.getEmptyCatches()) {
			emptyCatches.put(findMethodForCatch(emptyCatch), "EmptyCatch");
		}

		for (CatchClause dummyCatch : catchClauseVisitor.getDummyCatches()) {
			dummyCatches.put(findMethodForCatch(dummyCatch), "DummyCatch");
		}

		for (CatchClause throwStatement : catchClauseVisitor.getThrowStatements()) {
			// suspectMethods.put(findMethodForThrow(throwStatement), "throwStatement");
			throwMethods.put(findMethodForThrow(throwStatement), "LogThrow");
		}
	}

	private void getMethodsWithTargetTryClauses(OverCatchVisitor overCatchVisitor) {
		for (CatchClause catchblock : overCatchVisitor.getCatchBlocks()) {
			// suspectMethods.put(findMethodForCatch(catchblock), "Over-Catch");
			catchMethods.put(findMethodForCatch(catchblock), "Over-Catch");
		}
	}

	private ASTNode findParentMethodDeclaration(ASTNode node) {
		if (node != null && node.getParent() != null) {
			if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
				return node.getParent();
			} else {
				return findParentMethodDeclaration(node.getParent());
			}
		}
		return null;
	}

	private ASTNode findParentTryBlock(ASTNode node) {
		if (node != null && node.getParent() != null) {
			if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
				return node.getParent();
			} else {
				return findParentMethodDeclaration(node.getParent());
			}
		}
		return null;
	}

	private MethodDeclaration findMethodForThrow(CatchClause throwStatement) {
		return (MethodDeclaration) findParentMethodDeclaration(throwStatement);
	}

	private MethodDeclaration findMethodForCatch(CatchClause catchStatement) {
		return (MethodDeclaration) findParentMethodDeclaration(catchStatement);
	}

	private MethodDeclaration findMethodForInvoke(TryStatement methodInvocationStatement) {
		return (MethodDeclaration) findParentMethodDeclaration(methodInvocationStatement);
	}

	private MethodDeclaration findMethodForTryScope(TryStatement trymethodInvoc) {
		return (MethodDeclaration) findParentMethodDeclaration(trymethodInvoc);
	}

	private MethodDeclaration findMethodForThrow1(MethodInvocation methodInvoc) {
		return (MethodDeclaration) findParentMethodThrow1Declaration(methodInvoc);
	}

	private ASTNode findParentMethodThrow1Declaration(ASTNode node) {
		// TODO Auto-generated method stub
		if (node != null && node.getParent() != null) {
			if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
				return node.getParent();
			} else {
				return findParentMethodThrow1Declaration(node.getParent());
			}
		} else
			return null;
	}

	public HashMap<MethodDeclaration, String> getSuspectMethods() {
		return suspectMethods;
	}

	public HashMap<MethodDeclaration, String> getemptyCatches() {
		return emptyCatches;
	}

	public void printExceptions() {

		for (MethodDeclaration declaration : throwMethods.keySet()) {
			String type = throwMethods.get(declaration);
			System.out
					.println(String.format("The following method suffers from the Throw & Log anti-pattern: %s", type));
			if (declaration != null) {
				System.out.println(declaration.toString());
			}
		}

		for (MethodDeclaration declaration : catchMethods.keySet()) {
			String type = catchMethods.get(declaration);
			System.out
					.println(String.format("The following method suffers from the Over-Catch anti-pattern: %s", type));
			if (declaration != null) {
				System.out.println(declaration.toString());
			}
		}

		for (MethodDeclaration declaration : kitchenSinkMethods.keySet()) {
			String type = kitchenSinkMethods.get(declaration);
			System.out.println(String.format("The following method suffers from the %s anti-pattern", type));
			if (declaration != null) {
				System.out.println(declaration.toString());
			}
		}

		for (TryStatement tryBlock : tryBlocks.keySet()) {
			String type = tryBlocks.get(tryBlock);
			System.out.println(String.format("The following method is: ", type));
			if (tryBlock != null) {
				System.out.println(tryBlock.toString());
			}
		}

		for (MethodDeclaration declaration : suspectMethods.keySet()) {
			String type = suspectMethods.get(declaration);
			System.out.println(String.format("The following method suffers from the %s pattern", type));
			System.out.println(declaration.toString());
		}
	}

}
