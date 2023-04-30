package soen6591.visitors;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.TryStatement;

public class TryScopeVisitor extends ASTVisitor {
    private HashSet<TryStatement> tryStmts = new HashSet<>();
    private int numberOfTryScopes = 0;

    @Override
    public boolean visit(TryStatement node) {
        tryStmts.add(node);

        MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor("TryScope");
        node.accept(methodInvocationVisitor);

        String bodyString = node.getBody().getParent().getParent().getParent().toString();
        int openingBraceIndex = bodyString.indexOf('{');

        if (openingBraceIndex >= 0) {
            String statementBeforeBody = bodyString.substring(0, openingBraceIndex);

            if (statementBeforeBody.contains("for") || statementBeforeBody.contains("switch") || statementBeforeBody.contains("if") || statementBeforeBody.contains("while") || statementBeforeBody.contains("else")) {
                numberOfTryScopes++;
            }
        }

        return super.visit(node);
    }

    public HashSet<TryStatement> getTryStmts() {
        return tryStmts;
    }

    public int getNumberOfTryScopes() {
        return numberOfTryScopes;
    }
}

