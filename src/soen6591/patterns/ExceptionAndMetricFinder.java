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
// import soen6591.visitors.FlowSoucreDeclareMethods;
// import soen6591.visitors.FlowTypePrevelanceVisitor;
import soen6591.visitors.MethodInvocationVisitor;
import soen6591.visitors.MethodInvokeVisitor;
import soen6591.visitors.OverCatchVisitor;
import soen6591.visitors.ReturnStatementVisitor;
import soen6591.visitors.Throw1ClauseVisitor;
import soen6591.visitors.TryScopeVisitor;
import soen6591.visitors.TryVisitor;

public class ExceptionAndMetricFinder {
	HashMap<MethodDeclaration, String> emptyCatches = new HashMap<>();
	HashMap<MethodDeclaration, String> dummyCatches = new HashMap<>();
	HashMap<MethodDeclaration, String> suspectMethods = new HashMap<>();
	HashMap<MethodDeclaration, String> throwMethods = new HashMap<>();
	HashMap<MethodDeclaration, String> catchMethods = new HashMap<>();
	HashMap<MethodDeclaration, String> kitchenSinkMethods = new HashMap<>();
	HashMap<TryStatement, String> tryBlocks = new HashMap<>();
	HashMap<MethodDeclaration, String> methodIvoke = new HashMap<>();
	HashMap<MethodDeclaration, String> tryScope = new HashMap<>();
	HashMap<MethodDeclaration, String> flowQuantity = new HashMap<>();
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
	private static Map<String, Integer> metricFlowTypePrevalance = new HashMap<String, Integer>();
	private static Map<String, Integer> metricFlowQuantity = new HashMap<String, Integer>();
	private static Map<String, Integer> metricExceptionHandlingStrategy = new HashMap<String, Integer>();
	private static Map<String, Integer> metricFlowSourceDeclared = new HashMap<String, Integer>();
	private static Map<String, Integer> metricCatchRecoverability = new HashMap<String, Integer>();

	private int invokedMethodsCount = 0;
	private int exceptionHandlingStrategyCount = 0;
	private int catchRecoverabilityCount = 0;

