package soen6591.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class CatchRecoverabilityVisitor extends ASTVisitor {
	/*
	 * This class implements an ASTVisitor that visits each CatchClause node in a Java source code AST
	 * and determines if the caught exception is recoverable or not.
	 * 
	 * Recoverable exceptions are exceptions that can be handled gracefully and recover from the error,
	 * while non-recoverable exceptions are exceptions that cannot be handled and the program cannot
	 * recover from the error.
	 * 
	 * The class keeps track of the number of recoverable exceptions found during the visit, and provides
	 * a method to retrieve this count.
	*/
	/* The type binding of the caught exception in the current CatchClause node */
	private ITypeBinding catchArgument;

	/* The count of recoverable exceptions found during the visit */
	private int recoverableExceptionCount = 0;

	/* 
	 * Visit method for CatchClause nodes in the AST. Determines if the caught exception is recoverable
	 * or not and updates the recoverableExceptionCount accordingly.
	 * 
	 * @param node The CatchClause node being visited
	 * @return true to continue visiting child nodes, false otherwise
	 */
	@Override
	public boolean visit(CatchClause node) {
		SingleVariableDeclaration exceptionType = node.getException();
		catchArgument = exceptionType.getType().resolveBinding();
		
		if(isRecoverableException(catchArgument)) {
			recoverableExceptionCount++;
		}
		
		return super.visit(node);
	}

	/* Returns the count of recoverable exceptions found during the visit */
	public int getRecoverableExceptionCount() {
		return recoverableExceptionCount;
	}

	/* 
	 * Determines if a given exception type is recoverable or not. 
	 * 
	 * Recoverable exceptions are exceptions that can be handled gracefully and recover from the error,
	 * while non-recoverable exceptions are exceptions that cannot be handled and the program cannot 
	 * recover from the error.
	 * 
	 * @param catchExceptionType The type binding of the caught exception being evaluated
	 * @return true if the exception is recoverable, false otherwise
	 */
	private boolean isRecoverableException(ITypeBinding catchExceptionType) {
		if(IsSuperType("java.lang.Error", catchExceptionType) || IsSuperType("java.lang.RuntimeException", catchExceptionType)) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * Recursively finds if the given subtype is a supertype of the reference type.
	 * 
	 * @param subType The subtype being evaluated
	 * @param referenceType The initial tracing reference to detect the super type
	 * @return true if the subtype is a supertype of the reference type, false otherwise
	 */
	public static Boolean IsSuperType(String subType, ITypeBinding referenceType) {
		if (subType == null || referenceType == null || referenceType.getQualifiedName().equals("java.lang.Object")) {
			return false;
		}

		if (subType.equals(referenceType.getSuperclass().getQualifiedName())) {
			return true;
		}
		
		return IsSuperType(subType, referenceType.getSuperclass());
	}

}
