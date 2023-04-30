package soen6591.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ReturnStatement;

/**
 * A visitor that collects all the return statements in a Java AST.
 */
public class ReturnStatementVisitor extends ASTVisitor {
    private final List<String> returnStatements = new ArrayList<>();

    /**
     * Visits a ReturnStatement node and adds its string representation to the list of return statements.
     *
     * @param node the ReturnStatement node to visit
     * @return true to visit the children of this node
     */
    @Override
    public boolean visit(ReturnStatement node) {
        returnStatements.add(node.toString());
        return super.visit(node);
    }

    /**
     * Gets the list of return statements collected by this visitor.
     *
     * @return the list of return statements
     */
    public List<String> getReturnStatements() {
        return returnStatements;
    }
}