	public void findExceptions(IProject project) throws JavaModelException, URISyntaxException {
		System.out.println("Inside ExceptionAndMetricFinder");
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();

		for (IPackageFragment mypackage : packages) {
			for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
				System.out.println("Compilation Unit name  "+ unit.toString());
				// AST node
				CompilationUnit parsedCompilationUnit = parse(unit);
//				//Pattern 1: log & throw
//				// AND Exception Metrics: Flow Handling Actions
				CatchClauseVisitor catchVisitor = new CatchClauseVisitor();
				parsedCompilationUnit.accept(catchVisitor);
				// Give detail of detection
                getMethodsWithTargetCatchClauses(catchVisitor);
                
				flowHandlingActionsCount = catchVisitor.getActionStatements().size();
				catchBlockCount = catchVisitor.getCatchBlockCount();
				catchBlockLOC = catchVisitor.getTryBlockLOC();
				catchBlockLOCStatements = catchVisitor.getCatchBlockLOCStatements();
				
				// 'Catch & Return Null' Anti-pattern
				catchReturnNullCount = catchVisitor.catchReturnNullCount();		
				
//				// Pattern 3: overcatch
				OverCatchVisitor overCatchVisitor = new OverCatchVisitor();
				parsedCompilationUnit.accept(overCatchVisitor);
				getMethodsWithTargetTryClauses(overCatchVisitor);
//
//				//Pattern 2 : Kitchen Sink
				Throw1ClauseVisitor throwUncheckedException1 = new Throw1ClauseVisitor();
				parsedCompilationUnit.accept(throwUncheckedException1);
				getMethodsWithTargetThrow1Clauses(throwUncheckedException1);
				
				//Exception Metrics: Try Quantity & Try Size-LOC
				TryVisitor tryVisitor = new TryVisitor();
				parsedCompilationUnit.accept(tryVisitor);
				//getMethodsWithTryBlock(tryVisitor);
				tryBlockCount = tryVisitor.getTryBlockCount();
				tryBlockLOC = tryVisitor.getTryBlockLOC();
				tryBlockLOCStatements = tryVisitor.getTryBlockLOCStatements();

//				//Exception Metrics: Try Size-SLOC & Catch Size-SLOC
				CommentVisitorTryAndCatch CommentVisitor = new CommentVisitorTryAndCatch(parsedCompilationUnit, unit.getSource().split("\n"));
				CommentVisitor.setTree(parsedCompilationUnit);
				parsedCompilationUnit.accept(CommentVisitor);
				
				for (Comment comment : (List<Comment>) parsedCompilationUnit.getCommentList()) {
					 comment.accept(CommentVisitor);
				 }
				tryBlockSLOC = CommentVisitor.getCommentInTryCount();
				//SampleHandler.printMessage("Satatementttttt:" + tryBlockLOCStatements);
				catchBlockSLOC = CommentVisitor.getCommentInCatchCount();
				//SampleHandler.printMessage("Satatementttttt:" + catchBlockLOCStatements);	
//				
				// For 'Incomplete Implementation' Anti-pattern
				incompleteDPCount = CommentVisitor.getToDoOrFixMeCommentsCount();
//
				//Invoke method for each class
				System.out.println("Class Name " + unit.getElementName());
				MethodInvokeVisitor numberOfMethodInvoked = new MethodInvokeVisitor();
				parsedCompilationUnit.accept(numberOfMethodInvoked);
				getMethodsWithTargetInvoke(numberOfMethodInvoked);
				invokedMethodsCount = numberOfMethodInvoked.getNumberofMethodInvoke();
				System.out.println("Number of Invoke methods " + numberOfMethodInvoked.getNumberofMethodInvoke());

				//Try Scope for each class
				System.out.println("-------- Try Scope for each class ------------");
				System.out.println("Class Name " + unit.getElementName());
				TryScopeVisitor numberOfTryScope = new TryScopeVisitor();
				parsedCompilationUnit.accept(numberOfTryScope);
				getMethodsWithTargetTryScope(numberOfTryScope);
				System.out.println("Number of Try Scope " + numberOfTryScope.getNumberOfTryScope());
	
				
				
//				//Flow quantity 
				System.out.println("-------- Flow Quantity for each class ------------");
				System.out.println("Class Name " + unit.getElementName());
				FlowQuantityVisitor numberOfflowhandler = new FlowQuantityVisitor();
				parsedCompilationUnit.accept(numberOfflowhandler);
				getMethodsWithTargetFlowQuantity(numberOfflowhandler);
				System.out.println("Number of flow Quantity " + numberOfflowhandler.getNumberOfFlowQuantity());	
				
				
//				//Flow type prevalence 
				// System.out.println("-------- Flow Type Prevalence for each class ------------");
				// System.out.println("Class Name " + unit.getElementName());
				// FlowTypePrevelanceVisitor numberOfflowtypeprevalance = new FlowTypePrevelanceVisitor();
				// parsedCompilationUnit.accept(numberOfflowtypeprevalance);
				// getMethodsWithTargetTypePrevalance(numberOfflowtypeprevalance);
				// int averageNumber;
				// if (numberOfflowtypeprevalance.getNumberOfTryBlocks() ==0 )
				// 	averageNumber =0;
				// else
				// 	averageNumber = numberOfflowtypeprevalance.getNumberOfFlowTypePrevalance()/numberOfflowtypeprevalance.getNumberOfTryBlocks();
				// System.out.println("numberOfflowtypeprevalance.getNumberOfFlowTypePrevalance() " + numberOfflowtypeprevalance.getNumberOfFlowTypePrevalance());
				// System.out.println("numberOfflowtypeprevalance.getNumberOfTryBlocks() " + numberOfflowtypeprevalance.getNumberOfTryBlocks());
				// System.out.println("Number of flow type Prevalance " + averageNumber);
				
				// int numberofFlowQuantity = numberOfflowhandler.getNumberOfFlowQuantity() + numberOfflowtypeprevalance.getNumberOfFlowTypePrevalance();
			
				// Exception Handling Strategy - Charactristics metric
				ExceptionHandlingStrategyVisitor exceptionHandlingStrategyVisitor = new ExceptionHandlingStrategyVisitor();
				parsedCompilationUnit.accept(exceptionHandlingStrategyVisitor);
				exceptionHandlingStrategyCount = exceptionHandlingStrategyVisitor.ExceptionHandlingStrategyCount();

				// Flow Souce Declared methods
				// System.out.println("-------- Flow Souce Declared methods for each class ------------");
				// System.out.println("Class Name " + unit.getElementName());
				// FlowSoucreDeclareMethods numberOfflowSoucreDeclareMethods = new FlowSoucreDeclareMethods();
				// parsedCompilationUnit.accept(numberOfflowSoucreDeclareMethods);
				// System.out.println("Number of flow source Declared " + numberOfflowSoucreDeclareMethods.getNumberOfFlowSouceDeclared());

				
//				// Catch Recoverability anti-pattern
				CatchRecoverabilityVisitor catchRecoverabilityVisitor = new CatchRecoverabilityVisitor();
				parsedCompilationUnit.accept(catchRecoverabilityVisitor);
				catchRecoverabilityCount = catchRecoverabilityVisitor.getRecoverableExceptionCount();
				
				printCharacteristicsMetrics(unit.getElementName());			
///////////////////////////////////////////////////////////////////////////////////////				
				//////Metrics
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
				metricTryScope.put(unit.getElementName(), numberOfTryScope.getNumberOfTryScope());
		//		metricFlowTypePrevalance.put(unit.getElementName(), averageNumber);
				metricFlowQuantity.put(unit.getElementName(), numberofFlowQuantity);
				metricExceptionHandlingStrategy.put(unit.getElementName(), exceptionHandlingStrategyCount);
			//    metricFlowSourceDeclared.put(unit.getElementName(), numberOfflowSoucreDeclareMethods.getNumberOfFlowSouceDeclared());
				metricCatchRecoverability.put(unit.getElementName(), catchRecoverabilityCount);
			}
		}
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
	public static Map<String, Integer> getProject_Metric_FlowSourceDeclared() {
		return metricFlowSourceDeclared;
	}
	public static Map<String, Integer> getProject_Metric_FlowQuantity() {
		return metricFlowQuantity;
	}
	public static Map<String, Integer> getProject_Metric_FlowTypePrevalance() {
		return metricFlowTypePrevalance;
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
	
	private void getMethodsWithTargetThrow1Clauses(Throw1ClauseVisitor throwUncheckedException) {
		// TODO Auto-generated method stub
		for (MethodInvocation methodInvocationStatement : Throw1ClauseVisitor.getmethodInvocationStatements()) {
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
		for (TryStatement methodInvocationStatement : TryScopeVisitor.getTryStatements()) {
			tryScope.put(findMethodForTryScope(methodInvocationStatement), "Try Scope");
		}
	}
	// private void getMethodsWithTargetTypePrevalance(FlowTypePrevelanceVisitor numberOfTypePrevalance) {
	// 	// TODO Auto-generated method stub
	// 	for (TryStatement methodInvocationStatement : FlowTypePrevelanceVisitor.getTryStatements()) {
	// 		tryScope.put(findMethodForTryScope(methodInvocationStatement), "Flow Type Prevalance");
	// 	}
	// }

	private void getMethodsWithTargetCatchClauses(CatchClauseVisitor catchClauseVisitor) {
		
		for(CatchClause emptyCatch: catchClauseVisitor.getEmptyCatches()) {
			emptyCatches.put(findMethodForCatch(emptyCatch), "EmptyCatch");
		}
		
		for(CatchClause dummyCatch: catchClauseVisitor.getDummyCatches()) {
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
			System.out.println(
					String.format("The following method suffers from the Throw & Log anti-pattern: %s", type));
			if (declaration != null) {
				System.out.println(declaration.toString());
			}
		}
		
		for (MethodDeclaration declaration : catchMethods.keySet()) {
			String type = catchMethods.get(declaration);
			System.out.println(
					String.format("The following method suffers from the Over-Catch anti-pattern: %s", type));
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
		
		for(MethodDeclaration declaration : suspectMethods.keySet()) {
			String type = suspectMethods.get(declaration);
			System.out.println(String.format("The following method suffers from the %s pattern", type));
			System.out.println(declaration.toString());
		}
		
		System.out.println(String.format("Throw & Log anti-pattern Detected Count: %s", throwMethods.size()));
		System.out.println(String.format("Over-Catch anti-pattern Detected Count: %s", catchMethods.size()));
		System.out.println(
				String.format("Throwing the Kitchen Sink anti-pattern Detected Count: %s", kitchenSinkMethods.size()));
		System.out.println(String.format("Catch and Do Nothing(Empty Catch) anti-pattern Detected Count: %s", emptyCatches.size()));
		System.out.println(String.format("Dummy Handler anti-pattern Detected Count: %s", dummyCatches.size()));
		System.out.println(String.format("Method Invoke Detected Count: %s", methodIvoke.size()));
	}
	
	public void printCharacteristicsMetrics(String fileName){
		System.out.println("printing CharacteristicsMetrics ");
		System.out.println("File name: " + fileName);
		System.out.println("Flow Handling Actions Count: " + flowHandlingActionsCount);
		System.out.println("Try Block Count:" + tryBlockCount);
		System.out.println("Try-LOC:" + tryBlockLOC);
		System.out.println("Try-SLOC:" + tryBlockSLOC);
		System.out.println("Catch Block Count:" + catchBlockCount);
		System.out.println("Catch-LOC:" + catchBlockLOC);
		System.out.println("Catch-SLOC:" + catchBlockSLOC);
		System.out.println("Incomplete Implementation anti-pattern Detected Count:" + incompleteDPCount);
		System.out.println("Catch & Return Null anti-pattern Detected Count:" + catchReturnNullCount);
		System.out.println("Exception Handling Strategy anti-pattern Count :" + exceptionHandlingStrategyCount);
	}

	public static CompilationUnit parse(ICompilationUnit unit) {
		@SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
}
