package soen6591.visitors;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class FlowQuantityVisitor extends ASTVisitor {
    private int n =0;
    private static HashSet<MethodInvocation> m = new HashSet<>();

    /**
     * Visits a Method Invocation node in the AST and counts the number of checked exceptions
     * thrown by the method invocation using the MethodInvocationVisitor class.
     * 
     * @param node the Method Invocation node to visit
     * @return true to visit the children of this node, false otherwise
     */
    public boolean visit(MethodInvocation node) {
        System.out.println("Visited FlowQ");
        MethodInvocationVisitor mis = new MethodInvocationVisitor("throwBlock");
        node.accept(mis);
        n = n + mis.getNumberofCheckedException();
        return super.visit(node);
    }
    
    /**
     * Returns a set of all the Method Invocation statements encountered during the traversal.
     * 
     * @return a HashSet of MethodInvocation statements
     */
    public static HashSet<MethodInvocation> getmethodInvocationStatements() {
        return m;
    }
    
    /**
     * Returns the number of flow quantity found during the traversal.
     * 
     * @return the number of flow quantity found
     */
    public int getNumberOfFlowQuantity() {
        return n;
    }
}