package soen6591.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class CatchRecoverabilityVisitor extends ASTVisitor {
	private ITypeBinding catchArgument;
	private int recoverableExceptionCount = 0;
	
	@Override
	public boolean visit(CatchClause node) {
		SingleVariableDeclaration exceptionType = node.getException();
		catchArgument = exceptionType.getType().resolveBinding();
		
		if(isRecoverableException(catchArgument)) {
			recoverableExceptionCount++;
		}
		
		return super.visit(node);
	}

	public int getRecoverableExceptionCount() {
		return recoverableExceptionCount;
	}
	
	
	private boolean isRecoverableException(ITypeBinding catchExceptionType) {

		if(IsSuperType("java.lang.Error", catchExceptionType) || IsSuperType("java.lang.RuntimeException", catchExceptionType)) {
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * Recursively find if the given subtype is a supertype of the reference type.
	 * 
	 * @param subtype       type to evaluate
	 * @param referenceType initial tracing reference to detect the super type
	 */
	public static Boolean IsSuperType(String subType, ITypeBinding referenceType) {

		if (subType == null || referenceType == null || referenceType.getQualifiedName().equals("java.lang.Object")) {
			return false;
		}

		if (subType.equals(referenceType.getSuperclass().getQualifiedName())) {
			//SampleHandler.printMessage("1111:" + subType + "2222:" + referenceType.getSuperclass().getQualifiedName());
			return true;
		}
		
		
		return IsSuperType(subType, referenceType.getSuperclass());
	}
}
