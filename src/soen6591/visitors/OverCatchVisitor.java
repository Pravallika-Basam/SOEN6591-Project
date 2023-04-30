package soen6591.visitors;

import java.util.*;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;

import soen6591.handlers.AntiPatternDetectorHandler;

public class OverCatchVisitor extends ASTVisitor {
	private HashSet<CatchClause> catchStmts = new HashSet<>();

	
	/**
	* Visits the catch block to analyze whether the catch block catches a super type of an exception
	* thrown by the try block. If a catch block catches a super type of an exception thrown by the try
	* block, it is added to the list of catch blocks that need to be refactored.
	* @param node the catch block to be visited
	* @return true to visit the children of this node, false otherwise
	*/
	@Override
	public boolean visit(CatchClause node) {
		TryStatement tryStatement = (TryStatement) node.getParent();
		
		MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor("TryBlock");
		tryStatement.accept(methodInvocationVisitor);

		ASTNode parentNode = findParentMethod(tryStatement);
		String parentMethodName = new String();
		if (parentNode.getNodeType() == ASTNode.METHOD_DECLARATION) {
			MethodDeclaration parentMethod = (MethodDeclaration) parentNode;
			parentNode = parentMethod.getBody();
			parentMethodName = getMethodNameWithoutBinding(parentMethod, true);
		}

		SingleVariableDeclaration exceptionType = node.getException();
		ITypeBinding catchException = exceptionType.getType().resolveBinding();

		ITypeBinding invokedMethodException = methodInvocationVisitor.getExceptionType();


		if (isThirdPatternException(catchException, invokedMethodException)) {
			catchStmts.add(node);
		}
		
		return super.visit(node);
	}

	public HashSet<CatchClause> getCatchBlocks() {
		return catchStmts;
	}

	private boolean isThirdPatternException(ITypeBinding catchException, ITypeBinding invokedMethodException) {
		if(invokedMethodException != null) {
			if (IsSuperType(catchException, invokedMethodException)) {
				return true;
			} else {
				return false;
			}
		}else {
			return false;
		}
		
	}

	public static ASTNode findParentMethod(ASTNode node) {

		int parentNodeType = node.getParent().getNodeType();

		if (parentNodeType == ASTNode.METHOD_DECLARATION) {
			return node.getParent();
		}
		if (parentNodeType == ASTNode.INITIALIZER) {
			return node.getParent();
		}
		if (parentNodeType == ASTNode.TYPE_DECLARATION) {
			return node.getParent();
		}
		if (parentNodeType == ASTNode.METHOD_DECLARATION) {
			return node.getParent();
		}

		return findParentMethod(node.getParent());
	}

	public static String getMethodNameWithoutBinding(MethodDeclaration method, boolean quotes) {

		String methodName = new String();

		methodName = ((quotes) ? "\"" : "") + method.getName().toString();
		methodName += "(";

		for (Object param : method.parameters()) {
			SingleVariableDeclaration svParam = (SingleVariableDeclaration) param;
			methodName += svParam.getType().toString() + ",";
		}
		methodName += ")" + ((quotes) ? "\"" : "");

		methodName = methodName.replace(",)", ")");

		return methodName;

	}
	
	public static Boolean IsSuperType(ITypeBinding subType, ITypeBinding referenceType) {

		if (subType == null || referenceType == null || referenceType.getQualifiedName().equals("java.lang.Object"))
			return false;

		if (subType.isEqualTo(referenceType.getSuperclass()))
			return true;
		
		
		return IsSuperType(subType, referenceType.getSuperclass());
	}
}
