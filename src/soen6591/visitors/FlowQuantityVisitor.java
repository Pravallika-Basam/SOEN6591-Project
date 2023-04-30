package soen6591.visitors;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class FlowQuantityVisitor extends ASTVisitor {
    private int numberOfFlowQuantity =0;
    private static HashSet<MethodInvocation> methodInvocationStatement = new HashSet<>();

    public boolean visit(MethodInvocation node) {
        System.out.println("Visited FlowQ");
        MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor("throwBlock");
        node.accept(methodInvocationVisitor);
        numberOfFlowQuantity = numberOfFlowQuantity + methodInvocationVisitor.getNumberofCheckedException();
        return super.visit(node);
    }
    public static HashSet<MethodInvocation> getmethodInvocationStatements() {
        return methodInvocationStatement;
    }
    public int getNumberOfFlowQuantity() {
        return numberOfFlowQuantity;
    }
}