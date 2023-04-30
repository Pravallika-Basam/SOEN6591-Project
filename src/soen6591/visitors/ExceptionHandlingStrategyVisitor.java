package soen6591.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

import soen6591.handlers.AntiPatternDetectorHandler;

/**
 * This class is an implementation of the Abstract Syntax Tree Visitor that visits TryStatements and determines the
 * exception handling strategy of the catch blocks. It keeps track of the count of the exception handling strategies found.
 *
 */
public class ExceptionHandlingStrategyVisitor extends ASTVisitor {

    private ITypeBinding tryException;
    private ITypeBinding ArgForCatch;
    public ITypeBinding[] meb;
	//An array of method names that throw an exception
    private static String[] ThrowMethods = {"throw"};
    private int StratCount = 0;

    /**
     * This method visits a TryStatement and determines the exception handling strategy of the catch blocks.
     * @param node
     * @return
     */
    @Override
    public boolean visit(TryStatement node) {
        for(Object statement : node.getBody().statements()) {
            if(IsThrownStatement(statement.toString())) {
                if(statement instanceof ThrowStatement) {
                    ITypeBinding throwInTry = ((ThrowStatement)statement).getExpression().resolveTypeBinding();
                    tryException = throwInTry;
                }
            }
        }

        CatchClauseVisitor cv = new CatchClauseVisitor();
        node.accept(cv);
        ArgForCatch = cv.getCatchArguments();
        if(isStrategy(ArgForCatch, tryException)) {
            if(isStrategy(ArgForCatch, tryException)) {
                StratCount++;
            }
        }
        MethodInvocationVisitor mic = new MethodInvocationVisitor("throwBlock");
        node.accept(mic);
        meb = mic.getMethodExceptionBindings();
        if(meb != null) {
            for(ITypeBinding methodException: meb) {
                if(ArgForCatch != null && methodException != null) {
                    if(isStrategy(ArgForCatch, methodException)) {
                        StratCount++;
                    }
                }
            }
        }
        return super.visit(node);
    }

    /**
     * This method checks if the exception handling strategy is the same as the one used in the try block.
     * @param ArgForCatch
     * @param tryException
     * @return
     */
    private boolean isStrategy(ITypeBinding ArgForCatch, ITypeBinding tryException) {
        if(tryException != null && ArgForCatch != null) {
            if(tryException.equals(ArgForCatch)) {
                return true;
            }
            else if(IsSuperType(ArgForCatch, tryException)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if a given type (subType) is a subtype of another given type (referenceType). 
     * If either subType or referenceType is null, or if referenceType is java.lang.Object,then this method returns false.
     * If subType is equal to the superclass of referenceType, then this method returns true.
     * @param subType
     * @param referenceType
     * @return
     */
    public static Boolean IsSuperType(ITypeBinding subType, ITypeBinding referenceType) {
        if (subType == null || referenceType == null || referenceType.getQualifiedName().equals("java.lang.Object"))
            return false;
        if (subType.isEqualTo(referenceType.getSuperclass()))
            return true;
        return IsSuperType(subType, referenceType.getSuperclass());
    }

    private static boolean IsThrownStatement(String statement) {
        if (statement == null) return false;
        for (String logmethod : ThrowMethods) {
            if (statement.indexOf(logmethod) > -1) {
                return true;
            }
        }
        return false;
    }
    
    public int ExceptionHandlingStrategyCount() {
        return StratCount;
    }
}