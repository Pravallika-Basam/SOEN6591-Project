package soen6591.visitors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;

public class TryVisitor extends ASTVisitor {
    private static HashSet<TryStatement> trySmts = new HashSet<>();
    private int tryBlockCount = 0;
    private int tryBlockLOC = 0;
    private ArrayList<String> tryBlockSLOC = new ArrayList<String>();

    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(TryStatement node) {
        trySmts.add(node);
        tryBlockCount++;
        MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor("TryBlock");
        node.accept(methodInvocationVisitor);

        List<Statement> bodyStatements = node.getBody().statements();
        for (Statement st : bodyStatements) {
            tryBlockSLOC.add(st.toString());
            tryBlockLOC++;
        }

        return super.visit(node);
    }

    public static HashSet<TryStatement> getTryBlocks() {
        return trySmts;
    }

    public int getTryBlockCount() {
        return tryBlockCount;
    }

    public int getTryBlockLOC() {
        return tryBlockLOC;
    }

    public ArrayList<String> getTryBlockLOCStatements() {
        return tryBlockSLOC;
    }
}