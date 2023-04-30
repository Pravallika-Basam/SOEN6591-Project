package soen6591.visitors;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class ThrowClauseVisitor extends ASTVisitor  {
	private static HashSet<MethodInvocation> methodInvocationSmts = new HashSet<>();
	
	public boolean visit(MethodInvocation node) {
		MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor("throwBlock");
		node.accept(methodInvocationVisitor);
		
		if(methodInvocationVisitor.getNumberofCheckedException() > 2) {
			methodInvocationSmts.add(node);
		}

		return super.visit(node);
	}
	public static HashSet<MethodInvocation> getMethodInvocationSmts() {
		return methodInvocationSmts;
	}
}
