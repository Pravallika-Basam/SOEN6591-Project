package soen6591.visitors;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TryStatement;

import soen6591.handlers.AntiPatternDetectorHandler;


/**
 * visits TryStatement nodes in the Abstract Syntax Tree (AST) of a Java program and collects information about the method invocations inside them.
 * @author Ankur
 * @param tryStatements: a HashSet of TryStatement nodes collected by the visitor.
 * @return returns a HashSet of TryStatement and returns an integer that represents the total number of method invocations collected by the visitor.
 */

public class MethodInvokeVisitor extends ASTVisitor 
{
private static HashSet<TryStatement> tryStatements = new HashSet<>();
	private int numberofMethodInvoke1;
	@Override
	public boolean visit(TryStatement node) 
	{
		MethodInvocationVisitor methodInvocation = new MethodInvocationVisitor("MethodInvoke");
		node.accept(methodInvocation);		
		
		numberofMethodInvoke1 = numberofMethodInvoke1 + methodInvocation.getNumberofMethodInvoke();
		return super.visit(node);
	}
	
	public int getNumberofMethodInvoke() 
	{
		return numberofMethodInvoke1;
	}
	public static HashSet<TryStatement> getTryStatements() 
	{
		return tryStatements;
	}
	
}